package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.model.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// VIEWMODEL DE LA PANTALLA DETALLE
// Entrega a DetalleScreen el curso que el usuario tocó en Home (con sus pasos de receta).
// Es un ViewModel normal (no AndroidViewModel) porque no necesita el Context:
// los cursos salen de CursoRepository, que no usa base de datos ni SharedPreferences.
class DetalleViewModel : ViewModel() {

    // Fuente de los cursos (la misma que usa HomeViewModel)
    private val cursoRepository = CursoRepository()

    // Curso que se está mostrando: privado y mutable (solo este ViewModel lo cambia).
    // Es Curso? (puede ser null) porque parte vacío y porque el id podría no existir.
    private val _curso = MutableStateFlow<Curso?>(null)

    // Versión pública de solo lectura: DetalleScreen la observa con collectAsState()
    val curso: StateFlow<Curso?> = _curso.asStateFlow()

    // La llama DetalleScreen con el id que llegó por la navegación ("detalle/3" → id = 3).
    // Si el repositorio no encuentra el curso, _curso queda en null y la pantalla
    // muestra "Receta no encontrada" (la app no se cae).
    fun cargarCurso(id: Int) {
        _curso.value = cursoRepository.obtenerCursoPorId(id)
    }
}
