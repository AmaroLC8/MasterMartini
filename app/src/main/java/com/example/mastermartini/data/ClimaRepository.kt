package com.example.mastermartini.data

import com.example.mastermartini.data.remote.RetrofitClient

// Repositorio para obtener la temperatura actual desde la API de Open-Meteo
class ClimaRepository {

    // Consulta la temperatura actual en °C o devuelve null ante errores de red
    suspend fun obtenerTemperaturaActual(): Double? {
        return try {
            val respuesta = RetrofitClient.climaApi.obtenerClimaActual()
            respuesta.climaActual?.temperature
        } catch (e: Exception) {
            null
        }
    }
}
