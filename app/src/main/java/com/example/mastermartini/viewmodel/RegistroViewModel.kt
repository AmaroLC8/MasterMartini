package com.example.mastermartini.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.FotoRepository
import com.example.mastermartini.data.ProveedorRepositorios
import com.example.mastermartini.data.UsuarioException
import com.example.mastermartini.data.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado del formulario de registro y edición de perfil
data class RegistroUiState(
    val nombre: String = "",
    val errorNombre: String? = null,
    val rut: String = "",
    val errorRut: String? = null,
    val correo: String = "",
    val errorCorreo: String? = null,
    val rubro: String = "Panadería",
    val telefono: String = "",
    val errorTelefono: String? = null,
    val password: String = "",
    val errorPassword: String? = null,
    val personasNegocio: String = "",
    val errorPersonas: String? = null,
    val fotoLocal: Bitmap? = null,
    val avisoCamara: String? = null,
    val esFormularioValido: Boolean = false,
    val puedeIntentarRegistrar: Boolean = false,
    val cargando: Boolean = false,
    val errorGeneral: String? = null,
    val esEdicion: Boolean = false
)

// ViewModel de registro y edición de perfil con validaciones y captura de foto local
class RegistroViewModel(
    application: Application,
    private val usuarioRepository: UsuarioRepository,
    private val almacenamiento: AlmacenamientoLocal
) : AndroidViewModel(application) {

    private val fotoRepository = FotoRepository(application)

    val opcionesPersonas = listOf(
        "Solo yo",
        "2 a 5 personas",
        "6 a 20 personas",
        "Más de 20 personas",
        "Prefiero no decirlo"
    )

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    init {
        cargarPerfilGuardado()

        viewModelScope.launch {
            val fotoGuardada = fotoRepository.obtenerFoto()
            _uiState.update { it.copy(fotoLocal = fotoGuardada) }
        }
    }

    // Sincroniza el formulario con la sesión almacenada o lo reinicia si no hay sesión
    fun cargarPerfilGuardado() {
        _uiState.value = RegistroUiState(
            fotoLocal = _uiState.value.fotoLocal,
            esEdicion = almacenamiento.obtenerIdUsuario() != null
        )

        if (almacenamiento.existePerfil()) {
            onNombreChange(almacenamiento.obtenerNombre())
            onRutChange(almacenamiento.obtenerRut())
            onCorreoChange(almacenamiento.obtenerCorreo())
            onRubroChange(almacenamiento.obtenerRubro())
            onTelefonoChange(almacenamiento.obtenerTelefono())

            val personasGuardado = almacenamiento.obtenerPersonasNegocio()
            if (personasGuardado in opcionesPersonas) {
                onPersonasNegocioChange(personasGuardado)
            }
        }
    }

    fun hayPerfilGuardado(): Boolean {
        return almacenamiento.existePerfil()
    }

    fun onNombreChange(nuevoTexto: String) {
        val error = if (nuevoTexto.trim().length < 3) "El nombre debe tener al menos 3 caracteres" else null
        _uiState.update { it.copy(nombre = nuevoTexto, errorNombre = error) }
        revisarValidez()
    }

    fun onRutChange(nuevoTexto: String) {
        val error = if (nuevoTexto.isBlank()) {
            "Ingresa el RUT de la empresa"
        } else if (!tieneFormatoRut(nuevoTexto)) {
            "Formato incorrecto: usa números, guion y dígito (Ej: 12345678-5)"
        } else if (!esRutValido(nuevoTexto)) {
            "Dígito verificador incorrecto, revisa el RUT"
        } else null
        _uiState.update { it.copy(rut = nuevoTexto, errorRut = error) }
        revisarValidez()
    }

    private fun limpiarRut(rut: String): String {
        return rut.replace(".", "").replace(" ", "").uppercase()
    }

    private fun tieneFormatoRut(rut: String): Boolean {
        val limpio = limpiarRut(rut)
        val partes = limpio.split("-")

        if (partes.size != 2) return false

        val cuerpo = partes[0]
        val dv = partes[1]

        val cuerpoCorrecto = cuerpo.length in 7..8 && cuerpo.all { it.isDigit() }
        val dvCorrecto = dv.length == 1 && (dv[0].isDigit() || dv[0] == 'K')
        return cuerpoCorrecto && dvCorrecto
    }

    // Valida el dígito verificador del RUT chileno mediante algoritmo módulo 11
    private fun esRutValido(rut: String): Boolean {
        val limpio = limpiarRut(rut)
        if (!tieneFormatoRut(limpio)) return false

        val cuerpo = limpio.substringBefore("-")
        val dvIngresado = limpio.substringAfter("-")

        var suma = 0
        var multiplicador = 2
        for (i in cuerpo.length - 1 downTo 0) {
            val digito = cuerpo[i].digitToInt()
            suma += digito * multiplicador
            multiplicador++
            if (multiplicador > 7) {
                multiplicador = 2
            }
        }

        val resultado = 11 - (suma % 11)

        val dvCalculado = if (resultado == 11) {
            "0"
        } else if (resultado == 10) {
            "K"
        } else {
            resultado.toString()
        }

        return dvCalculado == dvIngresado
    }

    fun onCorreoChange(nuevoTexto: String) {
        val error = if (!nuevoTexto.contains("@") || !nuevoTexto.contains(".")) {
            "Correo no válido (debe incluir @ y punto)"
        } else null
        _uiState.update { it.copy(correo = nuevoTexto, errorCorreo = error) }
        revisarValidez()
    }

    fun onRubroChange(nuevoRubro: String) {
        _uiState.update { it.copy(rubro = nuevoRubro) }
    }

    // Valida celular chileno de 9 dígitos iniciando con 9 ignorando espacios
    fun onTelefonoChange(nuevoTexto: String) {
        val soloNumeros = nuevoTexto.replace(" ", "")

        val error = if (soloNumeros.isEmpty()) {
            "Ingresa tu número de celular"
        } else if (!soloNumeros.all { it.isDigit() }) {
            "El teléfono solo debe tener números"
        } else if (!soloNumeros.startsWith("9")) {
            "El celular debe empezar con 9"
        } else if (soloNumeros.length != 9) {
            "El celular debe tener 9 dígitos (Ej: 912345678)"
        } else null

        _uiState.update { it.copy(telefono = nuevoTexto, errorTelefono = error) }
        revisarValidez()
    }

    fun onPasswordChange(nuevoTexto: String) {
        val error = if (nuevoTexto.length < 4) "La contraseña debe tener al menos 4 caracteres" else null
        _uiState.update { it.copy(password = nuevoTexto, errorPassword = error) }
        revisarValidez()
    }

    fun onPersonasNegocioChange(opcion: String) {
        _uiState.update { it.copy(personasNegocio = opcion, errorPersonas = null) }
        revisarValidez()
    }

    fun onFotoTomada(bitmap: Bitmap) {
        viewModelScope.launch {
            val guardada = fotoRepository.guardarFoto(bitmap)
            mostrarFotoGuardada(guardada)
        }
    }

    fun onFotoElegida(uri: Uri) {
        viewModelScope.launch {
            val guardada = fotoRepository.guardarFotoDesdeUri(uri)
            mostrarFotoGuardada(guardada)
        }
    }

    private fun mostrarFotoGuardada(foto: Bitmap?) {
        if (foto != null) {
            _uiState.update { it.copy(fotoLocal = foto, avisoCamara = null) }
        } else {
            onImagenNoDisponible()
        }
    }

    fun onImagenNoDisponible() {
        _uiState.update {
            it.copy(avisoCamara = "No se pudo abrir la imagen. Prueba con otra o regístrate igual.")
        }
    }

    fun onPermisoCamaraDenegado() {
        _uiState.update {
            it.copy(avisoCamara = "Sin permiso de cámara no podemos tomar la foto. Puedes registrarte igual.")
        }
    }

    fun onCamaraNoDisponible() {
        _uiState.update {
            it.copy(avisoCamara = "No se encontró una cámara en este dispositivo. Puedes registrarte igual.")
        }
    }

    private fun revisarValidez() {
        val actual = _uiState.value
        val passwordValida = actual.esEdicion ||
                (actual.password.isNotBlank() && actual.errorPassword == null)
        val camposTextoValidos = actual.nombre.isNotBlank() && actual.errorNombre == null &&
                actual.rut.isNotBlank() && actual.errorRut == null &&
                actual.correo.isNotBlank() && actual.errorCorreo == null &&
                actual.telefono.isNotBlank() && actual.errorTelefono == null &&
                passwordValida

        val valido = camposTextoValidos && actual.personasNegocio.isNotBlank()

        _uiState.update {
            it.copy(esFormularioValido = valido, puedeIntentarRegistrar = camposTextoValidos)
        }
    }

    // Registra un nuevo usuario o actualiza el perfil existente y persiste la sesión
    fun guardarPerfil(onExito: () -> Unit) {
        val actual = _uiState.value

        if (actual.cargando) return

        if (actual.personasNegocio.isBlank()) {
            _uiState.update {
                it.copy(errorPersonas = "Elige una opción (puedes elegir \"Prefiero no decirlo\")")
            }
            return
        }

        if (!actual.esFormularioValido) return

        _uiState.update { it.copy(cargando = true, errorGeneral = null) }

        viewModelScope.launch {
            try {
                val usuario = if (actual.esEdicion) {
                    val id = almacenamiento.obtenerIdUsuario() ?: throw UsuarioException.NoEncontrado()
                    usuarioRepository.actualizarPerfil(
                        id = id,
                        nombre = actual.nombre,
                        telefono = actual.telefono,
                        rubro = actual.rubro,
                        personasNegocio = actual.personasNegocio
                    )
                } else {
                    usuarioRepository.registrar(
                        nombre = actual.nombre,
                        rut = actual.rut,
                        correo = actual.correo,
                        telefono = actual.telefono,
                        rubro = actual.rubro,
                        personasNegocio = actual.personasNegocio,
                        password = actual.password
                    )
                }
                almacenamiento.guardarSesion(usuario)
                cargarPerfilGuardado()
                onExito()
            } catch (e: UsuarioException) {
                _uiState.update { it.copy(cargando = false, errorGeneral = e.message) }
            }
        }
    }

    fun obtenerNombreGuardado(): String {
        return almacenamiento.obtenerNombre()
    }

    fun obtenerRutGuardado(): String {
        return almacenamiento.obtenerRut()
    }

    fun obtenerCorreoGuardado(): String {
        return almacenamiento.obtenerCorreo()
    }

    fun obtenerRubroGuardado(): String {
        return almacenamiento.obtenerRubro()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                RegistroViewModel(
                    application = application,
                    usuarioRepository = ProveedorRepositorios.usuarioRepository,
                    almacenamiento = AlmacenamientoLocal(application)
                )
            }
        }
    }
}