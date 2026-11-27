package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.DatosEmpresa
import com.example.mastermartini.data.InscripcionRepository
import com.example.mastermartini.data.MENSAJE_SIN_CONEXION
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Inscripcion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

const val FILTRO_TODOS = "Todos"
const val FILTRO_GRATIS = "Gratis"
const val FILTRO_PLAN_PRO = "Plan Pro"

// Tarjeta resumida de un curso en el que el usuario está inscrito
data class MiCurso(
    val cursoId: Int,
    val titulo: String,
    val porcentaje: Int
)

// Estado del catálogo de cursos online y sección "Mis cursos"
data class CapacitacionesUiState(
    val estado: EstadoUi<List<Capacitacion>> = EstadoUi.Cargando,
    val filtros: List<String> = listOf(FILTRO_TODOS, FILTRO_GRATIS, FILTRO_PLAN_PRO),
    val filtroSeleccionado: String = FILTRO_TODOS,
    val totalCursos: Int = 0,
    val precioPlanPro: String = "",
    val misCursos: List<MiCurso> = emptyList(),
    val errorMisCursos: String? = null
)

// ViewModel del catálogo de cursos del servidor con filtros, inscripciones y Reintentar
class CapacitacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val capacitacionRepository = CapacitacionRepository()
    private val inscripcionRepository = InscripcionRepository()
    private val almacenamiento = AlmacenamientoLocal(application)

    private var inscripciones: List<Inscripcion> = emptyList()
    private var errorMisCursos: String? = null
    private var todasLasCapacitaciones: List<Capacitacion> = emptyList()

    private val _uiState = MutableStateFlow(
        CapacitacionesUiState(precioPlanPro = formatearPesos(DatosEmpresa.PRECIO_PLAN_PRO))
    )
    val uiState: StateFlow<CapacitacionesUiState> = _uiState.asStateFlow()

    init {
        recargarCapacitaciones()
    }

    // Pide el catálogo y las inscripciones al servidor; también es la acción del botón Reintentar
    fun recargarCapacitaciones() {
        // El indicador solo aparece si aún no hay cursos en pantalla, así al volver no parpadea
        if (_uiState.value.estado !is EstadoUi.Exito) {
            _uiState.update { it.copy(estado = EstadoUi.Cargando) }
        }
        viewModelScope.launch {
            val resultado = capacitacionRepository.obtenerCapacitacionesRemotas()
            resultado.fold(
                onSuccess = { lista ->
                    todasLasCapacitaciones = lista
                    cargarInscripciones()
                    _uiState.value = armarEstado(_uiState.value.filtroSeleccionado)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(estado = EstadoUi.Error(textoDelError(error))) }
                }
            )
        }
    }

    // Trae las inscripciones; si fallan, "Mis cursos" muestra su propio aviso con Reintentar
    private suspend fun cargarInscripciones() {
        val usuarioId = almacenamiento.obtenerIdUsuario()
        if (usuarioId == null) {
            inscripciones = emptyList()
            errorMisCursos = null
            return
        }
        inscripcionRepository.misInscripciones(usuarioId).fold(
            onSuccess = { lista ->
                inscripciones = lista
                errorMisCursos = null
            },
            onFailure = { error ->
                inscripciones = emptyList()
                errorMisCursos = textoDelError(error)
            }
        )
    }

    private fun armarMisCursos(): List<MiCurso> {
        return inscripciones.map { inscripcion ->
            val curso = todasLasCapacitaciones.find { capacitacion -> capacitacion.id == inscripcion.cursoId }
            val totalLecciones = curso?.totalLecciones ?: 0
            MiCurso(
                cursoId = inscripcion.cursoId,
                titulo = inscripcion.cursoTitulo ?: curso?.titulo ?: "Curso Master Martini",
                porcentaje = inscripcion.porcentaje(totalLecciones)
            )
        }
    }

    // Solo se puede filtrar cuando ya llegaron los cursos del servidor
    fun seleccionarFiltro(filtro: String) {
        if (_uiState.value.estado !is EstadoUi.Exito) return
        _uiState.value = armarEstado(filtro)
    }

    // Categorías distintas de los cursos del servidor, en el orden en que llegan
    private fun categoriasDelServidor(): List<String> {
        return todasLasCapacitaciones
            .map { capacitacion -> capacitacion.categoria }
            .filter { categoria -> categoria.isNotBlank() }
            .distinct()
    }

    private fun armarEstado(filtroSeleccionado: String): CapacitacionesUiState {
        val filtros = listOf(FILTRO_TODOS, FILTRO_GRATIS, FILTRO_PLAN_PRO) + categoriasDelServidor()
        // Si la categoría elegida ya no está en el servidor, se vuelve a "Todos"
        val filtro = if (filtroSeleccionado in filtros) filtroSeleccionado else FILTRO_TODOS
        return CapacitacionesUiState(
            estado = EstadoUi.Exito(filtrar(filtro)),
            filtros = filtros,
            filtroSeleccionado = filtro,
            totalCursos = todasLasCapacitaciones.size,
            misCursos = armarMisCursos(),
            errorMisCursos = errorMisCursos,
            precioPlanPro = formatearPesos(DatosEmpresa.PRECIO_PLAN_PRO)
        )
    }

    private fun filtrar(filtro: String): List<Capacitacion> {
        return when (filtro) {
            FILTRO_TODOS -> todasLasCapacitaciones
            FILTRO_GRATIS -> todasLasCapacitaciones.filter { it.esGratis }
            FILTRO_PLAN_PRO -> todasLasCapacitaciones.filter { !it.esGratis }
            else -> todasLasCapacitaciones.filter { it.categoria == filtro }
        }
    }

    private fun textoDelError(error: Throwable): String {
        if (error is ServidorException) {
            return error.mensajeServidor ?: error.message ?: MENSAJE_SIN_CONEXION
        }
        return error.message ?: MENSAJE_SIN_CONEXION
    }

    // Formatea un monto entero como pesos chilenos con separador de miles por punto
    private fun formatearPesos(monto: Int): String {
        val conComas = String.format(Locale.US, "%,d", monto)
        return "$" + conComas.replace(",", ".")
    }
}