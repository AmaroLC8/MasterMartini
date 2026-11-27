package com.example.mastermartini.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class PreguntaTest : StringSpec({

    "una pregunta sin respuesta no está respondida" {
        Pregunta(texto = "¿Puedo usar harina integral?").estaRespondida.shouldBeFalse()
        Pregunta(respuesta = "   ").estaRespondida.shouldBeFalse()
    }

    "una pregunta con respuesta está respondida" {
        Pregunta(respuesta = "Sí, agrega un poco más de agua").estaRespondida.shouldBeTrue()
    }

    "fechaLegible convierte la fecha ISO del servidor" {
        Pregunta(fechaPregunta = "2026-11-27T10:30:00").fechaLegible shouldBe "27 nov 2026"
        Pregunta(fechaPregunta = "2026-01-05T08:00:00").fechaLegible shouldBe "5 ene 2026"
    }

    "formatearFecha devuelve texto vacío si la fecha no sirve" {
        Pregunta.formatearFecha(null) shouldBe ""
        Pregunta.formatearFecha("2026-11") shouldBe ""
        Pregunta.formatearFecha("2026-13-01T00:00:00") shouldBe ""
        Pregunta.formatearFecha("abcd-ef-ghT00:00:00") shouldBe ""
    }
})
