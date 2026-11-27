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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ViewModel de favoritos: cruza los ids guardados en Room con las recetas del servidor
class FavoritosViewModel(application: Application) : AndroidViewModel(application) {

    private val cursoRepository = CursoRepository()
    private val favoritoRepository = FavoritoRepository(
        MasterMartiniDatabase.getInstance(application).favoritoDao()
    )

    // Recetas del servidor: Cargando, Exito o Error
    private val _recetas = MutableStateFlow<EstadoUi<List<Curso>>>(EstadoUi.Cargando)

    // Si llegaron las recetas, deja solo las favoritas; si no, pasa Cargando o Error tal cual
    val cursosFavoritos: StateFlow<EstadoUi<List<Curso>>> =
        combine(_recetas, favoritoRepository.obtenerIdsFavoritos()) { estado, ids ->
            if (estado is EstadoUi.Exito) {
                EstadoUi.Exito(estado.datos.filter { curso -> ids.contains(curso.id) })
            } else {
                estado
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EstadoUi.Cargando)

    init {
        cargarRecetas(forzarRecarga = false)
    }

    // Acción del botón Reintentar: si el servidor no respondía, se vuelve a intentar
    fun reintentar() {
        cargarRecetas(forzarRecarga = true)
    }

    private fun cargarRecetas(forzarRecarga: Boolean) {
        _recetas.value = EstadoUi.Cargando
        viewModelScope.launch {
            cursoRepository.obtenerRecetas(forzarRecarga).fold(
                onSuccess = { lista -> _recetas.value = EstadoUi.Exito(lista) },
                onFailure = { error -> _recetas.value = EstadoUi.Error(textoDeError(error)) }
            )
        }
    }

    fun quitarFavorito(cursoId: Int) {
        viewModelScope.launch {
            favoritoRepository.quitar(cursoId)
        }
    }
}
