package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.ProveedorRepositorios
import com.example.mastermartini.data.UsuarioException
import com.example.mastermartini.data.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PasoRecuperar {
    CORREO,
    CODIGO,
    LISTO
}

// Estado del flujo de recuperación y restablecimiento de contraseña
data class RecuperarPasswordUiState(
    val paso: PasoRecuperar = PasoRecuperar.CORREO,
    val correo: String = "",
    val errorCorreo: String? = null,
    val codigo: String = "",
    val errorCodigo: String? = null,
    val nuevaPassword: String = "",
    val errorNuevaPassword: String? = null,
    val repetirPassword: String = "",
    val errorRepetirPassword: String? = null,
    val mensaje: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false
)

// ViewModel del flujo de recuperación de contraseña en dos pasos con validación de código
class RecuperarPasswordViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarPasswordUiState())
    val uiState: StateFlow<RecuperarPasswordUiState> = _uiState.asStateFlow()

    private fun validarCorreo(correo: String): String? {
        return if (!correo.contains("@") || !correo.contains(".")) {
            "Correo no válido (debe incluir @ y punto)"
        } else null
    }

    private fun validarCodigo(codigo: String): String? {
        val limpio = codigo.trim()
        return if (limpio.length != 6 || !limpio.all { it.isDigit() }) {
            "El código tiene 6 números"
        } else null
    }

    private fun validarNuevaPassword(password: String): String? {
        return if (password.length < 4) "La contraseña debe tener al menos 4 caracteres" else null
    }

    private fun validarRepetir(nueva: String, repetir: String): String? {
        return if (repetir != nueva) "Las contraseñas no coinciden" else null
    }

    fun onCorreoChange(nuevoTexto: String) {
        _uiState.update {
            it.copy(correo = nuevoTexto, errorCorreo = validarCorreo(nuevoTexto), errorGeneral = null)
        }
    }

    fun onCodigoChange(nuevoTexto: String) {
        _uiState.update {
            it.copy(codigo = nuevoTexto, errorCodigo = validarCodigo(nuevoTexto), errorGeneral = null)
        }
    }

    fun onNuevaPasswordChange(nuevoTexto: String) {
        _uiState.update {
            it.copy(
                nuevaPassword = nuevoTexto,
                errorNuevaPassword = validarNuevaPassword(nuevoTexto),
                errorRepetirPassword = if (it.repetirPassword.isEmpty()) null
                else validarRepetir(nuevoTexto, it.repetirPassword),
                errorGeneral = null
            )
        }
    }

    fun onRepetirPasswordChange(nuevoTexto: String) {
        _uiState.update {
            it.copy(
                repetirPassword = nuevoTexto,
                errorRepetirPassword = validarRepetir(it.nuevaPassword, nuevoTexto),
                errorGeneral = null
            )
        }
    }

    // Valida el correo y solicita el código de verificación al backend
    fun enviarCodigo() {
        val actual = _uiState.value
        if (actual.cargando) return

        val errorCorreo = validarCorreo(actual.correo)
        if (errorCorreo != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo) }
            return
        }

        pedirCodigo(
            correo = actual.correo.trim(),
            mensajeExito = "Si el correo está registrado, te enviamos un código de 6 dígitos"
        )
    }

    fun reenviarCodigo() {
        val actual = _uiState.value
        if (actual.cargando) return
        pedirCodigo(
            correo = actual.correo.trim(),
            mensajeExito = "Si el correo está registrado, te enviamos un código nuevo de 6 dígitos"
        )
    }

    private fun pedirCodigo(correo: String, mensajeExito: String) {
        _uiState.update { it.copy(cargando = true, errorGeneral = null, mensaje = null) }
        viewModelScope.launch {
            try {
                usuarioRepository.solicitarCodigo(correo)
                _uiState.update {
                    it.copy(cargando = false, paso = PasoRecuperar.CODIGO, mensaje = mensajeExito)
                }
            } catch (e: UsuarioException) {
                _uiState.update { it.copy(cargando = false, errorGeneral = e.message) }
            }
        }
    }

    // Valida el código recibido y actualiza la contraseña en el repositorio
    fun restablecer() {
        val actual = _uiState.value
        if (actual.cargando) return

        val errorCodigo = validarCodigo(actual.codigo)
        val errorNueva = validarNuevaPassword(actual.nuevaPassword)
        val errorRepetir = validarRepetir(actual.nuevaPassword, actual.repetirPassword)

        if (errorCodigo != null || errorNueva != null || errorRepetir != null) {
            _uiState.update {
                it.copy(
                    errorCodigo = errorCodigo,
                    errorNuevaPassword = errorNueva,
                    errorRepetirPassword = errorRepetir
                )
            }
            return
        }

        _uiState.update { it.copy(cargando = true, errorGeneral = null) }
        viewModelScope.launch {
            try {
                usuarioRepository.restablecerPassword(
                    correo = actual.correo.trim(),
                    codigo = actual.codigo.trim(),
                    nuevaPassword = actual.nuevaPassword
                )
                _uiState.update {
                    it.copy(
                        cargando = false,
                        paso = PasoRecuperar.LISTO,
                        mensaje = null,
                        nuevaPassword = "",
                        repetirPassword = ""
                    )
                }
            } catch (e: UsuarioException) {
                _uiState.update { it.copy(cargando = false, errorGeneral = e.message) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RecuperarPasswordViewModel(ProveedorRepositorios.usuarioRepository)
            }
        }
    }
}
