package com.example.mastermartini.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.PreguntaRepository
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.model.Pregunta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado del panel de preguntas sin responder
data class PreguntasPendientesUiState(
    val estado: EstadoUi<List<Pregunta>> = EstadoUi.Cargando,
    val respuestas: Map<Long, String> = emptyMap(),
    val errores: Map<Long, String> = emptyMap(),
    val enviando: Set<Long> = emptySet(),
    val mensajeSnackbar: String? = null
)

// ViewModel del panel donde el instructor o el admin responden las preguntas pendientes
class PreguntasPendientesViewModel(
    private val preguntaRepository: PreguntaRepository,
    private val almacenamiento: AlmacenamientoLocal
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreguntasPendientesUiState())
    val uiState: StateFlow<PreguntasPendientesUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    // Pide al servidor las preguntas pendientes del usuario con sesión iniciada
    fun cargar() {
        val usuarioId = almacenamiento.obtenerIdUsuario()
        if (usuarioId == null) {
            _uiState.update { it.copy(estado = EstadoUi.Error("Debes iniciar sesión para ver las preguntas")) }
            return
        }

        _uiState.update { it.copy(estado = EstadoUi.Cargando) }
        viewModelScope.launch {
            val resultado = preguntaRepository.pendientes(usuarioId)
            resultado.fold(
                onSuccess = { lista ->
                    _uiState.update { it.copy(estado = EstadoUi.Exito(lista)) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(estado = EstadoUi.Error(textoDelError(error))) }
                }
            )
        }
    }

    // Guarda el borrador de respuesta de una pregunta y borra su error
    fun onRespuestaChange(preguntaId: Long, texto: String) {
        _uiState.update {
            it.copy(respuestas = it.respuestas + (preguntaId to texto), errores = it.errores - preguntaId)
        }
    }

    // Valida la respuesta y la envía; si sale bien la pregunta sale de la lista
    fun responder(pregunta: Pregunta) {
        val estadoActual = _uiState.value
        if (pregunta.id in estadoActual.enviando) return

        val texto = estadoActual.respuestas[pregunta.id].orEmpty().trim()
        if (texto.isEmpty()) {
            _uiState.update {
                it.copy(errores = it.errores + (pregunta.id to "Escribe una respuesta antes de enviarla"))
            }
            return
        }
        val usuarioId = almacenamiento.obtenerIdUsuario()
        if (usuarioId == null) {
            _uiState.update { it.copy(mensajeSnackbar = "Debes iniciar sesión para responder") }
            return
        }

        _uiState.update { it.copy(enviando = it.enviando + pregunta.id) }
        viewModelScope.launch {
            val resultado = preguntaRepository.responder(usuarioId, pregunta.id, texto)
            resultado.fold(
                onSuccess = {
                    _uiState.update { actual ->
                        val estadoNuevo = if (actual.estado is EstadoUi.Exito) {
                            EstadoUi.Exito(actual.estado.datos.filter { otra -> otra.id != pregunta.id })
                        } else {
                            actual.estado
                        }
                        actual.copy(
                            estado = estadoNuevo,
                            respuestas = actual.respuestas - pregunta.id,
                            enviando = actual.enviando - pregunta.id,
                            mensajeSnackbar = "Respuesta enviada"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(enviando = it.enviando - pregunta.id, mensajeSnackbar = textoDelError(error))
                    }
                }
            )
        }
    }

    private fun textoDelError(error: Throwable): String {
        if (error is ServidorException) {
            return error.mensajeServidor ?: error.message ?: "No se pudo completar la acción"
        }
        return error.message ?: "No se pudo completar la acción"
    }

    fun snackbarMostrado() {
        _uiState.update { it.copy(mensajeSnackbar = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                PreguntasPendientesViewModel(
                    preguntaRepository = PreguntaRepository(),
                    almacenamiento = AlmacenamientoLocal(application)
                )
            }
        }
    }
}
