package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.DatosEmpresa
import com.example.mastermartini.data.InscripcionRepository
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Inscripcion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val filtros: List<String> = emptyList(),
    val filtroSeleccionado: String = FILTRO_TODOS,
    val capacitaciones: List<Capacitacion> = emptyList(),
    val precioPlanPro: String = "",
    val misCursos: List<MiCurso> = emptyList()
)

// ViewModel que gestiona el catálogo remoto de capacitaciones, filtros e inscripciones
class CapacitacionesViewModel(application: Application) : AndroidViewModel(application) {

    private val capacitacionRepository = CapacitacionRepository()
    private val inscripcionRepository = InscripcionRepository()
    private val almacenamiento = AlmacenamientoLocal(application)

    private var inscripciones: List<Inscripcion> = emptyList()
    private var todasLasCapacitaciones: List<Capacitacion> = capacitacionRepository.obtenerCapacitaciones()
    private val categorias: List<String> = capacitacionRepository.obtenerCategorias()

    private val filtros: List<String> = listOf(FILTRO_TODOS, FILTRO_GRATIS, FILTRO_PLAN_PRO) + categorias

    private val _uiState = MutableStateFlow(armarEstado(FILTRO_TODOS))
    val uiState: StateFlow<CapacitacionesUiState> = _uiState.asStateFlow()

    init {
        recargarCapacitaciones()
    }

    // Actualiza el catálogo remoto e inscripciones conservando el filtro seleccionado
    fun recargarCapacitaciones() {
        viewModelScope.launch {
            todasLasCapacitaciones = capacitacionRepository.obtenerCapacitacionesRemotas()
            cargarInscripciones()
            _uiState.value = armarEstado(_uiState.value.filtroSeleccionado)
        }
    }

    private suspend fun cargarInscripciones() {
        val usuarioId = almacenamiento.obtenerIdUsuario()
        if (usuarioId == null) {
            inscripciones = emptyList()
            return
        }
        inscripcionRepository.misInscripciones(usuarioId).onSuccess { lista ->
            inscripciones = lista
        }
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

    fun seleccionarFiltro(filtro: String) {
        _uiState.value = armarEstado(filtro)
    }

    private fun armarEstado(filtroSeleccionado: String): CapacitacionesUiState {
        return CapacitacionesUiState(
            filtros = filtros,
            filtroSeleccionado = filtroSeleccionado,
            capacitaciones = filtrar(filtroSeleccionado),
            misCursos = armarMisCursos(),
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

    // Formatea un monto entero como pesos chilenos con separador de miles por punto
    private fun formatearPesos(monto: Int): String {
        val conComas = String.format(Locale.US, "%,d", monto)
        return "$" + conComas.replace(",", ".")
    }
}