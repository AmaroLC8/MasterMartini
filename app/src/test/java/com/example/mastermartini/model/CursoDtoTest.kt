package com.example.mastermartini.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class CursoDtoTest : StringSpec({

    "toCurso copia los datos que llegan del microservicio" {
        val dto = CursoDto(
            id = 7,
            titulo = "Brioche",
            descripcion = "Pan dulce",
            categoria = "Panadería",
            nivel = "Básico",
            instructor = "Chef Ana",
            duracionTotal = "40 min",
            insumoPrincipal = "Master Export Horneo",
            videoResumenUrl = "https://video/resumen.mp4",
            esVegano = true,
            loQueLograras = listOf("Rinde 12 unidades"),
            incluye = listOf("500 g de harina"),
            modulos = listOf(
                ModuloDto(
                    titulo = "M1",
                    lecciones = listOf(
                        LeccionDto(titulo = "Amasado", descripcion = "Amasa 10 minutos"),
                        LeccionDto(titulo = "Horneado", descripcion = null)
                    )
                )
            )
        )

        val curso = dto.toCurso()

        curso.id shouldBe 7
        curso.titulo shouldBe "Brioche"
        curso.insumo shouldBe "Master Export Horneo"
        curso.chef shouldBe "Chef Ana"
        curso.duracion shouldBe "40 min"
        curso.categoria shouldBe "Panadería"
        curso.dificultad shouldBe "Básico"
        curso.rendimiento shouldBe "Rinde 12 unidades"
        curso.ingredientes shouldContainExactly listOf("500 g de harina")
        curso.esVegano shouldBe true
        curso.videoUrl shouldBe "https://video/resumen.mp4"
        // Si la lección no tiene descripción se usa su título como paso
        curso.pasos shouldContainExactly listOf("Amasa 10 minutos", "Horneado")
    }

    "toCurso usa valores por defecto cuando el servidor no manda datos" {
        val curso = CursoDto(id = 3).toCurso()

        curso.titulo shouldBe "Curso Master Martini"
        curso.insumo shouldBe "Insumo Master Martini"
        curso.chef shouldBe "Instructor Master Martini"
        curso.duracion shouldBe "20 min"
        curso.categoria shouldBe "Pastelería"
        curso.descripcion shouldBe ""
        curso.dificultad shouldBe "Intermedio"
        curso.rendimiento shouldBe "Uso profesional B2B"
        curso.ingredientes.shouldBeEmpty()
        curso.videoUrl.shouldBeNull()
        curso.pasos shouldContainExactly
            listOf("Revisa la ficha técnica y sigue las instrucciones del video.")
    }

    "toCurso toma el video de la primera lección si no hay video de resumen" {
        val dto = CursoDto(
            modulos = listOf(
                ModuloDto(lecciones = listOf(LeccionDto(videoUrl = "https://video/leccion1.mp4")))
            )
        )
        dto.toCurso().videoUrl shouldBe "https://video/leccion1.mp4"
    }
})
