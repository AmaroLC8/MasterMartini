package com.example.mastermartini.data

import com.example.mastermartini.data.remote.InteraccionesApi
import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.data.remote.dto.PreguntaRequest
import com.example.mastermartini.data.remote.dto.RespuestaRequest
import com.example.mastermartini.model.Pregunta
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

// Endpoints de preguntas del microservicio de interacciones (9093) simulados con MockK
class PreguntaRepositoryTest : StringSpec({

    val api = mockk<InteraccionesApi>()
    val pregunta = Pregunta(
        id = 7, cursoId = 2, cursoTitulo = "Pan de masa madre", usuarioId = 1,
        autorNombre = "Alumno de prueba", texto = "¿Cuánto debe reposar la masa?",
        fechaPregunta = "2026-11-27T10:30:00"
    )
    val respondida = pregunta.copy(respuesta = "Unas 12 horas en frío", respondidaPor = "Instructor de prueba")
    val sinConexion = MENSAJE_SIN_CONEXION

    beforeTest {
        clearMocks(api)
        mockkObject(RetrofitClient)
        every { RetrofitClient.interaccionesApi } returns api
    }

    afterTest {
        unmockkObject(RetrofitClient)
    }

    // ---------- listarDelCurso ----------

    "listarDelCurso devuelve las preguntas del curso" {
        runTest {
            coEvery { api.preguntasDelCurso(2) } returns Response.success(listOf(respondida, pregunta))
            PreguntaRepository().listarDelCurso(2).getOrThrow() shouldBe listOf(respondida, pregunta)
        }
    }

    "listarDelCurso con 404 devuelve ServidorException con el mensaje del servidor" {
        runTest {
            coEvery { api.preguntasDelCurso(any()) } returns
                respuestaError(404, cuerpoError("Curso no encontrado", "NOT_FOUND"))
            val error = PreguntaRepository().listarDelCurso(99).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 404
            error.mensajeServidor shouldBe "Curso no encontrado"
        }
    }

    "listarDelCurso con respuesta exitosa sin cuerpo se trata como error" {
        runTest {
            coEvery { api.preguntasDelCurso(any()) } returns Response.success(null)
            PreguntaRepository().listarDelCurso(2).exceptionOrNull().shouldBeInstanceOf<ServidorException>()
        }
    }

    "listarDelCurso sin conexión avisa que no hay servidor" {
        runTest {
            coEvery { api.preguntasDelCurso(any()) } throws IOException("sin red")
            PreguntaRepository().listarDelCurso(2).exceptionOrNull()!!.message shouldBe sinConexion
        }
    }

    // ---------- preguntar ----------

    "preguntar envía el texto con el usuario en el header" {
        runTest {
            coEvery { api.preguntar(2, 1, PreguntaRequest(texto = "¿Cuánto debe reposar la masa?")) } returns
                Response.success(pregunta)
            PreguntaRepository().preguntar(usuarioId = 1, cursoId = 2, texto = "¿Cuánto debe reposar la masa?")
                .getOrThrow() shouldBe pregunta
        }
    }

    "preguntar sin estar inscrito (403) devuelve el mensaje del servidor" {
        runTest {
            coEvery { api.preguntar(any(), any(), any()) } returns
                respuestaError(403, cuerpoError("Debes inscribirte en el curso para hacer preguntas", "FORBIDDEN"))
            val error = PreguntaRepository().preguntar(1, 2, "Hola").exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 403
            error.mensajeServidor shouldBe "Debes inscribirte en el curso para hacer preguntas"
        }
    }

    "preguntar con respuesta exitosa sin cuerpo se trata como error" {
        runTest {
            coEvery { api.preguntar(any(), any(), any()) } returns Response.success(null)
            val error = PreguntaRepository().preguntar(1, 2, "Hola").exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.mensajeServidor.shouldBeNull()
        }
    }

    "preguntar sin conexión devuelve un fallo" {
        runTest {
            coEvery { api.preguntar(any(), any(), any()) } throws IOException("sin red")
            PreguntaRepository().preguntar(1, 2, "Hola").exceptionOrNull()!!.message shouldBe sinConexion
        }
    }

    // ---------- pendientes ----------

    "pendientes devuelve las preguntas sin responder" {
        runTest {
            coEvery { api.preguntasPendientes(3) } returns Response.success(listOf(pregunta))
            PreguntaRepository().pendientes(usuarioId = 3).getOrThrow() shouldBe listOf(pregunta)
        }
    }

    "pendientes para un rol sin permiso (403) devuelve ServidorException" {
        runTest {
            coEvery { api.preguntasPendientes(any()) } returns
                respuestaError(403, cuerpoError("No tienes permiso", "FORBIDDEN"))
            val error = PreguntaRepository().pendientes(1).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 403
        }
    }

    "pendientes con respuesta exitosa sin cuerpo se trata como error" {
        runTest {
            coEvery { api.preguntasPendientes(any()) } returns Response.success(null)
            PreguntaRepository().pendientes(3).exceptionOrNull().shouldBeInstanceOf<ServidorException>()
        }
    }

    "pendientes sin conexión devuelve un fallo" {
        runTest {
            coEvery { api.preguntasPendientes(any()) } throws IOException("sin red")
            PreguntaRepository().pendientes(3).exceptionOrNull()!!.message shouldBe sinConexion
        }
    }

    // ---------- responder ----------

    "responder envía la respuesta y devuelve la pregunta respondida" {
        runTest {
            coEvery { api.responderPregunta(7, 3, RespuestaRequest(respuesta = "Unas 12 horas en frío")) } returns
                Response.success(respondida)
            val resultado = PreguntaRepository().responder(usuarioId = 3, preguntaId = 7, respuesta = "Unas 12 horas en frío")
            resultado.getOrThrow().estaRespondida.shouldBeTrue()
        }
    }

    "responder una pregunta de otro curso (403) devuelve ServidorException" {
        runTest {
            coEvery { api.responderPregunta(any(), any(), any()) } returns
                respuestaError(403, cuerpoError("No tienes permiso", "FORBIDDEN"))
            val error = PreguntaRepository().responder(3, 7, "Respuesta").exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.mensajeServidor shouldBe "No tienes permiso"
        }
    }

    "responder con respuesta exitosa sin cuerpo se trata como error" {
        runTest {
            coEvery { api.responderPregunta(any(), any(), any()) } returns Response.success(null)
            PreguntaRepository().responder(3, 7, "Respuesta").exceptionOrNull().shouldBeInstanceOf<ServidorException>()
        }
    }

    "responder sin conexión devuelve un fallo" {
        runTest {
            coEvery { api.responderPregunta(any(), any(), any()) } throws IOException("sin red")
            PreguntaRepository().responder(3, 7, "Respuesta").exceptionOrNull()!!.message shouldBe sinConexion
        }
    }

    // ---------- eliminar ----------

    "eliminar exitoso devuelve éxito" {
        runTest {
            coEvery { api.eliminarPregunta(7, 1) } returns Response.success(Unit)
            PreguntaRepository().eliminar(usuarioId = 1, preguntaId = 7).isSuccess.shouldBeTrue()
        }
    }

    "eliminar con 500 sin cuerpo devuelve ServidorException sin mensaje" {
        runTest {
            coEvery { api.eliminarPregunta(any(), any()) } returns respuestaError(500)
            val error = PreguntaRepository().eliminar(1, 7).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 500
            error.mensajeServidor.shouldBeNull()
        }
    }

    "si la corrutina se cancela, ninguna función lo trata como error de conexión" {
        runTest {
            coEvery { api.preguntasDelCurso(any()) } throws CancellationException("pantalla cerrada")
            coEvery { api.preguntar(any(), any(), any()) } throws CancellationException("pantalla cerrada")
            coEvery { api.preguntasPendientes(any()) } throws CancellationException("pantalla cerrada")
            coEvery { api.responderPregunta(any(), any(), any()) } throws CancellationException("pantalla cerrada")
            coEvery { api.eliminarPregunta(any(), any()) } throws CancellationException("pantalla cerrada")
            shouldThrow<CancellationException> { PreguntaRepository().listarDelCurso(2) }
            shouldThrow<CancellationException> { PreguntaRepository().preguntar(1, 2, "Hola") }
            shouldThrow<CancellationException> { PreguntaRepository().pendientes(3) }
            shouldThrow<CancellationException> { PreguntaRepository().responder(3, 7, "Respuesta") }
            shouldThrow<CancellationException> { PreguntaRepository().eliminar(1, 7) }
        }
    }

    "eliminar sin conexión devuelve un fallo" {
        runTest {
            coEvery { api.eliminarPregunta(any(), any()) } throws IOException("sin red")
            PreguntaRepository().eliminar(1, 7).exceptionOrNull()!!.message shouldBe sinConexion
        }
    }
})
