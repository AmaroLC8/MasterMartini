package com.example.mastermartini.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class CapacitacionTest : StringSpec({

    val leccion15 = Leccion(titulo = "A", descripcion = "a", duracion = "15 min")
    val leccion10 = Leccion(titulo = "B", descripcion = "b", duracion = "10 min")
    val leccion5 = Leccion(titulo = "C", descripcion = "c", duracion = "5 min")

    "Leccion.minutos lee el número de la duración" {
        leccion15.minutos shouldBe 15
    }

    "Leccion.minutos es 0 si la duración no tiene números" {
        Leccion(titulo = "X", descripcion = "x", duracion = "sin dato").minutos shouldBe 0
        Leccion(titulo = "X", descripcion = "x", duracion = "").minutos shouldBe 0
    }

    "Modulo.totalMinutos suma los minutos de sus lecciones" {
        Modulo(titulo = "M1", lecciones = listOf(leccion15, leccion10)).totalMinutos shouldBe 25
        Modulo(titulo = "Vacío", lecciones = emptyList()).totalMinutos shouldBe 0
    }

    "Capacitacion.totalLecciones cuenta las lecciones de todos los módulos" {
        val capacitacion = Capacitacion(
            id = 1, titulo = "Curso", descripcion = "", categoria = "Pastelería",
            nivel = "Básico", esGratis = true, instructor = "Chef", duracionTotal = "30 min",
            insumoPrincipal = "Bravo Crem", loQueLograras = emptyList(), incluye = emptyList(),
            modulos = listOf(
                Modulo(titulo = "M1", lecciones = listOf(leccion15, leccion10)),
                Modulo(titulo = "M2", lecciones = listOf(leccion5))
            )
        )
        capacitacion.totalLecciones shouldBe 3
        capacitacion.copy(modulos = emptyList()).totalLecciones shouldBe 0
    }
})
