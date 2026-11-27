package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.model.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.Normalizer

// Estado de la pantalla de búsqueda de recetas
data class BuscarUiState(
    val texto: String = "",
    val resultados: List<Curso> = emptyList(),
    val categorias: List<String> = emptyList()
)

// ViewModel para búsqueda en tiempo real de recetas y sugerencias por categoría
class BuscarViewModel : ViewModel() {

    private val cursoRepository = CursoRepository()
    private val todosLosCursos: List<Curso> = cursoRepository.obtenerCursos()

    private val _uiState = MutableStateFlow(
        BuscarUiState(categorias = cursoRepository.obtenerCategorias())
    )
    val uiState: StateFlow<BuscarUiState> = _uiState.asStateFlow()

    fun onTextoChange(nuevoTexto: String) {
        _uiState.value = _uiState.value.copy(
            texto = nuevoTexto,
            resultados = buscar(nuevoTexto)
        )
    }

    fun limpiar() {
        _uiState.value = _uiState.value.copy(texto = "", resultados = emptyList())
    }

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
