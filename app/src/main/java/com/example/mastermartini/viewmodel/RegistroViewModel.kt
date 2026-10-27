package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mastermartini.data.AlmacenamientoLocal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 1. EL MOLDE DE ESTADO (Visto en tu diapositiva de clase)
// Agrupa todo lo que la pantalla necesita saber en un solo paquete inmutable
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
    val password: String = "",            // 👈 Nuevo: Guarda la contraseña ingresada
    val errorPassword: String? = null,    // 👈 Nuevo: Mensaje de error de la contraseña
    val tamanoNegocio: String = "Micro",
    val tieneFoto: Boolean = false,
    val avisoCamara: String? = null,
    val esFormularioValido: Boolean = false
)

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    // Capa de persistencia local (IE 2.3.1)
    private val almacenamiento = AlmacenamientoLocal(application)

    // Flujo de estado reactivo oficial de Jetpack Compose
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    init {
        cargarPerfilGuardado()
    }

    private fun cargarPerfilGuardado() {
        if (almacenamiento.existePerfil()) {
            onNombreChange(almacenamiento.obtenerNombre())
            onRutChange(almacenamiento.obtenerRut())
            onCorreoChange(almacenamiento.obtenerCorreo())
            onRubroChange(almacenamiento.obtenerRubro())
            onTelefonoChange(almacenamiento.obtenerTelefono())
            onTamanoNegocioChange(almacenamiento.obtenerTamanoNegocio())
        }
    }

    fun hayPerfilGuardado(): Boolean {
        return almacenamiento.existePerfil()
    }

    // --- REGLAS DE NEGOCIO DESACOPLADAS (IE 2.2.1) ---

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

    private fun esRutValido(rut: String): Boolean {
        val limpio = limpiarRut(rut)
        if (!tieneFormatoRut(limpio)) return false

        val cuerpo = limpio.substringBefore("-")
        val dvIngresado = limpio.substringAfter("-")

        var suma = 0
        var multiplicador = 2
        for (i in cuerpo.length - 1 downTo 0) { // 👈 Corregido
            val digito = cuerpo[i].digitToInt()
            suma += digito * multiplicador
            multiplicador++
            if (multiplicador > 7) {
                multiplicador = 2
            }
        }

        val resultado = 11 - (suma % 11) // 👈 Corregido

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

    // 👈 NUEVA FUNCIÓN: Maneja el cambio del campo contraseña
    fun onPasswordChange(nuevoTexto: String) {
        val error = if (nuevoTexto.length < 4) "La contraseña debe tener al menos 4 caracteres" else null
        _uiState.update { it.copy(password = nuevoTexto, errorPassword = error) }
        revisarValidez()
    }

    fun onTamanoNegocioChange(nuevoTamano: String) {
        _uiState.update { it.copy(tamanoNegocio = nuevoTamano) }
    }

    fun registrarFotoCapturada() {
        _uiState.update { it.copy(tieneFoto = true, avisoCamara = null) }
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

    // Actualizado para que exija que la contraseña sea válida antes de habilitar el botón
    private fun revisarValidez() {
        val actual = _uiState.value
        val valido = actual.nombre.isNotBlank() && actual.errorNombre == null &&
                actual.rut.isNotBlank() && actual.errorRut == null &&
                actual.correo.isNotBlank() && actual.errorCorreo == null &&
                actual.telefono.isNotBlank() && actual.errorTelefono == null &&
                actual.password.isNotBlank() && actual.errorPassword == null // 👈 Incluido en la validación

        _uiState.update { it.copy(esFormularioValido = valido) }
    }

    // Actualizado para pasar la contraseña a SharedPreferences
    fun guardarPerfil(onExito: () -> Unit) {
        val actual = _uiState.value
        if (actual.esFormularioValido) {
            almacenamiento.guardarPerfil(
                nombre = actual.nombre,
                rut = actual.rut,
                correo = actual.correo,
                rubro = actual.rubro,
                telefono = actual.telefono,
                tamanoNegocio = actual.tamanoNegocio,
                password = actual.password // 👈 Guarda la contraseña ingresada
            )
            onExito()
        }
    }

    // 👈 NUEVA FUNCIÓN: Permite a LoginScreen verificar credenciales en AlmacenamientoLocal
    fun validarLogin(rutOCorreo: String, pass: String): Boolean {
        return almacenamiento.validarCredenciales(rutOCorreo, pass)
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
}