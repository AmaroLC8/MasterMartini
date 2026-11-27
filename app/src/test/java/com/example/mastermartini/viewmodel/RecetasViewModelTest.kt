package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.MENSAJE_SIN_CONEXION
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.data.recetasDePrueba
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

// Las recetas vienen del servidor simulado con las 6 recetas inventadas de RecetasDePrueba.kt
@OptIn(ExperimentalCoroutinesApi::class)
class RecetasViewModelTest : StringSpec({

    fun minutos(duracion: String) = duracion.filter { it.isDigit() }.toInt()

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockkConstructor(CursoRepository::class)
        coEvery { anyConstructed<CursoRepository>().obtenerRecetas(any()) } returns Result.success(recetasDePrueba)
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ---------- Estados de carga ----------

    "al abrir parte en Cargando y luego muestra las 6 recetas en orden alfabético" {
        runTest {
            val vm = RecetasViewModel()
            vm.uiState.value.estado shouldBe EstadoUi.Cargando
            vm.uiState.value.recetas.shouldBeEmpty()
            advanceUntilIdle()

            val estado = vm.uiState.value
            estado.recetas shouldHaveSize 6
            estado.categoriaSeleccionada.shouldBeNull()
            estado.orden shouldBe OrdenRecetas.ALFABETICO
            estado.recetas.map { it.id } shouldContainExactly listOf(2, 1, 5, 3, 4, 6)
            // Al abrir usa la memoria si ya hay recetas (no fuerza la recarga)
            coVerify(exactly = 1) { anyConstructed<CursoRepository>().obtenerRecetas(false) }
        }
    }

    "si el servidor no responde se muestra el error y los filtros no hacen nada" {
        runTest {
            coEvery { anyConstructed<CursoRepository>().obtenerRecetas(any()) } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            val vm = RecetasViewModel()
            advanceUntilIdle()

            vm.uiState.value.estado shouldBe EstadoUi.Error(MENSAJE_SIN_CONEXION)
            vm.seleccionarCategoria("Pastelería")
            vm.cambiarOrden(OrdenRecetas.DURACION)
            vm.alternarSoloVeganas()
            vm.uiState.value.estado shouldBe EstadoUi.Error(MENSAJE_SIN_CONEXION)
            vm.uiState.value.categoriaSeleccionada.shouldBeNull()
            vm.uiState.value.soloVeganas.shouldBeFalse()
        }
    }

    "un error del servidor muestra su mensaje" {
        runTest {
            coEvery { anyConstructed<CursoRepository>().obtenerRecetas(any()) } returns
                Result.failure(ServidorException(500, "Falla interna"))
            val vm = RecetasViewModel()
            advanceUntilIdle()
            vm.uiState.value.estado shouldBe EstadoUi.Error("Falla interna")
        }
    }

    "Reintentar fuerza la recarga y muestra las recetas" {
        runTest {
            coEvery { anyConstructed<CursoRepository>().obtenerRecetas(false) } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            coEvery { anyConstructed<CursoRepository>().obtenerRecetas(true) } returns Result.success(recetasDePrueba)
            val vm = RecetasViewModel()
            advanceUntilIdle()

            vm.reintentar()
            vm.uiState.value.estado shouldBe EstadoUi.Cargando
            advanceUntilIdle()

            vm.uiState.value.recetas shouldHaveSize 6
            coVerify(exactly = 1) { anyConstructed<CursoRepository>().obtenerRecetas(true) }
        }
    }

    "al recargar se conservan los filtros; si la categoría ya no existe vuelve a Todas" {
        runTest {
            val vm = RecetasViewModel()
            advanceUntilIdle()
            vm.seleccionarCategoria("Heladería")
            vm.alternarSoloVeganas()

            // Ahora el servidor ya no tiene recetas de Heladería
            coEvery { anyConstructed<CursoRepository>().obtenerRecetas(true) } returns
                Result.success(recetasDePrueba.filter { it.categoria != "Heladería" })
            vm.reintentar()
            advanceUntilIdle()

            vm.uiState.value.categoriaSeleccionada.shouldBeNull()
            vm.uiState.value.soloVeganas.shouldBeTrue()
            vm.uiState.value.recetas.map { it.id } shouldContainExactly listOf(3, 4)
        }
    }

    // ---------- Filtros y orden ----------

    "las categorías y secciones salen de las recetas del servidor, en el orden en que llegan" {
        runTest {
            val vm = RecetasViewModel()
            advanceUntilIdle()
            val estado = vm.uiState.value
            estado.categorias shouldContainExactly listOf("Chocolatería", "Pastelería", "Panadería", "Heladería")
            estado.secciones.keys.toList() shouldBe estado.categorias
            estado.secciones.values.sumOf { it.size } shouldBe 6
        }
    }

    "elegir una categoría muestra solo sus recetas y una sola sección" {
        runTest {
            val vm = RecetasViewModel()
            advanceUntilIdle()
            vm.seleccionarCategoria("Pastelería")
            val estado = vm.uiState.value
            estado.categoriaSeleccionada shouldBe "Pastelería"
            estado.recetas.map { it.id }.sorted() shouldContainExactly listOf(2, 4)
            estado.secciones.keys shouldBe setOf("Pastelería")

            vm.seleccionarCategoria(null)
            vm.uiState.value.recetas shouldHaveSize 6
        }
    }

    "ordenar por duración deja primero las recetas más cortas" {
        runTest {
            val vm = RecetasViewModel()
            advanceUntilIdle()
            vm.cambiarOrden(OrdenRecetas.DURACION)
            val duraciones = vm.uiState.value.recetas.map { minutos(it.duracion) }
            duraciones shouldBe listOf(15, 20, 25, 40, 60, 90)
            vm.uiState.value.orden shouldBe OrdenRecetas.DURACION
        }
    }

    "el filtro de veganas muestra solo recetas veganas y se puede quitar" {
        runTest {
            val vm = RecetasViewModel()
            advanceUntilIdle()
            vm.alternarSoloVeganas()
            vm.uiState.value.soloVeganas.shouldBeTrue()
            vm.uiState.value.recetas.map { it.id }.sorted() shouldContainExactly listOf(3, 4)

            vm.alternarSoloVeganas()
            vm.uiState.value.soloVeganas.shouldBeFalse()
            vm.uiState.value.recetas shouldHaveSize 6
        }
    }

    "los filtros se combinan y se conservan al cambiar el orden" {
        runTest {
            val vm = RecetasViewModel()
            advanceUntilIdle()
            vm.seleccionarCategoria("Pastelería")
            vm.alternarSoloVeganas()
            vm.cambiarOrden(OrdenRecetas.DURACION)
            val estado = vm.uiState.value
            estado.recetas.map { it.id } shouldBe listOf(4)
            estado.categoriaSeleccionada shouldBe "Pastelería"
            estado.soloVeganas.shouldBeTrue()
        }
    }

    "una duración sin números cuenta como 0 minutos al ordenar" {
        runTest {
            coEvery { anyConstructed<CursoRepository>().obtenerRecetas(any()) } returns
                Result.success(listOf(recetasDePrueba[0], recetasDePrueba[1].copy(duracion = "rápida")))
            val vm = RecetasViewModel()
            advanceUntilIdle()
            vm.cambiarOrden(OrdenRecetas.DURACION)
            vm.uiState.value.recetas.map { it.id } shouldContainExactly listOf(2, 1)
        }
    }
})
