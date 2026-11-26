package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.CursoServidorException
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Leccion
import com.example.mastermartini.model.Modulo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ESTADO DEL FORMULARIO "CREAR CURSO ONLINE"
// Junta en un solo objeto lo que CrearCursoScreen muestra: lo que va escribiendo el
// instructor o administrador, si se está enviando y el mensaje de error.
// Algunos campos parten con un valor de ejemplo para que el formulario sea más rápido de llenar.
data class CrearCursoUiState(
    // Datos del curso
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "Chocolatería",
    val nivel: String = "Básico",
    // true = curso gratis; false = solo con Plan Pro
    val esGratis: Boolean = true,
    val duracionTotal: String = "45 min",
    // Debe llamarse igual que un producto de ProductoRepository (el Detalle lo busca por nombre)
    val insumoPrincipal: String = "Chocolate Ariba",
    // Enlace .mp4 del video subido a Cloudinary (opcional)
    val videoCloudinaryUrl: String = "",
    // El primer módulo y su primera lección (el backend exige al menos uno de cada uno)
    val tituloModulo: String = "Módulo 1: Introducción y técnica",
    val tituloLeccion: String = "Clase 1: Preparación paso a paso",
    val descripcionLeccion: String = "Demostración práctica con insumos Master Martini.",
    val duracionLeccion: String = "15 min",
    // true mientras se espera la respuesta del servidor (el botón muestra una ruedita)
    val cargando: Boolean = false,
    // Texto del error para mostrar en rojo. null = no hay error
    val mensajeError: String? = null,
    // true cuando el servidor guardó el curso: la pantalla vuelve atrás sola
    val guardadoExitoso: Boolean = false
)

// VIEWMODEL DEL FORMULARIO "CREAR CURSO ONLINE" (solo Instructor o Administrador)
// Recibe lo que se escribe, VALIDA los datos (aquí y no en la pantalla) y envía el curso
// al microservicio de cursos (puerto 9092) a través de CapacitacionRepository.
// Es AndroidViewModel porque necesita el Context para leer la sesión en SharedPreferences.
class CrearCursoViewModel(application: Application) : AndroidViewModel(application) {

    // Sesión guardada (id y nombre de quien crea el curso)
    private val almacenamiento = AlmacenamientoLocal(application)
    // Habla con el microservicio de cursos (Retrofit)
    private val repository = CapacitacionRepository()

    // Estado privado y mutable: solo este ViewModel lo cambia
    private val _uiState = MutableStateFlow(CrearCursoUiState())

    // Versión pública de solo lectura: CrearCursoScreen la observa con collectAsState()
    val uiState: StateFlow<CrearCursoUiState> = _uiState.asStateFlow()

    // Opciones de los botones de categoría y nivel (las mismas 5 categorías de los cursos)
    val categorias = listOf("Chocolatería", "Pastelería", "Panadería", "Heladería", "Decoración")
    val niveles = listOf("Básico", "Intermedio", "Avanzado")

    // FUNCIONES onXxxChange: se llaman cada vez que el usuario escribe o elige una opción.
    // copy() cambia solo ese campo. En los campos obligatorios también se borra el error
    // (mensajeError = null), así el aviso rojo desaparece apenas el usuario lo corrige.
    fun onTituloChange(v: String) = _uiState.update { it.copy(titulo = v, mensajeError = null) }
    fun onDescripcionChange(v: String) = _uiState.update { it.copy(descripcion = v, mensajeError = null) }
    fun onCategoriaChange(v: String) = _uiState.update { it.copy(categoria = v) }
    fun onNivelChange(v: String) = _uiState.update { it.copy(nivel = v) }
    fun onEsGratisChange(v: Boolean) = _uiState.update { it.copy(esGratis = v) }
    fun onDuracionChange(v: String) = _uiState.update { it.copy(duracionTotal = v) }
    fun onInsumoChange(v: String) = _uiState.update { it.copy(insumoPrincipal = v) }
    fun onVideoUrlChange(v: String) = _uiState.update { it.copy(videoCloudinaryUrl = v, mensajeError = null) }
    fun onTituloModuloChange(v: String) = _uiState.update { it.copy(tituloModulo = v) }
    fun onTituloLeccionChange(v: String) = _uiState.update { it.copy(tituloLeccion = v) }

    // Se llama al tocar "Publicar curso". Primero valida; si algo falla, muestra el error
    // y hace return (no se envía nada). Si todo está bien, arma el curso y lo envía.
    fun publicarCurso() {
        val actual = _uiState.value
        // 1. Campos obligatorios
        if (actual.titulo.isBlank() || actual.descripcion.isBlank() || actual.insumoPrincipal.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Completa título, descripción e insumo principal") }
            return
        }

        // 2. Enlace del video: es opcional (vacío = null), pero si se escribe debe ser una dirección web
        val urlLimpia = actual.videoCloudinaryUrl.trim().ifBlank { null }
        if (urlLimpia != null && !urlLimpia.startsWith("http")) {
            _uiState.update { it.copy(mensajeError = "El enlace de Cloudinary debe empezar con http:// o https://") }
            return
        }

        // 3. Sesión: el servidor necesita el id de quien crea el curso (header X-Usuario-Id)
        // para revisar su rol. Si no hay sesión (null), NO se inventa un id: se avisa y no se envía.
        val usuarioId = almacenamiento.obtenerIdUsuario()
        if (usuarioId == null) {
            _uiState.update { it.copy(mensajeError = "Debes iniciar sesión como instructor o administrador") }
            return
        }
        // El nombre de la sesión queda como instructor del curso
        val nombreInstructor = almacenamiento.obtenerNombre()

        // Se arma el curso. trim() quita espacios sobrantes e ifBlank pone un valor si quedó vacío.
        // El backend exige al menos 1 módulo y 1 lección al crear el curso.
        // El mismo video de Cloudinary se usa como resumen del curso y como video de la lección 1.
        val nuevoCurso = Capacitacion(
            id = 0,
            titulo = actual.titulo.trim(),
            descripcion = actual.descripcion.trim(),
            categoria = actual.categoria,
            nivel = actual.nivel,
            esGratis = actual.esGratis,
            instructor = nombreInstructor,
            duracionTotal = actual.duracionTotal.trim().ifBlank { "30 min" },
            insumoPrincipal = actual.insumoPrincipal.trim(),
            videoResumenUrl = urlLimpia,
            loQueLograras = listOf("Dominar el uso de ${actual.insumoPrincipal.trim()}"),
            incluye = listOf("Certificado Master Martini", "Acceso ilimitado"),
            modulos = listOf(
                Modulo(
                    titulo = actual.tituloModulo.trim().ifBlank { "Módulo 1" },
                    lecciones = listOf(
                        Leccion(
                            titulo = actual.tituloLeccion.trim().ifBlank { "Lección 1" },
                            descripcion = actual.descripcionLeccion.trim(),
                            duracion = actual.duracionLeccion.trim().ifBlank { "15 min" },
                            videoUrl = urlLimpia
                        )
                    )
                )
            )
        )

        // 4. Envío al servidor dentro de una corrutina (la llamada a internet es lenta)
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }
            // obtenerIdUsuario() devuelve Long y el header del servidor es Int: toInt() lo convierte
            val resultado = repository.crearCapacitacion(usuarioId.toInt(), nuevoCurso)
            // fold: una función para cuando salió bien (onSuccess) y otra para cuando falló (onFailure)
            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(cargando = false, guardadoExitoso = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(cargando = false, mensajeError = textoDelError(error)) }
                }
            )
        }
    }

    // Elige el texto del error que ve el usuario.
    // Si el servidor respondió con un código de error (CursoServidorException), se revisa el código:
    //   403 -> quien está conectado no es instructor ni administrador
    //   503 -> el servidor de cursos no pudo consultar al microservicio de usuarios
    //   otro -> el "mensaje" que mandó el servidor en su ErrorDto (o el texto por defecto si no vino)
    // Si no es un error del servidor (ej. sin conexión), se usa el mensaje que trae el error.
    private fun textoDelError(error: Throwable): String {
        if (error is CursoServidorException) {
            return when (error.codigo) {
                403 -> "Solo instructores o administradores pueden crear cursos"
                503 -> "El servicio de usuarios no está disponible"
                else -> error.mensajeServidor ?: error.message ?: "No se pudo crear el curso"
            }
        }
        return error.message ?: "No se pudo crear el curso"
    }
}