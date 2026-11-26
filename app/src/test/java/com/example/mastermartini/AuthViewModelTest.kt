package com.example.mastermartini

import android.app.Application
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.UsuarioRepository
import com.example.mastermartini.viewmodel.RegistroViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.mockk.every
import io.mockk.mockk
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest : StringSpec({

    val testDispatcher = StandardTestDispatcher()
    val carpetaTemporal = File(System.getProperty("java.io.tmpdir"))
    val mockApp = mockk<Application>(relaxed = true) {
        every { filesDir } returns carpetaTemporal
        every { cacheDir } returns carpetaTemporal
    }
    val mockUsuarioRepo = mockk<UsuarioRepository>(relaxed = true)
    val mockAlmacenamiento = mockk<AlmacenamientoLocal>(relaxed = true)

    beforeTest {
        Dispatchers.setMain(testDispatcher)
        every { mockAlmacenamiento.existePerfil() } returns false
    }

    afterTest {
        Dispatchers.resetMain()
    }

    // PRUEBA 1: Validación de RUT correcto en Registro
    "1. RegistroViewModel debe validar correctamente un RUT chileno válido" {
        val viewModel = RegistroViewModel(mockApp, mockUsuarioRepo, mockAlmacenamiento)
        viewModel.onRutChange("12345678-5")
        viewModel.uiState.value.errorRut.shouldBeNull()
    }

    // PRUEBA 2: Validación de RUT inválido
    "2. RegistroViewModel debe marcar error cuando el RUT tiene formato inválido" {
        val viewModel = RegistroViewModel(mockApp, mockUsuarioRepo, mockAlmacenamiento)
        viewModel.onRutChange("123456")
        viewModel.uiState.value.errorRut.shouldNotBeNull()
    }

    // PRUEBA 3: Validación de teléfono móvil chileno
    "3. RegistroViewModel debe detectar error si el teléfono no cumple el formato chileno" {
        val viewModel = RegistroViewModel(mockApp, mockUsuarioRepo, mockAlmacenamiento)
        viewModel.onTelefonoChange("81234567")
        viewModel.uiState.value.errorTelefono.shouldNotBeNull()
    }

    // PRUEBA 4: Formulario incompleto no debe habilitar el registro
    "4. RegistroViewModel debe mantener esFormularioValido en false si faltan campos obligatorios" {
        val viewModel = RegistroViewModel(mockApp, mockUsuarioRepo, mockAlmacenamiento)
        viewModel.onRutChange("12345678-5")
        viewModel.uiState.value.esFormularioValido.shouldBeFalse()
    }
})