package com.example.mastermartini.data

import com.example.mastermartini.data.remote.RetrofitClient

// REPOSITORIO DEL CLIMA (API externa Open-Meteo, gratuita y sin clave)
// Es el único lugar de la app que habla con RetrofitClient.climaApi.
// El ViewModel no sabe que existe Retrofit ni el JSON de Open-Meteo (MVVM):
// solo pide la temperatura. Si mañana se cambia de API del clima, solo cambia esta clase.
class ClimaRepository {

    // Devuelve la temperatura actual en °C, o null si no se pudo obtener.
    // Usa las coordenadas de Santiago (-33.45, -70.66), que son los valores por defecto
    // de ClimaApiService.obtenerClimaActual(), por eso aquí se llama sin parámetros.
    // suspend: la llamada a internet es lenta, se espera dentro de una corrutina.
    suspend fun obtenerTemperaturaActual(): Double? {
        // try/catch: sin internet o con la API caída, Retrofit lanza una excepción.
        // Se atrapa y se devuelve null para que la app NUNCA se caiga;
        // el ViewModel, al recibir null, simplemente no muestra la tarjeta del clima.
        return try {
            val respuesta = RetrofitClient.climaApi.obtenerClimaActual()
            // climaActual puede venir null si el JSON no trae "current_weather"
            respuesta.climaActual?.temperature
        } catch (e: Exception) {
            null
        }
    }
}
