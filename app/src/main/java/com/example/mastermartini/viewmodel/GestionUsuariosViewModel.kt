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

// ESTADO DEL PANEL "USUARIOS Y ROLES"
data class GestionUsuariosUiState(
    // Cargando / Exito(lista ya filtrada por rol) / Error(mensaje). Ver EstadoUi.kt
    val estado: EstadoUi<List<Usuario>> = EstadoUi.Cargando,
    // Rol elegido en los FilterChips. null = "Todos"
    val filtroRol: Rol? = null,
    // Id del administrador que está usando la app. Su fila muestra "Tú" y no se puede cambiar.
    val idPropio: Long? = null,
    // Ids de los usuarios con una acción en curso (cambiar rol o eliminar): su fila muestra
    // un indicador y sus controles se desactivan
    val guardando: Set<Long> = emptySet(),
    // Error de una acción, para mostrar en un Snackbar. null = nada que mostrar
    val mensajeSnackbar: String? = null
)

// VIEWMODEL DEL PANEL "USUARIOS Y ROLES" (solo Administrador)
// Mismo estilo que GestionClientesViewModel: repositorio y almacenamiento por el constructor.
class GestionUsuariosViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val almacenamiento: AlmacenamientoLocal
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GestionUsuariosUiState(idPropio = almacenamiento.obtenerIdUsuario())
    )
    val uiState: StateFlow<GestionUsuariosUiState> = _uiState.asStateFlow()

    // Lista COMPLETA de usuarios. Se pide una vez y el filtro por rol se hace aquí
    // (sin volver a llamar al servidor), así cambiar de chip es instantáneo.
    private var usuarios: List<Usuario> = emptyList()

    init {
        cargar()
    }

    // Pide todos los usuarios (rol = null). También la usa el botón "Reintentar".
    fun cargar() {
        _uiState.update { it.copy(estado = EstadoUi.Cargando) }
        viewModelScope.launch {
            try {
                // El backend deja LISTAR a Ventas y a Administrador, pero este panel es solo
                // para quien puede gestionar usuarios (Administrador). Se le pregunta al rol
                // guardado (polimorfismo): si no puede, se muestra el mismo error de "sin permiso".
                // Cambiar rol y eliminar los vuelve a revisar el repositorio en cada acción.
                if (!almacenamiento.obtenerRol().puedeGestionarUsuarios) {
                    throw UsuarioException.AccesoDenegado()
                }
                usuarios = usuarioRepository.listarUsuarios(idSolicitante(), null)
                _uiState.update { it.copy(estado = EstadoUi.Exito(filtrar(it.filtroRol))) }
            } catch (e: UsuarioException) {
                _uiState.update { it.copy(estado = EstadoUi.Error(e.message ?: "No se pudo cargar la lista")) }
            }
        }
    }

    // Id de quien pide la acción (la sesión). Si no hay sesión, no tiene permiso.
    private fun idSolicitante(): Long {
        return almacenamiento.obtenerIdUsuario() ?: throw UsuarioException.AccesoDenegado()
    }

    // Se llama al tocar un FilterChip. null = "Todos"
    fun seleccionarFiltro(rol: Rol?) {
        _uiState.update { actual ->
            val nuevoEstado = if (actual.estado is EstadoUi.Exito) EstadoUi.Exito(filtrar(rol)) else actual.estado
            actual.copy(filtroRol = rol, estado = nuevoEstado)
        }
    }

    // Deja solo los usuarios del rol elegido (o todos si es null)
    private fun filtrar(rol: Rol?): List<Usuario> {
        return if (rol == null) usuarios else usuarios.filter { usuario -> usuario.rol == rol }
    }

    // REGLA: ¿esta fila es la del propio administrador? Si lo es, no puede cambiar su rol
    // ni eliminarse (se quedaría sin acceso o la app quedaría sin administrador).
    // La pantalla la usa para desactivar los controles y mostrar "Tú".
    fun esPropio(usuario: Usuario): Boolean {
        return usuario.id == _uiState.value.idPropio
    }

    // Se llama al elegir un rol en el menú desplegable de una fila
    fun cambiarRol(usuario: Usuario, nuevoRol: Rol) {
        // La regla se revisa AQUÍ también (no solo desactivando el botón en la pantalla):
        // así se cumple aunque alguien llegue a llamar esta función de otra forma.
        if (esPropio(usuario)) {
            _uiState.update { it.copy(mensajeSnackbar = "No puedes cambiar tu propio rol") }
            return
        }
        // Mismo rol que ya tenía, o ya hay una acción en curso en esa fila: no se hace nada
        if (usuario.rol == nuevoRol || usuario.id in _uiState.value.guardando) return

        ejecutarAccion(usuario.id) {
            val actualizado = usuarioRepository.cambiarRol(idSolicitante(), usuario.id, nuevoRol)
            usuarios = usuarios.map { if (it.id == actualizado.id) actualizado else it }
        }
    }

    // Se llama al CONFIRMAR en el diálogo "¿Eliminar a ...?"
    fun eliminarUsuario(usuario: Usuario) {
        if (esPropio(usuario)) {
            _uiState.update { it.copy(mensajeSnackbar = "No puedes eliminar tu propia cuenta") }
            return
        }
        if (usuario.id in _uiState.value.guardando) return

        ejecutarAccion(usuario.id) {
            usuarioRepository.eliminarUsuario(idSolicitante(), usuario.id)
            // filter deja a todos MENOS al eliminado
            usuarios = usuarios.filter { it.id != usuario.id }
        }
    }

    // Lo que comparten cambiarRol y eliminarUsuario:
    // marca la fila como "guardando", ejecuta la acción con try/catch, vuelve a armar la lista
    // y, pase lo que pase (finally), saca la fila de "guardando".
    // "accion" es una función suspend que se le pasa entre llaves { }.
    private fun ejecutarAccion(idUsuario: Long, accion: suspend () -> Unit) {
        _uiState.update { it.copy(guardando = it.guardando + idUsuario) }
        viewModelScope.launch {
            try {
                accion()
                _uiState.update { it.copy(estado = EstadoUi.Exito(filtrar(it.filtroRol))) }
            } catch (e: UsuarioException) {
                // Ej. AccesoDenegado si quien pide no es Administrador
                _uiState.update { it.copy(mensajeSnackbar = e.message) }
            } finally {
                _uiState.update { it.copy(guardando = it.guardando - idUsuario) }
            }
        }
    }

    // La pantalla avisa que ya mostró el Snackbar
    fun snackbarMostrado() {
        _uiState.update { it.copy(mensajeSnackbar = null) }
    }

    // FACTORY: igual que en LoginViewModel
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                GestionUsuariosViewModel(
                    usuarioRepository = ProveedorRepositorios.usuarioRepository,
                    almacenamiento = AlmacenamientoLocal(application)
                )
            }
        }
    }
}
