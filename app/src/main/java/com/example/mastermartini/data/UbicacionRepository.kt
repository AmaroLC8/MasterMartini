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

// Repositorio de geocodificación inversa para resolver comuna y región desde coordenadas
class UbicacionRepository(private val context: Context) {

    // Resuelve el nombre de comuna y región con timeout de 8 segundos
    suspend fun obtenerNombreLugar(latitud: Double, longitud: Double): String? {
        if (!Geocoder.isPresent()) {
            return null
        }

        val geocoder = Geocoder(context, Locale.forLanguageTag("es-CL"))

        return try {
            val direcciones = withTimeoutOrNull(8000) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    buscarConListener(geocoder, latitud, longitud)
                } else {
                    buscarFormaAntigua(geocoder, latitud, longitud)
                }
            }
            armarNombre(direcciones?.firstOrNull())
        } catch (e: Exception) {
            null
        }
    }

    // Geocodificación asíncrona mediante callback para Android 13+
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun buscarConListener(
        geocoder: Geocoder,
        latitud: Double,
        longitud: Double
    ): List<Address> = suspendCancellableCoroutine { continuacion ->
        geocoder.getFromLocation(latitud, longitud, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                continuacion.resume(addresses)
            }

            override fun onError(errorMessage: String?) {
                continuacion.resume(emptyList())
            }
        })
    }

    // Geocodificación bloqueante en Dispatchers.IO para Android 12 o inferior
    @Suppress("DEPRECATION")
    private suspend fun buscarFormaAntigua(
        geocoder: Geocoder,
        latitud: Double,
        longitud: Double
    ): List<Address> = withContext(Dispatchers.IO) {
        geocoder.getFromLocation(latitud, longitud, 1) ?: emptyList()
    }

    private fun armarNombre(direccion: Address?): String? {
        if (direccion == null) {
            return null
        }

        val comuna = direccion.subLocality ?: direccion.locality
        val region = direccion.adminArea

        return if (comuna != null && region != null) {
            "$comuna, $region"
        } else if (comuna != null) {
            comuna
        } else if (region != null) {
            region
        } else {
            null
        }
    }
}
