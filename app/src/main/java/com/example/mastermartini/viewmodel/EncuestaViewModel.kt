package com.example.mastermartini.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EncuestaState(
    val interesesSeleccionados: Set<String> = emptySet(),
    val nivelExperiencia: String = "",
    val ubicacionGps: String = "",
    val errorIntereses: String? = null,
    val errorNivel: String? = null,
    val mensajeExito: String? = null,
    val cargandoUbicacion: Boolean = false
)

class EncuestaViewModel : ViewModel(){
    private val _uiState = MutableStateFlow(EncuestaState())
    val uiState: StateFlow<EncuestaState> = _uiState.asStateFlow()

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


    @SuppressLint("MissingPermission")
    fun obtenerUbicacion(context: Context) {
        _uiState.update { it.copy(cargandoUbicacion = true) }
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitud = location.latitude
                    val longitud = location.longitude
                    val coords = "Lat: $latitud, Lon: $longitud"
                    _uiState.update {
                        it.copy(
                            ubicacionGps = coords,
                            cargandoUbicacion = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            ubicacionGps = "Ubicación GPS activa (Modo Emulador)",
                            cargandoUbicacion = false
                        )
                    }
                }
            }
            .addOnFailureListener {
                _uiState.update {
                    it.copy(
                        ubicacionGps = "No se pudo obtener la ubicación.",
                        cargandoUbicacion = false
                    )
                }
            }
    }



    fun registrarRespuestaSinGps(){
        _uiState.update {
            it.copy(ubicacionGps = "Ubicacion no proporcionada (permiso denegado)")
        }
    }
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