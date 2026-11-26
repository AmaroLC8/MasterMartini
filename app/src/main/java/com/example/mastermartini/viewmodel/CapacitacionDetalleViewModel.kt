package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.InscripcionRepository
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Inscripcion
import com.example.mastermartini.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la pantalla de detalle de un curso online
data class CapacitacionDetalleUiState(
    val capacitacion: Capacitacion? = null,
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
    val mensajeErrorInscripcion: String? = null
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

    private val _uiState = MutableStateFlow(CapacitacionDetalleUiState())
    val uiState: StateFlow<CapacitacionDetalleUiState> = _uiState.asStateFlow()

    // Carga el curso remoto, su insumo asociado, permisos del rol e inscripción vigente
    fun cargarCapacitacion(id: Int) {
        if (_uiState.value.capacitacion?.id == id) return

        viewModelScope.launch {
            val capacitacionEncontrada = capacitacionRepository.obtenerCapacitacionPorIdRemoto(id)

            val productoEncontrado = if (capacitacionEncontrada != null) {
                productoRepository.obtenerProductoPorNombre(capacitacionEncontrada.insumoPrincipal)
            } else {
                null
            }

            _uiState.value = CapacitacionDetalleUiState(
                capacitacion = capacitacionEncontrada,
                producto = productoEncontrado,
                modulosAbiertos = setOf(0),
                puedeEditar = calcularPuedeEditar(capacitacionEncontrada),
                puedeEliminar = calcularPuedeEliminar(capacitacionEncontrada),
                inscripcion = buscarMiInscripcion(id)
            )
        }
    }

    // Recarga los datos del curso conservando los módulos desplegados por el usuario
    fun recargarCapacitacion(id: Int) {
        viewModelScope.launch {
            val capacitacionActualizada = capacitacionRepository.obtenerCapacitacionPorIdRemoto(id)
            val productoEncontrado = if (capacitacionActualizada != null) {
                productoRepository.obtenerProductoPorNombre(capacitacionActualizada.insumoPrincipal)
            } else {
                null
            }
            _uiState.update {
                it.copy(
                    capacitacion = capacitacionActualizada,
                    producto = productoEncontrado,
                    puedeEditar = calcularPuedeEditar(capacitacionActualizada),
                    puedeEliminar = calcularPuedeEliminar(capacitacionActualizada),
                    inscripcion = buscarMiInscripcion(id)
                )
            }
        }
    }

    private fun calcularPuedeEditar(capacitacion: Capacitacion?): Boolean {
        if (capacitacion == null) return false
        val rol = almacenamiento.obtenerRol()
        return rol.puedeEditarCurso(capacitacion.instructorId, almacenamiento.obtenerIdUsuario())
    }

    private fun calcularPuedeEliminar(capacitacion: Capacitacion?): Boolean {
        if (capacitacion == null) return false
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