package com.example.mastermartini.data

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

// REPOSITORIO DE UBICACIÓN (GEOCODING INVERSO)
// Convierte coordenadas (ej. -33.4263, -70.6200) en el NOMBRE del lugar
// (ej. "Providencia, Región Metropolitana"). A esto se le llama "geocoding inverso":
// el geocoding normal va de dirección -> coordenadas; el inverso va de coordenadas -> dirección.
// Usa Geocoder, que viene con Android: le pregunta a un servicio de Google por internet.
class UbicacionRepository(private val context: Context) {

    // Devuelve "Comuna, Región" o null si no se pudo (sin internet, sin servicio,
    // o el servicio no conoce ese punto). Nunca lanza un error: la app no se cae.
    suspend fun obtenerNombreLugar(latitud: Double, longitud: Double): String? {
        // Algunos celulares (o emuladores sin Google) no traen el servicio de Geocoder.
        // isPresent() lo revisa antes, para no esperar una respuesta que nunca va a llegar.
        if (!Geocoder.isPresent()) {
            return null
        }

        // Locale es-CL: pide los nombres en español de Chile ("Región Metropolitana",
        // y no "Santiago Metropolitan Region").
        // Se usa forLanguageTag("es-CL") y no Locale("es", "CL") porque ese constructor está deprecado.
        val geocoder = Geocoder(context, Locale.forLanguageTag("es-CL"))

        return try {
            // withTimeoutOrNull: si el servicio no responde en 8 segundos, se rinde y
            // devuelve null. Así la barra de carga de la encuesta no queda girando para siempre.
            val direcciones = withTimeoutOrNull(8000) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13 o más (API 33+): forma nueva, con listener
                    buscarConListener(geocoder, latitud, longitud)
                } else {
                    // Android 12 o menos: forma antigua
                    buscarFormaAntigua(geocoder, latitud, longitud)
                }
            }
            // firstOrNull(): la primera dirección encontrada (la más cercana), o null si la lista está vacía.
            // El "?." es por si withTimeoutOrNull devolvió null (se acabó el tiempo).
            armarNombre(direcciones?.firstOrNull())
        } catch (e: Exception) {
            // IOException si no hay internet, IllegalArgumentException si las coordenadas
            // están fuera de rango, etc. En todos los casos: no hay nombre.
            null
        }
    }

    // ANDROID 13+ (API 33): getFromLocation con un "listener".
    // Esta versión NO bloquea: pide la dirección y Android llama a onGeocode (o a onError)
    // cuando tiene la respuesta, más tarde.
    // suspendCancellableCoroutine "convierte" ese aviso en una función suspend:
    // la corrutina se PAUSA aquí hasta que se llama a continuacion.resume(...), y ese
    // valor es lo que devuelve la función. Así el ViewModel la usa como cualquier otra suspend.
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun buscarConListener(
        geocoder: Geocoder,
        latitud: Double,
        longitud: Double
    ): List<Address> = suspendCancellableCoroutine { continuacion ->
        // 1 = queremos como máximo 1 dirección
        geocoder.getFromLocation(latitud, longitud, 1, object : Geocoder.GeocodeListener {
            // Llegó la respuesta: se "despierta" la corrutina con la lista
            override fun onGeocode(addresses: MutableList<Address>) {
                continuacion.resume(addresses)
            }

            // Hubo un error (ej. sin internet): se despierta con una lista vacía
            override fun onError(errorMessage: String?) {
                continuacion.resume(emptyList())
            }
        })
    }

    // ANDROID 12 O MENOS: getFromLocation "normal", que devuelve la lista directo.
    // Está DEPRECADA desde Android 13 porque BLOQUEA el hilo mientras espera la respuesta
    // de internet (si se usara en el hilo principal, la pantalla se congelaría).
    // Igual hay que usarla aquí, porque en Android 12 o menos la versión con listener NO EXISTE.
    // Por eso: withContext(Dispatchers.IO) la corre en un hilo aparte (no se congela la pantalla),
    // y @Suppress("DEPRECATION") le dice a Android Studio que ya sabemos que está deprecada.
    @Suppress("DEPRECATION")
    private suspend fun buscarFormaAntigua(
        geocoder: Geocoder,
        latitud: Double,
        longitud: Double
    ): List<Address> = withContext(Dispatchers.IO) {
        // Puede devolver null: en ese caso, lista vacía
        geocoder.getFromLocation(latitud, longitud, 1) ?: emptyList()
    }

    // Arma el texto "Comuna, Región" a partir de una dirección.
    // - subLocality: el sector dentro de la ciudad (a veces es la comuna, ej. "Providencia").
    // - locality: la ciudad o comuna (ej. "Santiago"). Se usa si no hay subLocality.
    // - adminArea: la región (ej. "Región Metropolitana").
    // Cualquiera de los tres puede venir vacío (null), por eso se revisan uno por uno.
    private fun armarNombre(direccion: Address?): String? {
        if (direccion == null) {
            return null
        }

        // ?: (Elvis): si subLocality es null, usa locality
        val comuna = direccion.subLocality ?: direccion.locality
        val region = direccion.adminArea

        return if (comuna != null && region != null) {
            "$comuna, $region"
        } else if (comuna != null) {
            comuna
        } else if (region != null) {
            region
        } else {
            // La dirección no traía ni comuna ni región
            null
        }
    }
}
