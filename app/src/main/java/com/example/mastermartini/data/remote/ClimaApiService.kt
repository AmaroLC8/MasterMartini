package com.example.mastermartini.data.remote

import com.example.mastermartini.model.ClimaExternoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ClimaApiService {

    // Valores por defecto: coordenadas de Santiago de Chile (latitud -33.45, longitud -70.66).
    // ClimaRepository la llama sin parámetros, así que siempre se consulta el clima de Santiago.
    @GET("v1/forecast")
    suspend fun obtenerClimaActual(
        @Query("latitude") latitud: Double = -33.45,
        @Query("longitude") longitud: Double = -70.66,
        @Query("current_weather") actual: Boolean = true
    ): ClimaExternoDto
}