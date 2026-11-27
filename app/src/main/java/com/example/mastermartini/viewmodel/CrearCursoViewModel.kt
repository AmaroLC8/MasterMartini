package com.example.mastermartini.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.CloudinaryRepository
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Leccion
import com.example.mastermartini.model.Modulo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado del formulario para crear o editar un curso online
data class CrearCursoUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "Chocolatería",
    val nivel: String = "Básico",
    val esGratis: Boolean = true,
    val duracionTotal: String = "45 min",
    val insumoPrincipal: String = "Chocolate Ariba",
    val videoUrl: String = "",
    val subiendoVideo: Boolean = false,
    val errorVideo: String? = null,
    val tituloModulo: String = "Módulo 1: Introducción y técnica",
    val tituloLeccion: String = "Clase 1: Preparación paso a paso",
    val descripcionLeccion: String = "Demostración práctica con insumos Master Martini.",
    val duracionLeccion: String = "15 min",
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val guardadoExitoso: Boolean = false,
    val idEditando: Int? = null
) {
    val esEdicion: Boolean
        get() = idEditando != null
}

// ViewModel del formulario para crear o editar cursos online con subida de video a Cloudinary
class CrearCursoViewModel(application: Application) : AndroidViewModel(application) {

    private val almacenamiento = AlmacenamientoLocal(application)
    private val repository = CapacitacionRepository()
    private val cloudinaryRepository = CloudinaryRepository()

    private var cursoOriginal: Capacitacion? = null

    private val _uiState = MutableStateFlow(CrearCursoUiState())
    val uiState: StateFlow<CrearCursoUiState> = _uiState.asStateFlow()

    val categorias = listOf("Chocolatería", "Pastelería", "Panadería", "Heladería", "Decoración")
    val niveles = listOf("Básico", "Intermedio", "Avanzado")

    fun onTituloChange(v: String) = _uiState.update { it.copy(titulo = v, mensajeError = null) }
    fun onDescripcionChange(v: String) = _uiState.update { it.copy(descripcion = v, mensajeError = null) }
    fun onCategoriaChange(v: String) = _uiState.update { it.copy(categoria = v) }
    fun onNivelChange(v: String) = _uiState.update { it.copy(nivel = v) }
    fun onEsGratisChange(v: Boolean) = _uiState.update { it.copy(esGratis = v) }
    fun onDuracionChange(v: String) = _uiState.update { it.copy(duracionTotal = v) }
    fun onInsumoChange(v: String) = _uiState.update { it.copy(insumoPrincipal = v) }
    fun onTituloModuloChange(v: String) = _uiState.update { it.copy(tituloModulo = v) }
    fun onTituloLeccionChange(v: String) = _uiState.update { it.copy(tituloLeccion = v) }

    // Sube el video seleccionado a Cloudinary y guarda la URL segura resultante
    fun subirVideoCurso(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    subiendoVideo = true,
                    errorVideo = null,
                    mensajeError = null
                )
            }
            val resultado = cloudinaryRepository.subirVideoDesdeUri(context, uri)
            resultado.fold(
                onSuccess = { urlSegura ->
                    _uiState.update {
                        it.copy(
                            subiendoVideo = false,
                            videoUrl = urlSegura,
                            errorVideo = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            subiendoVideo = false,
                            errorVideo = error.message
                                ?: "No se pudo subir el video. Revisa tu conexión e inténtalo nuevamente."
                        )
                    }
                }
            )
        }
    }

    // Carga los datos del curso remoto para precargar el formulario en modo edición
    fun cargarParaEditar(id: Int) {
        if (_uiState.value.idEditando == id) return

        _uiState.update { it.copy(idEditando = id, cargando = true, mensajeError = null) }
        viewModelScope.launch {
            val curso = repository.obtenerCapacitacionPorIdRemoto(id)
            if (curso == null) {
                _uiState.update { it.copy(cargando = false, mensajeError = "No encontramos el curso para editar") }
                return@launch
            }
            cursoOriginal = curso
            _uiState.update {
                it.copy(
                    cargando = false,
                    titulo = curso.titulo,
                    descripcion = curso.descripcion,
                    categoria = curso.categoria,
                    nivel = curso.nivel,
                    esGratis = curso.esGratis,
                    duracionTotal = curso.duracionTotal,
                    insumoPrincipal = curso.insumoPrincipal,
                    videoUrl = curso.videoResumenUrl ?: "",
                    errorVideo = null
                )
            }
        }
    }

    // Valida los campos del formulario y crea o actualiza el curso en el microservicio
    fun publicarCurso() {
        val actual = _uiState.value
        if (actual.titulo.isBlank() || actual.descripcion.isBlank() || actual.insumoPrincipal.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Completa título, descripción e insumo principal") }
            return
        }

        if (actual.subiendoVideo) {
            _uiState.update { it.copy(mensajeError = "Espera a que termine de subirse el video antes de guardar") }
            return
        }

        val urlLimpia = actual.videoUrl.trim().ifBlank { null }
        if (urlLimpia != null && !urlLimpia.startsWith("http")) {
            _uiState.update { it.copy(mensajeError = "El enlace de Cloudinary debe empezar con http:// o https://") }
            return
        }

        val usuarioId = almacenamiento.obtenerIdUsuario()
        if (usuarioId == null) {
            _uiState.update { it.copy(mensajeError = "Debes iniciar sesión como instructor o administrador") }
            return
        }

        val idEditando = actual.idEditando
        if (idEditando != null) {
            guardarCambios(usuarioId.toInt(), idEditando, urlLimpia)
            return
        }

        val nombreInstructor = almacenamiento.obtenerNombre()

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

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }
            val resultado = repository.crearCapacitacion(usuarioId.toInt(), nuevoCurso)
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

    // Actualiza los datos generales preservando módulos y lecciones del curso original
    private fun guardarCambios(usuarioId: Int, idEditando: Int, urlVideo: String?) {
        val original = cursoOriginal
        if (original == null) {
            _uiState.update { it.copy(mensajeError = "No se pudo cargar el curso. Vuelve atrás e inténtalo otra vez.") }
            return
        }
        val actual = _uiState.value
        val cursoEditado = original.copy(
            titulo = actual.titulo.trim(),
            descripcion = actual.descripcion.trim(),
            categoria = actual.categoria,
            nivel = actual.nivel,
            esGratis = actual.esGratis,
            duracionTotal = actual.duracionTotal.trim().ifBlank { original.duracionTotal },
            insumoPrincipal = actual.insumoPrincipal.trim(),
            videoResumenUrl = urlVideo
        )

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }
            val resultado = repository.actualizarCapacitacion(usuarioId, idEditando, cursoEditado)
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

    private fun textoDelError(error: Throwable): String {
        if (error is ServidorException) {
            return when (error.codigo) {
                403 -> if (_uiState.value.esEdicion) {
                    "Solo el instructor del curso o un administrador pueden editarlo"
                } else {
                    "Solo instructores o administradores pueden crear cursos"
                }
                503 -> "El servicio de usuarios no está disponible"
                else -> error.mensajeServidor ?: error.message ?: "No se pudo crear el curso"
            }
        }
        return error.message ?: "No se pudo crear el curso"
    }
}