package com.example.mastermartini.data

import com.example.mastermartini.data.remote.ClimaApiService
import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.model.ClimaActualDto
import com.example.mastermartini.model.ClimaExternoDto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.io.IOException
import kotlinx.coroutines.test.runTest

// API de Open-Meteo simulada: el repositorio nunca debe lanzar errores a la pantalla
class ClimaRepositoryTest : StringSpec({

    val api = mockk<ClimaApiService>()

    beforeTest {
        clearMocks(api)
        mockkObject(RetrofitClient)
        every { RetrofitClient.climaApi } returns api
    }

    afterTest {
        unmockkObject(RetrofitClient)
    }

    "devuelve la temperatura actual que entrega la API" {
        runTest {
            coEvery { api.obtenerClimaActual(any(), any(), any()) } returns
                ClimaExternoDto(climaActual = ClimaActualDto(temperature = 18.5))
            ClimaRepository().obtenerTemperaturaActual() shouldBe 18.5
        }
    }

    "devuelve null si la respuesta no trae el clima actual" {
        runTest {
            coEvery { api.obtenerClimaActual(any(), any(), any()) } returns ClimaExternoDto()
            ClimaRepository().obtenerTemperaturaActual().shouldBeNull()
        }
    }

    "devuelve null sin conexión" {
        runTest {
            coEvery { api.obtenerClimaActual(any(), any(), any()) } throws IOException("sin red")
            ClimaRepository().obtenerTemperaturaActual().shouldBeNull()
        }
    }

    "devuelve null si el servidor responde con error" {
        runTest {
            coEvery { api.obtenerClimaActual(any(), any(), any()) } throws errorHttp(503)
            ClimaRepository().obtenerTemperaturaActual().shouldBeNull()
        }
    }
})
