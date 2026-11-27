package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.UsuarioException
import com.example.mastermartini.data.UsuarioRepository
import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
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
class GestionUsuariosViewModelTest : StringSpec({

    val repo = mockk<UsuarioRepository>()
    val almacenamiento = mockk<AlmacenamientoLocal>()
    val admin = usuarioDePrueba(4, Rol.Administrador)
    val cliente = usuarioDePrueba(1, Rol.Cliente)
    val instructor = usuarioDePrueba(2, Rol.Instructor)

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        clearMocks(repo, almacenamiento)
        every { almacenamiento.obtenerIdUsuario() } returns 4L
        every { almacenamiento.obtenerRol() } returns Rol.Administrador
        coEvery { repo.listarUsuarios(4, null) } returns listOf(cliente, instructor, admin)
    }

    afterTest {
        Dispatchers.resetMain()
    }

    // Devuelve la lista mostrada cuando el estado es Exito
    fun GestionUsuariosViewModel.listaMostrada(): List<Long> {
        val estado = uiState.value.estado
        estado.shouldBeInstanceOf<EstadoUi.Exito<List<Usuario>>>()
        return estado.datos.map { it.id }
    }

    // ---------- Cargar ----------

    "parte en Cargando y luego muestra todos los usuarios" {
        runTest {
            val vm = GestionUsuariosViewModel(repo, almacenamiento)
            vm.uiState.value.estado shouldBe EstadoUi.Cargando
            vm.uiState.value.idPropio shouldBe 4L
            advanceUntilIdle()
            vm.listaMostrada() shouldBe listOf(1L, 2L, 4L)
        }
    }

    "un rol sin permiso (Ventas) ve un error de acceso" {
        runTest {
            every { almacenamiento.obtenerRol() } returns Rol.Ventas
            val vm = GestionUsuariosViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.uiState.value.estado shouldBe EstadoUi.Error("No tienes permiso para hacer esta acción.")
            coVerify(exactly = 0) { repo.listarUsuarios(any(), any()) }
        }
    }

    "sin sesión iniciada también se niega el acceso" {
        runTest {
            every { almacenamiento.obtenerIdUsuario() } returns null
            val vm = GestionUsuariosViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.uiState.value.estado shouldBe EstadoUi.Error("No tienes permiso para hacer esta acción.")
        }
    }

    "si el servidor no responde se muestra el error de conexión" {
        runTest {
            coEvery { repo.listarUsuarios(any(), any()) } throws UsuarioException.SinConexion()
            val vm = GestionUsuariosViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.uiState.value.estado shouldBe
                EstadoUi.Error("No pudimos conectarnos. Revisa tu internet e inténtalo otra vez.")
        }
    }

    // ---------- Filtro por rol ----------

    "el filtro muestra solo los usuarios del rol elegido" {
        runTest {
            val vm = GestionUsuariosViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.seleccionarFiltro(Rol.Instructor)
            vm.uiState.value.filtroRol shouldBe Rol.Instructor
            vm.listaMostrada() shouldBe listOf(2L)
            vm.seleccionarFiltro(null)
            vm.listaMostrada() shouldBe listOf(1L, 2L, 4L)
        }
    }

    "elegir un filtro mientras hay error guarda el filtro pero mantiene el error" {
        runTest {
            every { almacenamiento.obtenerRol() } returns Rol.Cliente
            val vm = GestionUsuariosViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.seleccionarFiltro(Rol.Ventas)
            vm.uiState.value.filtroRol shouldBe Rol.Ventas
            vm.uiState.value.estado.shouldBeInstanceOf<EstadoUi.Error>()
        }
    }

    // ---------- Cambiar rol ----------

    "el administrador no puede cambiar su propio rol" {
        val vm = GestionUsuariosViewModel(repo, almacenamiento)
        vm.esPropio(admin).shouldBeTrue()
        vm.esPropio(cliente).shouldBeFalse()
        vm.cambiarRol(admin, Rol.Cliente)
        vm.uiState.value.mensajeSnackbar shouldBe "No puedes cambiar tu propio rol"
    }

    "cambiar al mismo rol no llama al servidor" {
        runTest {
            val vm = GestionUsuariosViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.cambiarRol(cliente, Rol.Cliente)
            advanceUntilIdle()
            coVerify(exactly = 0) { repo.cambiarRol(any(), any(), any()) }
        }
    }

    "cambiar el rol actualiza la lista y libera la fila" {
        runTest {
            coEvery { repo.cambiarRol(4, 1, Rol.Ventas) } returns cliente.copy(rol = Rol.Ventas)
            val vm = GestionUsuariosViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.seleccionarFiltro(Rol.Ventas)
            vm.listaMostrada().shouldBeEmpty()

            vm.cambiarRol(cliente, Rol.Ventas)
            vm.uiState.value.guardando shouldBe setOf(1L)
            // Mientras se guarda, un segundo toque se ignora
            vm.cambiarRol(cliente, Rol.Instructor)
            advanceUntilIdle()

            vm.listaMostrada() shouldBe listOf(1L)
            vm.uiState.value.guardando.shouldBeEmpty()
            coVerify(exactly = 1) { repo.cambiarRol(any(), any(), any()) }
        }
    }

    "si falla el cambio de rol se muestra el mensaje y se libera la fila" {
        runTest {
            coEvery { repo.cambiarRol(any(), any(), any()) } throws UsuarioException.AccesoDenegado()
            val vm = GestionUsuariosViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.cambiarRol(cliente, Rol.Instructor)
            advanceUntilIdle()

            vm.uiState.value.mensajeSnackbar shouldBe "No tienes permiso para hacer esta acción."
            vm.uiState.value.guardando.shouldBeEmpty()
            vm.snackbarMostrado()
            vm.uiState.value.mensajeSnackbar.shouldBeNull()
        }
    }

    // ---------- Eliminar ----------

    "el administrador no puede eliminar su propia cuenta" {
        val vm = GestionUsuariosViewModel(repo, almacenamiento)
        vm.eliminarUsuario(admin)
        vm.uiState.value.mensajeSnackbar shouldBe "No puedes eliminar tu propia cuenta"
    }

    "eliminar un usuario lo quita de la lista" {
        runTest {
            coEvery { repo.eliminarUsuario(4, 2) } returns Unit
            val vm = GestionUsuariosViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.eliminarUsuario(instructor)
            vm.eliminarUsuario(instructor)
            advanceUntilIdle()

            vm.listaMostrada() shouldBe listOf(1L, 4L)
            coVerify(exactly = 1) { repo.eliminarUsuario(any(), any()) }
        }
    }

    "si falla eliminar el usuario sigue en la lista" {
        runTest {
            coEvery { repo.eliminarUsuario(any(), any()) } throws UsuarioException.NoEncontrado()
            val vm = GestionUsuariosViewModel(repo, almacenamiento)
            advanceUntilIdle()
            vm.eliminarUsuario(instructor)
            advanceUntilIdle()

            vm.listaMostrada() shouldBe listOf(1L, 2L, 4L)
            vm.uiState.value.mensajeSnackbar shouldBe "No encontramos ese usuario."
        }
    }

    "la Factory crea el ViewModel con sus dependencias" {
        GestionUsuariosViewModel.Factory
            .create(GestionUsuariosViewModel::class.java, extrasConAplicacion(aplicacionDePrueba()))
            .shouldBeInstanceOf<GestionUsuariosViewModel>()
    }
})
