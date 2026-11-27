package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.FavoritoRepository
import com.example.mastermartini.data.local.MasterMartiniDatabase
import com.example.mastermartini.model.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel de la pantalla principal que expone el catálogo de recetas y favoritos desde Room
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val cursoRepository = CursoRepository()
    private val favoritoRepository = FavoritoRepository(
        MasterMartiniDatabase.getInstance(application).favoritoDao()
    )

    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())
    val cursos: StateFlow<List<Curso>> = _cursos.asStateFlow()

    val favoritos: StateFlow<List<Int>> = favoritoRepository.obtenerIdsFavoritos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFavoritos: StateFlow<Int> = favoritoRepository.contarFavoritos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        cargarCursos()
    }

    private fun cargarCursos() {
        _cursos.value = cursoRepository.obtenerCursos()
    }

    // Agrega o quita una receta de favoritos en Room
    fun alternarFavorito(cursoId: Int) {
        viewModelScope.launch {
            if (favoritos.value.contains(cursoId)) {
                favoritoRepository.quitar(cursoId)
            } else {
                favoritoRepository.agregar(cursoId)
            }
        }
    }
}
