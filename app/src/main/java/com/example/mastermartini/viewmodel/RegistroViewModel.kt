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
    val password: String = "",            // Contraseña que se crea al registrarse (se usa después en el login)
    val errorPassword: String? = null,    // Mensaje de error de la contraseña
    // Tamaño del negocio: "Micro", "Pequeño" o "Mediano" (se elige con FilterChips)
    val tamanoNegocio: String = "Micro",
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
    // La contraseña no se precarga: al editar los datos hay que volver a escribirla.
    private fun cargarPerfilGuardado() {
        if (almacenamiento.existePerfil()) {
            onNombreChange(almacenamiento.obtenerNombre())
            onRutChange(almacenamiento.obtenerRut())
            onCorreoChange(almacenamiento.obtenerCorreo())
            onRubroChange(almacenamiento.obtenerRubro())
            // Si el perfil se guardó antes de existir el teléfono, llega "" y el campo
            // muestra "Ingresa tu número de celular" para que el usuario lo complete.
            onTelefonoChange(almacenamiento.obtenerTelefono())
            onTamanoNegocioChange(almacenamiento.obtenerTamanoNegocio())
        }
    }

    // true si ya hay un perfil guardado. Hoy MainActivity no la usa (la app siempre parte en "login"),
    // pero sirve si se quiere elegir la pantalla inicial según haya perfil o no.
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

    // Celular chileno: 9 dígitos y empieza con 9 (Ej: 9 1234 5678).
    // Se revisa en orden y se muestra el primer error que se encuentre.
    fun onTelefonoChange(nuevoTexto: String) {
        // Se ignoran los espacios: "9 1234 5678" se revisa como "912345678"
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

        // Se guarda el texto tal como lo escribió el usuario (con espacios), para no cambiarle lo que ve
        _uiState.update { it.copy(telefono = nuevoTexto, errorTelefono = error) }
        revisarValidez()
    }

    // Contraseña del registro: mínimo 4 caracteres.
    // Es la que después pide el LoginViewModel para iniciar sesión.
    fun onPasswordChange(nuevoTexto: String) {
        val error = if (nuevoTexto.length < 4) "La contraseña debe tener al menos 4 caracteres" else null
        _uiState.update { it.copy(password = nuevoTexto, errorPassword = error) }
        revisarValidez()
    }

    // El tamaño se elige con chips, así que no puede venir mal escrito: no necesita validación
    fun onTamanoNegocioChange(nuevoTamano: String) {
        _uiState.update { it.copy(tamanoNegocio = nuevoTamano) }
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

    // Equivalente directo a perfilCompleto() de tu diapositiva de clase.
    // El formulario es válido si todos los campos tienen texto y ninguno tiene error
    // (incluida la contraseña). Con esto se habilita el botón "REGISTRAR Y ACCEDER".
    private fun revisarValidez() {
        val actual = _uiState.value
        val valido = actual.nombre.isNotBlank() && actual.errorNombre == null &&
                actual.rut.isNotBlank() && actual.errorRut == null &&
                actual.correo.isNotBlank() && actual.errorCorreo == null &&
                actual.telefono.isNotBlank() && actual.errorTelefono == null &&
                actual.password.isNotBlank() && actual.errorPassword == null

        _uiState.update { it.copy(esFormularioValido = valido) }
    }

    // Guarda en el disco del celular mediante SharedPreferences (también la contraseña,
    // para que el login pueda compararla después)
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
                password = actual.password
            )
            onExito() // Dispara la navegación hacia Home (la decide MainActivity)
        }
    }

    // Lectores del perfil guardado. MainActivity los usa para mostrar los datos en Home.
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