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

// VIEWMODEL DE LA PANTALLA FAVORITOS
// Entrega a FavoritosScreen los cursos que el usuario marcó con el corazón.
// Junta dos fuentes: los ids guardados en Room (FavoritoRepository) y los datos
// completos de cada curso (CursoRepository).
// Es AndroidViewModel porque necesita el Context (application) para abrir la base de datos.
class FavoritosViewModel(application: Application) : AndroidViewModel(application) {

    // Fuente de los cursos (la misma que usan Home y Detalle)
    private val cursoRepository = CursoRepository()

    // Fuente de los favoritos: el mismo repositorio y la misma tabla que usa HomeViewModel.
    // Por eso un corazón marcado en Home aparece aquí, y uno quitado aquí se vacía en Home.
    private val favoritoRepository = FavoritoRepository(
        MasterMartiniDatabase.getInstance(application).favoritoDao()
    )

    // Catálogo completo. Se pide UNA vez y se guarda, para no recrear la lista
    // cada vez que Room avisa un cambio en los favoritos.
    private val todosLosCursos: List<Curso> = cursoRepository.obtenerCursos()

    // Cursos favoritos, completos (con título, chef, etc.), listos para dibujar.
    // 1. obtenerIdsFavoritos(): Flow de Room con los ids, ej. [2, 7]. Se vuelve a emitir solo cada vez que la tabla cambia.
    // 2. map { ids -> ... }: TRANSFORMA cada lista de ids que llega en una lista de cursos.
    //    filter se queda con los cursos cuyo id está en esa lista (ej. el curso 2 y el 7).
    // 3. stateIn(...): convierte el Flow en StateFlow, que la pantalla lee con collectAsState():
    //    - viewModelScope: el flujo vive mientras viva el ViewModel.
    //    - WhileSubscribed(5000): escucha a Room solo mientras la pantalla lo observa,
    //      y espera 5 segundos antes de cortar (así no se corta al rotar la pantalla).
    //    - emptyList(): valor inicial mientras Room responde la primera vez.
    val cursosFavoritos: StateFlow<List<Curso>> = favoritoRepository.obtenerIdsFavoritos()
        .map { ids ->
            todosLosCursos.filter { curso -> ids.contains(curso.id) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Se llama al tocar el corazón rojo de una tarjeta.
    // viewModelScope.launch abre una corrutina, necesaria porque quitar() es suspend.
    // No hay que sacar el curso de la lista a mano: Room avisa el cambio por el Flow,
    // map vuelve a filtrar y la pantalla se redibuja sin ese curso.
    fun quitarFavorito(cursoId: Int) {
        viewModelScope.launch {
            favoritoRepository.quitar(cursoId)
        }
    }
}
