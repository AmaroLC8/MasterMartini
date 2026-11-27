package com.example.mastermartini.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.CloudinaryRepository
import com.example.mastermartini.data.MENSAJE_SIN_CONEXION
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.model.Capacitacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Aviso que se muestra al eliminar o reordenar en un curso que ya tiene alumnos
const val AVISO_AVANCE_ALUMNOS = "Los alumnos inscritos podrían perder el avance marcado en este curso."

// Mensaje cuando Cloudinary falla sin explicar por qué
const val MENSAJE_ERROR_SUBIDA = "No se pudo subir el video. Revisa tu conexión e inténtalo nuevamente."

// Estado del formulario para crear o editar un curso online con sus módulos y lecciones
data class CrearCursoUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "Chocolatería",
    val nivel: String = "Básico",
    val esGratis: Boolean = true,
    val insumoPrincipal: String = "Chocolate Ariba",
    val videoUrl: String = "",
    val subiendoVideo: Boolean = false,
    val errorVideo: String? = null,
    val modulos: List<ModuloEditable> = emptyList(),
    // Errores por campo (clave de ClaveError → mensaje), en el mismo orden de la pantalla
    val errores: Map<String, String> = emptyMap(),
    // Después del primer intento de guardar, los errores se recalculan con cada cambio
    val mostrarErrores: Boolean = false,
    // Sube en 1 cada vez que hay que llevar la pantalla al primer error
    val pedidoIrAlError: Int = 0,
    val accionPendiente: AccionPendiente? = null,
    // Al editar, el aviso de reordenar se muestra una sola vez
    val avisoOrdenAceptado: Boolean = false,
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val guardadoExitoso: Boolean = false,
    val idEditando: Int? = null,
    val cargandoCurso: Boolean = false,
    val cursoNoExiste: Boolean = false,
    val errorCarga: String? = null
) {
    val esEdicion: Boolean
        get() = idEditando != null

    // Suma los minutos de todas las lecciones con Modulo.totalMinutos
    val minutosTotales: Int
        get() = modulos.sumOf { it.aModulo().totalMinutos }

    val duracionTotalTexto: String
        get() = formatearMinutos(minutosTotales)

    val cantidadErrores: Int
        get() = errores.size

    val primerError: String?
        get() = errores.keys.firstOrNull()

    // Mientras se sube cualquier video (del curso o de una lección) no se puede guardar
    val hayVideoSubiendo: Boolean
        get() = subiendoVideo || modulos.any { modulo -> modulo.lecciones.any { it.subiendoVideo } }
}

// ViewModel del formulario para crear o editar cursos online con subida de videos a Cloudinary
class CrearCursoViewModel(application: Application) : AndroidViewModel(application) {

    private val almacenamiento = AlmacenamientoLocal(application)
    private val repository = CapacitacionRepository()
    private val cloudinaryRepository = CloudinaryRepository()

    private var cursoOriginal: Capacitacion? = null

    // Contador para dar un id local distinto a cada módulo y lección
    private var siguienteId = 1

    val categorias = listOf("Chocolatería", "Pastelería", "Panadería", "Heladería", "Decoración")
    val niveles = listOf("Básico", "Intermedio", "Avanzado")

    // Un curso nuevo parte con un módulo que tiene una lección vacía
    private val _uiState = MutableStateFlow(CrearCursoUiState(modulos = listOf(moduloNuevo())))
    val uiState: StateFlow<CrearCursoUiState> = _uiState.asStateFlow()

    fun onTituloChange(v: String) = actualizar { it.copy(titulo = v) }
    fun onDescripcionChange(v: String) = actualizar { it.copy(descripcion = v) }
    fun onCategoriaChange(v: String) = actualizar { it.copy(categoria = v) }
    fun onNivelChange(v: String) = actualizar { it.copy(nivel = v) }
    fun onEsGratisChange(v: Boolean) = actualizar { it.copy(esGratis = v) }
    fun onInsumoChange(v: String) = actualizar { it.copy(insumoPrincipal = v) }

    // Aplica un cambio, borra el mensaje general y recalcula los errores si ya se intentó guardar
    private fun actualizar(cambio: (CrearCursoUiState) -> CrearCursoUiState) {
        _uiState.update { estado -> revalidar(cambio(estado).copy(mensajeError = null)) }
    }

    private fun revalidar(estado: CrearCursoUiState): CrearCursoUiState {
        return if (estado.mostrarErrores) estado.copy(errores = validar(estado)) else estado
    }

    private fun nuevoId(): Int = siguienteId++

    private fun leccionNueva() = LeccionEditable(idLocal = nuevoId())

    private fun moduloNuevo() = ModuloEditable(idLocal = nuevoId(), lecciones = listOf(leccionNueva()))

    // ---------- Módulos ----------

    // Cambia solo el módulo con ese id
    private fun cambiarModulo(idModulo: Int, cambio: (ModuloEditable) -> ModuloEditable) {
        actualizar { estado ->
            estado.copy(modulos = estado.modulos.map { if (it.idLocal == idModulo) cambio(it) else it })
        }
    }

    // Agrega un módulo al final con una lección vacía; agregar al final no muestra aviso
    fun agregarModulo() = actualizar { it.copy(modulos = it.modulos + moduloNuevo()) }

    fun onTituloModuloChange(idModulo: Int, v: String) = cambiarModulo(idModulo) { it.copy(titulo = v) }

    // Abre o cierra la Card del módulo
    fun alternarModulo(idModulo: Int) = cambiarModulo(idModulo) { it.copy(expandido = !it.expandido) }

    fun pedirEliminarModulo(idModulo: Int) {
        _uiState.update { it.copy(accionPendiente = AccionPendiente.EliminarModulo(idModulo)) }
    }

    // Al editar un curso, la primera vez que se reordena se pide confirmar el aviso
    fun moverModulo(idModulo: Int, haciaArriba: Boolean) {
        if (necesitaAvisoOrden()) {
            _uiState.update { it.copy(accionPendiente = AccionPendiente.MoverModulo(idModulo, haciaArriba)) }
        } else {
            aplicarMoverModulo(idModulo, haciaArriba)
        }
    }

    private fun aplicarMoverModulo(idModulo: Int, haciaArriba: Boolean) {
        actualizar { estado ->
            val indice = estado.modulos.indexOfFirst { it.idLocal == idModulo }
            estado.copy(modulos = moverEnLista(estado.modulos, indice, haciaArriba))
        }
    }

    // ---------- Lecciones ----------

    // Cambia solo la lección con ese id dentro de su módulo
    private fun cambiarLeccion(idModulo: Int, idLeccion: Int, cambio: (LeccionEditable) -> LeccionEditable) {
        cambiarModulo(idModulo) { modulo ->
            modulo.copy(lecciones = modulo.lecciones.map { if (it.idLocal == idLeccion) cambio(it) else it })
        }
    }

    // Agrega una lección vacía al final del módulo y lo deja abierto
    fun agregarLeccion(idModulo: Int) = cambiarModulo(idModulo) {
        it.copy(lecciones = it.lecciones + leccionNueva(), expandido = true)
    }

    fun onTituloLeccionChange(idModulo: Int, idLeccion: Int, v: String) =
        cambiarLeccion(idModulo, idLeccion) { it.copy(titulo = v) }

    fun onDescripcionLeccionChange(idModulo: Int, idLeccion: Int, v: String) =
        cambiarLeccion(idModulo, idLeccion) { it.copy(descripcion = v) }

    fun onDuracionLeccionChange(idModulo: Int, idLeccion: Int, v: String) =
        cambiarLeccion(idModulo, idLeccion) { it.copy(duracion = v) }

    // Link pegado a mano; borra el error de una subida anterior
    fun onVideoLeccionChange(idModulo: Int, idLeccion: Int, v: String) =
        cambiarLeccion(idModulo, idLeccion) { it.copy(videoUrl = v, errorSubida = null) }

    fun pedirEliminarLeccion(idModulo: Int, idLeccion: Int) {
        _uiState.update { it.copy(accionPendiente = AccionPendiente.EliminarLeccion(idModulo, idLeccion)) }
    }

    fun moverLeccion(idModulo: Int, idLeccion: Int, haciaArriba: Boolean) {
        if (necesitaAvisoOrden()) {
            _uiState.update {
                it.copy(accionPendiente = AccionPendiente.MoverLeccion(idModulo, idLeccion, haciaArriba))
            }
        } else {
            aplicarMoverLeccion(idModulo, idLeccion, haciaArriba)
        }
    }

    private fun aplicarMoverLeccion(idModulo: Int, idLeccion: Int, haciaArriba: Boolean) {
        cambiarModulo(idModulo) { modulo ->
            val indice = modulo.lecciones.indexOfFirst { it.idLocal == idLeccion }
            modulo.copy(lecciones = moverEnLista(modulo.lecciones, indice, haciaArriba))
        }
    }

    // Sube el video de una lección a Cloudinary; el indicador de progreso es solo de esa lección
    fun subirVideoLeccion(context: Context, uri: Uri, idModulo: Int, idLeccion: Int) {
        cambiarLeccion(idModulo, idLeccion) { it.copy(subiendoVideo = true, errorSubida = null) }
        viewModelScope.launch {
            val resultado = cloudinaryRepository.subirVideoDesdeUri(context, uri)
            resultado.fold(
                onSuccess = { urlSegura ->
                    cambiarLeccion(idModulo, idLeccion) { it.copy(subiendoVideo = false, videoUrl = urlSegura) }
                },
                onFailure = { error ->
                    cambiarLeccion(idModulo, idLeccion) {
                        it.copy(subiendoVideo = false, errorSubida = error.message ?: MENSAJE_ERROR_SUBIDA)
                    }
                }
            )
        }
    }

    // ---------- Confirmaciones (AlertDialog) ----------

    private fun necesitaAvisoOrden(): Boolean {
        val estado = _uiState.value
        return estado.esEdicion && !estado.avisoOrdenAceptado
    }

    // El instructor tocó "Sí" en el AlertDialog: se hace la acción que estaba esperando
    fun confirmarAccion() {
        val accion = _uiState.value.accionPendiente ?: return
        _uiState.update { it.copy(accionPendiente = null) }
        when (accion) {
            is AccionPendiente.EliminarModulo -> actualizar { estado ->
                estado.copy(modulos = estado.modulos.filter { it.idLocal != accion.idModulo })
            }
            is AccionPendiente.EliminarLeccion -> cambiarModulo(accion.idModulo) { modulo ->
                modulo.copy(lecciones = modulo.lecciones.filter { it.idLocal != accion.idLeccion })
            }
            is AccionPendiente.MoverModulo -> {
                _uiState.update { it.copy(avisoOrdenAceptado = true) }
                aplicarMoverModulo(accion.idModulo, accion.haciaArriba)
            }
            is AccionPendiente.MoverLeccion -> {
                _uiState.update { it.copy(avisoOrdenAceptado = true) }
                aplicarMoverLeccion(accion.idModulo, accion.idLeccion, accion.haciaArriba)
            }
        }
    }

    fun cancelarAccion() {
        _uiState.update { it.copy(accionPendiente = null) }
    }

    // ---------- Video de presentación del curso ----------

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
                            errorVideo = error.message ?: MENSAJE_ERROR_SUBIDA
                        )
                    }
                }
            )
        }
    }

    // ---------- Cargar un curso para editarlo ----------

    // Carga el curso del servidor para llenar el formulario en modo edición
    fun cargarParaEditar(id: Int) {
        if (_uiState.value.idEditando == id) return
        _uiState.update { it.copy(idEditando = id) }
        pedirCursoParaEditar(id)
    }

    // Acción del botón Reintentar cuando no se pudo cargar el curso a editar
    fun reintentarCarga() {
        val id = _uiState.value.idEditando ?: return
        pedirCursoParaEditar(id)
    }

    private fun pedirCursoParaEditar(id: Int) {
        _uiState.update {
            it.copy(cargandoCurso = true, cursoNoExiste = false, errorCarga = null, mensajeError = null)
        }
        viewModelScope.launch {
            val resultado = repository.obtenerCapacitacionPorIdRemoto(id)
            resultado.fold(
                onSuccess = { curso ->
                    cursoOriginal = curso
                    _uiState.update {
                        it.copy(
                            cargandoCurso = false,
                            titulo = curso.titulo,
                            descripcion = curso.descripcion,
                            categoria = curso.categoria,
                            nivel = curso.nivel,
                            esGratis = curso.esGratis,
                            insumoPrincipal = curso.insumoPrincipal,
                            videoUrl = curso.videoResumenUrl ?: "",
                            errorVideo = null,
                            modulos = modulosEditables(curso),
                            errores = emptyMap(),
                            mostrarErrores = false
                        )
                    }
                },
                onFailure = { error ->
                    // 404: el curso ya no existe; cualquier otro error se puede reintentar
                    val noExiste = error is ServidorException && error.codigo == 404
                    _uiState.update {
                        it.copy(
                            cargandoCurso = false,
                            cursoNoExiste = noExiste,
                            errorCarga = if (noExiste) null else textoErrorCarga(error)
                        )
                    }
                }
            )
        }
    }

    // Pasa los módulos reales del curso a módulos editables (cerrados, para ver la lista completa)
    private fun modulosEditables(curso: Capacitacion): List<ModuloEditable> {
        return curso.modulos.map { modulo ->
            ModuloEditable(
                idLocal = nuevoId(),
                titulo = modulo.titulo,
                expandido = false,
                lecciones = modulo.lecciones.map { leccion ->
                    LeccionEditable(
                        idLocal = nuevoId(),
                        titulo = leccion.titulo,
                        descripcion = leccion.descripcion,
                        duracion = leccion.duracion,
                        videoUrl = leccion.videoUrl ?: ""
                    )
                }
            )
        }
    }

    private fun textoErrorCarga(error: Throwable): String {
        if (error is ServidorException) {
            return error.mensajeServidor ?: error.message ?: MENSAJE_SIN_CONEXION
        }
        return error.message ?: MENSAJE_SIN_CONEXION
    }

    // ---------- Validar y guardar ----------

    // Revisa cada campo con las mismas reglas del backend de cursos y devuelve los errores en orden
    private fun validar(estado: CrearCursoUiState): Map<String, String> {
        val errores = LinkedHashMap<String, String>()
        if (estado.titulo.isBlank()) errores[ClaveError.TITULO] = "Escribe el título del curso"
        if (estado.descripcion.isBlank()) errores[ClaveError.DESCRIPCION] = "Escribe de qué trata el curso"
        if (estado.insumoPrincipal.isBlank()) errores[ClaveError.INSUMO] = "Escribe el insumo Master Martini que se usa"
        if (estado.categoria !in categorias) {
            errores[ClaveError.CATEGORIA] = "Elige una categoría: Chocolatería, Pastelería, Panadería, Heladería o Decoración"
        }
        if (estado.nivel !in niveles) errores[ClaveError.NIVEL] = "Elige un nivel: Básico, Intermedio o Avanzado"
        if (estado.modulos.isEmpty()) errores[ClaveError.MODULOS] = "Agrega al menos un módulo"

        for (modulo in estado.modulos) {
            if (modulo.titulo.isBlank()) {
                errores[ClaveError.tituloModulo(modulo.idLocal)] = "Escribe el título del módulo"
            }
            if (modulo.lecciones.isEmpty()) {
                errores[ClaveError.leccionesModulo(modulo.idLocal)] = "Agrega al menos una lección a este módulo"
            }
            for (leccion in modulo.lecciones) {
                if (leccion.titulo.isBlank()) {
                    errores[ClaveError.tituloLeccion(leccion.idLocal)] = "Escribe el título de la lección"
                }
                if (leccion.descripcion.isBlank()) {
                    errores[ClaveError.descripcionLeccion(leccion.idLocal)] = "Escribe de qué trata la lección"
                }
                if (!duracionEsValida(leccion.duracion)) {
                    errores[ClaveError.duracionLeccion(leccion.idLocal)] = "Escribe los minutos, por ejemplo: 15 min"
                }
                val url = leccion.videoUrl.trim()
                if (url.isNotEmpty() && !url.startsWith("http://") && !url.startsWith("https://")) {
                    errores[ClaveError.videoLeccion(leccion.idLocal)] =
                        "El enlace del video debe empezar con http:// o https://"
                }
            }
        }
        return errores
    }

    // Valida el formulario completo y crea o actualiza el curso en el microservicio
    fun publicarCurso() {
        val actual = _uiState.value
        if (actual.hayVideoSubiendo) {
            _uiState.update { it.copy(mensajeError = "Espera a que termine de subirse el video antes de guardar") }
            return
        }

        val errores = validar(actual)
        if (errores.isNotEmpty()) {
            // Abre los módulos con errores y pide a la pantalla que vaya al primero
            _uiState.update { estado ->
                estado.copy(
                    mostrarErrores = true,
                    errores = errores,
                    mensajeError = null,
                    pedidoIrAlError = estado.pedidoIrAlError + 1,
                    modulos = estado.modulos.map { modulo ->
                        val tieneErrores = modulo.clavesDeError().any { it in errores }
                        if (tieneErrores) modulo.copy(expandido = true) else modulo
                    }
                )
            }
            return
        }
        _uiState.update { it.copy(mostrarErrores = true, errores = emptyMap()) }

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
            duracionTotal = actual.duracionTotalTexto,
            insumoPrincipal = actual.insumoPrincipal.trim(),
            videoResumenUrl = urlLimpia,
            loQueLograras = listOf("Dominar el uso de ${actual.insumoPrincipal.trim()}"),
            incluye = listOf("Certificado Master Martini", "Acceso ilimitado"),
            modulos = actual.modulos.map { it.aModulo() }
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

    // Actualiza el curso con los datos generales y los módulos editados (PUT)
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
            duracionTotal = actual.duracionTotalTexto,
            insumoPrincipal = actual.insumoPrincipal.trim(),
            videoResumenUrl = urlVideo,
            modulos = actual.modulos.map { it.aModulo() }
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
                // 400: datos rechazados por el backend; se muestra su mensaje
                400 -> error.mensajeServidor ?: "El servidor rechazó los datos del curso. Revísalos e inténtalo otra vez."
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
