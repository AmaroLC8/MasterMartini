package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.ChecklistRepository
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.local.MasterMartiniDatabase
import com.example.mastermartini.model.Curso
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// VIEWMODEL DE LA PANTALLA DETALLE
// Entrega a DetalleScreen el curso que el usuario tocó en Home (con sus pasos de receta)
// y el checklist de ingredientes guardado en Room.
// Es AndroidViewModel (y no ViewModel normal) porque necesita el Context (application)
// para abrir la base de datos, igual que HomeViewModel.
class DetalleViewModel(application: Application) : AndroidViewModel(application) {

    // Fuente de los cursos (la misma que usa HomeViewModel)
    private val cursoRepository = CursoRepository()

    // Fuente del checklist: se abre la base de datos (singleton), se saca su DAO
    // y se le pasa al repositorio. El ViewModel solo usa el repositorio.
    private val checklistRepository = ChecklistRepository(
        MasterMartiniDatabase.getInstance(application).checklistDao()
    )

    // Curso que se está mostrando: privado y mutable (solo este ViewModel lo cambia).
    // Es Curso? (puede ser null) porque parte vacío y porque el id podría no existir.
    private val _curso = MutableStateFlow<Curso?>(null)

    // Versión pública de solo lectura: DetalleScreen la observa con collectAsState()
    val curso: StateFlow<Curso?> = _curso.asStateFlow()

    // Posiciones de los ingredientes marcados del curso (ej. [0, 2] = el 1° y el 3° están listos).
    // Privado y mutable; parte vacío hasta que Room responde.
    private val _marcados = MutableStateFlow<List<Int>>(emptyList())

    // Versión pública de solo lectura para la pantalla
    val marcados: StateFlow<List<Int>> = _marcados.asStateFlow()

    // Guarda la corrutina que está escuchando a Room, para poder detenerla
    // si se carga otro curso (así no quedan dos escuchando a la vez).
    private var trabajoChecklist: Job? = null

    // La llama DetalleScreen con el id que llegó por la navegación ("detalle/3" → id = 3).
    // Si el repositorio no encuentra el curso, _curso queda en null y la pantalla
    // muestra "Receta no encontrada" (la app no se cae).
    fun cargarCurso(id: Int) {
        _curso.value = cursoRepository.obtenerCursoPorId(id)

        // Se detiene la escucha anterior (si había una) antes de empezar la nueva
        trabajoChecklist?.cancel()

        // viewModelScope.launch abre una corrutina que vive mientras viva el ViewModel.
        // collect se queda ESCUCHANDO el Flow de Room: cada vez que la tabla cambia
        // (al marcar, desmarcar o reiniciar), llega la lista nueva y se copia a _marcados.
        // Así la pantalla se actualiza sola, sin volver a consultar a mano.
        trabajoChecklist = viewModelScope.launch {
            checklistRepository.obtenerMarcados(id).collect { listaMarcados ->
                _marcados.value = listaMarcados
            }
        }
    }

    // Se llama al tocar un ingrediente: si ya estaba marcado lo desmarca, si no lo marca.
    // No se cambia _marcados a mano: se guarda en Room y Room avisa el cambio por el Flow.
    fun alternarIngrediente(indice: Int) {
        // Si no hay curso cargado, no hay nada que guardar
        val cursoActual = _curso.value ?: return
        val estabaMarcado = _marcados.value.contains(indice)

        viewModelScope.launch {
            // !estabaMarcado: si estaba marcado se guarda false, y si no, true
            checklistRepository.marcar(cursoActual.id, indice, !estabaMarcado)
        }
    }

    // Desmarca todos los ingredientes del curso actual (botón "Reiniciar lista")
    fun reiniciarChecklist() {
        val cursoActual = _curso.value ?: return
        viewModelScope.launch {
            checklistRepository.reiniciar(cursoActual.id)
        }
    }
}
