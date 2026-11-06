package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.model.Curso
import com.example.mastermartini.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ESTADO DE LA PANTALLA DETALLE DE UN PRODUCTO
data class ProductoDetalleUiState(
    // Producto que se muestra. null = todavía no se carga, o el id no existe
    val producto: Producto? = null,
    // Recetas de la app que usan este producto (puede estar vacía)
    val recetas: List<Curso> = emptyList()
)

// VIEWMODEL DEL DETALLE DE UN PRODUCTO
// Busca el producto por su id y las recetas que lo usan.
// Es un ViewModel normal: los datos son listas fijas de los repositorios.
class ProductoDetalleViewModel : ViewModel() {

    private val productoRepository = ProductoRepository()

    // Estado privado y mutable: solo este ViewModel lo cambia
    private val _uiState = MutableStateFlow(ProductoDetalleUiState())

    // Versión pública de solo lectura: ProductoDetalleScreen la observa con collectAsState()
    val uiState: StateFlow<ProductoDetalleUiState> = _uiState.asStateFlow()

    // La llama ProductoDetalleScreen con el id que llegó por la ruta ("producto/9" -> id = 9).
    // Si el producto no existe, queda en null y la pantalla dice "Producto no encontrado"
    // (la app no se cae).
    fun cargarProducto(id: Int) {
        val producto = productoRepository.obtenerProductoPorId(id)

        // Si no hay producto, tampoco hay recetas que buscar
        val recetas = if (producto != null) {
            productoRepository.obtenerRecetasQueUsan(producto)
        } else {
            emptyList()
        }

        _uiState.value = ProductoDetalleUiState(producto = producto, recetas = recetas)
    }
}
