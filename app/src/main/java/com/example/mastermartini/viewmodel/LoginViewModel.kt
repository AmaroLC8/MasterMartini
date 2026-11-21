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

// Estado de la pantalla de Login: todo lo que LoginScreen necesita para dibujarse.
// Cada campo tiene su texto y su mensaje de error (null = sin error).
// errorGeneral es para cuando los dos campos están llenos pero el repositorio responde con un error
// (ej. "RUT/correo o contraseña incorrectos").
data class LoginUiState(
    val usuario: String = "",              // RUT o correo que escribe la persona
    val errorUsuario: String? = null,
    val password: String = "",
    val errorPassword: String? = null,
    val errorGeneral: String? = null,
    // true mientras se espera la respuesta del repositorio (el backend).
    // Con esto el botón se desactiva (no se puede tocar dos veces) y muestra un indicador de carga.
    val cargando: Boolean = false
)

// ViewModel del Login. Aquí van TODAS las validaciones del formulario (IE 2.2.1):
// la pantalla solo muestra el estado y avisa cuando el usuario escribe o toca "INGRESAR".
//
// INYECCIÓN POR CONSTRUCTOR: el ViewModel NO crea sus dependencias, las RECIBE.
// - usuarioRepository: quién revisa el RUT/correo y la contraseña (hoy el falso, mañana el backend).
// - almacenamiento: dónde se guarda la sesión al entrar.
// Así, en una prueba se le puede pasar un repositorio de mentira (MockK) y revisar
// qué hace el ViewModel cuando el login sale bien o mal, sin servidor ni celular.
// Ya no necesita ser AndroidViewModel: el Context solo lo usa el Factory para crear AlmacenamientoLocal.
class LoginViewModel(
    private val usuarioRepository: UsuarioRepository,
    private val almacenamiento: AlmacenamientoLocal
) : ViewModel() {

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
    // 3. Si están llenos, le pregunta al repositorio (en una corrutina, porque demora).
    // 4. Si sale bien, guarda la sesión y llama a onExito() para que MainActivity navegue a "home".
    //    Si sale mal, muestra el mensaje del error en errorGeneral.
    fun iniciarSesion(onExito: () -> Unit) {
        val actual = _uiState.value

        // Si ya se está esperando una respuesta, no se manda otra (evita el doble toque)
        if (actual.cargando) return

        val errorUsuario = if (actual.usuario.isBlank()) "Ingresa tu RUT o correo" else null
        val errorPassword = if (actual.password.isBlank()) "Ingresa tu contraseña" else null

        if (errorUsuario != null || errorPassword != null) {
            _uiState.update {
                it.copy(errorUsuario = errorUsuario, errorPassword = errorPassword)
            }
            return
        }

        // Empieza la espera: se borra el error anterior y se muestra el indicador de carga
        _uiState.update { it.copy(cargando = true, errorGeneral = null) }

        // viewModelScope.launch abre una corrutina: login() es suspend y puede esperar
        // la respuesta sin congelar la pantalla.
        viewModelScope.launch {
            // try/catch: se INTENTA iniciar sesión; si el repositorio LANZA una UsuarioException
            // (ej. CredencialesInvalidas), el catch la ATRAPA y la app no se cae.
            try {
                val usuario = usuarioRepository.login(actual.usuario.trim(), actual.password)
                // Si llegó aquí, el login salió bien: se guarda quién entró (id, rol, Plan Pro y datos)
                almacenamiento.guardarSesion(usuario)
                _uiState.update { it.copy(cargando = false) }
                onExito()
            } catch (e: UsuarioException) {
                // e.message es el texto en español que trae cada error (ver UsuarioException.kt)
                _uiState.update { it.copy(cargando = false, errorGeneral = e.message) }
            }
        }
    }

    // FACTORY: la "receta" para crear este ViewModel con sus dependencias.
    // viewModel() de Compose no sabe qué pasarle al constructor, así que MainActivity
    // le entrega esta receta: viewModel(factory = LoginViewModel.Factory).
    // companion object: pertenece a la clase, por eso se usa como LoginViewModel.Factory.
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            // initializer { } dice CÓMO se arma el ViewModel.
            initializer {
                // this[APPLICATION_KEY] entrega la Application (el Context de la app),
                // que necesita AlmacenamientoLocal para abrir SharedPreferences.
                // checkNotNull: si por algún motivo no viniera, avisa con un error claro.
                val application = checkNotNull(this[APPLICATION_KEY])
                LoginViewModel(
                    usuarioRepository = ProveedorRepositorios.usuarioRepository,
                    almacenamiento = AlmacenamientoLocal(application)
                )
            }
        }
    }
}
