package com.example.mastermartini.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.UbicacionRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

// Estado de la encuesta de preferencias e intereses
data class EncuestaState(
    val interesesSeleccionados: Set<String> = emptySet(),
    val nivelExperiencia: String = "",
    val ubicacionGps: String = "",
    val coordenadasGps: String = "",
    val avisoGps: String? = null,
    val errorIntereses: String? = null,
    val errorNivel: String? = null,
    val mensajeExito: String? = null,
    val cargandoUbicacion: Boolean = false
)

// ViewModel de la encuesta de preferencias con captura de ubicación GPS y geocodificación inversa
class EncuestaViewModel(application: Application) : AndroidViewModel(application) {

    private val almacenamiento = AlmacenamientoLocal(application)
    private val ubicacionRepository = UbicacionRepository(application)

    private val _uiState = MutableStateFlow(EncuestaState())
    val uiState: StateFlow<EncuestaState> = _uiState.asStateFlow()

    init {
        cargarEncuestaGuardada()
    }

    private fun cargarEncuestaGuardada() {
        if (almacenamiento.existeEncuesta()) {
            _uiState.update {
                it.copy(
                    interesesSeleccionados = almacenamiento.obtenerInteresesEncuesta(),
                    nivelExperiencia = almacenamiento.obtenerNivelEncuesta(),
                    ubicacionGps = almacenamiento.obtenerUbicacionEncuesta()
                )
            }
        }
    }

    val opcionesIntereses = listOf(
        "Chocolatería y Coberturas",
        "Pastelería Fina", "Panadería Tradicional e Industrial",
        "Heladería Artesanal",
        "Decoración y Cremas Vegetales"
    )

    val opcionesNivel = listOf(
        "Básico / Principiante",
        "Intermedio / Taller",
        "Avanzado / Maestro Pastelero"
    )

    fun toggleInteres(interes: String){
        _uiState.update { currentState ->
            val nuevosIntereses =
                currentState.interesesSeleccionados.toMutableSet().apply {
                    if (contains(interes)) remove(interes)
                    else add(interes)
                }
                currentState.copy(interesesSeleccionados = nuevosIntereses,
                    errorIntereses = if (nuevosIntereses.isNotEmpty()) null else
                    currentState.errorIntereses
                )
        }
    }
    fun seleccionarNivel(nivel: String){
        _uiState.update {
            it.copy(
                nivelExperiencia = nivel,
                errorNivel = null
            )
        }
    }

    // Solicita la ubicación actual mediante FusedLocationProviderClient y resuelve su dirección
    @SuppressLint("MissingPermission")
    fun obtenerUbicacion() {
        _uiState.update { it.copy(cargandoUbicacion = true, avisoGps = null) }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>())

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val coords = String.format(
                            Locale.US, "Lat: %.4f, Lon: %.4f",
                            location.latitude, location.longitude
                        )
                        buscarNombreLugar(location.latitude, location.longitude, coords)
                    } else {
                        mostrarAvisoSinUbicacion()
                    }
                }
                .addOnFailureListener {
                    mostrarAvisoSinUbicacion()
                }
        } catch (e: SecurityException) {
            mostrarAvisoSinUbicacion()
        }
    }

    // Obtiene comuna y región mediante geocodificación inversa, usando coordenadas como respaldo
    private fun buscarNombreLugar(latitud: Double, longitud: Double, coords: String) {
        viewModelScope.launch {
            val nombreLugar = ubicacionRepository.obtenerNombreLugar(latitud, longitud)

            if (nombreLugar != null) {
                _uiState.update {
                    it.copy(
                        ubicacionGps = nombreLugar,
                        coordenadasGps = coords,
                        avisoGps = null,
                        cargandoUbicacion = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        ubicacionGps = coords,
                        coordenadasGps = "",
                        avisoGps = "No pudimos obtener el nombre del lugar; se guardaron las coordenadas.",
                        cargandoUbicacion = false
                    )
                }
            }
        }
    }

    private fun mostrarAvisoSinUbicacion() {
        _uiState.update {
            it.copy(
                avisoGps = "No se pudo obtener tu ubicación. Revisa que el GPS esté activado.",
                cargandoUbicacion = false
            )
        }
    }

    fun registrarRespuestaSinGps(){
        _uiState.update {
            it.copy(avisoGps = "Sin permiso de ubicación. Puedes guardar la encuesta igual.")
        }
    }

    // Valida áreas de interés y nivel antes de persistir la encuesta localmente
    fun guardarRespuesta(): Boolean{
        val state = _uiState.value
        var valido = true

        var errIntereses:String? = null
        var errNivel:String? = null

        if(state.interesesSeleccionados.isEmpty()){
            errIntereses="Debes seleccionar al menos un área de interés."
            valido=false
        }
        if (state.nivelExperiencia.isBlank()){
            errNivel="Debes seleccionar tu nivel de experiencia."
            valido = false
        }

        if (valido) {
            almacenamiento.guardarEncuesta(
                intereses = state.interesesSeleccionados,
                nivel = state.nivelExperiencia,
                ubicacion = state.ubicacionGps
            )
        }

        _uiState.update {
            it.copy(
                errorIntereses = errIntereses,
                errorNivel = errNivel,
                mensajeExito = if (valido) "Encuesta registrada con éxito" else null
            )
        }
        return valido
    }
}