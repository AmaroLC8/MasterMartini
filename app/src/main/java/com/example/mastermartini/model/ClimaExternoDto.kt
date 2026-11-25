package com.example.mastermartini.model

import com.google.gson.annotations.SerializedName

data class ClimaExternoDto(
    @SerializedName("current_weather")
    val climaActual: ClimaActualDto? = null
)

data class ClimaActualDto(
    val temperature: Double = 0.0,
    val windspeed: Double = 0.0,
    val weathercode: Int = 0
)