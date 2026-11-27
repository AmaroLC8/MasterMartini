package com.example.mastermartini.model

import com.google.gson.Gson
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

// Revisa que @SerializedName lea bien los nombres de campo de las APIs externas
class DtosExternosTest : StringSpec({

    "CloudinaryResponse lee secure_url como secureUrl" {
        val json = """{"secure_url":"https://res.cloudinary.com/video.mp4"}"""
        Gson().fromJson(json, CloudinaryResponse::class.java).secureUrl shouldBe
            "https://res.cloudinary.com/video.mp4"
    }

    "ClimaExternoDto lee current_weather como climaActual" {
        val json = """{"current_weather":{"temperature":21.3,"windspeed":5.0,"weathercode":1}}"""
        val clima = Gson().fromJson(json, ClimaExternoDto::class.java).climaActual!!
        clima.temperature shouldBe 21.3
        clima.weathercode shouldBe 1
    }
})
