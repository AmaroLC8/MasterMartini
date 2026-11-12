package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ESTADO DE LA PANTALLA DETALLE DEL CURSO
data class CapacitacionDetalleUiState(
    // Curso que se muestra. null mientras carga o si el id no existe
    val capacitacion: Capacitacion? = null,
    // Producto Master Martini principal del curso. null si no se encontró por nombre
    val producto: Producto? = null,
    // Posiciones de los módulos DESPLEGADOS (abiertos). Ej: {0, 2} = el 1° y el 3° están abiertos.
    // Es un Set (conjunto) y no una List porque un módulo está abierto o no:
    // no tiene sentido que aparezca dos veces.
    val modulosAbiertos: Set<Int> = emptySet()
)

// VIEWMODEL DEL DETALLE DE UN CURSO ONLINE
// Busca el curso por su id, su producto principal, y recuerda qué módulos están abiertos.
// El estado abierto/cerrado vive AQUÍ y no en la pantalla (MVVM): la pantalla solo dibuja.
// Es un ViewModel normal: no usa Room ni el Context.
class CapacitacionDetalleViewModel : ViewModel() {

    private val capacitacionRepository = CapacitacionRepository()

    // Para encontrar el producto principal del curso (igual que DetalleViewModel con el insumo)
    private val productoRepository = ProductoRepository()

    // Estado privado y mutable: solo este ViewModel lo cambia
    private val _uiState = MutableStateFlow(CapacitacionDetalleUiState())

    // Versión pública de solo lectura: CapacitacionDetalleScreen la observa con collectAsState()
    val uiState: StateFlow<CapacitacionDetalleUiState> = _uiState.asStateFlow()

    // La llama la pantalla con el id que llegó por la ruta ("capacitacion/4" -> id = 4).
    fun cargarCapacitacion(id: Int) {
        // Si ese curso ya está cargado, no se hace nada. Pasa al girar el celular: la pantalla
        // se vuelve a crear y llama otra vez a esta función, pero el ViewModel sobrevive.
        // Sin este if, los módulos que el usuario abrió se cerrarían solos.
        if (_uiState.value.capacitacion?.id == id) return

        val capacitacionEncontrada = capacitacionRepository.obtenerCapacitacionPorId(id)

        // Se busca el producto por el nombre EXACTO del insumo principal.
        // Si no hay curso o ningún producto se llama así, queda en null.
        val productoEncontrado = if (capacitacionEncontrada != null) {
            productoRepository.obtenerProductoPorNombre(capacitacionEncontrada.insumoPrincipal)
        } else {
            null
        }

        _uiState.value = CapacitacionDetalleUiState(
            capacitacion = capacitacionEncontrada,
            producto = productoEncontrado,
            // Al abrir el curso, el Módulo 1 (posición 0) parte desplegado,
            // así el usuario ve de inmediato cómo son las lecciones.
            modulosAbiertos = setOf(0)
        )
    }

    // Se llama al tocar el encabezado de un módulo: si estaba abierto lo cierra, y al revés.
    // "-" y "+" sobre un Set devuelven un Set NUEVO (sin o con ese número).
    // Se crea uno nuevo (en vez de cambiar el que hay) para que el StateFlow note el cambio.
    fun alternarModulo(indice: Int) {
        val abiertos = _uiState.value.modulosAbiertos
        val nuevosAbiertos = if (abiertos.contains(indice)) {
            abiertos - indice
        } else {
            abiertos + indice
        }
        // copy() crea un estado igual al actual, cambiando solo modulosAbiertos
        _uiState.value = _uiState.value.copy(modulosAbiertos = nuevosAbiertos)
    }

    // REGLAS DE LAS LECCIONES (freemium). Están aquí y no en la pantalla para que,
    // si cambian (ej. "las 2 primeras lecciones son muestra"), se toque un solo lugar.

    // ¿Es la 1ª lección del Módulo 1? (posiciones 0 y 0, porque las listas parten en 0)
    private fun esPrimeraLeccion(indiceModulo: Int, indiceLeccion: Int): Boolean {
        return indiceModulo == 0 && indiceLeccion == 0
    }

    // ¿Esta lección lleva la etiqueta "Muestra gratis"?
    // Solo en los cursos del Plan Pro, y solo la 1ª lección del Módulo 1.
    // (En un curso gratis no tiene sentido: todas las lecciones ya son gratis.)
    fun esMuestraGratis(indiceModulo: Int, indiceLeccion: Int): Boolean {
        val capacitacion = _uiState.value.capacitacion ?: return false
        return !capacitacion.esGratis && esPrimeraLeccion(indiceModulo, indiceLeccion)
    }

    // ¿Esta lección lleva candado? Solo en los cursos del Plan Pro, y nunca la muestra gratis.
    // En los cursos gratis ninguna lección tiene candado.
    // FASE 2: aquí también se revisará si el usuario tiene el Plan Pro activo.
    fun estaBloqueada(indiceModulo: Int, indiceLeccion: Int): Boolean {
        val capacitacion = _uiState.value.capacitacion ?: return false
        return !capacitacion.esGratis && !esPrimeraLeccion(indiceModulo, indiceLeccion)
    }
}
