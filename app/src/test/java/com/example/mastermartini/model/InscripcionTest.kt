package com.example.mastermartini.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class InscripcionTest : StringSpec({

    "claveLeccion arma la clave como modulo-leccion" {
        Inscripcion.claveLeccion(modulo = 2, leccion = 3) shouldBe "2-3"
    }

    "estaCompletada es verdadero solo para las lecciones marcadas" {
        val inscripcion = Inscripcion(leccionesCompletadas = listOf("1-1", "1-2"))
        inscripcion.estaCompletada(modulo = 1, leccion = 2).shouldBeTrue()
        inscripcion.estaCompletada(modulo = 2, leccion = 1).shouldBeFalse()
    }

    "porcentaje devuelve 0 si el curso no tiene lecciones" {
        val inscripcion = Inscripcion(leccionesCompletadas = listOf("1-1"))
        inscripcion.porcentaje(totalLecciones = 0) shouldBe 0
    }

    "porcentaje calcula el avance según las lecciones completadas" {
        Inscripcion().porcentaje(totalLecciones = 4) shouldBe 0
        Inscripcion(leccionesCompletadas = listOf("1-1")).porcentaje(totalLecciones = 4) shouldBe 25
        Inscripcion(leccionesCompletadas = listOf("1-1", "1-2", "2-1"))
            .porcentaje(totalLecciones = 3) shouldBe 100
    }

    "porcentaje nunca pasa de 100 aunque haya más completadas que lecciones" {
        val inscripcion = Inscripcion(leccionesCompletadas = listOf("1-1", "1-2", "1-3"))
        inscripcion.porcentaje(totalLecciones = 2) shouldBe 100
    }
})
