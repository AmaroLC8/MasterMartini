package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// VIEWMODEL DE LA PANTALLA "NUESTROS PRODUCTOS"
// Entrega los productos Master Martini agrupados por línea (Margarinas, Cremas vegetales...).
// Es un ViewModel normal (no AndroidViewModel) porque los productos son una lista fija:
// no usa Room ni el Context.
class ProductosViewModel : ViewModel() {

    private val productoRepository = ProductoRepository()

    // Productos agrupados: clave = línea, valor = productos de esa línea.
    // Privado y mutable: solo este ViewModel lo cambia. Parte vacío y se llena en init.
    private val _productosPorLinea = MutableStateFlow<Map<String, List<Producto>>>(emptyMap())

    // Versión pública de solo lectura: ProductosScreen la observa con collectAsState()
    val productosPorLinea: StateFlow<Map<String, List<Producto>>> = _productosPorLinea.asStateFlow()

    // init se ejecuta una vez, al crearse el ViewModel.
    // Va DESPUÉS de _productosPorLinea porque Kotlin inicializa las propiedades en orden.
    init {
        cargarProductos()
    }

    // Agrupa los productos por línea, en el orden de obtenerLineas()
    // (igual que agruparPorSeccion en RecetasViewModel).
    private fun cargarProductos() {
        // groupBy: {"Margarinas" = [Gran Master Hoja, Master Export Horneo], "Mantecas" = [...], ...}
        val agrupados = productoRepository.obtenerProductos().groupBy { producto -> producto.linea }

        // Se recorren las líneas en orden para que siempre salgan igual,
        // y se saltan las que no tengan productos (así no queda un título vacío).
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
