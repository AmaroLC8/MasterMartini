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

data class EncuestaState(
    val interesesSeleccionados: Set<String> = emptySet(),
    val nivelExperiencia: String = "",
    // Nombre del lugar (ej. "Providencia, Región Metropolitana"). Si no se pudo obtener el nombre,
    // aquí quedan las coordenadas (ej. "Lat: -33.4489, Lon: -70.6693"). Es lo que se guarda en la encuesta.
    val ubicacionGps: String = "",
    // Coordenadas, para mostrarlas en letra chica bajo el nombre. Vacío si no hubo nombre
    // (porque las coordenadas ya están en ubicacionGps) o si la encuesta se cargó de lo guardado.
    val coordenadasGps: String = "",
    val avisoGps: String? = null,           // Aviso en rojo si no hay permiso o falla el GPS (null = sin aviso)
    val errorIntereses: String? = null,
    val errorNivel: String? = null,
    val mensajeExito: String? = null,
    val cargandoUbicacion: Boolean = false
)

// Es AndroidViewModel (y no ViewModel) porque necesita la Application para crear
// AlmacenamientoLocal y guardar las respuestas en SharedPreferences.
class EncuestaViewModel(application: Application) : AndroidViewModel(application) {

    // Acceso a SharedPreferences para guardar y leer la encuesta
    private val almacenamiento = AlmacenamientoLocal(application)

    // Convierte las coordenadas del GPS en el nombre del lugar (geocoding inverso)
    private val ubicacionRepository = UbicacionRepository(application)

    private val _uiState = MutableStateFlow(EncuestaState())
    val uiState: StateFlow<EncuestaState> = _uiState.asStateFlow()

    // Al crear el ViewModel (cada vez que se entra a la encuesta) se cargan
    // las respuestas guardadas, para que aparezcan marcadas.
    // Va DESPUÉS de _uiState: Kotlin inicializa en orden y _uiState tiene que existir antes.
    init {
        cargarEncuestaGuardada()
    }

    // Copia al estado lo que se guardó la última vez. Si nunca se respondió, no hace nada
    // y la encuesta aparece vacía. No muestra el mensaje de éxito: ese es solo al guardar.
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


    // Pide la ubicación ACTUAL al GPS (RECURSO NATIVO).
    // - getCurrentLocation pide una ubicación nueva. lastLocation solo devuelve la última
    //   que el celular tenía guardada, y en el emulador casi siempre es null.
    // - Usa getApplication() como Context: al ser AndroidViewModel no hace falta recibirlo
    //   desde la pantalla (y el ViewModel no debe guardar el Context de una Activity).
    // - La pantalla revisa el permiso antes de llamar; @SuppressLint le dice a Android Studio
    //   que ya lo sabemos, y el try/catch cubre el caso raro de que se quite el permiso justo antes.
    @SuppressLint("MissingPermission")
    fun obtenerUbicacion() {
        // Muestra la barra de carga y borra un aviso anterior
        _uiState.update { it.copy(cargandoUbicacion = true, avisoGps = null) }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>())

        try {
            // PRIORITY_HIGH_ACCURACY = usar el GPS para la mayor precisión.
            // El segundo parámetro (null) sería para cancelar la búsqueda; no lo usamos.
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // %.4f = 4 decimales (unos 11 metros de precisión).
                        // Locale.US para que use punto decimal y no coma.
                        val coords = String.format(
                            Locale.US, "Lat: %.4f, Lon: %.4f",
                            location.latitude, location.longitude
                        )
                        // Ya tenemos las coordenadas; ahora se busca el nombre del lugar.
                        // La barra de carga sigue visible mientras tanto (cargandoUbicacion = true).
                        buscarNombreLugar(location.latitude, location.longitude, coords)
                    } else {
                        // El GPS respondió pero sin ubicación (por ejemplo, GPS apagado)
                        mostrarAvisoSinUbicacion()
                    }
                }
                .addOnFailureListener {
                    // Hubo un error al pedir la ubicación
                    mostrarAvisoSinUbicacion()
                }
        } catch (e: SecurityException) {
            // Se quitó el permiso: avisamos en vez de que la app se caiga
            mostrarAvisoSinUbicacion()
        }
    }

    // GEOCODING INVERSO: pasa de coordenadas a "Comuna, Región".
    // obtenerNombreLugar es suspend (espera la respuesta de internet), por eso va en
    // viewModelScope.launch. Mientras espera, la pantalla NO se congela.
    private fun buscarNombreLugar(latitud: Double, longitud: Double, coords: String) {
        viewModelScope.launch {
            val nombreLugar = ubicacionRepository.obtenerNombreLugar(latitud, longitud)

            if (nombreLugar != null) {
                // Hay nombre: se muestra (y se guarda) el nombre, y las coordenadas van aparte en chico
                _uiState.update {
                    it.copy(
                        ubicacionGps = nombreLugar,
                        coordenadasGps = coords,
                        avisoGps = null,
                        cargandoUbicacion = false
                    )
                }
            } else {
                // Sin nombre (sin internet o sin servicio): se usan las coordenadas como antes,
                // y se avisa. La encuesta se puede guardar igual.
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

    // Aviso cuando no se pudo obtener la ubicación. No toca ubicacionGps:
    // si ya había coordenadas de antes, se mantienen.
    private fun mostrarAvisoSinUbicacion() {
        _uiState.update {
            it.copy(
                avisoGps = "No se pudo obtener tu ubicación. Revisa que el GPS esté activado.",
                cargandoUbicacion = false
            )
        }
    }

    // Se llama cuando la persona niega el permiso de ubicación.
    // Solo muestra el aviso: la ubicación es opcional y la encuesta se puede guardar igual.
    fun registrarRespuestaSinGps(){
        _uiState.update {
            it.copy(avisoGps = "Sin permiso de ubicación. Puedes guardar la encuesta igual.")
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

        // Si la encuesta está completa, se guarda ANTES de mostrar "registrada con éxito",
        // así el mensaje dice la verdad. Si no es válida, no se guarda nada.
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