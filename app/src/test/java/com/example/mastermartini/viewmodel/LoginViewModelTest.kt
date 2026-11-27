package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.AlmacenamientoLocal
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
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : StringSpec({

    val repo = mockk<UsuarioRepository>()
    val almacenamiento = mockk<AlmacenamientoLocal>(relaxed = true)

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        clearMocks(repo, almacenamiento)
    }

    afterTest {
        Dispatchers.resetMain()
    }

    "los campos vacíos muestran su error al escribir" {
        val vm = LoginViewModel(repo, almacenamiento)
        vm.onUsuarioChange(" ")
        vm.onPasswordChange("")
        vm.uiState.value.errorUsuario shouldBe "Ingresa tu RUT o correo"
        vm.uiState.value.errorPassword shouldBe "Ingresa tu contraseña"
        vm.onUsuarioChange("cliente@example.com")
        vm.onPasswordChange("Demo1234")
        vm.uiState.value.errorUsuario.shouldBeNull()
        vm.uiState.value.errorPassword.shouldBeNull()
    }

    "iniciar sesión con campos vacíos marca los errores y no llama al repositorio" {
        runTest {
            val vm = LoginViewModel(repo, almacenamiento)
            vm.iniciarSesion {}
            advanceUntilIdle()
            vm.uiState.value.errorUsuario shouldBe "Ingresa tu RUT o correo"
            vm.uiState.value.errorPassword shouldBe "Ingresa tu contraseña"
            coVerify(exactly = 0) { repo.login(any(), any()) }
        }
    }

    "login exitoso guarda la sesión y avisa a la pantalla" {
        runTest {
            val usuario = usuarioDePrueba(1)
            coEvery { repo.login("cliente@example.com", "Demo1234") } returns usuario
            val vm = LoginViewModel(repo, almacenamiento)
            vm.onUsuarioChange("  cliente@example.com ")
            vm.onPasswordChange("Demo1234")
            var exito = false

            vm.iniciarSesion { exito = true }
            vm.uiState.value.cargando.shouldBeTrue()
            // Un segundo toque mientras carga se ignora
            vm.iniciarSesion { }
            advanceUntilIdle()

            exito.shouldBeTrue()
            vm.uiState.value.cargando.shouldBeFalse()
            verify { almacenamiento.guardarSesion(usuario) }
            coVerify(exactly = 1) { repo.login(any(), any()) }
        }
    }

    "credenciales incorrectas muestran el mensaje y no guardan sesión" {
        runTest {
            coEvery { repo.login(any(), any()) } throws UsuarioException.CredencialesInvalidas()
            val vm = LoginViewModel(repo, almacenamiento)
            vm.onUsuarioChange("cliente@example.com")
            vm.onPasswordChange("mala")
            vm.iniciarSesion {}
            advanceUntilIdle()

            vm.uiState.value.errorGeneral shouldBe "RUT/correo o contraseña incorrectos"
            vm.uiState.value.cargando.shouldBeFalse()
            verify(exactly = 0) { almacenamiento.guardarSesion(any()) }
        }
    }

    "la Factory crea el ViewModel con sus dependencias" {
        LoginViewModel.Factory
            .create(LoginViewModel::class.java, extrasConAplicacion(aplicacionDePrueba()))
            .shouldBeInstanceOf<LoginViewModel>()
    }
})
