package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.MENSAJE_SIN_CONEXION
import com.example.mastermartini.data.recetasDePrueba
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

// Busca sobre las 6 recetas inventadas de RecetasDePrueba.kt que entrega el servidor simulado
@OptIn(ExperimentalCoroutinesApi::class)
class BuscarViewModelTest : StringSpec({

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockkConstructor(CursoRepository::class)
        coEvery { anyConstructed<CursoRepository>().obtenerRecetas(any()) } returns Result.success(recetasDePrueba)
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ViewModel con las recetas ya cargadas
    fun TestScope.buscadorListo(): BuscarViewModel {
        val vm = BuscarViewModel()
        advanceUntilIdle()
        return vm
    }

    "al abrir parte en Cargando; luego no hay resultados y se sugieren las categorías del servidor" {
        runTest {
            val vm = BuscarViewModel()
            vm.uiState.value.estado shouldBe EstadoUi.Cargando
            advanceUntilIdle()
            vm.uiState.value.estado shouldBe EstadoUi.Exito(emptyList())
            vm.uiState.value.resultados.shouldBeEmpty()
            vm.uiState.value.categorias shouldContainExactly
                listOf("Chocolatería", "Pastelería", "Panadería", "Heladería")
        }
    }

    "busca por insumo sin importar mayúsculas" {
        runTest {
            val vm = buscadorListo()
            vm.onTextoChange("COBERTURA SELVA NEGRA")
            vm.uiState.value.texto shouldBe "COBERTURA SELVA NEGRA"
            vm.uiState.value.resultados.map { it.id } shouldContainExactly listOf(1, 6)
        }
    }

    "busca por categoría aunque se escriba sin tilde" {
        runTest {
            val vm = buscadorListo()
            vm.onTextoChange("pasteleria")
            vm.uiState.value.resultados.map { it.id } shouldContainExactly listOf(2, 4)
        }
    }

    "busca por título sin tilde y dentro de los ingredientes" {
        runTest {
            val vm = buscadorListo()
            vm.onTextoChange("lucuma")
            vm.uiState.value.resultados.map { it.id } shouldContainExactly listOf(5)
            vm.onTextoChange("almendras")
            vm.uiState.value.resultados.map { it.id } shouldContainExactly listOf(4)
        }
    }

    "un texto vacío o sin coincidencias no muestra resultados" {
        runTest {
            val vm = buscadorListo()
            vm.onTextoChange("   ")
            vm.uiState.value.resultados.shouldBeEmpty()
            vm.onTextoChange("pizza napolitana")
            vm.uiState.value.resultados.shouldBeEmpty()
        }
    }

    "limpiar borra el texto y los resultados" {
        runTest {
            val vm = buscadorListo()
            vm.onTextoChange("crema")
            vm.limpiar()
            vm.uiState.value.texto shouldBe ""
            vm.uiState.value.resultados.shouldBeEmpty()
        }
    }

    "lo escrito mientras carga se busca apenas llegan las recetas" {
        runTest {
            val vm = BuscarViewModel()
            vm.onTextoChange("pan")
            vm.uiState.value.estado shouldBe EstadoUi.Cargando
            vm.uiState.value.texto shouldBe "pan"
            advanceUntilIdle()
            vm.uiState.value.resultados.map { it.id } shouldContainExactly listOf(3)
        }
    }

    "si el servidor no responde se muestra el error y Reintentar fuerza la recarga" {
        runTest {
            coEvery { anyConstructed<CursoRepository>().obtenerRecetas(false) } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            val vm = BuscarViewModel()
            advanceUntilIdle()
            vm.uiState.value.estado shouldBe EstadoUi.Error(MENSAJE_SIN_CONEXION)
            vm.onTextoChange("helado")
            vm.uiState.value.estado shouldBe EstadoUi.Error(MENSAJE_SIN_CONEXION)

            vm.reintentar()
            advanceUntilIdle()

            vm.uiState.value.resultados.map { it.id } shouldContainExactly listOf(5)
            coVerify(exactly = 1) { anyConstructed<CursoRepository>().obtenerRecetas(true) }
        }
    }
})
