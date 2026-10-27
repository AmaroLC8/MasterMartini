package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mastermartini.data.AlmacenamientoLocal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Estado de la pantalla de Login: todo lo que LoginScreen necesita para dibujarse.
// Cada campo tiene su texto y su mensaje de error (null = sin error).
// errorGeneral es para cuando los dos campos están llenos pero las credenciales no coinciden.
data class LoginUiState(
    val usuario: String = "",              // RUT o correo que escribe la persona
    val errorUsuario: String? = null,
    val password: String = "",
    val errorPassword: String? = null,
    val errorGeneral: String? = null
)

// ViewModel del Login. Aquí van TODAS las validaciones del formulario (IE 2.2.1):
// la pantalla solo muestra el estado y avisa cuando el usuario escribe o toca "INGRESAR".
// Es AndroidViewModel (y no ViewModel) porque necesita la Application para crear
// AlmacenamientoLocal, que lee el perfil guardado en SharedPreferences.
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Acceso a los datos guardados en el registro (RUT, correo y contraseña)
    private val almacenamiento = AlmacenamientoLocal(application)

    // Estado privado que solo el ViewModel puede cambiar...
    private val _uiState = MutableStateFlow(LoginUiState())
    // ...y versión de solo lectura que observa la pantalla
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Se llama cada vez que la persona escribe en el campo RUT o correo.
    // Guarda el texto, revisa que no esté vacío y borra el error general,
    // porque si está corrigiendo el dato ya no tiene sentido mostrar "incorrectos".
    fun onUsuarioChange(nuevoTexto: String) {
        val error = if (nuevoTexto.isBlank()) "Ingresa tu RUT o correo" else null
        _uiState.update {
            it.copy(usuario = nuevoTexto, errorUsuario = error, errorGeneral = null)
        }
    }

    // Igual que onUsuarioChange, pero para la contraseña
    fun onPasswordChange(nuevoTexto: String) {
        val error = if (nuevoTexto.isBlank()) "Ingresa tu contraseña" else null
        _uiState.update {
            it.copy(password = nuevoTexto, errorPassword = error, errorGeneral = null)
        }
    }

    // Se llama al tocar "INGRESAR".
    // 1. Revisa los dos campos (por si la persona tocó el botón sin escribir nada).
    // 2. Si alguno está vacío, muestra su error y no sigue.
    // 3. Si están llenos, compara con lo guardado en el registro.
    // 4. Si coincide, llama a onExito() para que MainActivity navegue a "home".
    fun iniciarSesion(onExito: () -> Unit) {
        val actual = _uiState.value

        val errorUsuario = if (actual.usuario.isBlank()) "Ingresa tu RUT o correo" else null
        val errorPassword = if (actual.password.isBlank()) "Ingresa tu contraseña" else null

        if (errorUsuario != null || errorPassword != null) {
            _uiState.update {
                it.copy(errorUsuario = errorUsuario, errorPassword = errorPassword)
            }
            return
        }

        val credencialesCorrectas = almacenamiento.validarCredenciales(actual.usuario, actual.password)

        if (credencialesCorrectas) {
            _uiState.update { it.copy(errorGeneral = null) }
            onExito()
        } else {
            _uiState.update { it.copy(errorGeneral = "RUT/correo o contraseña incorrectos") }
        }
    }
}
