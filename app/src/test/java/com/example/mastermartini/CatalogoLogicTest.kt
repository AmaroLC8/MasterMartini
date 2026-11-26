package com.example.mastermartini

import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.model.Capacitacion
import com.example.mastermartini.viewmodel.CapacitacionDetalleViewModel
import com.example.mastermartini.viewmodel.CapacitacionesViewModel
import com.example.mastermartini.viewmodel.FILTRO_GRATIS
import com.example.mastermartini.viewmodel.FILTRO_PLAN_PRO
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogoLogicTest : StringSpec({

    val testDispatcher = StandardTestDispatcher()

    beforeTest { Dispatchers.setMain(testDispatcher) }
    afterTest { Dispatchers.resetMain() }

    // PRUEBA 5: Repositorio de recetas/cursos retorna las 10 recetas
    "5. CursoRepository debe retornar el catálogo completo de 10 recetas técnicas" {
        val repository = CursoRepository()
        val cursos = repository.obtenerCursos()
        cursos.size shouldBe 10
    }

    // PRUEBA 6: Repositorio de capacitaciones retorna listado de cursos online
    "6. CapacitacionRepository debe retornar la lista de capacitaciones disponibles" {
        val repository = CapacitacionRepository()
        val capacitaciones = repository.obtenerCapacitaciones()
        capacitaciones.shouldNotBeEmpty()
    }

    // PRUEBA 7: Búsqueda por ID en CapacitacionRepository
    "7. CapacitacionRepository debe encontrar una capacitación existente por su ID" {
        val repository = CapacitacionRepository()
        val curso = repository.obtenerCapacitacionPorId(1)
        curso.shouldNotBeNull()
        curso.id shouldBe 1
    }

    // PRUEBA 8: Simulación con MockK de repositorio remoto (Modelo Freemium)
    "8. MockK debe simular correctamente cursos gratuitos y Plan Pro desde CapacitacionRepository" {
        runTest {
            val mockRepo = mockk<CapacitacionRepository>()
            val listaSimulada = listOf(
                mockk<Capacitacion> { coEvery { esGratis } returns true },
                mockk<Capacitacion> { coEvery { esGratis } returns false }
            )
            coEvery { mockRepo.obtenerCapacitacionesRemotas() } returns listaSimulada

            val resultado = mockRepo.obtenerCapacitacionesRemotas()
            resultado.size shouldBe 2
            resultado.first().esGratis.shouldBeTrue()
            resultado.last().esGratis.shouldBeFalse()
        }
    }

    // PRUEBA 9: Filtro Freemium en CapacitacionesViewModel
    "9. CapacitacionesViewModel debe filtrar correctamente los cursos gratuitos y Plan Pro" {
        val viewModel = CapacitacionesViewModel()

        viewModel.seleccionarFiltro(FILTRO_GRATIS)
        viewModel.uiState.value.filtroSeleccionado shouldBe FILTRO_GRATIS
        viewModel.uiState.value.capacitaciones.all { it.esGratis }.shouldBeTrue()

        viewModel.seleccionarFiltro(FILTRO_PLAN_PRO)
        viewModel.uiState.value.capacitaciones.all { !it.esGratis }.shouldBeTrue()
    }

    // PRUEBA 10: Despliegue de módulos en CapacitacionDetalleViewModel
    "10. CapacitacionDetalleViewModel debe abrir y cerrar módulos al llamar alternarModulo" {
        val viewModel = CapacitacionDetalleViewModel()
        viewModel.alternarModulo(1)
        viewModel.uiState.value.modulosAbiertos shouldContain 1

        viewModel.alternarModulo(1)
        viewModel.uiState.value.modulosAbiertos.contains(1).shouldBeFalse()
    }
})