package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.InscripcionRepository
import com.example.mastermartini.data.MENSAJE_SIN_CONEXION
import com.example.mastermartini.data.PreguntaRepository
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Inscripcion
import com.example.mastermartini.model.Pregunta
import com.example.mastermartini.model.Producto
import com.example.mastermartini.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la pantalla de detalle de un curso online
data class CapacitacionDetalleUiState(
    val cargando: Boolean = true,
    val capacitacion: Capacitacion? = null,
    val cursoNoExiste: Boolean = false,
    val errorCarga: String? = null,
    val producto: Producto? = null,
    val modulosAbiertos: Set<Int> = emptySet(),
    val puedeEditar: Boolean = false,
    val puedeEliminar: Boolean = false,
    val mostrarConfirmacionEliminar: Boolean = false,
    val eliminando: Boolean = false,
    val mensajeError: String? = null,
    val inscripcion: Inscripcion? = null,
    val procesando: Boolean = false,
    val mostrarConfirmacionCancelar: Boolean = false,
    val mensajeErrorInscripcion: String? = null,
    val preguntas: List<Pregunta> = emptyList(),
    val cargandoPreguntas: Boolean = false,
    val errorPreguntas: String? = null,
    val textoPregunta: String = "",
    val errorPregunta: String? = null,
    val enviandoPregunta: Boolean = false,
    val preguntaAEliminar: Pregunta? = null,
    val mensajeSnackbar: String? = null
) {
    val estaInscrito: Boolean
        get() = inscripcion != null

    val totalLecciones: Int
        get() = capacitacion?.totalLecciones ?: 0

    val leccionesCompletadas: Int
        get() = (inscripcion?.leccionesCompletadas?.size ?: 0).coerceAtMost(totalLecciones)

    val porcentaje: Int
        get() = inscripcion?.porcentaje(totalLecciones) ?: 0
}

// ViewModel que gestiona el detalle del curso online, permisos e inscripciones del usuario
class CapacitacionDetalleViewModel(application: Application) : AndroidViewModel(application) {

    private val capacitacionRepository = CapacitacionRepository()
    private val inscripcionRepository = InscripcionRepository()
    private val almacenamiento = AlmacenamientoLocal(application)
    private val productoRepository = ProductoRepository()
    private val preguntaRepository = PreguntaRepository()

    private val _uiState = MutableStateFlow(CapacitacionDetalleUiState())
    val uiState: StateFlow<CapacitacionDetalleUiState> = _uiState.asStateFlow()

    // Carga el curso por primera vez; si ya está cargado no lo vuelve a pedir
    fun cargarCapacitacion(id: Int) {
        if (_uiState.value.capacitacion?.id == id) return
        pedirCapacitacion(id)
    }

    // Acción del botón Reintentar cuando falló la conexión
    fun reintentarCarga(id: Int) {
        pedirCapacitacion(id)
    }

    // Pide el curso al servidor y arma el estado con su insumo, permisos e inscripción
    private fun pedirCapacitacion(id: Int) {
        _uiState.update { it.copy(cargando = true, cursoNoExiste = false, errorCarga = null) }

        viewModelScope.launch {
            val resultado = capacitacionRepository.obtenerCapacitacionPorIdRemoto(id)
            resultado.fold(
                onSuccess = { capacitacion ->
                    _uiState.value = CapacitacionDetalleUiState(
                        cargando = false,
                        capacitacion = capacitacion,
                        producto = productoRepository.obtenerProductoPorNombre(capacitacion.insumoPrincipal),
                        modulosAbiertos = setOf(0),
                        puedeEditar = calcularPuedeEditar(capacitacion),
                        puedeEliminar = calcularPuedeEliminar(),
                        inscripcion = buscarMiInscripcion(id)
                    )
                    cargarPreguntas(id)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            capacitacion = null,
                            cursoNoExiste = esCursoNoEncontrado(error),
                            errorCarga = if (esCursoNoEncontrado(error)) null else textoErrorCarga(error)
                        )
                    }
                }
            )
        }
    }

    // Recarga el curso al volver de editarlo, conservando los módulos abiertos
    fun recargarCapacitacion(id: Int) {
        viewModelScope.launch {
            val resultado = capacitacionRepository.obtenerCapacitacionPorIdRemoto(id)
            resultado.fold(
                onSuccess = { capacitacion ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            capacitacion = capacitacion,
                            cursoNoExiste = false,
                            errorCarga = null,
                            producto = productoRepository.obtenerProductoPorNombre(capacitacion.insumoPrincipal),
                            puedeEditar = calcularPuedeEditar(capacitacion),
                            puedeEliminar = calcularPuedeEliminar(),
                            inscripcion = buscarMiInscripcion(id)
                        )
                    }
                    cargarPreguntas(id)
                },
                onFailure = { error ->
                    if (esCursoNoEncontrado(error)) {
                        // Lo borraron mientras estaba abierto
                        _uiState.update { it.copy(cargando = false, capacitacion = null, cursoNoExiste = true) }
                    } else if (_uiState.value.capacitacion != null) {
                        // Se mantiene lo que ya se ve y se avisa con un Snackbar
                        _uiState.update { it.copy(mensajeSnackbar = textoErrorCarga(error)) }
                    } else {
                        _uiState.update { it.copy(cargando = false, errorCarga = textoErrorCarga(error)) }
                    }
                }
            )
        }
    }

    // El servidor responde 404 cuando el curso no existe o fue eliminado
    private fun esCursoNoEncontrado(error: Throwable): Boolean {
        return error is ServidorException && error.codigo == 404
    }

    private fun textoErrorCarga(error: Throwable): String {
        if (error is ServidorException) {
            return error.mensajeServidor ?: error.message ?: MENSAJE_SIN_CONEXION
        }
        return error.message ?: MENSAJE_SIN_CONEXION
    }

    private fun calcularPuedeEditar(capacitacion: Capacitacion): Boolean {
        val rol = almacenamiento.obtenerRol()
        return rol.puedeEditarCurso(capacitacion.instructorId, almacenamiento.obtenerIdUsuario())
    }

    // Eliminar no depende del curso: solo el administrador puede hacerlo
    private fun calcularPuedeEliminar(): Boolean {
        return almacenamiento.obtenerRol().puedeEliminarCursos
    }

    fun abrirConfirmacionEliminar() {
        _uiState.update { it.copy(mostrarConfirmacionEliminar = true, mensajeError = null) }
    }

    fun cerrarConfirmacionEliminar() {
        _uiState.update { it.copy(mostrarConfirmacionEliminar = false) }
    }

    // Elimina el curso en el microservicio validando sesión activa
    fun eliminarCurso(onEliminado: () -> Unit) {
        val capacitacion = _uiState.value.capacitacion ?: return
        val usuarioId = almacenamiento.obtenerIdUsuario()
        if (usuarioId == null) {
            _uiState.update {
                it.copy(
                    mostrarConfirmacionEliminar = false,
                    mensajeError = "Debes iniciar sesión como administrador"
                )
            }
            return
        }

        _uiState.update {
            it.copy(mostrarConfirmacionEliminar = false, eliminando = true, mensajeError = null)
        }
        viewModelScope.launch {
            val resultado = capacitacionRepository.eliminarCapacitacion(usuarioId.toInt(), capacitacion.id)
            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(eliminando = false) }
                    onEliminado()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(eliminando = false, mensajeError = textoDelError(error)) }
                }
            )
        }
    }

    private fun textoDelError(error: Throwable): String {
        if (error is ServidorException) {
            return when (error.codigo) {
                403 -> "Solo el administrador puede eliminar cursos"
                503 -> "El servicio de usuarios no está disponible"
                else -> error.mensajeServidor ?: error.message ?: "No se pudo eliminar el curso"
            }
        }
        return error.message ?: "No se pudo eliminar el curso"
    }

    private suspend fun buscarMiInscripcion(cursoId: Int): Inscripcion? {
        val usuarioId = almacenamiento.obtenerIdUsuario() ?: return null
        val inscripciones = inscripcionRepository.misInscripciones(usuarioId).getOrNull() ?: return null
        return inscripciones.find { inscripcion -> inscripcion.cursoId == cursoId }
    }

    // Registra la inscripción en el curso validando Plan Pro desde el servidor
    fun inscribirse() {
        val capacitacion = _uiState.value.capacitacion ?: return
        if (_uiState.value.procesando) return
        val usuarioId = almacenamiento.obtenerIdUsuario()
        if (usuarioId == null) {
            _uiState.update { it.copy(mensajeErrorInscripcion = "Debes iniciar sesión para inscribirte") }
            return
        }

        _uiState.update { it.copy(procesando = true, mensajeErrorInscripcion = null) }
        viewModelScope.launch {
            val resultado = inscripcionRepository.inscribirse(usuarioId, capacitacion.id)
            resultado.fold(
                onSuccess = { inscripcionNueva ->
                    _uiState.update { it.copy(procesando = false, inscripcion = inscripcionNueva) }
                },
                onFailure = { error ->
                    if (error is ServidorException && error.codigo == 409) {
                        val existente = buscarMiInscripcion(capacitacion.id)
                        _uiState.update { it.copy(procesando = false, inscripcion = existente) }
                    } else {
                        val texto = textoErrorInscripcion(
                            error,
                            mensaje403 = "Necesitas el Plan Pro para este curso. Pídele a nuestro equipo de ventas que lo active"
                        )
                        _uiState.update { it.copy(procesando = false, mensajeErrorInscripcion = texto) }
                    }
                }
            )
        }
    }

    // Actualiza en el servidor el estado visto/pendiente de una lección
    fun alternarLeccion(modulo: Int, leccion: Int) {
        val inscripcion = _uiState.value.inscripcion ?: return
        val usuarioId = almacenamiento.obtenerIdUsuario() ?: return
        val completada = !inscripcion.estaCompletada(modulo, leccion)

        viewModelScope.launch {
            val resultado = inscripcionRepository.marcarLeccion(
                usuarioId = usuarioId,
                inscripcionId = inscripcion.id,
                modulo = modulo,
                leccion = leccion,
                completada = completada
            )
            resultado.fold(
                onSuccess = { inscripcionActualizada ->
                    _uiState.update {
                        it.copy(inscripcion = inscripcionActualizada, mensajeErrorInscripcion = null)
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(mensajeErrorInscripcion = textoErrorInscripcion(error)) }
                }
            )
        }
    }

    fun abrirConfirmacionCancelar() {
        _uiState.update { it.copy(mostrarConfirmacionCancelar = true, mensajeErrorInscripcion = null) }
    }

    fun cerrarConfirmacionCancelar() {
        _uiState.update { it.copy(mostrarConfirmacionCancelar = false) }
    }

    // Cancela la inscripción vigente del usuario en el servidor
    fun cancelarInscripcion() {
        val inscripcion = _uiState.value.inscripcion ?: return
        val usuarioId = almacenamiento.obtenerIdUsuario() ?: return

        _uiState.update {
            it.copy(mostrarConfirmacionCancelar = false, procesando = true, mensajeErrorInscripcion = null)
        }
        viewModelScope.launch {
            val resultado = inscripcionRepository.cancelar(usuarioId, inscripcion.id)
            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(procesando = false, inscripcion = null) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(procesando = false, mensajeErrorInscripcion = textoErrorInscripcion(error))
                    }
                }
            )
        }
    }

    private fun textoErrorInscripcion(error: Throwable, mensaje403: String? = null): String {
        if (error is ServidorException) {
            if (error.codigo == 403 && mensaje403 != null) {
                return mensaje403
            }
            return error.mensajeServidor ?: error.message ?: "No se pudo completar la acción"
        }
        return error.message ?: "No se pudo completar la acción"
    }

    // ---------- Preguntas al instructor ----------

    // Pide al servidor las preguntas del curso; también sirve para reintentar
    fun cargarPreguntas(cursoId: Int) {
        _uiState.update { it.copy(cargandoPreguntas = true, errorPreguntas = null) }
        viewModelScope.launch {
            val resultado = preguntaRepository.listarDelCurso(cursoId)
            resultado.fold(
                onSuccess = { lista ->
                    _uiState.update { it.copy(cargandoPreguntas = false, preguntas = lista) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(cargandoPreguntas = false, errorPreguntas = textoErrorInscripcion(error))
                    }
                }
            )
        }
    }

    // Guarda lo que escribe el usuario sin pasar del límite de caracteres
    fun onTextoPreguntaChange(texto: String) {
        _uiState.update {
            it.copy(textoPregunta = texto.take(Pregunta.MAX_CARACTERES), errorPregunta = null)
        }
    }

    // Valida el texto y envía la pregunta; si sale bien la agrega al inicio de la lista
    fun enviarPregunta() {
        val estado = _uiState.value
        val capacitacion = estado.capacitacion ?: return
        if (estado.enviandoPregunta) return

        val texto = estado.textoPregunta.trim()
        if (texto.isEmpty()) {
            _uiState.update { it.copy(errorPregunta = "Escribe tu pregunta antes de enviarla") }
            return
        }
        if (!estado.estaInscrito) {
            _uiState.update { it.copy(errorPregunta = "Inscríbete para hacer preguntas") }
            return
        }
        val usuarioId = almacenamiento.obtenerIdUsuario()
        if (usuarioId == null) {
            _uiState.update { it.copy(errorPregunta = "Debes iniciar sesión para hacer preguntas") }
            return
        }

        _uiState.update { it.copy(enviandoPregunta = true, errorPregunta = null) }
        viewModelScope.launch {
            val resultado = preguntaRepository.preguntar(usuarioId, capacitacion.id, texto)
            resultado.fold(
                onSuccess = { preguntaNueva ->
                    _uiState.update {
                        it.copy(
                            enviandoPregunta = false,
                            preguntas = listOf(preguntaNueva) + it.preguntas,
                            textoPregunta = "",
                            mensajeSnackbar = "Tu pregunta fue enviada al instructor"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(enviandoPregunta = false, mensajeSnackbar = textoErrorInscripcion(error))
                    }
                }
            )
        }
    }

    // Solo el autor de la pregunta o el administrador ven el botón de eliminar
    fun puedeEliminarPregunta(pregunta: Pregunta): Boolean {
        if (almacenamiento.obtenerRol() == Rol.Administrador) return true
        val usuarioId = almacenamiento.obtenerIdUsuario() ?: return false
        return pregunta.usuarioId == usuarioId
    }

    fun abrirConfirmacionEliminarPregunta(pregunta: Pregunta) {
        _uiState.update { it.copy(preguntaAEliminar = pregunta) }
    }

    fun cerrarConfirmacionEliminarPregunta() {
        _uiState.update { it.copy(preguntaAEliminar = null) }
    }

    // Borra en el servidor la pregunta confirmada en el diálogo
    fun eliminarPregunta() {
        val pregunta = _uiState.value.preguntaAEliminar ?: return
        _uiState.update { it.copy(preguntaAEliminar = null) }
        val usuarioId = almacenamiento.obtenerIdUsuario()
        if (usuarioId == null) {
            _uiState.update { it.copy(mensajeSnackbar = "Debes iniciar sesión para eliminar preguntas") }
            return
        }

        viewModelScope.launch {
            val resultado = preguntaRepository.eliminar(usuarioId, pregunta.id)
            resultado.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            preguntas = it.preguntas.filter { actual -> actual.id != pregunta.id },
                            mensajeSnackbar = "Pregunta eliminada"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(mensajeSnackbar = textoErrorInscripcion(error)) }
                }
            )
        }
    }

    // La pantalla avisa que ya mostró el Snackbar para no repetirlo
    fun snackbarMostrado() {
        _uiState.update { it.copy(mensajeSnackbar = null) }
    }

    fun estaVista(indiceModulo: Int, indiceLeccion: Int): Boolean {
        return _uiState.value.inscripcion?.estaCompletada(indiceModulo, indiceLeccion) == true
    }

    fun alternarModulo(indice: Int) {
        val abiertos = _uiState.value.modulosAbiertos
        val nuevosAbiertos = if (abiertos.contains(indice)) {
            abiertos - indice
        } else {
            abiertos + indice
        }
        _uiState.value = _uiState.value.copy(modulosAbiertos = nuevosAbiertos)
    }

    private fun esPrimeraLeccion(indiceModulo: Int, indiceLeccion: Int): Boolean {
        return indiceModulo == 0 && indiceLeccion == 0
    }

    fun esMuestraGratis(indiceModulo: Int, indiceLeccion: Int): Boolean {
        val capacitacion = _uiState.value.capacitacion ?: return false
        if (_uiState.value.estaInscrito) return false
        return !capacitacion.esGratis && esPrimeraLeccion(indiceModulo, indiceLeccion)
    }

    fun estaBloqueada(indiceModulo: Int, indiceLeccion: Int): Boolean {
        val capacitacion = _uiState.value.capacitacion ?: return false
        if (_uiState.value.estaInscrito) return false
        return !capacitacion.esGratis && !esPrimeraLeccion(indiceModulo, indiceLeccion)
    }
}