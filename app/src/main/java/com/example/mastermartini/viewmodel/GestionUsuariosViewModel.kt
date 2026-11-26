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

// Estado del panel de administración de usuarios y roles
data class GestionUsuariosUiState(
    val estado: EstadoUi<List<Usuario>> = EstadoUi.Cargando,
    val filtroRol: Rol? = null,
    val idPropio: Long? = null,
    val guardando: Set<Long> = emptySet(),
    val mensajeSnackbar: String? = null
)

// ViewModel del panel de administración para filtrar usuarios, cambiar roles y eliminar cuentas
class GestionUsuariosViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val almacenamiento: AlmacenamientoLocal
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GestionUsuariosUiState(idPropio = almacenamiento.obtenerIdUsuario())
    )
    val uiState: StateFlow<GestionUsuariosUiState> = _uiState.asStateFlow()

    private var usuarios: List<Usuario> = emptyList()

    init {
        cargar()
    }

    // Carga el listado completo de usuarios verificando permiso de administrador
    fun cargar() {
        _uiState.update { it.copy(estado = EstadoUi.Cargando) }
        viewModelScope.launch {
            try {
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

    private fun idSolicitante(): Long {
        return almacenamiento.obtenerIdUsuario() ?: throw UsuarioException.AccesoDenegado()
    }

    fun seleccionarFiltro(rol: Rol?) {
        _uiState.update { actual ->
            val nuevoEstado = if (actual.estado is EstadoUi.Exito) EstadoUi.Exito(filtrar(rol)) else actual.estado
            actual.copy(filtroRol = rol, estado = nuevoEstado)
        }
    }

    private fun filtrar(rol: Rol?): List<Usuario> {
        return if (rol == null) usuarios else usuarios.filter { usuario -> usuario.rol == rol }
    }

    fun esPropio(usuario: Usuario): Boolean {
        return usuario.id == _uiState.value.idPropio
    }

    // Modifica el rol de un usuario impidiendo alterar la propia cuenta activa
    fun cambiarRol(usuario: Usuario, nuevoRol: Rol) {
        if (esPropio(usuario)) {
            _uiState.update { it.copy(mensajeSnackbar = "No puedes cambiar tu propio rol") }
            return
        }
        if (usuario.rol == nuevoRol || usuario.id in _uiState.value.guardando) return

        ejecutarAccion(usuario.id) {
            val actualizado = usuarioRepository.cambiarRol(idSolicitante(), usuario.id, nuevoRol)
            usuarios = usuarios.map { if (it.id == actualizado.id) actualizado else it }
        }
    }

    // Elimina la cuenta de un usuario impidiendo autoeliminación
    fun eliminarUsuario(usuario: Usuario) {
        if (esPropio(usuario)) {
            _uiState.update { it.copy(mensajeSnackbar = "No puedes eliminar tu propia cuenta") }
            return
        }
        if (usuario.id in _uiState.value.guardando) return

        ejecutarAccion(usuario.id) {
            usuarioRepository.eliminarUsuario(idSolicitante(), usuario.id)
            usuarios = usuarios.filter { it.id != usuario.id }
        }
    }

    private fun ejecutarAccion(idUsuario: Long, accion: suspend () -> Unit) {
        _uiState.update { it.copy(guardando = it.guardando + idUsuario) }
        viewModelScope.launch {
            try {
                accion()
                _uiState.update { it.copy(estado = EstadoUi.Exito(filtrar(it.filtroRol))) }
            } catch (e: UsuarioException) {
                _uiState.update { it.copy(mensajeSnackbar = e.message) }
            } finally {
                _uiState.update { it.copy(guardando = it.guardando - idUsuario) }
            }
        }
    }

    fun snackbarMostrado() {
        _uiState.update { it.copy(mensajeSnackbar = null) }
    }

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
