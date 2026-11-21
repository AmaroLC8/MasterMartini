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
    // Respuesta a "¿Cuántas personas trabajan en tu negocio?" (una de opcionesPersonas).
    // Vacío = todavía no elige. Reemplaza al antiguo "tamaño del negocio" (Micro/Pequeño/Mediano),
    // que podía sonar despectivo y el usuario no tenía cómo saber cuál le correspondía.
    val personasNegocio: String = "",
    // Error de esa pregunta. Solo aparece al tocar "Registrar" sin haber elegido (null = sin error)
    val errorPersonas: String? = null,
    // Foto del local ya guardada en el celular (null = todavía no hay foto).
    // Antes vivía en un remember de la pantalla y se perdía al salir; ahora la guarda
    // el ViewModel y se vuelve a cargar del archivo al abrir la app.
    val fotoLocal: Bitmap? = null,
    // Mensaje que se muestra si no se pudo usar la cámara o abrir la imagen (null = no hay aviso)
    val avisoCamara: String? = null,
    // true cuando TODO está completo y sin errores (incluida la pregunta de personas).
    // Con esto aparece el banner "Formulario validado" y se permite guardar.
    val esFormularioValido: Boolean = false,
    // true cuando los campos de TEXTO están completos y sin errores. Con esto se habilita
    // el botón "REGISTRAR Y ACCEDER": así, si falta elegir cuántas personas trabajan,
    // el usuario puede tocarlo y ver el error de esa pregunta (con el botón apagado no lo vería).
    val puedeIntentarRegistrar: Boolean = false,
    // true mientras se espera la respuesta del repositorio: el botón se desactiva y muestra carga
    val cargando: Boolean = false,
    // Error que devolvió el repositorio (ej. "Ya existe una cuenta con ese correo..."). null = sin error
    val errorGeneral: String? = null,
    // true = "Editar datos" (ya hay una sesión iniciada); false = crear una cuenta nueva.
    // Al editar se llama a actualizarPerfil() en vez de registrar(), porque registrar()
    // respondería "dato duplicado" con el propio RUT y correo del usuario.
    // El RUT, el correo y la contraseña no se cambian al editar (el backend no lo permite
    // en PUT /{id}), por eso la pantalla los muestra fijos o los esconde.
    val esEdicion: Boolean = false
)

// INYECCIÓN POR CONSTRUCTOR: igual que LoginViewModel, recibe el repositorio de usuarios
// y el almacenamiento en vez de crearlos (así se puede probar con MockK).
// Sigue siendo AndroidViewModel porque FotoRepository necesita la Application para
// guardar la foto del local en la memoria interna.
class RegistroViewModel(
    application: Application,
    private val usuarioRepository: UsuarioRepository,
    // Capa de persistencia local (IE 2.3.1)
    private val almacenamiento: AlmacenamientoLocal
) : AndroidViewModel(application) {

    // Guarda y lee la foto del local como archivo JPEG en la memoria interna
    private val fotoRepository = FotoRepository(application)

    // Opciones de "¿Cuántas personas trabajan en tu negocio?". Están aquí (y no en la pantalla)
    // porque son datos del formulario: la pantalla solo las dibuja, y el ViewModel las usa
    // para revisar que lo guardado sea una opción válida.
    // Va ANTES de init: cargarPerfilGuardado() la usa, y Kotlin inicializa en orden.
    val opcionesPersonas = listOf(
        "Solo yo",
        "2 a 5 personas",
        "6 a 20 personas",
        "Más de 20 personas",
        "Prefiero no decirlo"
    )

    // Flujo de estado reactivo oficial de Jetpack Compose
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    // init se ejecuta una sola vez, cuando se crea el ViewModel (al abrir la app).
    // Va DESPUÉS de _uiState porque Kotlin inicializa las propiedades en orden:
    // si estuviera antes, _uiState todavía no existiría.
    init {
        cargarPerfilGuardado()

        // La foto se carga AUNQUE no haya perfil completo, porque se guarda apenas se elige
        // (antes de tocar "Registrar"). Leer un archivo es lento, por eso va en una corrutina:
        // obtenerFoto() trabaja en Dispatchers.IO y aquí solo se espera el resultado.
        viewModelScope.launch {
            val fotoGuardada = fotoRepository.obtenerFoto()
            _uiState.update { it.copy(fotoLocal = fotoGuardada) }
        }
    }

    // Deja el formulario igual a lo que hay guardado en la sesión.
    // Si hay sesión, copia sus datos (queda precargado para "Editar datos").
    // Si no hay sesión, el formulario queda vacío (para crear una cuenta nueva).
    // Es pública porque este ViewModel vive en MainActivity (lo comparten Registro y Home):
    // MainActivity la llama al iniciar sesión y al cerrarla, para que el formulario
    // muestre los datos de la persona que está usando la app y no los de la anterior.
    // Usa las mismas funciones onXChange() que el usuario al escribir, así los datos
    // pasan por las mismas validaciones y revisarValidez() habilita el botón.
    // La contraseña no se precarga (no se guarda en el celular) y al editar no se pide.
    fun cargarPerfilGuardado() {
        // Se parte de un formulario vacío, conservando solo la foto (que es del celular).
        // esEdicion = true si hay un usuario con sesión iniciada.
        _uiState.value = RegistroUiState(
            fotoLocal = _uiState.value.fotoLocal,
            esEdicion = almacenamiento.obtenerIdUsuario() != null
        )

        if (almacenamiento.existePerfil()) {
            onNombreChange(almacenamiento.obtenerNombre())
            onRutChange(almacenamiento.obtenerRut())
            onCorreoChange(almacenamiento.obtenerCorreo())
            onRubroChange(almacenamiento.obtenerRubro())
            // Si el perfil se guardó antes de existir el teléfono, llega "" y el campo
            // muestra "Ingresa tu número de celular" para que el usuario lo complete.
            onTelefonoChange(almacenamiento.obtenerTelefono())

            // Solo se precarga si lo guardado es una de las opciones NUEVAS.
            // Si es un valor antiguo ("Micro", "Pequeño", "Mediano") o cualquier otro texto,
            // queda vacío y el usuario tiene que elegir de nuevo. "in" revisa si está en la lista.
            val personasGuardado = almacenamiento.obtenerPersonasNegocio()
            if (personasGuardado in opcionesPersonas) {
                onPersonasNegocioChange(personasGuardado)
            }
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
    // Se envía al repositorio al registrarse y es la que después pide el login.
    fun onPasswordChange(nuevoTexto: String) {
        val error = if (nuevoTexto.length < 4) "La contraseña debe tener al menos 4 caracteres" else null
        _uiState.update { it.copy(password = nuevoTexto, errorPassword = error) }
        revisarValidez()
    }

    // Se llama al tocar una opción de "¿Cuántas personas trabajan en tu negocio?".
    // Al elegir una, se borra el error (si había) y se revisa otra vez el formulario.
    // Como se elige de una lista, no puede venir mal escrita: la única validación es que no esté vacía.
    fun onPersonasNegocioChange(opcion: String) {
        _uiState.update { it.copy(personasNegocio = opcion, errorPersonas = null) }
        revisarValidez()
    }

    // FOTO DEL LOCAL
    // Se llama con la foto que devolvió la cámara (TakePicturePreview entrega un Bitmap).
    // viewModelScope.launch: guardar el archivo es lento y guardarFoto() es suspend.
    fun onFotoTomada(bitmap: Bitmap) {
        viewModelScope.launch {
            val guardada = fotoRepository.guardarFoto(bitmap)
            mostrarFotoGuardada(guardada)
        }
    }

    // Se llama con el Uri de la imagen elegida en la galería (PickVisualMedia)
    // o en el explorador de archivos (GetContent). Los dos entregan un Uri, por eso
    // comparten esta misma función.
    fun onFotoElegida(uri: Uri) {
        viewModelScope.launch {
            val guardada = fotoRepository.guardarFotoDesdeUri(uri)
            mostrarFotoGuardada(guardada)
        }
    }

    // Si se guardó bien, se muestra y se borra cualquier aviso anterior.
    // Si falló (null), se deja la foto que había y se avisa: la app no se cae.
    private fun mostrarFotoGuardada(foto: Bitmap?) {
        if (foto != null) {
            _uiState.update { it.copy(fotoLocal = foto, avisoCamara = null) }
        } else {
            onImagenNoDisponible()
        }
    }

    // Se llama si la imagen no se pudo leer o guardar, o si el celular no tiene
    // una app de galería o de archivos para abrir. La foto es opcional: solo se avisa.
    fun onImagenNoDisponible() {
        _uiState.update {
            it.copy(avisoCamara = "No se pudo abrir la imagen. Prueba con otra o regístrate igual.")
        }
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
    // Revisa el formulario en dos partes:
    // 1. camposTextoValidos: todos los campos de texto tienen algo y ninguno tiene error
    //    (incluida la contraseña). Con esto se habilita el botón "REGISTRAR Y ACCEDER".
    // 2. valido: lo anterior Y además se eligió cuántas personas trabajan (es obligatoria).
    //    Con esto aparece el banner verde y se permite guardar.
    private fun revisarValidez() {
        val actual = _uiState.value
        // Al editar los datos la contraseña no se pide (no se cambia desde aquí), así que cuenta como lista
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

    // Se llama al tocar "REGISTRAR Y ACCEDER" (o "GUARDAR CAMBIOS" al editar).
    // Envía los datos al repositorio (el backend) y, si sale bien, guarda la sesión
    // en SharedPreferences. La contraseña NO se guarda en el celular.
    fun guardarPerfil(onExito: () -> Unit) {
        val actual = _uiState.value

        // Si ya se está esperando una respuesta, no se manda otra (evita el doble toque)
        if (actual.cargando) return

        // Si falta la pregunta de personas, se muestra su error AHORA (al intentar registrar)
        // y no se guarda nada. No se muestra antes para no recibir al usuario con un error en rojo.
        if (actual.personasNegocio.isBlank()) {
            _uiState.update {
                it.copy(errorPersonas = "Elige una opción (puedes elegir \"Prefiero no decirlo\")")
            }
            return
        }

        if (!actual.esFormularioValido) return

        // Empieza la espera: se borra el error anterior y se muestra el indicador de carga
        _uiState.update { it.copy(cargando = true, errorGeneral = null) }

        viewModelScope.launch {
            // try/catch: si el repositorio LANZA una UsuarioException (ej. DatoDuplicado),
            // el catch la ATRAPA, se muestra su mensaje y la app no se cae.
            try {
                val usuario = if (actual.esEdicion) {
                    // EDITAR DATOS: se actualiza la cuenta de la sesión actual.
                    // Si no hubiera id (no debería pasar), se trata como "usuario no encontrado".
                    val id = almacenamiento.obtenerIdUsuario() ?: throw UsuarioException.NoEncontrado()
                    usuarioRepository.actualizarPerfil(
                        id = id,
                        nombre = actual.nombre,
                        telefono = actual.telefono,
                        rubro = actual.rubro,
                        personasNegocio = actual.personasNegocio
                    )
                } else {
                    // CUENTA NUEVA: si el correo o el RUT ya existen, lanza DatoDuplicado
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
                // Salió bien: se guarda la sesión con lo que devolvió el repositorio
                almacenamiento.guardarSesion(usuario)
                // Se recarga el formulario desde la sesión: queda en modo edición,
                // sin la contraseña escrita y con cargando = false
                cargarPerfilGuardado()
                onExito() // Vibra y navega (lo decide la pantalla y MainActivity). Solo en el éxito.
            } catch (e: UsuarioException) {
                _uiState.update { it.copy(cargando = false, errorGeneral = e.message) }
            }
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

    // FACTORY: la "receta" para crear este ViewModel con sus dependencias
    // (MainActivity lo usa así: viewModel(factory = RegistroViewModel.Factory)).
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // La Application la necesitan AndroidViewModel, FotoRepository y AlmacenamientoLocal
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