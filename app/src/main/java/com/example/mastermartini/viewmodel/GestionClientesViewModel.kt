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

// Estado del panel de gestión de clientes y suscripción Plan Pro
data class GestionClientesUiState(
    val estado: EstadoUi<List<Usuario>> = EstadoUi.Cargando,
    val busqueda: String = "",
    val guardando: Set<Long> = emptySet(),
    val mensajeSnackbar: String? = null
)

// ViewModel del panel de clientes para búsqueda y activación de Plan Pro
class GestionClientesViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val almacenamiento: AlmacenamientoLocal
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionClientesUiState())
    val uiState: StateFlow<GestionClientesUiState> = _uiState.asStateFlow()

    private var clientes: List<Usuario> = emptyList()

    init {
        cargar()
    }

    // Obtiene el listado de clientes desde el repositorio validando la sesión activa
    fun cargar() {
        _uiState.update { it.copy(estado = EstadoUi.Cargando) }
        viewModelScope.launch {
            try {
                val idSolicitante = almacenamiento.obtenerIdUsuario() ?: throw UsuarioException.AccesoDenegado()
                clientes = usuarioRepository.listarUsuarios(idSolicitante, Rol.Cliente)
                _uiState.update { it.copy(estado = EstadoUi.Exito(filtrar(it.busqueda))) }
            } catch (e: UsuarioException) {
                _uiState.update { it.copy(estado = EstadoUi.Error(e.message ?: "No se pudo cargar la lista")) }
            }
        }
    }

    fun onBusquedaChange(texto: String) {
        _uiState.update { actual ->
            val nuevoEstado = if (actual.estado is EstadoUi.Exito) EstadoUi.Exito(filtrar(texto)) else actual.estado
            actual.copy(busqueda = texto, estado = nuevoEstado)
        }
    }

    private fun filtrar(busqueda: String): List<Usuario> {
        val buscado = normalizar(busqueda).trim()
        if (buscado.isEmpty()) return clientes
        return clientes.filter { usuario ->
            normalizar(usuario.nombre).contains(buscado) || normalizar(usuario.correo).contains(buscado)
        }
    }

    private fun normalizar(texto: String): String {
        val separado = Normalizer.normalize(texto, Normalizer.Form.NFD)
        return separado.replace(Regex("\\p{Mn}"), "").lowercase()
    }

    // Actualiza el Plan Pro con actualización optimista y reversión ante fallo del servidor
    fun cambiarPlanPro(cliente: Usuario, activo: Boolean) {
        if (cliente.id in _uiState.value.guardando) return

        val estadoAnterior = cliente.planPro
        reemplazarCliente(cliente.copy(planPro = activo))
        _uiState.update { it.copy(guardando = it.guardando + cliente.id) }

        viewModelScope.launch {
            try {
                val idSolicitante = almacenamiento.obtenerIdUsuario() ?: throw UsuarioException.AccesoDenegado()
                val actualizado = usuarioRepository.cambiarPlanPro(idSolicitante, cliente.id, activo)
                reemplazarCliente(actualizado)
            } catch (e: UsuarioException) {
                reemplazarCliente(cliente.copy(planPro = estadoAnterior))
                _uiState.update { it.copy(mensajeSnackbar = e.message) }
            } finally {
                _uiState.update { it.copy(guardando = it.guardando - cliente.id) }
            }
        }
    }

    private fun reemplazarCliente(nuevo: Usuario) {
        clientes = clientes.map { usuario -> if (usuario.id == nuevo.id) nuevo else usuario }
        _uiState.update { it.copy(estado = EstadoUi.Exito(filtrar(it.busqueda))) }
    }

    fun snackbarMostrado() {
        _uiState.update { it.copy(mensajeSnackbar = null) }
    }

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
