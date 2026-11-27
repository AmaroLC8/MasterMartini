package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.data.esNoEncontrado
import com.example.mastermartini.data.recetasQueUsan
import com.example.mastermartini.data.textoDeError
import com.example.mastermartini.model.Curso
import com.example.mastermartini.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado de la pantalla de detalle de producto y sus recetas asociadas
data class ProductoDetalleUiState(
    val cargando: Boolean = true,
    val producto: Producto? = null,
    // 404: el producto ya no existe en el servidor
    val noExiste: Boolean = false,
    val errorCarga: String? = null,
    val recetas: List<Curso> = emptyList(),
    // Si fallan solo las recetas, el producto se muestra igual con este aviso
    val errorRecetas: String? = null
)

// ViewModel que carga el producto seleccionado y las recetas donde se utiliza
class ProductoDetalleViewModel : ViewModel() {

    private val productoRepository = ProductoRepository()
    private val cursoRepository = CursoRepository()

    private var idActual: Int? = null

    private val _uiState = MutableStateFlow(ProductoDetalleUiState())
    val uiState: StateFlow<ProductoDetalleUiState> = _uiState.asStateFlow()

    // Carga el producto; si ya está cargado no lo vuelve a pedir
    fun cargarProducto(id: Int) {
        if (idActual == id && _uiState.value.producto != null) return
        idActual = id
        pedirProducto(id, forzarRecarga = false)
    }

    // Acción del botón Reintentar (del producto o de sus recetas)
    fun reintentar() {
        val id = idActual ?: return
        pedirProducto(id, forzarRecarga = true)
    }

    private fun pedirProducto(id: Int, forzarRecarga: Boolean) {
        _uiState.value = ProductoDetalleUiState(cargando = true)
        viewModelScope.launch {
            productoRepository.obtenerProductoPorId(id, forzarRecarga).fold(
                onSuccess = { producto ->
                    val resultadoRecetas = cursoRepository.obtenerRecetas(forzarRecarga)
                    _uiState.value = ProductoDetalleUiState(
                        cargando = false,
                        producto = producto,
                        recetas = resultadoRecetas.getOrNull()?.let { recetasQueUsan(producto, it) } ?: emptyList(),
                        errorRecetas = resultadoRecetas.exceptionOrNull()?.let { textoDeError(it) }
                    )
                },
                onFailure = { error ->
                    _uiState.value = if (esNoEncontrado(error)) {
                        ProductoDetalleUiState(cargando = false, noExiste = true)
                    } else {
                        ProductoDetalleUiState(cargando = false, errorCarga = textoDeError(error))
                    }
                }
            )
        }
    }
}
