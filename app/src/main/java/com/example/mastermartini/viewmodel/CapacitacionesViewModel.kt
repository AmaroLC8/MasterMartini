package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.DatosEmpresa
import com.example.mastermartini.model.Capacitacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

const val FILTRO_TODOS = "Todos"
const val FILTRO_GRATIS = "Gratis"
const val FILTRO_PLAN_PRO = "Plan Pro"

data class CapacitacionesUiState(
    val filtros: List<String> = emptyList(),
    val filtroSeleccionado: String = FILTRO_TODOS,
    val capacitaciones: List<Capacitacion> = emptyList(),
    val precioPlanPro: String = ""
)

class CapacitacionesViewModel : ViewModel() {

    private val capacitacionRepository = CapacitacionRepository()

    private var todasLasCapacitaciones: List<Capacitacion> = capacitacionRepository.obtenerCapacitaciones()
    private val categorias: List<String> = capacitacionRepository.obtenerCategorias()
    private val filtros: List<String> = listOf(FILTRO_TODOS, FILTRO_GRATIS, FILTRO_PLAN_PRO) + categorias

    private val _uiState = MutableStateFlow(armarEstado(FILTRO_TODOS))
    val uiState: StateFlow<CapacitacionesUiState> = _uiState.asStateFlow()

    init {
        recargarCapacitaciones()
    }

    // Trae los cursos desde el microservicio (incluyendo los creados por Admin)
    fun recargarCapacitaciones() {
        viewModelScope.launch {
            todasLasCapacitaciones = capacitacionRepository.obtenerCapacitacionesRemotas()
            _uiState.value = armarEstado(_uiState.value.filtroSeleccionado)
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

    private fun formatearPesos(monto: Int): String {
        val conComas = String.format(Locale.US, "%,d", monto)
        return "$" + conComas.replace(",", ".")
    }
}