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

// Los pasos de "Recuperar contraseña". Es un enum (como OrdenRecetas) para que solo
// existan estos 3: si alguien escribe mal un paso, no compila.
enum class PasoRecuperar {
    CORREO,  // Paso 1: escribir el correo y pedir el código
    CODIGO,  // Paso 2: escribir el código y la nueva contraseña
    LISTO    // Contraseña cambiada: mensaje de éxito y botón para volver al login
}

// ESTADO DE LA PANTALLA RECUPERAR CONTRASEÑA
// Cada campo tiene su texto y su error (null = sin error), igual que en Registro.
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
    // Aviso informativo (no es un error), ej. "Si el correo está registrado, te enviamos..."
    val mensaje: String? = null,
    // Error que devolvió el repositorio (ej. "El código no es correcto o ya venció...")
    val errorGeneral: String? = null,
    // true mientras se espera la respuesta: los botones se desactivan (como en Login)
    val cargando: Boolean = false
)

// VIEWMODEL DE RECUPERAR CONTRASEÑA
// Mismo estilo que Login: valida aquí (no en la pantalla) y llama al repositorio con try/catch.
// Recibe el repositorio por el constructor (inyección) y se crea con su Factory.
// No recibe AlmacenamientoLocal porque no guarda nada en el celular: la contraseña
// nueva queda en el servidor, y después la persona inicia sesión normalmente.
class RecuperarPasswordViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarPasswordUiState())
    val uiState: StateFlow<RecuperarPasswordUiState> = _uiState.asStateFlow()

    // --- VALIDACIONES (devuelven el mensaje de error, o null si está bien) ---

    // Misma regla que el correo del Registro: debe tener @ y punto
    private fun validarCorreo(correo: String): String? {
        return if (!correo.contains("@") || !correo.contains(".")) {
            "Correo no válido (debe incluir @ y punto)"
        } else null
    }

    // Exactamente 6 números (ni 5, ni 7, ni letras)
    private fun validarCodigo(codigo: String): String? {
        val limpio = codigo.trim()
        return if (limpio.length != 6 || !limpio.all { it.isDigit() }) {
            "El código tiene 6 números"
        } else null
    }

    // Misma regla que la contraseña del Registro: mínimo 4 caracteres
    private fun validarNuevaPassword(password: String): String? {
        return if (password.length < 4) "La contraseña debe tener al menos 4 caracteres" else null
    }

    // Las dos contraseñas deben ser iguales
    private fun validarRepetir(nueva: String, repetir: String): String? {
        return if (repetir != nueva) "Las contraseñas no coinciden" else null
    }

    // --- CAMBIOS DE LOS CAMPOS (se llaman con cada letra) ---
    // Se borra errorGeneral: si la persona está corrigiendo, el error anterior ya no aplica.

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
                // Si ya escribió la repetición, se vuelve a comparar con la nueva
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

    // --- PASO 1: PEDIR EL CÓDIGO ---
    // Valida el correo y llama a solicitarCodigo(). Si sale bien, pasa al paso 2.
    fun enviarCodigo() {
        val actual = _uiState.value
        if (actual.cargando) return // evita el doble toque

        val errorCorreo = validarCorreo(actual.correo)
        if (errorCorreo != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo) }
            return
        }

        pedirCodigo(
            correo = actual.correo.trim(),
            // MISMO mensaje exista o no el correo: así nadie puede usar esta pantalla
            // para averiguar qué correos tienen cuenta (el repositorio tampoco lo dice).
            mensajeExito = "Si el correo está registrado, te enviamos un código de 6 dígitos"
        )
    }

    // Botón "Reenviar código" del paso 2: pide un código nuevo para el mismo correo.
    // El código anterior deja de servir (el repositorio guarda solo el último).
    fun reenviarCodigo() {
        val actual = _uiState.value
        if (actual.cargando) return
        pedirCodigo(
            correo = actual.correo.trim(),
            mensajeExito = "Si el correo está registrado, te enviamos un código nuevo de 6 dígitos"
        )
    }

    // Lo que comparten enviarCodigo() y reenviarCodigo(): llamar al repositorio con try/catch
    private fun pedirCodigo(correo: String, mensajeExito: String) {
        _uiState.update { it.copy(cargando = true, errorGeneral = null, mensaje = null) }
        viewModelScope.launch {
            try {
                usuarioRepository.solicitarCodigo(correo)
                _uiState.update {
                    it.copy(cargando = false, paso = PasoRecuperar.CODIGO, mensaje = mensajeExito)
                }
            } catch (e: UsuarioException) {
                // Ej. SinConexion cuando esté el backend real
                _uiState.update { it.copy(cargando = false, errorGeneral = e.message) }
            }
        }
    }

    // --- PASO 2: CAMBIAR LA CONTRASEÑA ---
    // Revisa los 3 campos (por si tocó el botón sin escribir) y llama a restablecerPassword().
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
                // Salió bien: paso final. Se borran las contraseñas de la memoria del estado.
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
                // Ej. CodigoInvalido: "El código no es correcto o ya venció. Pide uno nuevo."
                _uiState.update { it.copy(cargando = false, errorGeneral = e.message) }
            }
        }
    }

    // FACTORY: la receta para crear este ViewModel con su repositorio
    // (MainActivity: viewModel(factory = RecuperarPasswordViewModel.Factory)).
    // No necesita la Application porque no usa AlmacenamientoLocal.
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RecuperarPasswordViewModel(ProveedorRepositorios.usuarioRepository)
            }
        }
    }
}
