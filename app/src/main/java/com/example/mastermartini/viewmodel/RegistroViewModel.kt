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
    // Mensaje que se muestra si no se pudo usar la cámara (null = no hay aviso)
    val avisoCamara: String? = null,
    val esFormularioValido: Boolean = false
)

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    // Capa de persistencia local (IE 2.3.1)
    private val almacenamiento = AlmacenamientoLocal(application)

    // Flujo de estado reactivo oficial de Jetpack Compose
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    // init se ejecuta una sola vez, cuando se crea el ViewModel (al abrir la app).
    // Va DESPUÉS de _uiState porque Kotlin inicializa las propiedades en orden:
    // si estuviera antes, _uiState todavía no existiría.
    init {
        cargarPerfilGuardado()
    }

    // Si ya hay un perfil en SharedPreferences, lo copia al formulario (queda precargado al editar).
    // Usa las mismas funciones onXChange() que el usuario al escribir, así los datos
    // pasan por las mismas validaciones y revisarValidez() habilita el botón.
    private fun cargarPerfilGuardado() {
        if (almacenamiento.existePerfil()) {
            onNombreChange(almacenamiento.obtenerNombre())
            onRutChange(almacenamiento.obtenerRut())
            onCorreoChange(almacenamiento.obtenerCorreo())
            onRubroChange(almacenamiento.obtenerRubro())
        }
    }

    // MainActivity la usa para decidir la pantalla inicial ("home" o "registro")
    fun hayPerfilGuardado(): Boolean {
        return almacenamiento.existePerfil()
    }

    // --- REGLAS DE NEGOCIO DESACOPLADAS (IE 2.2.1) ---

    fun onNombreChange(nuevoTexto: String) {
        val error = if (nuevoTexto.trim().length < 3) "El nombre debe tener al menos 3 caracteres" else null
        _uiState.update { it.copy(nombre = nuevoTexto, errorNombre = error) }
        revisarValidez()
    }

    // El RUT se revisa en 3 niveles, de lo más simple a lo más completo,
    // para que el mensaje le diga al usuario exactamente qué corregir.
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

    // Deja el RUT "limpio" para revisarlo: sin puntos, sin espacios y con la k en mayúscula.
    // Ej: " 12.345.678-k " -> "12345678-K"
    private fun limpiarRut(rut: String): String {
        return rut.replace(".", "").replace(" ", "").uppercase()
    }

    // Revisa solo la FORMA del RUT (no si es real):
    // cuerpo de 7 u 8 números + guion + un dígito o K.
    // Acepta "12345678-5" y "12.345.678-5" porque primero se limpian los puntos.
    private fun tieneFormatoRut(rut: String): Boolean {
        val limpio = limpiarRut(rut)
        val partes = limpio.split("-")

        // Debe haber exactamente un guion (2 partes: cuerpo y dígito)
        if (partes.size != 2) return false

        val cuerpo = partes[0]
        val dv = partes[1]

        val cuerpoCorrecto = cuerpo.length in 7..8 && cuerpo.all { it.isDigit() }
        val dvCorrecto = dv.length == 1 && (dv[0].isDigit() || dv[0] == 'K')
        return cuerpoCorrecto && dvCorrecto
    }

    // Revisa si el RUT es VÁLIDO calculando su dígito verificador con el algoritmo módulo 11.
    // Ejemplo resuelto: 12345678 -> dígito 5 (ver ESTUDIO.md).
    private fun esRutValido(rut: String): Boolean {
        // Paso 1: limpiar y revisar la forma. Si la forma está mal, no tiene sentido calcular.
        val limpio = limpiarRut(rut)
        if (!tieneFormatoRut(limpio)) return false

        // Paso 2: separar el cuerpo ("12345678") del dígito verificador ingresado ("5" o "K")
        val cuerpo = limpio.substringBefore("-")
        val dvIngresado = limpio.substringAfter("-")

        // Paso 3: recorrer el cuerpo de DERECHA a IZQUIERDA multiplicando cada número
        // por 2, 3, 4, 5, 6, 7 y volver a empezar en 2 cuando se pasa de 7. Se suman los resultados.
        var suma = 0
        var multiplicador = 2
        for (i in cuerpo.length - 1 downTo 0) {
            val digito = cuerpo[i].digitToInt() // convierte el carácter '8' en el número 8
            suma += digito * multiplicador
            multiplicador++
            if (multiplicador > 7) {
                multiplicador = 2
            }
        }

        // Paso 4: módulo 11. Se resta a 11 el resto de dividir la suma por 11.
        val resultado = 11 - (suma % 11)

        // Paso 5: convertir el resultado en dígito. 11 se escribe "0" y 10 se escribe "K".
        val dvCalculado = if (resultado == 11) {
            "0"
        } else if (resultado == 10) {
            "K"
        } else {
            resultado.toString()
        }

        // Paso 6: el RUT es válido si el dígito calculado es igual al que escribió el usuario
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

    fun registrarFotoCapturada() {
        // Si la foto se tomó bien, se borra cualquier aviso anterior de la cámara
        _uiState.update { it.copy(tieneFoto = true, avisoCamara = null) }
    }

    // Se llama cuando el usuario rechaza el permiso de cámara.
    // La foto es opcional, así que solo avisamos y el registro sigue funcionando.
    fun onPermisoCamaraDenegado() {
        _uiState.update {
            it.copy(avisoCamara = "Sin permiso de cámara no podemos tomar la foto. Puedes registrarte igual.")
        }
    }

    // Se llama si el celular no tiene una app de cámara para abrir (evita que la app se caiga)
    fun onCamaraNoDisponible() {
        _uiState.update {
            it.copy(avisoCamara = "No se encontró una cámara en este dispositivo. Puedes registrarte igual.")
        }
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