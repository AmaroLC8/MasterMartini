package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.model.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Modos de ordenamiento disponibles para las recetas
enum class OrdenRecetas {
    ALFABETICO,
    DURACION
}

// Estado de la pantalla de recetas
data class RecetasUiState(
    val categorias: List<String> = emptyList(),
    val categoriaSeleccionada: String? = null,
    val orden: OrdenRecetas = OrdenRecetas.ALFABETICO,
    val soloVeganas: Boolean = false,
    val recetas: List<Curso> = emptyList(),
    val secciones: Map<String, List<Curso>> = emptyMap()
)

// ViewModel que filtra por categoría/vegano y ordena el catálogo de recetas
class RecetasViewModel : ViewModel() {

    private val cursoRepository = CursoRepository()

    private val todosLosCursos: List<Curso> = cursoRepository.obtenerCursos()
    private val categorias: List<String> = cursoRepository.obtenerCategorias()

    private val _uiState = MutableStateFlow(
        armarEstado(categoriaSeleccionada = null, orden = OrdenRecetas.ALFABETICO, soloVeganas = false)
    )

    val uiState: StateFlow<RecetasUiState> = _uiState.asStateFlow()

    fun seleccionarCategoria(categoria: String?) {
        _uiState.value = armarEstado(
            categoriaSeleccionada = categoria,
            orden = _uiState.value.orden,
            soloVeganas = _uiState.value.soloVeganas
        )
    }

    fun cambiarOrden(orden: OrdenRecetas) {
        _uiState.value = armarEstado(
            categoriaSeleccionada = _uiState.value.categoriaSeleccionada,
            orden = orden,
            soloVeganas = _uiState.value.soloVeganas
        )
    }

    fun alternarSoloVeganas() {
        _uiState.value = armarEstado(
            categoriaSeleccionada = _uiState.value.categoriaSeleccionada,
            orden = _uiState.value.orden,
            soloVeganas = !_uiState.value.soloVeganas
        )
    }

    private fun armarEstado(
        categoriaSeleccionada: String?,
        orden: OrdenRecetas,
        soloVeganas: Boolean
    ): RecetasUiState {
        val recetas = filtrarYOrdenar(categoriaSeleccionada, orden, soloVeganas)
        return RecetasUiState(
            categorias = categorias,
            categoriaSeleccionada = categoriaSeleccionada,
            orden = orden,
            soloVeganas = soloVeganas,
            recetas = recetas,
            secciones = agruparPorSeccion(recetas)
        )
    }

    // Aplica filtros de sección/vegano y ordena la lista resultante
    private fun filtrarYOrdenar(
        categoriaSeleccionada: String?,
        orden: OrdenRecetas,
        soloVeganas: Boolean
    ): List<Curso> {
        val porSeccion = if (categoriaSeleccionada == null) {
            todosLosCursos
        } else {
            todosLosCursos.filter { curso -> curso.categoria == categoriaSeleccionada }
        }

        val filtradas = if (soloVeganas) {
            porSeccion.filter { curso -> curso.esVegano }
        } else {
            porSeccion
        }

        return when (orden) {
            OrdenRecetas.ALFABETICO -> filtradas.sortedBy { curso -> curso.titulo }
            OrdenRecetas.DURACION -> filtradas.sortedBy { curso -> minutosDe(curso) }
        }
    }

    private fun minutosDe(curso: Curso): Int {
        return curso.duracion.filter { it.isDigit() }.toIntOrNull() ?: 0
    }

    // Agrupa las recetas respetando el orden de categorías del repositorio
    private fun agruparPorSeccion(recetas: List<Curso>): Map<String, List<Curso>> {
        val agrupadas = recetas.groupBy { curso -> curso.categoria }

        val seccionesEnOrden = mutableMapOf<String, List<Curso>>()
        for (categoria in categorias) {
            val recetasDeLaSeccion = agrupadas[categoria]
            if (recetasDeLaSeccion != null) {
                seccionesEnOrden[categoria] = recetasDeLaSeccion
            }
        }
        return seccionesEnOrden
    }
}
