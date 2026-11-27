package com.example.mastermartini.data

import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.data.remote.UsuarioApi
import com.example.mastermartini.data.remote.dto.ActualizarPerfilRequest
import com.example.mastermartini.data.remote.dto.CambiarRolRequest
import com.example.mastermartini.data.remote.dto.LoginRequest
import com.example.mastermartini.data.remote.dto.MensajeDto
import com.example.mastermartini.data.remote.dto.PlanProRequest
import com.example.mastermartini.data.remote.dto.RegistroRequest
import com.example.mastermartini.data.remote.dto.UsuarioDto
import com.example.mastermartini.model.Rol
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import retrofit2.Response

// RetrofitClient se reemplaza con mockkObject para que la API sea un mock y no haya red real
class UsuarioRepositoryRemotoTest : StringSpec({

    val api = mockk<UsuarioApi>()
    val usuarioDto = UsuarioDto(
        id = 1, nombre = "Cliente", rut = "11111111-1", correo = "cliente@example.com",
        telefono = "911111111", rubro = "Panadería", personasNegocio = "Solo yo",
        rol = "CLIENTE", planPro = false
    )

    beforeTest {
        clearMocks(api)
        mockkObject(RetrofitClient)
        every { RetrofitClient.usuariosApi } returns api
    }

    afterTest {
        unmockkObject(RetrofitClient)
    }

    // ---------- Casos exitosos ----------

    "login exitoso convierte la respuesta del servidor en Usuario" {
        runTest {
            coEvery { api.login(LoginRequest("cliente@example.com", "Demo1234")) } returns usuarioDto
            val usuario = UsuarioRepositoryRemoto().login("cliente@example.com", "Demo1234")
            usuario.id shouldBe 1
            usuario.rol shouldBe Rol.Cliente
        }
    }

    "registrar envía el teléfono solo con números" {
        runTest {
            coEvery { api.registrar(any()) } returns usuarioDto
            UsuarioRepositoryRemoto().registrar(
                "Cliente", "11111111-1", "cliente@example.com", "+56 9 1111 1111",
                "Panadería", "Solo yo", "Demo1234"
            )
            coVerify {
                api.registrar(
                    RegistroRequest(
                        "Cliente", "11111111-1", "cliente@example.com", "56911111111",
                        "Panadería", "Solo yo", "Demo1234"
                    )
                )
            }
        }
    }

    "solicitarCodigo y restablecerPassword llaman a la API" {
        runTest {
            coEvery { api.recuperar(any()) } returns MensajeDto("Código enviado")
            coEvery { api.restablecer(any()) } returns MensajeDto("Contraseña cambiada")
            val repo = UsuarioRepositoryRemoto()
            repo.solicitarCodigo("cliente@example.com")
            repo.restablecerPassword("cliente@example.com", "123456", "Nueva123")
            coVerify { api.recuperar(any()) }
            coVerify { api.restablecer(any()) }
        }
    }

    "obtenerPerfil envía el mismo id como solicitante" {
        runTest {
            coEvery { api.obtenerPorId(id = 1, idSolicitante = 1) } returns usuarioDto
            UsuarioRepositoryRemoto().obtenerPerfil(1).correo shouldBe "cliente@example.com"
        }
    }

    "actualizarPerfil envía el teléfono limpio y devuelve el usuario actualizado" {
        runTest {
            val cuerpo = ActualizarPerfilRequest("Nuevo", "912345678", "Pastelería", "Solo yo")
            coEvery { api.actualizarPerfil(1, 1, cuerpo) } returns usuarioDto.copy(nombre = "Nuevo")
            UsuarioRepositoryRemoto()
                .actualizarPerfil(1, "Nuevo", "9 1234 5678", "Pastelería", "Solo yo")
                .nombre shouldBe "Nuevo"
        }
    }

    "listarUsuarios envía el código del rol para filtrar" {
        runTest {
            coEvery { api.listar(idSolicitante = 3, rol = "INSTRUCTOR") } returns
                listOf(usuarioDto.copy(id = 2, rol = "INSTRUCTOR"))
            coEvery { api.listar(idSolicitante = 3, rol = null) } returns
                listOf(usuarioDto, usuarioDto.copy(id = 2))
            val repo = UsuarioRepositoryRemoto()
            repo.listarUsuarios(3, Rol.Instructor).first().rol shouldBe Rol.Instructor
            repo.listarUsuarios(3, null) shouldHaveSize 2
        }
    }

    "cambiarPlanPro y cambiarRol devuelven el usuario modificado" {
        runTest {
            coEvery { api.cambiarPlanPro(1, 3, PlanProRequest(true)) } returns usuarioDto.copy(planPro = true)
            coEvery { api.cambiarRol(1, 4, CambiarRolRequest("VENTAS")) } returns usuarioDto.copy(rol = "VENTAS")
            val repo = UsuarioRepositoryRemoto()
            repo.cambiarPlanPro(idSolicitante = 3, idUsuario = 1, activo = true).planPro.shouldBeTrue()
            repo.cambiarRol(idSolicitante = 4, idUsuario = 1, rol = Rol.Ventas).rol shouldBe Rol.Ventas
        }
    }

    "eliminarUsuario termina sin error si el servidor responde 204" {
        runTest {
            coEvery { api.eliminar(id = 1, idSolicitante = 4) } returns Response.success(Unit)
            UsuarioRepositoryRemoto().eliminarUsuario(idSolicitante = 4, idUsuario = 1)
            coVerify { api.eliminar(1, 4) }
        }
    }

    // ---------- Conversión de errores ----------

    "sin internet (IOException) se convierte en SinConexion" {
        runTest {
            coEvery { api.login(any()) } throws IOException("sin red")
            shouldThrow<UsuarioException.SinConexion> {
                UsuarioRepositoryRemoto().login("cliente@example.com", "Demo1234")
            }
        }
    }

    "401 se convierte en CredencialesInvalidas" {
        runTest {
            coEvery { api.login(any()) } throws errorHttp(401, cuerpoError("Credenciales inválidas", "UNAUTHORIZED"))
            shouldThrow<UsuarioException.CredencialesInvalidas> {
                UsuarioRepositoryRemoto().login("cliente@example.com", "mala")
            }
        }
    }

    "403 se convierte en AccesoDenegado" {
        runTest {
            coEvery { api.listar(any(), any()) } throws errorHttp(403, cuerpoError("Sin permiso", "FORBIDDEN"))
            shouldThrow<UsuarioException.AccesoDenegado> {
                UsuarioRepositoryRemoto().listarUsuarios(1, null)
            }
        }
    }

    "404 se convierte en NoEncontrado" {
        runTest {
            coEvery { api.obtenerPorId(any(), any()) } throws errorHttp(404, cuerpoError("No existe", "NOT_FOUND"))
            shouldThrow<UsuarioException.NoEncontrado> {
                UsuarioRepositoryRemoto().obtenerPerfil(99)
            }
        }
    }

    "409 por correo repetido se convierte en DatoDuplicado de correo" {
        runTest {
            coEvery { api.registrar(any()) } throws
                errorHttp(409, cuerpoError("El correo ya está registrado", "CONFLICT"))
            val error = shouldThrow<UsuarioException.DatoDuplicado> {
                UsuarioRepositoryRemoto().registrar("A", "1-9", "a@a.cl", "9", "R", "P", "Clave123")
            }
            error.message!! shouldContain "correo"
        }
    }

    "409 por RUT repetido se convierte en DatoDuplicado de RUT" {
        runTest {
            coEvery { api.registrar(any()) } throws
                errorHttp(409, cuerpoError("El RUT ya está registrado", "CONFLICT"))
            val error = shouldThrow<UsuarioException.DatoDuplicado> {
                UsuarioRepositoryRemoto().registrar("A", "1-9", "a@a.cl", "9", "R", "P", "Clave123")
            }
            error.message!! shouldContain "RUT"
        }
    }

    "400 al restablecer la contraseña se convierte en CodigoInvalido" {
        runTest {
            coEvery { api.restablecer(any()) } throws errorHttp(400, cuerpoError("Código vencido", "BAD_REQUEST"))
            shouldThrow<UsuarioException.CodigoInvalido> {
                UsuarioRepositoryRemoto().restablecerPassword("a@a.cl", "000000", "Nueva123")
            }
        }
    }

    "400 de validación muestra el mensaje del servidor" {
        runTest {
            coEvery { api.registrar(any()) } throws errorHttp(400, cuerpoError("Datos inválidos", "BAD_REQUEST"))
            val error = shouldThrow<UsuarioException.ErrorServidor> {
                UsuarioRepositoryRemoto().registrar("A", "1-9", "a@a.cl", "9", "R", "P", "Clave123")
            }
            error.message shouldBe "Datos inválidos"
        }
    }

    "400 con errores por campo muestra el primer campo con problema" {
        runTest {
            val json = """{"mensaje":"Datos inválidos","estado":"BAD_REQUEST","campos":{"telefono":"El teléfono debe tener 9 dígitos"}}"""
            coEvery { api.actualizarPerfil(any(), any(), any()) } throws errorHttp(400, json)
            val error = shouldThrow<UsuarioException.ErrorServidor> {
                UsuarioRepositoryRemoto().actualizarPerfil(1, "A", "9", "R", "P")
            }
            error.message shouldBe "El teléfono debe tener 9 dígitos"
        }
    }

    "500 o 503 se convierten en ErrorServidor con el mensaje del servidor" {
        runTest {
            coEvery { api.cambiarPlanPro(any(), any(), any()) } throws
                errorHttp(500, cuerpoError("Falla interna", "INTERNAL_SERVER_ERROR"))
            coEvery { api.cambiarRol(any(), any(), any()) } throws
                errorHttp(503, cuerpoError("Servicio en mantención", "SERVICE_UNAVAILABLE"))
            val repo = UsuarioRepositoryRemoto()

            shouldThrow<UsuarioException.ErrorServidor> {
                repo.cambiarPlanPro(3, 1, true)
            }.message shouldBe "Falla interna"
            shouldThrow<UsuarioException.ErrorServidor> {
                repo.cambiarRol(4, 1, Rol.Ventas)
            }.message shouldBe "Servicio en mantención"
        }
    }

    "un error sin cuerpo usa el mensaje genérico del servidor" {
        runTest {
            coEvery { api.recuperar(any()) } throws errorHttp(503)
            val error = shouldThrow<UsuarioException.ErrorServidor> {
                UsuarioRepositoryRemoto().solicitarCodigo("a@a.cl")
            }
            error.message shouldBe "Tuvimos un problema con el servidor. Inténtalo más tarde."
        }
    }

    "un cuerpo de error que no es JSON también usa el mensaje genérico" {
        runTest {
            coEvery { api.recuperar(any()) } throws errorHttp(500, "<html>Error</html>")
            shouldThrow<UsuarioException.ErrorServidor> {
                UsuarioRepositoryRemoto().solicitarCodigo("a@a.cl")
            }.message shouldBe "Tuvimos un problema con el servidor. Inténtalo más tarde."
        }
    }

    "eliminarUsuario con respuesta 403 se convierte en AccesoDenegado" {
        runTest {
            coEvery { api.eliminar(any(), any()) } returns respuestaError(403, cuerpoError("Sin permiso", "FORBIDDEN"))
            shouldThrow<UsuarioException.AccesoDenegado> {
                UsuarioRepositoryRemoto().eliminarUsuario(idSolicitante = 3, idUsuario = 1)
            }
        }
    }

    "un error inesperado se convierte en ErrorServidor" {
        runTest {
            coEvery { api.obtenerPorId(any(), any()) } throws IllegalStateException("JSON raro")
            shouldThrow<UsuarioException.ErrorServidor> {
                UsuarioRepositoryRemoto().obtenerPerfil(1)
            }
        }
    }

    "la cancelación de la corrutina no se transforma en otro error" {
        runTest {
            coEvery { api.login(any()) } throws CancellationException("pantalla cerrada")
            shouldThrow<CancellationException> {
                UsuarioRepositoryRemoto().login("a@a.cl", "Demo1234")
            }
        }
    }
})
