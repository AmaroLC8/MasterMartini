package com.example.mastermartini.data

import com.example.mastermartini.data.remote.CursosApiService
import com.example.mastermartini.data.remote.RetrofitClient
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.io.IOException
import kotlinx.coroutines.test.runTest
import retrofit2.Response

// Microservicio de cursos (9092) simulado: se prueban las respuestas y el respaldo local
class CapacitacionRepositoryRemotoTest : StringSpec({

    val api = mockk<CursosApiService>()
    // Se toma un curso real del catálogo local como base para las respuestas simuladas
    val base = CapacitacionRepository().obtenerCapacitaciones().first()

    beforeTest {
        clearMocks(api)
        mockkObject(RetrofitClient)
        every { RetrofitClient.cursosApi } returns api
    }

    afterTest {
        unmockkObject(RetrofitClient)
    }

    // ---------- Catálogo ----------

    "los cursos remotos sin imagen reciben la imagen de su categoría" {
        runTest {
            coEvery { api.obtenerCapacitaciones() } returns listOf(
                base.copy(id = 10, categoria = "Panadería", imagenUrl = null),
                base.copy(id = 11, categoria = "Heladería", imagenUrl = ""),
                base.copy(id = 12, categoria = "Decoración", imagenUrl = null),
                base.copy(id = 13, imagenUrl = "https://mi-imagen.jpg")
            )
            val cursos = CapacitacionRepository().obtenerCapacitacionesRemotas()

            cursos.map { it.imagenUrl } shouldBe listOf(
                DatosEmpresa.URL_IMG_PANADERIA,
                DatosEmpresa.URL_IMG_CHOCOLATERIA,
                DatosEmpresa.URL_IMG_PASTELERIA,
                "https://mi-imagen.jpg"
            )
        }
    }

    "si el servidor no tiene cursos se muestra el catálogo local" {
        runTest {
            coEvery { api.obtenerCapacitaciones() } returns emptyList()
            CapacitacionRepository().obtenerCapacitacionesRemotas() shouldHaveSize 6
        }
    }

    "sin conexión se muestra el catálogo local" {
        runTest {
            coEvery { api.obtenerCapacitaciones() } throws IOException("sin red")
            CapacitacionRepository().obtenerCapacitacionesRemotas() shouldHaveSize 6
        }
    }

    // ---------- Detalle por id ----------

    "obtenerCapacitacionPorIdRemoto devuelve el curso del servidor" {
        runTest {
            coEvery { api.obtenerCapacitacion(20) } returns base.copy(id = 20)
            CapacitacionRepository().obtenerCapacitacionPorIdRemoto(20)!!.id shouldBe 20
        }
    }

    "si el servidor responde 404 el curso no existe" {
        runTest {
            coEvery { api.obtenerCapacitacion(1) } throws errorHttp(404, cuerpoError("No existe", "NOT_FOUND"))
            CapacitacionRepository().obtenerCapacitacionPorIdRemoto(1).shouldBeNull()
        }
    }

    "si el servidor responde 500 se usa el curso local" {
        runTest {
            coEvery { api.obtenerCapacitacion(2) } throws errorHttp(500, cuerpoError("Falla", "ERROR"))
            CapacitacionRepository().obtenerCapacitacionPorIdRemoto(2)!!.id shouldBe 2
        }
    }

    "sin conexión el detalle se busca en el catálogo local" {
        runTest {
            coEvery { api.obtenerCapacitacion(3) } throws IOException("sin red")
            CapacitacionRepository().obtenerCapacitacionPorIdRemoto(3)!!.id shouldBe 3
        }
    }

    // ---------- Crear, actualizar y eliminar ----------

    "crearCapacitacion exitoso devuelve el curso creado" {
        runTest {
            coEvery { api.crearCapacitacion(2, base) } returns Response.success(base.copy(id = 30))
            val resultado = CapacitacionRepository().crearCapacitacion(2, base)
            resultado.getOrThrow().id shouldBe 30
        }
    }

    "crearCapacitacion con 403 devuelve ServidorException con el mensaje del servidor" {
        runTest {
            coEvery { api.crearCapacitacion(any(), any()) } returns
                respuestaError(403, cuerpoError("Solo instructores pueden crear cursos", "FORBIDDEN"))
            val error = CapacitacionRepository().crearCapacitacion(1, base).exceptionOrNull()

            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 403
            error.mensajeServidor shouldBe "Solo instructores pueden crear cursos"
        }
    }

    "crearCapacitacion con respuesta exitosa pero sin cuerpo se trata como error" {
        runTest {
            coEvery { api.crearCapacitacion(any(), any()) } returns Response.success(null)
            val error = CapacitacionRepository().crearCapacitacion(2, base).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.mensajeServidor.shouldBeNull()
        }
    }

    "crearCapacitacion sin conexión avisa que no hay servidor de cursos" {
        runTest {
            coEvery { api.crearCapacitacion(any(), any()) } throws IOException("sin red")
            CapacitacionRepository().crearCapacitacion(2, base)
                .exceptionOrNull()!!.message shouldBe "No se pudo conectar con el servidor de cursos (9092)"
        }
    }

    "actualizarCapacitacion exitoso devuelve el curso actualizado" {
        runTest {
            coEvery { api.actualizarCapacitacion(1, 2, any()) } returns Response.success(base.copy(titulo = "Nuevo"))
            CapacitacionRepository().actualizarCapacitacion(usuarioId = 2, id = 1, capacitacion = base)
                .getOrThrow().titulo shouldBe "Nuevo"
        }
    }

    "actualizarCapacitacion con 400 devuelve el código y mensaje del servidor" {
        runTest {
            coEvery { api.actualizarCapacitacion(any(), any(), any()) } returns
                respuestaError(400, cuerpoError("El título es obligatorio", "BAD_REQUEST"))
            val error = CapacitacionRepository().actualizarCapacitacion(2, 1, base).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 400
            error.mensajeServidor shouldBe "El título es obligatorio"
        }
    }

    "actualizarCapacitacion sin conexión devuelve un fallo" {
        runTest {
            coEvery { api.actualizarCapacitacion(any(), any(), any()) } throws IOException("sin red")
            CapacitacionRepository().actualizarCapacitacion(2, 1, base).isFailure.shouldBeTrue()
        }
    }

    "eliminarCapacitacion exitoso devuelve éxito" {
        runTest {
            coEvery { api.eliminarCapacitacion(1, 4) } returns Response.success(Unit)
            CapacitacionRepository().eliminarCapacitacion(usuarioId = 4, id = 1).isSuccess.shouldBeTrue()
        }
    }

    "eliminarCapacitacion con 404 devuelve ServidorException" {
        runTest {
            coEvery { api.eliminarCapacitacion(any(), any()) } returns
                respuestaError(404, cuerpoError("Curso no encontrado", "NOT_FOUND"))
            val error = CapacitacionRepository().eliminarCapacitacion(4, 99).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 404
        }
    }

    "eliminarCapacitacion sin conexión devuelve un fallo" {
        runTest {
            coEvery { api.eliminarCapacitacion(any(), any()) } throws IOException("sin red")
            CapacitacionRepository().eliminarCapacitacion(4, 1).isFailure.shouldBeTrue()
        }
    }
})
