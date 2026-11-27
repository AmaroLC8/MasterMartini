package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.data.lineasDeProductos
import com.example.mastermartini.data.textoDeError
import com.example.mastermartini.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel que trae los productos del servidor y los agrupa por línea comercial
class ProductosViewModel : ViewModel() {

    private val productoRepository = ProductoRepository()

    // Cargando, Error (con Reintentar) o Exito con los productos agrupados por línea
    private val _productosPorLinea = MutableStateFlow<EstadoUi<Map<String, List<Producto>>>>(EstadoUi.Cargando)
    val productosPorLinea: StateFlow<EstadoUi<Map<String, List<Producto>>>> = _productosPorLinea.asStateFlow()

    init {
        cargarProductos(forzarRecarga = false)
    }

    // Acción del botón Reintentar
    fun reintentar() {
        cargarProductos(forzarRecarga = true)
    }

    private fun cargarProductos(forzarRecarga: Boolean) {
        _productosPorLinea.value = EstadoUi.Cargando
        viewModelScope.launch {
            productoRepository.obtenerProductos(forzarRecarga).fold(
                onSuccess = { lista -> _productosPorLinea.value = EstadoUi.Exito(agruparPorLinea(lista)) },
                onFailure = { error -> _productosPorLinea.value = EstadoUi.Error(textoDeError(error)) }
            )
        }
    }

    // Agrupa respetando el orden en que aparecen las líneas en los datos del servidor
    private fun agruparPorLinea(productos: List<Producto>): Map<String, List<Producto>> {
        val agrupados = productos.groupBy { producto -> producto.linea }

        val enOrden = mutableMapOf<String, List<Producto>>()
        for (linea in lineasDeProductos(productos)) {
            val productosDeLaLinea = agrupados[linea]
            if (productosDeLaLinea != null) {
                enOrden[linea] = productosDeLaLinea
            }
        }
        return enOrden
    }
}
