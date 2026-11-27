package com.example.mastermartini.data

import android.util.Log
import com.example.mastermartini.model.Rol
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest

// Las cuentas de prueba son: 1 Cliente, 2 Instructor, 3 Ventas y 4 Administrador (clave Demo1234)
class UsuarioRepositoryFalsoTest : StringSpec({

    // Guarda lo que el repositorio escribe en Logcat para leer el código de recuperación
    val mensajesLog = mutableListOf<String>()

    beforeTest {
        mensajesLog.clear()
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } answers {
            mensajesLog.add(secondArg())
            0
        }
    }

    afterTest {
        unmockkStatic(Log::class)
    }

    fun ultimoCodigoEnviado(): String {
        return Regex("\\d{6}").find(mensajesLog.last())!!.value
    }

    // ---------- Login ----------

    "login funciona con el correo aunque tenga mayúsculas y espacios" {
        runTest {
            val usuario = UsuarioRepositoryFalso().login(" CLIENTE@example.com ", "Demo1234")
            usuario.id shouldBe 1
            usuario.rol shouldBe Rol.Cliente
        }
    }

    "login funciona con el RUT escrito con puntos" {
        runTest {
            val usuario = UsuarioRepositoryFalso().login("22.222.222-2", "Demo1234")
            usuario.rol shouldBe Rol.Instructor
        }
    }

    "login con contraseña incorrecta lanza CredencialesInvalidas" {
        runTest {
            shouldThrow<UsuarioException.CredencialesInvalidas> {
                UsuarioRepositoryFalso().login("cliente@example.com", "otraClave")
            }
        }
    }

    "login con un usuario que no existe lanza CredencialesInvalidas" {
        runTest {
            shouldThrow<UsuarioException.CredencialesInvalidas> {
                UsuarioRepositoryFalso().login("nadie@example.com", "Demo1234")
            }
        }
    }

    // ---------- Registro ----------

    "registrar crea un Cliente sin Plan Pro con RUT y correo normalizados" {
        runTest {
            val repo = UsuarioRepositoryFalso()
            val nuevo = repo.registrar(
                nombre = "  Rosa Pérez ", rut = "12.345.678-5", correo = "Rosa@Example.com",
                telefono = "912345678", rubro = "Pastelería",
                personasNegocio = "Solo yo", password = "Clave123"
            )
            nuevo.id shouldBe 5
            nuevo.nombre shouldBe "Rosa Pérez"
            nuevo.rut shouldBe "12345678-5"
            nuevo.correo shouldBe "rosa@example.com"
            nuevo.rol shouldBe Rol.Cliente
            nuevo.planPro.shouldBeFalse()
            // La cuenta nueva ya puede iniciar sesión
            repo.login("rosa@example.com", "Clave123").id shouldBe 5
        }
    }

    "registrar con un correo que ya existe lanza DatoDuplicado de correo" {
        runTest {
            val error = shouldThrow<UsuarioException.DatoDuplicado> {
                UsuarioRepositoryFalso().registrar(
                    "Otra", "99999999-9", "cliente@example.com", "911111111",
                    "Panadería", "Solo yo", "Clave123"
                )
            }
            error.message!! shouldContain "correo"
        }
    }

    "registrar con un RUT que ya existe lanza DatoDuplicado de RUT" {
        runTest {
            val error = shouldThrow<UsuarioException.DatoDuplicado> {
                UsuarioRepositoryFalso().registrar(
                    "Otra", "11.111.111-1", "nuevo@example.com", "911111111",
                    "Panadería", "Solo yo", "Clave123"
                )
            }
            error.message!! shouldContain "RUT"
        }
    }

    // ---------- Recuperar contraseña ----------

    "solicitarCodigo con un correo desconocido no genera código" {
        runTest {
            UsuarioRepositoryFalso().solicitarCodigo("nadie@example.com")
            mensajesLog.shouldBeEmpty()
        }
    }

    "restablecerPassword con el código correcto cambia la contraseña" {
        runTest {
            val repo = UsuarioRepositoryFalso()
            repo.solicitarCodigo("cliente@example.com")
            repo.restablecerPassword("cliente@example.com", ultimoCodigoEnviado(), "Nueva123")

            repo.login("cliente@example.com", "Nueva123").id shouldBe 1
            shouldThrow<UsuarioException.CredencialesInvalidas> {
                repo.login("cliente@example.com", "Demo1234")
            }
        }
    }

    "el código de recuperación solo sirve una vez" {
        runTest {
            val repo = UsuarioRepositoryFalso()
            repo.solicitarCodigo("cliente@example.com")
            val codigo = ultimoCodigoEnviado()
            repo.restablecerPassword("cliente@example.com", codigo, "Nueva123")

            shouldThrow<UsuarioException.CodigoInvalido> {
                repo.restablecerPassword("cliente@example.com", codigo, "Otra123")
            }
        }
    }

    "restablecerPassword con un código incorrecto lanza CodigoInvalido" {
        runTest {
            val repo = UsuarioRepositoryFalso()
            repo.solicitarCodigo("cliente@example.com")
            val codigoMalo = if (ultimoCodigoEnviado() == "000000") "111111" else "000000"

            shouldThrow<UsuarioException.CodigoInvalido> {
                repo.restablecerPassword("cliente@example.com", codigoMalo, "Nueva123")
            }
        }
    }

    "restablecerPassword sin haber pedido código lanza CodigoInvalido" {
        runTest {
            shouldThrow<UsuarioException.CodigoInvalido> {
                UsuarioRepositoryFalso().restablecerPassword("cliente@example.com", "123456", "Nueva123")
            }
        }
    }

    "restablecerPassword lanza NoEncontrado si la cuenta se eliminó después de pedir el código" {
        runTest {
            val repo = UsuarioRepositoryFalso()
            repo.solicitarCodigo("cliente@example.com")
            val codigo = ultimoCodigoEnviado()
            repo.eliminarUsuario(idSolicitante = 4, idUsuario = 1)

            shouldThrow<UsuarioException.NoEncontrado> {
                repo.restablecerPassword("cliente@example.com", codigo, "Nueva123")
            }
        }
    }

    // ---------- Perfil ----------

    "obtenerPerfil devuelve el usuario por su id" {
        runTest {
            UsuarioRepositoryFalso().obtenerPerfil(3).rol shouldBe Rol.Ventas
        }
    }

    "obtenerPerfil con un id que no existe lanza NoEncontrado" {
        runTest {
            shouldThrow<UsuarioException.NoEncontrado> {
                UsuarioRepositoryFalso().obtenerPerfil(99)
            }
        }
    }

    "actualizarPerfil guarda los cambios sin tocar RUT ni rol" {
        runTest {
            val repo = UsuarioRepositoryFalso()
            val actualizado = repo.actualizarPerfil(
                id = 1, nombre = " Cliente Editado ", telefono = "987654321",
                rubro = "Chocolatería", personasNegocio = "Solo yo"
            )
            actualizado.nombre shouldBe "Cliente Editado"
            actualizado.rubro shouldBe "Chocolatería"
            actualizado.rut shouldBe "11111111-1"
            repo.obtenerPerfil(1) shouldBe actualizado
        }
    }

    // ---------- Gestión de usuarios y permisos por rol ----------

    "Ventas puede listar todos los usuarios o filtrarlos por rol" {
        runTest {
            val repo = UsuarioRepositoryFalso()
            repo.listarUsuarios(idSolicitante = 3, rol = null) shouldHaveSize 4
            val instructores = repo.listarUsuarios(idSolicitante = 3, rol = Rol.Instructor)
            instructores.map { it.id } shouldBe listOf(2L)
        }
    }

    "Cliente e Instructor no pueden listar usuarios" {
        runTest {
            val repo = UsuarioRepositoryFalso()
            shouldThrow<UsuarioException.AccesoDenegado> { repo.listarUsuarios(1, null) }
            shouldThrow<UsuarioException.AccesoDenegado> { repo.listarUsuarios(2, null) }
        }
    }

    "un solicitante que no existe no tiene permisos" {
        runTest {
            shouldThrow<UsuarioException.AccesoDenegado> {
                UsuarioRepositoryFalso().listarUsuarios(idSolicitante = 99, rol = null)
            }
        }
    }

    "Ventas puede activar y quitar el Plan Pro" {
        runTest {
            val repo = UsuarioRepositoryFalso()
            repo.cambiarPlanPro(idSolicitante = 3, idUsuario = 1, activo = true).planPro.shouldBeTrue()
            repo.cambiarPlanPro(idSolicitante = 3, idUsuario = 1, activo = false).planPro.shouldBeFalse()
        }
    }

    "Cliente no puede cambiar el Plan Pro" {
        runTest {
            shouldThrow<UsuarioException.AccesoDenegado> {
                UsuarioRepositoryFalso().cambiarPlanPro(idSolicitante = 1, idUsuario = 1, activo = true)
            }
        }
    }

    "Administrador puede cambiar el rol de un usuario" {
        runTest {
            val repo = UsuarioRepositoryFalso()
            repo.cambiarRol(idSolicitante = 4, idUsuario = 1, rol = Rol.Instructor).rol shouldBe Rol.Instructor
            repo.obtenerPerfil(1).rol shouldBe Rol.Instructor
        }
    }

    "Ventas no puede cambiar roles" {
        runTest {
            shouldThrow<UsuarioException.AccesoDenegado> {
                UsuarioRepositoryFalso().cambiarRol(idSolicitante = 3, idUsuario = 1, rol = Rol.Administrador)
            }
        }
    }

    "Administrador puede eliminar una cuenta" {
        runTest {
            val repo = UsuarioRepositoryFalso()
            repo.eliminarUsuario(idSolicitante = 4, idUsuario = 2)
            shouldThrow<UsuarioException.NoEncontrado> { repo.obtenerPerfil(2) }
        }
    }

    "Instructor no puede eliminar cuentas" {
        runTest {
            shouldThrow<UsuarioException.AccesoDenegado> {
                UsuarioRepositoryFalso().eliminarUsuario(idSolicitante = 2, idUsuario = 1)
            }
        }
    }

    "eliminar un usuario que no existe lanza NoEncontrado" {
        runTest {
            shouldThrow<UsuarioException.NoEncontrado> {
                UsuarioRepositoryFalso().eliminarUsuario(idSolicitante = 4, idUsuario = 99)
            }
        }
    }
})
