package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ViewModel que expone el catálogo de productos agrupado por línea comercial
class ProductosViewModel : ViewModel() {

    private val productoRepository = ProductoRepository()

    private val _productosPorLinea = MutableStateFlow<Map<String, List<Producto>>>(emptyMap())
    val productosPorLinea: StateFlow<Map<String, List<Producto>>> = _productosPorLinea.asStateFlow()

    init {
        cargarProductos()
    }

    private fun cargarProductos() {
        val agrupados = productoRepository.obtenerProductos().groupBy { producto -> producto.linea }

        val enOrden = mutableMapOf<String, List<Producto>>()
        for (linea in productoRepository.obtenerLineas()) {
            val productosDeLaLinea = agrupados[linea]
            if (productosDeLaLinea != null) {
                enOrden[linea] = productosDeLaLinea
            }
        }
        _productosPorLinea.value = enOrden
    }
}
