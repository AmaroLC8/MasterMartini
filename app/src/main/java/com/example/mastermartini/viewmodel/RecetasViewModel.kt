package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.categoriasDeRecetas
import com.example.mastermartini.data.textoDeError
import com.example.mastermartini.model.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Modos de ordenamiento disponibles para las recetas
enum class OrdenRecetas {
    ALFABETICO,
    DURACION
}

// Estado de la pantalla de recetas
data class RecetasUiState(
    // Cargando, Error (con Reintentar) o Exito con las recetas ya filtradas y ordenadas
    val estado: EstadoUi<List<Curso>> = EstadoUi.Cargando,
    val categorias: List<String> = emptyList(),
    val categoriaSeleccionada: String? = null,
    val orden: OrdenRecetas = OrdenRecetas.ALFABETICO,
    val soloVeganas: Boolean = false,
    val secciones: Map<String, List<Curso>> = emptyMap()
) {
    // Recetas que se ven ahora (vacía mientras carga o si hubo error)
    val recetas: List<Curso>
        get() = (estado as? EstadoUi.Exito)?.datos ?: emptyList()
}

// ViewModel que trae las recetas del servidor, las filtra por categoría/vegano y las ordena
class RecetasViewModel : ViewModel() {

    private val cursoRepository = CursoRepository()

    private var todosLosCursos: List<Curso> = emptyList()
    private var categorias: List<String> = emptyList()

    private val _uiState = MutableStateFlow(RecetasUiState())
    val uiState: StateFlow<RecetasUiState> = _uiState.asStateFlow()

    init {
        cargarRecetas(forzarRecarga = false)
    }

    // Acción del botón Reintentar
    fun reintentar() {
        cargarRecetas(forzarRecarga = true)
    }

    // Pide las recetas y arma el estado con los filtros que ya estaban elegidos
    private fun cargarRecetas(forzarRecarga: Boolean) {
        _uiState.update { it.copy(estado = EstadoUi.Cargando) }
        viewModelScope.launch {
            cursoRepository.obtenerRecetas(forzarRecarga).fold(
                onSuccess = { lista ->
                    todosLosCursos = lista
                    // Las categorías salen de las recetas que llegaron
                    categorias = categoriasDeRecetas(lista)
                    val actual = _uiState.value
                    // Si la categoría elegida ya no existe en el servidor, se vuelve a "Todas"
                    val categoria = actual.categoriaSeleccionada?.takeIf { it in categorias }
                    _uiState.value = armarEstado(categoria, actual.orden, actual.soloVeganas)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(estado = EstadoUi.Error(textoDeError(error))) }
                }
            )
        }
    }

    // Los filtros solo se aplican cuando ya llegaron las recetas
    private fun hayRecetas(): Boolean = _uiState.value.estado is EstadoUi.Exito

    fun seleccionarCategoria(categoria: String?) {
        if (!hayRecetas()) return
        _uiState.value = armarEstado(
            categoriaSeleccionada = categoria,
            orden = _uiState.value.orden,
            soloVeganas = _uiState.value.soloVeganas
        )
    }

    fun cambiarOrden(orden: OrdenRecetas) {
        if (!hayRecetas()) return
        _uiState.value = armarEstado(
            categoriaSeleccionada = _uiState.value.categoriaSeleccionada,
            orden = orden,
            soloVeganas = _uiState.value.soloVeganas
        )
    }

    fun alternarSoloVeganas() {
        if (!hayRecetas()) return
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
            estado = EstadoUi.Exito(recetas),
            categorias = categorias,
            categoriaSeleccionada = categoriaSeleccionada,
            orden = orden,
            soloVeganas = soloVeganas,
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

    // Agrupa las recetas respetando el orden de las categorías que llegaron del servidor
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
