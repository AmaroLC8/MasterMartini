package com.example.mastermartini.data

import android.content.Context
import android.content.SharedPreferences
import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

// SharedPreferences se simula con MockK para revisar qué se guarda y qué se lee
class AlmacenamientoLocalTest : StringSpec({

    val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    val preferencias = mockk<SharedPreferences>(relaxed = true)
    val contexto = mockk<Context>()

    beforeTest {
        clearMocks(editor, preferencias, contexto)
        every { contexto.getSharedPreferences(any(), any()) } returns preferencias
        every { preferencias.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
    }

    "guardarSesion guarda los datos del usuario y borra la contraseña antigua" {
        val usuario = Usuario(
            id = 4, nombre = "Ana", rut = "11111111-1", correo = "ana@example.com",
            telefono = "911111111", rubro = "Panadería", personasNegocio = "Solo yo",
            rol = Rol.Ventas, planPro = true
        )
        AlmacenamientoLocal(contexto).guardarSesion(usuario)

        verify { editor.putLong("KEY_ID_USUARIO", 4) }
        verify { editor.putString("KEY_ROL", "VENTAS") }
        verify { editor.putBoolean("KEY_PLAN_PRO", true) }
        verify { editor.putString("KEY_NOMBRE", "Ana") }
        verify { editor.remove("KEY_PASSWORD") }
        verify { editor.apply() }
    }

    "los datos del perfil se leen desde SharedPreferences" {
        every { preferencias.getString("KEY_NOMBRE", any()) } returns "Ana"
        every { preferencias.getString("KEY_RUT", any()) } returns "11111111-1"
        every { preferencias.getString("KEY_CORREO", any()) } returns "ana@example.com"
        every { preferencias.getString("KEY_RUBRO", any()) } returns "Chocolatería"
        every { preferencias.getString("KEY_TELEFONO", any()) } returns "911111111"
        every { preferencias.getString("KEY_PERSONAS_NEGOCIO", any()) } returns "Solo yo"
        every { preferencias.getString("KEY_ROL", any()) } returns "ADMINISTRADOR"
        every { preferencias.getLong("KEY_ID_USUARIO", -1L) } returns 4L
        every { preferencias.getBoolean("KEY_PLAN_PRO", false) } returns true
        every { preferencias.contains("KEY_NOMBRE") } returns true

        val almacenamiento = AlmacenamientoLocal(contexto)

        almacenamiento.obtenerNombre() shouldBe "Ana"
        almacenamiento.obtenerRut() shouldBe "11111111-1"
        almacenamiento.obtenerCorreo() shouldBe "ana@example.com"
        almacenamiento.obtenerRubro() shouldBe "Chocolatería"
        almacenamiento.obtenerTelefono() shouldBe "911111111"
        almacenamiento.obtenerPersonasNegocio() shouldBe "Solo yo"
        almacenamiento.obtenerRol() shouldBe Rol.Administrador
        almacenamiento.obtenerIdUsuario() shouldBe 4L
        almacenamiento.tienePlanPro().shouldBeTrue()
        almacenamiento.existePerfil().shouldBeTrue()
    }

    "sin sesión guardada se usan los valores por defecto" {
        every { preferencias.getString(any(), any()) } returns null
        every { preferencias.getStringSet(any(), any()) } returns null
        every { preferencias.getLong("KEY_ID_USUARIO", -1L) } returns -1L
        every { preferencias.contains(any()) } returns false

        val almacenamiento = AlmacenamientoLocal(contexto)

        almacenamiento.obtenerNombre() shouldBe "Emprendedor"
        almacenamiento.obtenerRubro() shouldBe "Panadería"
        almacenamiento.obtenerRut() shouldBe ""
        almacenamiento.obtenerCorreo() shouldBe ""
        almacenamiento.obtenerTelefono() shouldBe ""
        almacenamiento.obtenerPersonasNegocio() shouldBe ""
        almacenamiento.obtenerRol() shouldBe Rol.Cliente
        almacenamiento.obtenerIdUsuario().shouldBeNull()
        almacenamiento.tienePlanPro().shouldBeFalse()
        almacenamiento.existePerfil().shouldBeFalse()
        almacenamiento.obtenerInteresesEncuesta().shouldBeEmpty()
        almacenamiento.obtenerNivelEncuesta() shouldBe ""
        almacenamiento.obtenerUbicacionEncuesta() shouldBe ""
        almacenamiento.existeEncuesta().shouldBeFalse()
        almacenamiento.obtenerRutaFoto() shouldBe ""
    }

    "cerrarSesion borra la sesión pero no la encuesta ni la foto" {
        AlmacenamientoLocal(contexto).cerrarSesion()

        verify { editor.remove("KEY_ID_USUARIO") }
        verify { editor.remove("KEY_NOMBRE") }
        verify(exactly = 0) { editor.remove("KEY_ENCUESTA_NIVEL") }
        verify(exactly = 0) { editor.remove("KEY_FOTO_LOCAL") }
        verify { editor.apply() }
    }

    "la encuesta se guarda y se vuelve a leer" {
        every { preferencias.getStringSet("KEY_ENCUESTA_INTERESES", any()) } returns setOf("Panadería")
        every { preferencias.getString("KEY_ENCUESTA_NIVEL", any()) } returns "Básico"
        every { preferencias.getString("KEY_ENCUESTA_UBICACION", any()) } returns "Santiago"
        every { preferencias.contains("KEY_ENCUESTA_NIVEL") } returns true

        val almacenamiento = AlmacenamientoLocal(contexto)
        almacenamiento.guardarEncuesta(setOf("Panadería"), "Básico", "Santiago")

        verify { editor.putStringSet("KEY_ENCUESTA_INTERESES", setOf("Panadería")) }
        verify { editor.putString("KEY_ENCUESTA_NIVEL", "Básico") }
        almacenamiento.obtenerInteresesEncuesta() shouldBe setOf("Panadería")
        almacenamiento.obtenerNivelEncuesta() shouldBe "Básico"
        almacenamiento.obtenerUbicacionEncuesta() shouldBe "Santiago"
        almacenamiento.existeEncuesta().shouldBeTrue()
    }

    "la ruta de la foto de perfil se guarda y se lee" {
        every { preferencias.getString("KEY_FOTO_LOCAL", any()) } returns "/fotos/perfil.jpg"

        val almacenamiento = AlmacenamientoLocal(contexto)
        almacenamiento.guardarRutaFoto("/fotos/perfil.jpg")

        verify { editor.putString("KEY_FOTO_LOCAL", "/fotos/perfil.jpg") }
        almacenamiento.obtenerRutaFoto() shouldBe "/fotos/perfil.jpg"
    }
})
