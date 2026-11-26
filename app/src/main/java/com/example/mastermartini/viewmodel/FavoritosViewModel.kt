package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.FavoritoRepository
import com.example.mastermartini.data.local.MasterMartiniDatabase
import com.example.mastermartini.model.Curso
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel de favoritos que combina los IDs guardados en Room con el catálogo de recetas
class FavoritosViewModel(application: Application) : AndroidViewModel(application) {

    private val cursoRepository = CursoRepository()
    private val favoritoRepository = FavoritoRepository(
        MasterMartiniDatabase.getInstance(application).favoritoDao()
    )

    private val todosLosCursos: List<Curso> = cursoRepository.obtenerCursos()

    val cursosFavoritos: StateFlow<List<Curso>> = favoritoRepository.obtenerIdsFavoritos()
        .map { ids ->
            todosLosCursos.filter { curso -> ids.contains(curso.id) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun quitarFavorito(cursoId: Int) {
        viewModelScope.launch {
            favoritoRepository.quitar(cursoId)
        }
    }
}
