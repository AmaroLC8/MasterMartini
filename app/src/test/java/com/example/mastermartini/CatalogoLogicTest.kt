package com.example.mastermartini

import android.app.Application
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.CatalogoCache
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.InscripcionRepository
import com.example.mastermartini.data.cursoGratisDePrueba
import com.example.mastermartini.data.cursoProDePrueba
import com.example.mastermartini.data.recetasDePrueba
import com.example.mastermartini.data.remote.CursosApiService
import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.viewmodel.CapacitacionDetalleViewModel
import com.example.mastermartini.viewmodel.CapacitacionesViewModel
import com.example.mastermartini.viewmodel.EstadoUi
import com.example.mastermartini.viewmodel.FILTRO_GRATIS
import com.example.mastermartini.viewmodel.FILTRO_PLAN_PRO
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogoLogicTest : StringSpec({

    // Los ViewModels reciben Application: basta un mock relaxed
    val mockApp = mockk<Application>(relaxed = true)
    // Los cursos ya no están en la app: vienen del servidor simulado con estos cursos inventados
    val cursosDelServidor = listOf(cursoGratisDePrueba, cursoProDePrueba)

    beforeTest { Dispatchers.setMain(StandardTestDispatcher()) }
    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // Prepara un ViewModel del catálogo cuyo repositorio responde con cursosDelServidor
    fun catalogoConServidorSimulado(): CapacitacionesViewModel {
        mockkConstructor(CapacitacionRepository::class, InscripcionRepository::class)
        coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionesRemotas() } returns
            Result.success(cursosDelServidor)
        coEvery { anyConstructed<InscripcionRepository>().misInscripciones(any()) } returns Result.success(emptyList())
        return CapacitacionesViewModel(mockApp)
    }

    fun CapacitacionesViewModel.cursosMostrados(): List<Capacitacion> {
        val estado = uiState.value.estado
        estado.shouldBeInstanceOf<EstadoUi.Exito<List<Capacitacion>>>()
        return estado.datos
    }

    // PRUEBA 5: el repositorio de recetas entrega lo que responde el servidor (ya no hay lista local)
    "5. CursoRepository debe retornar las recetas que entrega el servidor" {
        runTest {
            CatalogoCache.limpiar()
            val api = mockk<CursosApiService>()
            mockkObject(RetrofitClient)
            every { RetrofitClient.cursosApi } returns api
            coEvery { api.obtenerRecetas() } returns Response.success(recetasDePrueba)

            val recetas = CursoRepository().obtenerRecetas().getOrThrow()

            recetas.size shouldBe 6
            CatalogoCache.limpiar()
        }
    }

    // PRUEBA 6: el repositorio de capacitaciones entrega lo que responde el servidor
    "6. CapacitacionRepository debe retornar la lista de capacitaciones del servidor" {
        runTest {
            val api = mockk<CursosApiService>()
            mockkObject(RetrofitClient)
            every { RetrofitClient.cursosApi } returns api
            coEvery { api.obtenerCapacitaciones() } returns Response.success(cursosDelServidor)

            val capacitaciones = CapacitacionRepository().obtenerCapacitacionesRemotas().getOrThrow()

            capacitaciones.map { it.id } shouldContainExactly listOf(1, 4)
        }
    }

    // PRUEBA 7: búsqueda por ID en el servidor
    "7. CapacitacionRepository debe encontrar una capacitación existente por su ID en el servidor" {
        runTest {
            val api = mockk<CursosApiService>()
            mockkObject(RetrofitClient)
            every { RetrofitClient.cursosApi } returns api
            coEvery { api.obtenerCapacitacion(1) } returns Response.success(cursoGratisDePrueba)

            val curso = CapacitacionRepository().obtenerCapacitacionPorIdRemoto(1).getOrThrow()

            curso.id shouldBe 1
        }
    }

    // PRUEBA 8: filtro Freemium con los cursos que llegan del repositorio simulado
    "8. CapacitacionesViewModel debe separar cursos gratuitos y Plan Pro que vienen del servidor" {
        runTest {
            val viewModel = catalogoConServidorSimulado()
            advanceUntilIdle()

            viewModel.seleccionarFiltro(FILTRO_GRATIS)
            viewModel.uiState.value.filtroSeleccionado shouldBe FILTRO_GRATIS
            viewModel.cursosMostrados().map { it.id } shouldContainExactly listOf(1)

            viewModel.seleccionarFiltro(FILTRO_PLAN_PRO)
            viewModel.cursosMostrados().map { it.id } shouldContainExactly listOf(4)
        }
    }

    // PRUEBA 9: las categorías de los filtros salen de los cursos del servidor
    "9. CapacitacionesViewModel debe armar los filtros con las categorías de los cursos del servidor" {
        runTest {
            val viewModel = catalogoConServidorSimulado()
            advanceUntilIdle()

            viewModel.uiState.value.filtros shouldContainExactly
                listOf("Todos", "Gratis", "Plan Pro", "Chocolatería", "Panadería")
        }
    }

    // PRUEBA 10: Despliegue de módulos en CapacitacionDetalleViewModel
    "10. CapacitacionDetalleViewModel debe abrir y cerrar módulos al llamar alternarModulo" {
        val viewModel = CapacitacionDetalleViewModel(mockApp)
        viewModel.alternarModulo(1)
        viewModel.uiState.value.modulosAbiertos shouldContain 1

        viewModel.alternarModulo(1)
        viewModel.uiState.value.modulosAbiertos.contains(1).shouldBeFalse()
    }
})
