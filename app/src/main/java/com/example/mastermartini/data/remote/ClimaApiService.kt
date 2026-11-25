package com.example.mastermartini.data.remote

import com.example.mastermartini.model.ClimaExternoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ClimaApiService {

    @GET("v1/forecast")
    suspend fun obtenerClimaActual(
        @Query("latitude") latitud: Double = -33.45,
        @Query("longitude") longitud: Double = -70.66,
        @Query("current_weather") actual: Boolean = true
    ): ClimaExternoDto
}