package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.ProveedorRepositorios
import com.example.mastermartini.data.UsuarioException
import com.example.mastermartini.data.UsuarioRepository
import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer

// ESTADO DEL PANEL "CLIENTES Y PLAN PRO"
data class GestionClientesUiState(
    // Cargando / Exito(lista ya filtrada por el buscador) / Error(mensaje). Ver EstadoUi.kt
    val estado: EstadoUi<List<Usuario>> = EstadoUi.Cargando,
    // Lo que se escribe en el buscador
    val busqueda: String = "",
    // Ids de los clientes cuyo Plan Pro se está guardando AHORA.
    // Esa fila muestra un indicador y su Switch se desactiva. Es un Set porque pueden ser varios.
    val guardando: Set<Long> = emptySet(),
    // Error de una acción (no de la carga), para mostrar en un Snackbar. null = nada que mostrar
    val mensajeSnackbar: String? = null
)

// VIEWMODEL DEL PANEL "CLIENTES Y PLAN PRO" (Ventas y Administrador)
// Recibe el repositorio y el almacenamiento por el constructor (igual que Login),
// y se crea con su Factory.
class GestionClientesViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val almacenamiento: AlmacenamientoLocal
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionClientesUiState())
    val uiState: StateFlow<GestionClientesUiState> = _uiState.asStateFlow()

    // Lista COMPLETA de clientes que respondió el repositorio. El buscador siempre filtra
    // desde esta lista (nunca desde una ya filtrada), igual que en RecetasViewModel.
    private var clientes: List<Usuario> = emptyList()

    // Al abrir el panel se pide la lista
    init {
        cargar()
    }

    // Pide los clientes al repositorio. También la usa el botón "Reintentar".
    fun cargar() {
        _uiState.update { it.copy(estado = EstadoUi.Cargando) }
        viewModelScope.launch {
            try {
                // idSolicitante = quien pide (el usuario con sesión). El repositorio revisa su rol:
                // si no es Ventas ni Administrador, lanza AccesoDenegado.
                val idSolicitante = almacenamiento.obtenerIdUsuario() ?: throw UsuarioException.AccesoDenegado()
                clientes = usuarioRepository.listarUsuarios(idSolicitante, Rol.Cliente)
                _uiState.update { it.copy(estado = EstadoUi.Exito(filtrar(it.busqueda))) }
            } catch (e: UsuarioException) {
                // Ej. AccesoDenegado: "No tienes permiso para hacer esta acción."
                _uiState.update { it.copy(estado = EstadoUi.Error(e.message ?: "No se pudo cargar la lista")) }
            }
        }
    }

    // Se llama con cada letra del buscador
    fun onBusquedaChange(texto: String) {
        _uiState.update { actual ->
            // Solo se vuelve a filtrar si la lista ya cargó (si está Cargando o en Error, no hay lista)
            val nuevoEstado = if (actual.estado is EstadoUi.Exito) EstadoUi.Exito(filtrar(texto)) else actual.estado
            actual.copy(busqueda = texto, estado = nuevoEstado)
        }
    }

    // Deja los clientes cuyo nombre o correo contiene lo buscado (sin tildes ni mayúsculas)
    private fun filtrar(busqueda: String): List<Usuario> {
        val buscado = normalizar(busqueda).trim()
        if (buscado.isEmpty()) return clientes
        return clientes.filter { usuario ->
            normalizar(usuario.nombre).contains(buscado) || normalizar(usuario.correo).contains(buscado)
        }
    }

    // Igual que en BuscarViewModel: "José" -> "jose".
    // NFD separa la letra de su tilde, \p{Mn} borra las tildes sueltas, y luego todo a minúsculas.
    private fun normalizar(texto: String): String {
        val separado = Normalizer.normalize(texto, Normalizer.Form.NFD)
        return separado.replace(Regex("\\p{Mn}"), "").lowercase()
    }

    // Se llama al tocar el Switch "Plan Pro" de un cliente.
    // 1. Si esa fila ya se está guardando, no hace nada (evita el doble toque).
    // 2. Cambia el Switch AL TIRO en pantalla (así se siente rápido) y marca la fila como "guardando".
    // 3. Llama al repositorio. Si falla, DEVUELVE el Switch a como estaba y muestra el error.
    fun cambiarPlanPro(cliente: Usuario, activo: Boolean) {
        if (cliente.id in _uiState.value.guardando) return

        val estadoAnterior = cliente.planPro
        reemplazarCliente(cliente.copy(planPro = activo))
        _uiState.update { it.copy(guardando = it.guardando + cliente.id) }

        viewModelScope.launch {
            try {
                val idSolicitante = almacenamiento.obtenerIdUsuario() ?: throw UsuarioException.AccesoDenegado()
                val actualizado = usuarioRepository.cambiarPlanPro(idSolicitante, cliente.id, activo)
                // Se usa lo que respondió el repositorio (la versión "oficial" del usuario)
                reemplazarCliente(actualizado)
            } catch (e: UsuarioException) {
                // Falló: el Switch vuelve a su estado anterior y se avisa en el Snackbar
                reemplazarCliente(cliente.copy(planPro = estadoAnterior))
                _uiState.update { it.copy(mensajeSnackbar = e.message) }
            } finally {
                // finally se ejecuta SIEMPRE (haya salido bien o mal): la fila deja de "guardar"
                _uiState.update { it.copy(guardando = it.guardando - cliente.id) }
            }
        }
    }

    // Cambia un cliente dentro de la lista completa y vuelve a armar la lista filtrada.
    // map recorre la lista y devuelve una NUEVA: el que tiene el mismo id se reemplaza.
    private fun reemplazarCliente(nuevo: Usuario) {
        clientes = clientes.map { usuario -> if (usuario.id == nuevo.id) nuevo else usuario }
        _uiState.update { it.copy(estado = EstadoUi.Exito(filtrar(it.busqueda))) }
    }

    // La pantalla avisa que ya mostró el Snackbar: se borra para que no se repita
    fun snackbarMostrado() {
        _uiState.update { it.copy(mensajeSnackbar = null) }
    }

    // FACTORY: igual que en LoginViewModel
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                GestionClientesViewModel(
                    usuarioRepository = ProveedorRepositorios.usuarioRepository,
                    almacenamiento = AlmacenamientoLocal(application)
                )
            }
        }
    }
}
