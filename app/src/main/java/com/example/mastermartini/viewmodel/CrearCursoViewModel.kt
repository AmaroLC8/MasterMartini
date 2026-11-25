package com.example.mastermartini.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.model.Leccion
import com.example.mastermartini.model.Modulo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CrearCursoUiState(
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "Chocolatería",
    val nivel: String = "Básico",
    val esGratis: Boolean = true,
    val duracionTotal: String = "45 min",
    val insumoPrincipal: String = "Chocolate Ariba",
    val videoCloudinaryUrl: String = "",
    val tituloModulo: String = "Módulo 1: Introducción y técnica",
    val tituloLeccion: String = "Clase 1: Preparación paso a paso",
    val descripcionLeccion: String = "Demostración práctica con insumos Master Martini.",
    val duracionLeccion: String = "15 min",
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val guardadoExitoso: Boolean = false
)

class CrearCursoViewModel(application: Application) : AndroidViewModel(application) {

    private val almacenamiento = AlmacenamientoLocal(application)
    private val repository = CapacitacionRepository()

    private val _uiState = MutableStateFlow(CrearCursoUiState())
    val uiState: StateFlow<CrearCursoUiState> = _uiState.asStateFlow()

    val categorias = listOf("Chocolatería", "Pastelería", "Panadería", "Heladería", "Decoración")
    val niveles = listOf("Básico", "Intermedio", "Avanzado")

    fun onTituloChange(v: String) = _uiState.update { it.copy(titulo = v, mensajeError = null) }
    fun onDescripcionChange(v: String) = _uiState.update { it.copy(descripcion = v, mensajeError = null) }
    fun onCategoriaChange(v: String) = _uiState.update { it.copy(categoria = v) }
    fun onNivelChange(v: String) = _uiState.update { it.copy(nivel = v) }
    fun onEsGratisChange(v: Boolean) = _uiState.update { it.copy(esGratis = v) }
    fun onDuracionChange(v: String) = _uiState.update { it.copy(duracionTotal = v) }
    fun onInsumoChange(v: String) = _uiState.update { it.copy(insumoPrincipal = v) }
    fun onVideoUrlChange(v: String) = _uiState.update { it.copy(videoCloudinaryUrl = v, mensajeError = null) }
    fun onTituloModuloChange(v: String) = _uiState.update { it.copy(tituloModulo = v) }
    fun onTituloLeccionChange(v: String) = _uiState.update { it.copy(tituloLeccion = v) }

    fun publicarCurso() {
        val actual = _uiState.value
        if (actual.titulo.isBlank() || actual.descripcion.isBlank() || actual.insumoPrincipal.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Completa título, descripción e insumo principal") }
            return
        }

        val urlLimpia = actual.videoCloudinaryUrl.trim().ifBlank { null }
        if (urlLimpia != null && !urlLimpia.startsWith("http")) {
            _uiState.update { it.copy(mensajeError = "El enlace de Cloudinary debe empezar con http:// o https://") }
            return
        }

        // Lee el ID del usuario guardado en SharedPreferences (por defecto 2 si es prueba local)
        val usuarioId = almacenamiento.obtenerIdUsuario()?.toInt() ?: 2
        val nombreInstructor = almacenamiento.obtenerNombre()

        // El backend exige al menos 1 módulo y 1 lección al crear el curso[cite: 19]
        val nuevoCurso = Capacitacion(
            id = 0,
            titulo = actual.titulo.trim(),
            descripcion = actual.descripcion.trim(),
            categoria = actual.categoria,
            nivel = actual.nivel,
            esGratis = actual.esGratis,
            instructor = nombreInstructor,
            duracionTotal = actual.duracionTotal.trim().ifBlank { "30 min" },
            insumoPrincipal = actual.insumoPrincipal.trim(),
            videoResumenUrl = urlLimpia,
            loQueLograras = listOf("Dominar el uso de ${actual.insumoPrincipal.trim()}"),
            incluye = listOf("Certificado Master Martini", "Acceso ilimitado"),
            modulos = listOf(
                Modulo(
                    titulo = actual.tituloModulo.trim().ifBlank { "Módulo 1" },
                    lecciones = listOf(
                        Leccion(
                            titulo = actual.tituloLeccion.trim().ifBlank { "Lección 1" },
                            descripcion = actual.descripcionLeccion.trim(),
                            duracion = actual.duracionLeccion.trim().ifBlank { "15 min" },
                            videoUrl = urlLimpia
                        )
                    )
                )
            )
        )

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensajeError = null) }
            val resultado = repository.crearCapacitacion(usuarioId, nuevoCurso)
            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(cargando = false, guardadoExitoso = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(cargando = false, mensajeError = error.message) }
                }
            )
        }
    }
}