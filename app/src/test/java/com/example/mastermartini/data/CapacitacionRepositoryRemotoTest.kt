package com.example.mastermartini.data

import com.example.mastermartini.data.remote.CursosApiService
import com.example.mastermartini.data.remote.RetrofitClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
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

// Microservicio de cursos simulado: la app ya no tiene catálogo local de respaldo
class CapacitacionRepositoryRemotoTest : StringSpec({

    val api = mockk<CursosApiService>()
    val base = cursoGratisDePrueba

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
            coEvery { api.obtenerCapacitaciones() } returns Response.success(
                listOf(
                    base.copy(id = 10, categoria = "Panadería", imagenUrl = null),
                    base.copy(id = 11, categoria = "Heladería", imagenUrl = ""),
                    base.copy(id = 12, categoria = "Decoración", imagenUrl = null),
                    base.copy(id = 13, imagenUrl = "https://mi-imagen.jpg")
                )
            )
            val cursos = CapacitacionRepository().obtenerCapacitacionesRemotas().getOrThrow()

            cursos.map { it.imagenUrl } shouldBe listOf(
                DatosEmpresa.URL_IMG_PANADERIA,
                DatosEmpresa.URL_IMG_CHOCOLATERIA,
                DatosEmpresa.URL_IMG_PASTELERIA,
                "https://mi-imagen.jpg"
            )
        }
    }

    "si el servidor no tiene cursos devuelve una lista vacía (sin datos locales)" {
        runTest {
            coEvery { api.obtenerCapacitaciones() } returns Response.success(emptyList())
            CapacitacionRepository().obtenerCapacitacionesRemotas().getOrThrow().shouldBeEmpty()
        }
    }

    "si el servidor responde 500 el catálogo devuelve ServidorException" {
        runTest {
            coEvery { api.obtenerCapacitaciones() } returns respuestaError(500, cuerpoError("Falla interna"))
            val error = CapacitacionRepository().obtenerCapacitacionesRemotas().exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 500
            error.mensajeServidor shouldBe "Falla interna"
        }
    }

    "un catálogo exitoso sin cuerpo se trata como error del servidor" {
        runTest {
            coEvery { api.obtenerCapacitaciones() } returns Response.success(null)
            CapacitacionRepository().obtenerCapacitacionesRemotas().exceptionOrNull()
                .shouldBeInstanceOf<ServidorException>()
        }
    }

    "sin conexión el catálogo devuelve el mensaje compartido de conexión" {
        runTest {
            coEvery { api.obtenerCapacitaciones() } throws IOException("sin red")
            CapacitacionRepository().obtenerCapacitacionesRemotas()
                .exceptionOrNull()!!.message shouldBe MENSAJE_SIN_CONEXION
        }
    }

    "si la corrutina se cancela, la cancelación no se convierte en error de conexión" {
        runTest {
            coEvery { api.obtenerCapacitaciones() } throws CancellationException("pantalla cerrada")
            shouldThrow<CancellationException> {
                CapacitacionRepository().obtenerCapacitacionesRemotas()
            }
        }
    }

    // ---------- Detalle por id ----------

    "obtenerCapacitacionPorIdRemoto devuelve el curso del servidor con su imagen" {
        runTest {
            coEvery { api.obtenerCapacitacion(20) } returns Response.success(base.copy(id = 20, imagenUrl = null))
            val curso = CapacitacionRepository().obtenerCapacitacionPorIdRemoto(20).getOrThrow()
            curso.id shouldBe 20
            curso.imagenUrl shouldBe DatosEmpresa.URL_IMG_CHOCOLATERIA
        }
    }

    "si el servidor responde 404 se distingue como curso no encontrado" {
        runTest {
            coEvery { api.obtenerCapacitacion(1) } returns
                respuestaError(404, cuerpoError("Curso no encontrado", "NOT_FOUND"))
            val error = CapacitacionRepository().obtenerCapacitacionPorIdRemoto(1).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.codigo shouldBe 404
            error.mensajeServidor shouldBe "Curso no encontrado"
        }
    }

    "un detalle exitoso sin cuerpo se trata como error del servidor" {
        runTest {
            coEvery { api.obtenerCapacitacion(2) } returns Response.success(null)
            val error = CapacitacionRepository().obtenerCapacitacionPorIdRemoto(2).exceptionOrNull()
            error.shouldBeInstanceOf<ServidorException>()
            error.mensajeServidor.shouldBeNull()
        }
    }

    "sin conexión el detalle devuelve el mensaje compartido de conexión" {
        runTest {
            coEvery { api.obtenerCapacitacion(3) } throws IOException("sin red")
            val error = CapacitacionRepository().obtenerCapacitacionPorIdRemoto(3).exceptionOrNull()
            error.shouldBeInstanceOf<Exception>()
            (error is ServidorException) shouldBe false
            error.message shouldBe MENSAJE_SIN_CONEXION
        }
    }

    "el detalle también deja pasar la cancelación" {
        runTest {
            coEvery { api.obtenerCapacitacion(3) } throws CancellationException("pantalla cerrada")
            shouldThrow<CancellationException> {
                CapacitacionRepository().obtenerCapacitacionPorIdRemoto(3)
            }
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

    "crearCapacitacion sin conexión devuelve el mensaje compartido" {
        runTest {
            coEvery { api.crearCapacitacion(any(), any()) } throws IOException("sin red")
            CapacitacionRepository().crearCapacitacion(2, base)
                .exceptionOrNull()!!.message shouldBe MENSAJE_SIN_CONEXION
        }
    }

    "crearCapacitacion deja pasar la cancelación" {
        runTest {
            coEvery { api.crearCapacitacion(any(), any()) } throws CancellationException("cancelado")
            shouldThrow<CancellationException> { CapacitacionRepository().crearCapacitacion(2, base) }
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

    "actualizarCapacitacion deja pasar la cancelación" {
        runTest {
            coEvery { api.actualizarCapacitacion(any(), any(), any()) } throws CancellationException("cancelado")
            shouldThrow<CancellationException> { CapacitacionRepository().actualizarCapacitacion(2, 1, base) }
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

    "eliminarCapacitacion deja pasar la cancelación" {
        runTest {
            coEvery { api.eliminarCapacitacion(any(), any()) } throws CancellationException("cancelado")
            shouldThrow<CancellationException> { CapacitacionRepository().eliminarCapacitacion(4, 1) }
        }
    }
})
