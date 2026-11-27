package com.example.mastermartini.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.ClimaRepository
import com.example.mastermartini.data.FavoritoRepository
import com.example.mastermartini.data.FotoRepository
import com.example.mastermartini.data.local.MasterMartiniDatabase
import com.example.mastermartini.model.OpcionPanel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la pantalla de perfil con sesión, encuesta, favoritos y clima
data class PerfilUiState(
    val nombre: String = "",
    val rut: String = "",
    val correo: String = "",
    val telefono: String = "",
    val rubro: String = "",
    val personasNegocio: String = "",
    val foto: Bitmap? = null,
    val intereses: List<String> = emptyList(),
    val nivel: String = "",
    val ubicacion: String = "",
    val respondioEncuesta: Boolean = false,
    val totalFavoritos: Int = 0,
    val rol: String = "",
    val planPro: Boolean = false,
    val opcionesPanel: List<OpcionPanel> = emptyList(),
    val condicionesTaller: String = ""
)

// ViewModel del perfil que consolida sesión local, foto, conteo en Room y clima desde Open-Meteo
class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val almacenamiento = AlmacenamientoLocal(application)
    private val fotoRepository = FotoRepository(application)
    private val favoritoRepository = FavoritoRepository(
        MasterMartiniDatabase.getInstance(application).favoritoDao()
    )
    private val climaRepository = ClimaRepository()

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritoRepository.contarFavoritos().collect { total ->
                _uiState.update { it.copy(totalFavoritos = total) }
            }
        }

        viewModelScope.launch {
            val temperatura = climaRepository.obtenerTemperaturaActual()
            if (temperatura != null) {
                _uiState.update { it.copy(condicionesTaller = textoCondicionesTaller(temperatura)) }
            }
        }
    }

    // Recarga datos locales de sesión, encuesta, opciones de rol y foto del local
    fun cargarPerfil() {
        _uiState.update {
            it.copy(
                nombre = almacenamiento.obtenerNombre(),
                rut = almacenamiento.obtenerRut(),
                correo = almacenamiento.obtenerCorreo(),
                telefono = almacenamiento.obtenerTelefono(),
                rubro = almacenamiento.obtenerRubro(),
                personasNegocio = textoPersonas(almacenamiento.obtenerPersonasNegocio()),
                intereses = almacenamiento.obtenerInteresesEncuesta().sorted(),
                nivel = almacenamiento.obtenerNivelEncuesta(),
                ubicacion = almacenamiento.obtenerUbicacionEncuesta(),
                respondioEncuesta = almacenamiento.existeEncuesta(),
                rol = almacenamiento.obtenerRol().nombreVisible,
                planPro = almacenamiento.tienePlanPro(),
                opcionesPanel = almacenamiento.obtenerRol().opcionesPanel()
            )
        }

        viewModelScope.launch {
            val fotoGuardada = fotoRepository.obtenerFoto()
            _uiState.update { it.copy(foto = fotoGuardada) }
        }
    }

    fun cerrarSesion() {
        almacenamiento.cerrarSesion()
    }

    // Genera la recomendación técnica para el taller según la temperatura actual
    private fun textoCondicionesTaller(temperatura: Double): String {
        val grados = temperatura.toInt()
        val recomendacion = when {
            temperatura > 26.0 -> "Mantén las cremas refrigeradas antes de batir"
            temperatura < 16.0 -> "Ideal para templado de chocolate y laminado"
            else -> "Ideal para templado de chocolate"
        }
        return "Condiciones del taller hoy en Santiago: $grados °C — $recomendacion"
    }

    private fun textoPersonas(guardado: String): String {
        return if (guardado.isBlank() || guardado == "Prefiero no decirlo") {
            "No indicado"
        } else {
            guardado
        }
    }
}
