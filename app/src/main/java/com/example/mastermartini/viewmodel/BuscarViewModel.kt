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
import java.text.Normalizer

// Estado de la pantalla de búsqueda de recetas
data class BuscarUiState(
    val texto: String = "",
    // Cargando, Error (con Reintentar) o Exito con los resultados de la búsqueda
    val estado: EstadoUi<List<Curso>> = EstadoUi.Cargando,
    val categorias: List<String> = emptyList()
) {
    // Resultados que se ven ahora (vacíos mientras carga o si hubo error)
    val resultados: List<Curso>
        get() = (estado as? EstadoUi.Exito)?.datos ?: emptyList()
}

// ViewModel para búsqueda en tiempo real de recetas y sugerencias por categoría
class BuscarViewModel : ViewModel() {

    private val cursoRepository = CursoRepository()
    private var todosLosCursos: List<Curso> = emptyList()

    private val _uiState = MutableStateFlow(BuscarUiState())
    val uiState: StateFlow<BuscarUiState> = _uiState.asStateFlow()

    init {
        cargarRecetas(forzarRecarga = false)
    }

    // Acción del botón Reintentar
    fun reintentar() {
        cargarRecetas(forzarRecarga = true)
    }

    // Pide las recetas; al llegar, repite la búsqueda con lo que el usuario ya escribió
    private fun cargarRecetas(forzarRecarga: Boolean) {
        _uiState.update { it.copy(estado = EstadoUi.Cargando) }
        viewModelScope.launch {
            cursoRepository.obtenerRecetas(forzarRecarga).fold(
                onSuccess = { lista ->
                    todosLosCursos = lista
                    _uiState.update {
                        it.copy(
                            estado = EstadoUi.Exito(buscar(it.texto)),
                            categorias = categoriasDeRecetas(lista)
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(estado = EstadoUi.Error(textoDeError(error))) }
                }
            )
        }
    }

    // Se puede escribir mientras carga; los resultados se calculan cuando ya hay recetas
    fun onTextoChange(nuevoTexto: String) {
        _uiState.update { actual ->
            if (actual.estado is EstadoUi.Exito) {
                actual.copy(texto = nuevoTexto, estado = EstadoUi.Exito(buscar(nuevoTexto)))
            } else {
                actual.copy(texto = nuevoTexto)
            }
        }
    }

    fun limpiar() = onTextoChange("")

    private fun buscar(texto: String): List<Curso> {
        val buscado = normalizar(texto).trim()

        if (buscado.isEmpty()) {
            return emptyList()
        }

        return todosLosCursos.filter { curso ->
            normalizar(curso.titulo).contains(buscado) ||
                normalizar(curso.categoria).contains(buscado) ||
                normalizar(curso.insumo).contains(buscado) ||
                curso.ingredientes.any { ingrediente -> normalizar(ingrediente).contains(buscado) }
        }
    }

    // Elimina tildes y diacríticos en minúsculas para búsquedas insensibles a acentos
    private fun normalizar(texto: String): String {
        val separado = Normalizer.normalize(texto, Normalizer.Form.NFD)
        val sinTildes = separado.replace(Regex("\\p{Mn}"), "")
        return sinTildes.lowercase()
    }
}
