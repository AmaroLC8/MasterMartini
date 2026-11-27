package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.ChecklistRepository
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.data.local.MasterMartiniDatabase
import com.example.mastermartini.model.Curso
import com.example.mastermartini.model.Producto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ViewModel del detalle de receta que gestiona datos del curso, insumo y checklists en Room
class DetalleViewModel(application: Application) : AndroidViewModel(application) {

    private val cursoRepository = CursoRepository()
    private val checklistRepository = ChecklistRepository(
        MasterMartiniDatabase.getInstance(application).checklistDao()
    )
    private val productoRepository = ProductoRepository()

    private val _curso = MutableStateFlow<Curso?>(null)
    val curso: StateFlow<Curso?> = _curso.asStateFlow()

    private val _marcados = MutableStateFlow<List<Int>>(emptyList())
    val marcados: StateFlow<List<Int>> = _marcados.asStateFlow()

    private val _pasosCompletados = MutableStateFlow<List<Int>>(emptyList())
    val pasosCompletados: StateFlow<List<Int>> = _pasosCompletados.asStateFlow()

    private val _productoInsumo = MutableStateFlow<Producto?>(null)
    val productoInsumo: StateFlow<Producto?> = _productoInsumo.asStateFlow()

    private var trabajoChecklist: Job? = null
    private var trabajoPasos: Job? = null

    // Carga la receta, su producto insumo y suscribe los checklists persistidos en Room
    fun cargarCurso(id: Int) {
        val cursoEncontrado = cursoRepository.obtenerCursoPorId(id)
        _curso.value = cursoEncontrado

        _productoInsumo.value = if (cursoEncontrado != null) {
            productoRepository.obtenerProductoPorNombre(cursoEncontrado.insumo)
        } else {
            null
        }

        trabajoChecklist?.cancel()
        trabajoChecklist = viewModelScope.launch {
            checklistRepository.obtenerMarcados(id).collect { listaMarcados ->
                _marcados.value = listaMarcados
            }
        }

        trabajoPasos?.cancel()
        trabajoPasos = viewModelScope.launch {
            checklistRepository.obtenerPasosCompletados(id).collect { listaPasos ->
                _pasosCompletados.value = listaPasos
            }
        }
    }

    // Cambia el estado marcado de un ingrediente en Room
    fun alternarIngrediente(indice: Int) {
        val cursoActual = _curso.value ?: return
        val estabaMarcado = _marcados.value.contains(indice)

        viewModelScope.launch {
            checklistRepository.marcar(cursoActual.id, indice, !estabaMarcado)
        }
    }

    fun reiniciarChecklist() {
        val cursoActual = _curso.value ?: return
        viewModelScope.launch {
            checklistRepository.reiniciar(cursoActual.id)
        }
    }

    // Cambia el estado completado de un paso en Room
    fun alternarPaso(indice: Int) {
        val cursoActual = _curso.value ?: return
        val estabaCompletado = _pasosCompletados.value.contains(indice)

        viewModelScope.launch {
            checklistRepository.marcarPaso(cursoActual.id, indice, !estabaCompletado)
        }
    }

    fun reiniciarPasos() {
        val cursoActual = _curso.value ?: return
        viewModelScope.launch {
            checklistRepository.reiniciarPasos(cursoActual.id)
        }
    }
}
