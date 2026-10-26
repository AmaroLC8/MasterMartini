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

// VIEWMODEL DE LA PANTALLA HOME
// Entrega a HomeScreen los cursos (de CursoRepository) y los favoritos (de Room, vía FavoritoRepository).
// Es AndroidViewModel porque necesita el Context (application) para abrir la base de datos.
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    // Fuente de los cursos (datos ficticios, igual que antes)
    private val cursoRepository = CursoRepository()

    // Fuente de los favoritos: se abre la base de datos (singleton), se saca su DAO
    // y se le pasa al repositorio. El ViewModel solo usa el repositorio.
    private val favoritoRepository = FavoritoRepository(
        MasterMartiniDatabase.getInstance(application).favoritoDao()
    )

    // Lista de cursos: privada y mutable (solo este ViewModel la cambia).
    // Parte vacía y se llena en init.
    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())

    // Versión pública de solo lectura: HomeScreen la observa con collectAsState()
    val cursos: StateFlow<List<Curso>> = _cursos.asStateFlow()

    // Ids de los cursos favoritos. Room entrega un Flow y stateIn lo convierte en StateFlow:
    // - viewModelScope: el flujo vive mientras viva el ViewModel.
    // - WhileSubscribed(5000): escucha a Room solo mientras la pantalla lo observa,
    //   y espera 5 segundos antes de cortar (así no se corta al rotar la pantalla).
    // - emptyList(): valor inicial mientras Room responde la primera vez.
    val favoritos: StateFlow<List<Int>> = favoritoRepository.obtenerIdsFavoritos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cantidad de favoritos, para el texto "Mis favoritos: N". Mismo funcionamiento.
    val totalFavoritos: StateFlow<Int> = favoritoRepository.contarFavoritos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // init se ejecuta una vez, al crearse el ViewModel.
    // Va DESPUÉS de _cursos porque Kotlin inicializa las propiedades en orden.
    init {
        cargarCursos()
    }

    // Copia los cursos del repositorio al StateFlow. Al cambiar _cursos.value,
    // Compose vuelve a dibujar la lista automáticamente.
    private fun cargarCursos() {
        _cursos.value = cursoRepository.obtenerCursos()
    }

    // Se llama al tocar el corazón de un curso: si ya es favorito lo quita, si no lo agrega.
    // viewModelScope.launch abre una corrutina, necesaria porque agregar() y quitar() son suspend.
    // No hay que actualizar "favoritos" a mano: Room avisa el cambio por el Flow y la pantalla se redibuja.
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
