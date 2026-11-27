package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.model.Curso
import com.example.mastermartini.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Estado de la pantalla de detalle de producto y sus recetas asociadas
data class ProductoDetalleUiState(
    val producto: Producto? = null,
    val recetas: List<Curso> = emptyList()
)

// ViewModel que carga el producto seleccionado y las recetas donde se utiliza
class ProductoDetalleViewModel : ViewModel() {

    private val productoRepository = ProductoRepository()

    private val _uiState = MutableStateFlow(ProductoDetalleUiState())
    val uiState: StateFlow<ProductoDetalleUiState> = _uiState.asStateFlow()

    fun cargarProducto(id: Int) {
        val producto = productoRepository.obtenerProductoPorId(id)

        val recetas = if (producto != null) {
            productoRepository.obtenerRecetasQueUsan(producto)
        } else {
            emptyList()
        }

        _uiState.value = ProductoDetalleUiState(producto = producto, recetas = recetas)
    }
}
