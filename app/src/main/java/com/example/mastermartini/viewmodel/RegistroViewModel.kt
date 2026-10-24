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
    val tieneFoto: Boolean = false,
    val esFormularioValido: Boolean = false
)

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    // Capa de persistencia local (IE 2.3.1)
    private val almacenamiento = AlmacenamientoLocal(application)

    // Flujo de estado reactivo oficial de Jetpack Compose
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    // --- REGLAS DE NEGOCIO DESACOPLADAS (IE 2.2.1) ---

    fun onNombreChange(nuevoTexto: String) {
        val error = if (nuevoTexto.trim().length < 3) "El nombre debe tener al menos 3 caracteres" else null
        _uiState.update { it.copy(nombre = nuevoTexto, errorNombre = error) }
        revisarValidez()
    }

    fun onRutChange(nuevoTexto: String) {
        val error = if (nuevoTexto.length < 8 || !nuevoTexto.contains("-")) {
            "El RUT debe incluir guion (Ej: 12345678-9)"
        } else null
        _uiState.update { it.copy(rut = nuevoTexto, errorRut = error) }
        revisarValidez()
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

    fun registrarFotoCapturada() {
        _uiState.update { it.copy(tieneFoto = true) }
    }

    // Equivalente directo a perfilCompleto() de tu diapositiva de clase
    private fun revisarValidez() {
        val actual = _uiState.value
        val valido = actual.nombre.isNotBlank() && actual.errorNombre == null &&
                actual.rut.isNotBlank() && actual.errorRut == null &&
                actual.correo.isNotBlank() && actual.errorCorreo == null

        _uiState.update { it.copy(esFormularioValido = valido) }
    }

    // Guarda en el disco del celular mediante SharedPreferences
    fun guardarPerfil(onExito: () -> Unit) {
        val actual = _uiState.value
        if (actual.esFormularioValido) {
            almacenamiento.guardarPerfil(actual.nombre, actual.rut, actual.correo, actual.rubro)
            onExito() // Dispara la navegación hacia el catálogo
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
}