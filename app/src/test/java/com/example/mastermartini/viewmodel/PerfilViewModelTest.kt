package com.example.mastermartini.viewmodel

import android.graphics.Bitmap
import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.ClimaRepository
import com.example.mastermartini.data.FotoRepository
import com.example.mastermartini.data.local.FavoritoDao
import com.example.mastermartini.data.local.MasterMartiniDatabase
import com.example.mastermartini.model.Rol
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelTest : StringSpec({

    val app = aplicacionDePrueba()
    val favoritoDao = mockk<FavoritoDao>()
    val baseDeDatos = mockk<MasterMartiniDatabase>()

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        // Room se reemplaza: getInstance devuelve una base de datos de mentira
        mockkObject(MasterMartiniDatabase.Companion)
        every { MasterMartiniDatabase.getInstance(any()) } returns baseDeDatos
        every { baseDeDatos.favoritoDao() } returns favoritoDao
        every { favoritoDao.contarFavoritos() } returns flowOf(3)

        mockkConstructor(AlmacenamientoLocal::class, FotoRepository::class, ClimaRepository::class)
        coEvery { anyConstructed<FotoRepository>().obtenerFoto() } returns null
        coEvery { anyConstructed<ClimaRepository>().obtenerTemperaturaActual() } returns null
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    "al abrir se cuenta la cantidad de favoritos guardados en Room" {
        runTest {
            val vm = PerfilViewModel(app)
            advanceUntilIdle()
            vm.uiState.value.totalFavoritos shouldBe 3
        }
    }

    "sin datos del clima no se muestra la recomendación del taller" {
        runTest {
            val vm = PerfilViewModel(app)
            advanceUntilIdle()
            vm.uiState.value.condicionesTaller shouldBe ""
        }
    }

    "la recomendación del taller cambia según la temperatura" {
        runTest {
            val casos = listOf(
                30.4 to "Condiciones del taller hoy en Santiago: 30 °C — Mantén las cremas refrigeradas antes de batir",
                12.0 to "Condiciones del taller hoy en Santiago: 12 °C — Ideal para templado de chocolate y laminado",
                20.0 to "Condiciones del taller hoy en Santiago: 20 °C — Ideal para templado de chocolate"
            )
            for ((temperatura, texto) in casos) {
                coEvery { anyConstructed<ClimaRepository>().obtenerTemperaturaActual() } returns temperatura
                val vm = PerfilViewModel(app)
                advanceUntilIdle()
                vm.uiState.value.condicionesTaller shouldBe texto
            }
        }
    }

    "cargarPerfil junta la sesión, la encuesta, el rol y la foto" {
        runTest {
            val foto = mockk<Bitmap>()
            every { anyConstructed<AlmacenamientoLocal>().obtenerNombre() } returns "Ana"
            every { anyConstructed<AlmacenamientoLocal>().obtenerCorreo() } returns "ana@example.com"
            every { anyConstructed<AlmacenamientoLocal>().obtenerPersonasNegocio() } returns "2 a 5 personas"
            every { anyConstructed<AlmacenamientoLocal>().obtenerInteresesEncuesta() } returns
                setOf("Pastelería Fina", "Chocolatería y Coberturas")
            every { anyConstructed<AlmacenamientoLocal>().existeEncuesta() } returns true
            every { anyConstructed<AlmacenamientoLocal>().obtenerRol() } returns Rol.Ventas
            every { anyConstructed<AlmacenamientoLocal>().tienePlanPro() } returns true
            coEvery { anyConstructed<FotoRepository>().obtenerFoto() } returns foto
            val vm = PerfilViewModel(app)

            vm.cargarPerfil()
            advanceUntilIdle()

            val estado = vm.uiState.value
            estado.nombre shouldBe "Ana"
            estado.correo shouldBe "ana@example.com"
            estado.personasNegocio shouldBe "2 a 5 personas"
            estado.intereses shouldBe listOf("Chocolatería y Coberturas", "Pastelería Fina")
            estado.respondioEncuesta.shouldBeTrue()
            estado.rol shouldBe "Ventas"
            estado.planPro.shouldBeTrue()
            estado.opcionesPanel.map { it.ruta } shouldBe listOf("panel/clientes")
            estado.foto shouldBe foto
        }
    }

    "si no indicó cuántas personas trabajan se muestra No indicado" {
        every { anyConstructed<AlmacenamientoLocal>().obtenerPersonasNegocio() } returns "Prefiero no decirlo"
        val vm = PerfilViewModel(app)
        vm.cargarPerfil()
        vm.uiState.value.personasNegocio shouldBe "No indicado"

        every { anyConstructed<AlmacenamientoLocal>().obtenerPersonasNegocio() } returns ""
        vm.cargarPerfil()
        vm.uiState.value.personasNegocio shouldBe "No indicado"
    }

    "cerrarSesion borra la sesión guardada" {
        every { anyConstructed<AlmacenamientoLocal>().cerrarSesion() } just Runs
        val vm = PerfilViewModel(app)
        vm.cerrarSesion()
        verify { anyConstructed<AlmacenamientoLocal>().cerrarSesion() }
    }
})
