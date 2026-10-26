package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.model.Curso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// VIEWMODEL DE LA PANTALLA HOME
// Le pide los cursos al repositorio y se los entrega a HomeScreen a través de un StateFlow.
// Es un ViewModel normal (no AndroidViewModel) porque no necesita el Context:
// el repositorio de cursos no lee archivos del celular.
class HomeViewModel : ViewModel() {

    // Fuente de los datos (la pantalla nunca habla directo con el repositorio)
    private val repositorio = CursoRepository()

    // Lista de cursos: privada y mutable (solo este ViewModel la cambia).
    // Parte vacía y se llena en init.
    private val _cursos = MutableStateFlow<List<Curso>>(emptyList())

    // Versión pública de solo lectura: HomeScreen la observa con collectAsState()
    val cursos: StateFlow<List<Curso>> = _cursos.asStateFlow()

    // init se ejecuta una vez, al crearse el ViewModel.
    // Va DESPUÉS de _cursos porque Kotlin inicializa las propiedades en orden.
    init {
        cargarCursos()
    }

    // Copia los cursos del repositorio al StateFlow. Al cambiar _cursos.value,
    // Compose vuelve a dibujar la lista automáticamente.
    private fun cargarCursos() {
        _cursos.value = repositorio.obtenerCursos()
    }
}
