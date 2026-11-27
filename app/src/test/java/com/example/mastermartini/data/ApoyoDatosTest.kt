package com.example.mastermartini.data

import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.data.remote.UsuarioApi
import com.example.mastermartini.data.remote.dto.UsuarioDto
import com.example.mastermartini.data.remote.dto.aModelo
import com.example.mastermartini.data.remote.leerMensajeError
import com.example.mastermartini.model.Rol
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import retrofit2.Response

// Piezas de apoyo de la capa de datos: conversión de DTO, lectura de errores y excepciones
class ApoyoDatosTest : StringSpec({

    "UsuarioDto completo se convierte en Usuario con su rol" {
        val usuario = UsuarioDto(
            id = 3, nombre = "Ventas", rut = "33333333-3", correo = "ventas@example.com",
            telefono = "933333333", rubro = "Pastelería", personasNegocio = "Solo yo",
            rol = "VENTAS", planPro = true
        ).aModelo()

        usuario.id shouldBe 3
        usuario.rol shouldBe Rol.Ventas
        usuario.planPro.shouldBeTrue()
    }

    "UsuarioDto vacío se convierte en un Cliente con datos en blanco" {
        val usuario = UsuarioDto().aModelo()

        usuario.id shouldBe 0
        usuario.nombre shouldBe ""
        usuario.correo shouldBe ""
        usuario.rol shouldBe Rol.Cliente
        usuario.planPro.shouldBeFalse()
    }

    "leerMensajeError obtiene el mensaje del cuerpo JSON de error" {
        val respuesta = respuestaError<Unit>(409, cuerpoError("Ya existe", "CONFLICT"))
        leerMensajeError(respuesta) shouldBe "Ya existe"
    }

    "leerMensajeError devuelve null si no hay cuerpo o no es JSON" {
        leerMensajeError(Response.success(Unit)).shouldBeNull()
        leerMensajeError(respuestaError<Unit>(500, "texto plano")).shouldBeNull()
    }

    "ServidorException guarda el código y el mensaje del servidor" {
        val error = ServidorException(codigo = 403, mensajeServidor = "Sin permiso")
        error.codigo shouldBe 403
        error.mensajeServidor shouldBe "Sin permiso"
        error.message shouldBe "Error 403: verifica permisos o campos"
    }

    "ErrorServidor usa un mensaje genérico si el servidor lo manda vacío" {
        UsuarioException.ErrorServidor("  ").message shouldBe
            "Tuvimos un problema con el servidor. Inténtalo más tarde."
        UsuarioException.ErrorServidor("Falla puntual").message shouldBe "Falla puntual"
    }

    "ProveedorRepositorios entrega el repositorio remoto porque USAR_BACKEND es true" {
        mockkObject(RetrofitClient)
        every { RetrofitClient.usuariosApi } returns mockk<UsuarioApi>()

        ProveedorRepositorios.usuarioRepository.shouldBeInstanceOf<UsuarioRepositoryRemoto>()

        unmockkObject(RetrofitClient)
    }
})
