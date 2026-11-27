package com.example.mastermartini.data

import com.example.mastermartini.data.remote.InteraccionesApi
import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.data.remote.dto.InscripcionRequest
import com.example.mastermartini.data.remote.dto.ProgresoLeccionRequest
import com.example.mastermartini.model.Inscripcion
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
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
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import retrofit2.Response

// Microservicio de interacciones (9093) simulado con MockK
class InscripcionRepositoryTest : StringSpec({

    val api = mockk<InteraccionesApi>()
    val inscripcion = Inscripcion(id = 10, usuarioId = 1, cursoId = 2, leccionesCompletadas = listOf("1-1"))
    val sinConexion = MENSAJE_SIN_CONEXION

    beforeTest {
        clearMocks(api)
        mockkObject(RetrofitClient)
        every { RetrofitClient.interaccionesApi } returns api
    }

    afterTest {
        unmockkObject(RetrofitClient)
    }

    // ---------- inscribirse ----------

    "inscribirse exitoso devuelve la inscripción creada" {
        runTest {
            coEvery { api.inscribirse(1, InscripcionRequest(cursoId = 2)) } returns Response.success(inscripcion)
            InscripcionRepository().inscribirse(usuarioId = 1, cursoId = 2).getOrThrow() shouldBe inscripcion
        }
    }

    "inscribirse dos veces (409) devuelve ServidorException con el mensaje del servidor" {
        runTest {
            coEvery { api.inscribirse(any(), any()) } returns
                respuestaError(409, cuerpoError("Ya estás inscrito en este curso", "CONFLICT"))
            val error = InscripcionRepository().inscribirse(1, 2).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 409
            error.mensajeServidor shouldBe "Ya estás inscrito en este curso"
        }
    }

    "inscribirse con respuesta exitosa sin cuerpo se trata como error" {
        runTest {
            coEvery { api.inscribirse(any(), any()) } returns Response.success(null)
            val error = InscripcionRepository().inscribirse(1, 2).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.mensajeServidor.shouldBeNull()
        }
    }

    "inscribirse sin conexión avisa que no hay servidor de inscripciones" {
        runTest {
            coEvery { api.inscribirse(any(), any()) } throws IOException("sin red")
            InscripcionRepository().inscribirse(1, 2).exceptionOrNull()!!.message shouldBe sinConexion
        }
    }

    // ---------- misInscripciones ----------

    "misInscripciones devuelve la lista del usuario" {
        runTest {
            coEvery { api.misInscripciones(usuarioId = 1, idSolicitante = 1) } returns
                Response.success(listOf(inscripcion))
            InscripcionRepository().misInscripciones(1).getOrThrow() shouldBe listOf(inscripcion)
        }
    }

    "misInscripciones con 403 devuelve ServidorException" {
        runTest {
            coEvery { api.misInscripciones(any(), any()) } returns
                respuestaError(403, cuerpoError("Sin permiso", "FORBIDDEN"))
            val error = InscripcionRepository().misInscripciones(1).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 403
        }
    }

    "misInscripciones sin conexión devuelve un fallo" {
        runTest {
            coEvery { api.misInscripciones(any(), any()) } throws IOException("sin red")
            InscripcionRepository().misInscripciones(1).exceptionOrNull()!!.message shouldBe sinConexion
        }
    }

    // ---------- marcarLeccion ----------

    "marcarLeccion envía el módulo, la lección y el estado" {
        runTest {
            val cuerpo = ProgresoLeccionRequest(modulo = 1, leccion = 2, completada = true)
            val actualizada = inscripcion.copy(leccionesCompletadas = listOf("1-1", "1-2"))
            coEvery { api.marcarLeccion(10, 1, cuerpo) } returns Response.success(actualizada)

            val resultado = InscripcionRepository().marcarLeccion(
                usuarioId = 1, inscripcionId = 10, modulo = 1, leccion = 2, completada = true
            )
            resultado.getOrThrow().estaCompletada(1, 2).shouldBeTrue()
        }
    }

    "marcarLeccion con 404 devuelve ServidorException" {
        runTest {
            coEvery { api.marcarLeccion(any(), any(), any()) } returns
                respuestaError(404, cuerpoError("Inscripción no encontrada", "NOT_FOUND"))
            val error = InscripcionRepository().marcarLeccion(1, 99, 1, 1, true).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.mensajeServidor shouldBe "Inscripción no encontrada"
        }
    }

    "marcarLeccion sin conexión devuelve un fallo" {
        runTest {
            coEvery { api.marcarLeccion(any(), any(), any()) } throws IOException("sin red")
            InscripcionRepository().marcarLeccion(1, 10, 1, 1, true).isFailure.shouldBeTrue()
        }
    }

    // ---------- cancelar ----------

    "cancelar exitoso devuelve éxito" {
        runTest {
            coEvery { api.cancelar(10, 1) } returns Response.success(Unit)
            InscripcionRepository().cancelar(usuarioId = 1, inscripcionId = 10).isSuccess.shouldBeTrue()
        }
    }

    "cancelar con 500 sin cuerpo devuelve ServidorException sin mensaje" {
        runTest {
            coEvery { api.cancelar(any(), any()) } returns respuestaError(500)
            val error = InscripcionRepository().cancelar(1, 10).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 500
            error.mensajeServidor.shouldBeNull()
        }
    }

    "si la corrutina se cancela, inscribirse y cancelar no lo tratan como error de conexión" {
        runTest {
            coEvery { api.inscribirse(any(), any()) } throws CancellationException("pantalla cerrada")
            coEvery { api.misInscripciones(any(), any()) } throws CancellationException("pantalla cerrada")
            coEvery { api.marcarLeccion(any(), any(), any()) } throws CancellationException("pantalla cerrada")
            coEvery { api.cancelar(any(), any()) } throws CancellationException("pantalla cerrada")
            shouldThrow<CancellationException> { InscripcionRepository().inscribirse(1, 2) }
            shouldThrow<CancellationException> { InscripcionRepository().misInscripciones(1) }
            shouldThrow<CancellationException> { InscripcionRepository().marcarLeccion(1, 10, 1, 1, true) }
            shouldThrow<CancellationException> { InscripcionRepository().cancelar(1, 10) }
        }
    }

    "cancelar sin conexión devuelve un fallo" {
        runTest {
            coEvery { api.cancelar(any(), any()) } throws IOException("sin red")
            InscripcionRepository().cancelar(1, 10).exceptionOrNull()!!.message shouldBe sinConexion
        }
    }
})
