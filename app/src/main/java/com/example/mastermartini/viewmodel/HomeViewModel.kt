package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.FavoritoRepository
import com.example.mastermartini.data.local.MasterMartiniDatabase
import com.example.mastermartini.data.textoDeError
import com.example.mastermartini.model.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel de la pantalla principal: recetas del servidor y favoritos desde Room
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val cursoRepository = CursoRepository()
    private val favoritoRepository = FavoritoRepository(
        MasterMartiniDatabase.getInstance(application).favoritoDao()
    )

    // Recetas del servidor: Cargando, Exito (lista) o Error (con Reintentar)
    private val _cursos = MutableStateFlow<EstadoUi<List<Curso>>>(EstadoUi.Cargando)
    val cursos: StateFlow<EstadoUi<List<Curso>>> = _cursos.asStateFlow()

    val favoritos: StateFlow<List<Int>> = favoritoRepository.obtenerIdsFavoritos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFavoritos: StateFlow<Int> = favoritoRepository.contarFavoritos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        cargarCursos(forzarRecarga = false)
    }

    // Acción del botón Reintentar: vuelve a pedir las recetas al servidor
    fun reintentar() {
        cargarCursos(forzarRecarga = true)
    }

    // Lee las recetas (de memoria si ya llegaron antes) y actualiza el estado
    private fun cargarCursos(forzarRecarga: Boolean) {
        _cursos.value = EstadoUi.Cargando
        viewModelScope.launch {
            cursoRepository.obtenerRecetas(forzarRecarga).fold(
                onSuccess = { lista -> _cursos.value = EstadoUi.Exito(lista) },
                onFailure = { error -> _cursos.value = EstadoUi.Error(textoDeError(error)) }
            )
        }
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
