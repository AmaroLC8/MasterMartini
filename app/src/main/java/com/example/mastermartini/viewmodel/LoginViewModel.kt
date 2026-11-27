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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado del formulario de inicio de sesión
data class LoginUiState(
    val usuario: String = "",
    val errorUsuario: String? = null,
    val password: String = "",
    val errorPassword: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false
)

// ViewModel de inicio de sesión encargado de validar credenciales y persistir la sesión
class LoginViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val almacenamiento: AlmacenamientoLocal
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioChange(nuevoTexto: String) {
        val error = if (nuevoTexto.isBlank()) "Ingresa tu RUT o correo" else null
        _uiState.update {
            it.copy(usuario = nuevoTexto, errorUsuario = error, errorGeneral = null)
        }
    }

    fun onPasswordChange(nuevoTexto: String) {
        val error = if (nuevoTexto.isBlank()) "Ingresa tu contraseña" else null
        _uiState.update {
            it.copy(password = nuevoTexto, errorPassword = error, errorGeneral = null)
        }
    }

    // Valida campos no vacíos, autentica contra el repositorio y guarda la sesión local
    fun iniciarSesion(onExito: () -> Unit) {
        val actual = _uiState.value

        if (actual.cargando) return

        val errorUsuario = if (actual.usuario.isBlank()) "Ingresa tu RUT o correo" else null
        val errorPassword = if (actual.password.isBlank()) "Ingresa tu contraseña" else null

        if (errorUsuario != null || errorPassword != null) {
            _uiState.update {
                it.copy(errorUsuario = errorUsuario, errorPassword = errorPassword)
            }
            return
        }

        _uiState.update { it.copy(cargando = true, errorGeneral = null) }

        viewModelScope.launch {
            try {
                val usuario = usuarioRepository.login(actual.usuario.trim(), actual.password)
                almacenamiento.guardarSesion(usuario)
                _uiState.update { it.copy(cargando = false) }
                onExito()
            } catch (e: UsuarioException) {
                _uiState.update { it.copy(cargando = false, errorGeneral = e.message) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                LoginViewModel(
                    usuarioRepository = ProveedorRepositorios.usuarioRepository,
                    almacenamiento = AlmacenamientoLocal(application)
                )
            }
        }
    }
}
