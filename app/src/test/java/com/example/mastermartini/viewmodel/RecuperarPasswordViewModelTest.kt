package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.UsuarioException
import com.example.mastermartini.data.UsuarioRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class RecuperarPasswordViewModelTest : StringSpec({

    val repo = mockk<UsuarioRepository>()

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        clearMocks(repo)
        coEvery { repo.solicitarCodigo(any()) } returns Unit
        coEvery { repo.restablecerPassword(any(), any(), any()) } returns Unit
    }

    afterTest {
        Dispatchers.resetMain()
    }

    // Deja el ViewModel en el paso del código con datos válidos
    fun RecuperarPasswordViewModel.llenarPasoCodigo() {
        onCorreoChange("cliente@example.com")
        onCodigoChange(" 123456 ")
        onNuevaPasswordChange("Nueva123")
        onRepetirPasswordChange("Nueva123")
    }

    // ---------- Validaciones ----------

    "el correo debe tener @ y punto" {
        val vm = RecuperarPasswordViewModel(repo)
        vm.onCorreoChange("cliente")
        vm.uiState.value.errorCorreo shouldBe "Correo no válido (debe incluir @ y punto)"
        vm.onCorreoChange("cliente@example.com")
        vm.uiState.value.errorCorreo.shouldBeNull()
    }

    "el código debe tener exactamente 6 números" {
        val vm = RecuperarPasswordViewModel(repo)
        vm.onCodigoChange("123")
        vm.uiState.value.errorCodigo shouldBe "El código tiene 6 números"
        vm.onCodigoChange("12a456")
        vm.uiState.value.errorCodigo shouldBe "El código tiene 6 números"
        vm.onCodigoChange("123456")
        vm.uiState.value.errorCodigo.shouldBeNull()
    }

    "la nueva contraseña debe tener al menos 4 caracteres" {
        val vm = RecuperarPasswordViewModel(repo)
        vm.onNuevaPasswordChange("abc")
        vm.uiState.value.errorNuevaPassword shouldBe "La contraseña debe tener al menos 4 caracteres"
        // Si todavía no escribe la repetición, no se le muestra ese error
        vm.uiState.value.errorRepetirPassword.shouldBeNull()
    }

    "las dos contraseñas deben coincidir, se escriban en el orden que sea" {
        val vm = RecuperarPasswordViewModel(repo)
        vm.onNuevaPasswordChange("Nueva123")
        vm.onRepetirPasswordChange("Nueva12")
        vm.uiState.value.errorRepetirPassword shouldBe "Las contraseñas no coinciden"
        vm.onNuevaPasswordChange("Nueva12")
        vm.uiState.value.errorRepetirPassword.shouldBeNull()
    }

    // ---------- Paso 1: pedir el código ----------

    "enviar el código con un correo inválido muestra el error y no llama al repositorio" {
        runTest {
            val vm = RecuperarPasswordViewModel(repo)
            vm.enviarCodigo()
            advanceUntilIdle()
            vm.uiState.value.errorCorreo shouldBe "Correo no válido (debe incluir @ y punto)"
            coVerify(exactly = 0) { repo.solicitarCodigo(any()) }
        }
    }

    "enviar el código pasa al paso CODIGO con un mensaje" {
        runTest {
            val vm = RecuperarPasswordViewModel(repo)
            vm.onCorreoChange(" cliente@example.com ")
            vm.enviarCodigo()
            vm.uiState.value.cargando.shouldBeTrue()
            advanceUntilIdle()

            vm.uiState.value.cargando.shouldBeFalse()
            vm.uiState.value.paso shouldBe PasoRecuperar.CODIGO
            vm.uiState.value.mensaje shouldBe "Si el correo está registrado, te enviamos un código de 6 dígitos"
            coVerify { repo.solicitarCodigo("cliente@example.com") }
        }
    }

    "reenviar el código muestra el mensaje de código nuevo" {
        runTest {
            val vm = RecuperarPasswordViewModel(repo)
            vm.onCorreoChange("cliente@example.com")
            vm.reenviarCodigo()
            advanceUntilIdle()
            vm.uiState.value.mensaje shouldBe "Si el correo está registrado, te enviamos un código nuevo de 6 dígitos"
        }
    }

    "sin conexión al pedir el código se muestra el error y se queda en el paso CORREO" {
        runTest {
            coEvery { repo.solicitarCodigo(any()) } throws UsuarioException.SinConexion()
            val vm = RecuperarPasswordViewModel(repo)
            vm.onCorreoChange("cliente@example.com")
            vm.enviarCodigo()
            advanceUntilIdle()

            vm.uiState.value.paso shouldBe PasoRecuperar.CORREO
            vm.uiState.value.errorGeneral shouldBe "No pudimos conectarnos. Revisa tu internet e inténtalo otra vez."
        }
    }

    "mientras carga no se repiten los envíos" {
        runTest {
            val vm = RecuperarPasswordViewModel(repo)
            vm.llenarPasoCodigo()
            vm.enviarCodigo()
            vm.enviarCodigo()
            vm.reenviarCodigo()
            vm.restablecer()
            advanceUntilIdle()
            coVerify(exactly = 1) { repo.solicitarCodigo(any()) }
            coVerify(exactly = 0) { repo.restablecerPassword(any(), any(), any()) }
        }
    }

    // ---------- Paso 2: nueva contraseña ----------

    "restablecer con datos inválidos marca todos los errores" {
        runTest {
            val vm = RecuperarPasswordViewModel(repo)
            vm.onNuevaPasswordChange("abc")
            vm.restablecer()
            advanceUntilIdle()

            vm.uiState.value.errorCodigo shouldBe "El código tiene 6 números"
            vm.uiState.value.errorNuevaPassword shouldBe "La contraseña debe tener al menos 4 caracteres"
            vm.uiState.value.errorRepetirPassword shouldBe "Las contraseñas no coinciden"
            coVerify(exactly = 0) { repo.restablecerPassword(any(), any(), any()) }
        }
    }

    "restablecer con éxito pasa al paso LISTO y borra las contraseñas" {
        runTest {
            val vm = RecuperarPasswordViewModel(repo)
            vm.llenarPasoCodigo()
            vm.restablecer()
            advanceUntilIdle()

            vm.uiState.value.paso shouldBe PasoRecuperar.LISTO
            vm.uiState.value.nuevaPassword shouldBe ""
            vm.uiState.value.repetirPassword shouldBe ""
            coVerify { repo.restablecerPassword("cliente@example.com", "123456", "Nueva123") }
        }
    }

    "un código incorrecto o vencido muestra el mensaje del repositorio" {
        runTest {
            coEvery { repo.restablecerPassword(any(), any(), any()) } throws UsuarioException.CodigoInvalido()
            val vm = RecuperarPasswordViewModel(repo)
            vm.llenarPasoCodigo()
            vm.restablecer()
            advanceUntilIdle()

            vm.uiState.value.cargando.shouldBeFalse()
            vm.uiState.value.paso shouldBe PasoRecuperar.CORREO
            vm.uiState.value.errorGeneral shouldBe "El código no es correcto o ya venció. Pide uno nuevo."
        }
    }

    "la Factory crea el ViewModel con el repositorio de usuarios" {
        RecuperarPasswordViewModel.Factory
            .create(RecuperarPasswordViewModel::class.java, extrasConAplicacion(aplicacionDePrueba()))
            .shouldBeInstanceOf<RecuperarPasswordViewModel>()
    }
})
