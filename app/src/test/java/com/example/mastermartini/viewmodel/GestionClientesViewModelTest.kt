package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.UsuarioException
import com.example.mastermartini.data.UsuarioRepository
import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class GestionClientesViewModelTest : StringSpec({

    val repo = mockk<UsuarioRepository>()
    val almacenamiento = mockk<AlmacenamientoLocal>()
    val jose = usuarioDePrueba(1, nombre = "José Pérez", correo = "jose@panaderia.cl")
    val maria = usuarioDePrueba(5, nombre = "María Soto", correo = "maria@dulces.cl")

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        clearMocks(repo, almacenamiento)
        every { almacenamiento.obtenerIdUsuario() } returns 3L
        coEvery { repo.listarUsuarios(3, Rol.Cliente) } returns listOf(jose, maria)
    }

    afterTest {
        Dispatchers.resetMain()
    }

    // Devuelve los clientes que se ven en pantalla cuando el estado es Exito
    fun GestionClientesViewModel.clientesMostrados(): List<Usuario> {
        val estado = uiState.value.estado
        estado.shouldBeInstanceOf<EstadoUi.Exito<List<Usuario>>>()
        return estado.datos
    }

    "parte en Cargando y luego muestra solo clientes" {
        runTest {
            val vm = GestionClientesViewModel(repo, almacenamiento)
            vm.uiState.value.estado shouldBe EstadoUi.Cargando
            advanceUntilIdle()
            vm.clientesMostrados() shouldBe listOf(jose, maria)
            coVerify { repo.listarUsuarios(3, Rol.Cliente) }
        }
    }

    "sin sesión se muestra un error de acceso" {
        runTest {
            every { almacenamiento.obtenerIdUsuario() } returns null
            val vm = GestionClientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.uiState.value.estado shouldBe EstadoUi.Error("No tienes permiso para hacer esta acción.")
        }
    }

    "si el servidor falla se muestra su mensaje" {
        runTest {
            coEvery { repo.listarUsuarios(any(), any()) } throws UsuarioException.ErrorServidor("Servicio en mantención")
            val vm = GestionClientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.uiState.value.estado shouldBe EstadoUi.Error("Servicio en mantención")
        }
    }

    // ---------- Búsqueda ----------

    "la búsqueda ignora tildes y mayúsculas" {
        runTest {
            val vm = GestionClientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.onBusquedaChange("JOSE")
            vm.clientesMostrados() shouldBe listOf(jose)
            vm.onBusquedaChange("maría")
            vm.clientesMostrados() shouldBe listOf(maria)
        }
    }

    "la búsqueda también encuentra por correo y vacía muestra todos" {
        runTest {
            val vm = GestionClientesViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.onBusquedaChange("dulces.cl")
            vm.clientesMostrados() shouldBe listOf(maria)
            vm.onBusquedaChange("zzz")
            vm.clientesMostrados().shouldBeEmpty()
            vm.onBusquedaChange("   ")
            vm.clientesMostrados() shouldBe listOf(jose, maria)
        }
    }

    "buscar mientras carga guarda el texto pero sigue en Cargando" {
        val vm = GestionClientesViewModel(repo, almacenamiento)
        vm.onBusquedaChange("jose")
        vm.uiState.value.busqueda shouldBe "jose"
        vm.uiState.value.estado shouldBe EstadoUi.Cargando
    }

    // ---------- Plan Pro ----------

    "activar el Plan Pro se ve al tiro y queda confirmado por el servidor" {
        runTest {
            coEvery { repo.cambiarPlanPro(3, 1, true) } returns jose.copy(planPro = true)
            val vm = GestionClientesViewModel(repo, almacenamiento)
            advanceUntilIdle()

            vm.cambiarPlanPro(jose, activo = true)
            // Actualización optimista: el switch cambia antes de que responda el servidor
            vm.clientesMostrados().first().planPro shouldBe true
            vm.uiState.value.guardando shouldBe setOf(1L)
            vm.cambiarPlanPro(jose, activo = false)
            advanceUntilIdle()

            vm.clientesMostrados().first().planPro shouldBe true
            vm.uiState.value.guardando.shouldBeEmpty()
            coVerify(exactly = 1) { repo.cambiarPlanPro(any(), any(), any()) }
        }
    }

    "si el servidor rechaza el cambio el Plan Pro vuelve a como estaba" {
        runTest {
            coEvery { repo.cambiarPlanPro(any(), any(), any()) } throws UsuarioException.AccesoDenegado()
            val vm = GestionClientesViewModel(repo, almacenamiento)
            advanceUntilIdle()

            vm.cambiarPlanPro(jose, activo = true)
            advanceUntilIdle()

            vm.clientesMostrados().first().planPro shouldBe false
            vm.uiState.value.mensajeSnackbar shouldBe "No tienes permiso para hacer esta acción."
            vm.snackbarMostrado()
            vm.uiState.value.mensajeSnackbar.shouldBeNull()
        }
    }

    "si se pierde la sesión al cambiar el Plan Pro también se revierte" {
        runTest {
            // Hay sesión al cargar la lista, pero no al guardar
            every { almacenamiento.obtenerIdUsuario() } returnsMany listOf(3L, null)
            val vm = GestionClientesViewModel(repo, almacenamiento)
            advanceUntilIdle()

            vm.cambiarPlanPro(maria, activo = true)
            advanceUntilIdle()

            vm.clientesMostrados().last().planPro shouldBe false
            vm.uiState.value.mensajeSnackbar shouldBe "No tienes permiso para hacer esta acción."
        }
    }

    "la Factory crea el ViewModel con sus dependencias" {
        GestionClientesViewModel.Factory
            .create(GestionClientesViewModel::class.java, extrasConAplicacion(aplicacionDePrueba()))
            .shouldBeInstanceOf<GestionClientesViewModel>()
    }
})
