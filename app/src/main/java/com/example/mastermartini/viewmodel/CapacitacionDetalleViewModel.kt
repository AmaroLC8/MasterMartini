package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CapacitacionDetalleUiState(
    val capacitacion: Capacitacion? = null,
    val producto: Producto? = null,
    val modulosAbiertos: Set<Int> = emptySet()
)

class CapacitacionDetalleViewModel : ViewModel() {

    private val capacitacionRepository = CapacitacionRepository()
    private val productoRepository = ProductoRepository()

    private val _uiState = MutableStateFlow(CapacitacionDetalleUiState())
    val uiState: StateFlow<CapacitacionDetalleUiState> = _uiState.asStateFlow()

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
                modulosAbiertos = setOf(0)
            )
        }
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
        return !capacitacion.esGratis && esPrimeraLeccion(indiceModulo, indiceLeccion)
    }

    fun estaBloqueada(indiceModulo: Int, indiceLeccion: Int): Boolean {
        val capacitacion = _uiState.value.capacitacion ?: return false
        return !capacitacion.esGratis && !esPrimeraLeccion(indiceModulo, indiceLeccion)
    }
}