package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.MENSAJE_SIN_CONEXION
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.data.productosDePrueba
import com.example.mastermartini.data.recetasDePrueba
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
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

// Producto y recetas vienen del servidor simulado (RecetasDePrueba.kt)
@OptIn(ExperimentalCoroutinesApi::class)
class ProductoDetalleViewModelTest : StringSpec({

    val cobertura = productosDePrueba[0]

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockkConstructor(ProductoRepository::class, CursoRepository::class)
        coEvery { anyConstructed<ProductoRepository>().obtenerProductoPorId(any(), any()) } returns
            Result.failure(ServidorException(404, "Producto no encontrado"))
        coEvery { anyConstructed<ProductoRepository>().obtenerProductoPorId(1, any()) } returns Result.success(cobertura)
        coEvery { anyConstructed<CursoRepository>().obtenerRecetas(any()) } returns Result.success(recetasDePrueba)
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    "cargar un producto parte en Cargando y muestra las recetas que lo usan" {
        runTest {
            val vm = ProductoDetalleViewModel()
            vm.cargarProducto(1)
            vm.uiState.value.cargando.shouldBeTrue()
            advanceUntilIdle()

            val estado = vm.uiState.value
            estado.cargando.shouldBeFalse()
            estado.producto shouldBe cobertura
            estado.recetas.map { it.id } shouldBe listOf(1, 6)
            estado.errorRecetas.shouldBeNull()
        }
    }

    "cargar el mismo producto dos veces no lo vuelve a pedir" {
        runTest {
            val vm = ProductoDetalleViewModel()
            vm.cargarProducto(1)
            advanceUntilIdle()
            vm.cargarProducto(1)
            advanceUntilIdle()
            coVerify(exactly = 1) { anyConstructed<ProductoRepository>().obtenerProductoPorId(1, false) }
        }
    }

    "un producto que no existe (404) muestra que ya no está disponible" {
        runTest {
            val vm = ProductoDetalleViewModel()
            vm.cargarProducto(99)
            advanceUntilIdle()
            val estado = vm.uiState.value
            estado.noExiste.shouldBeTrue()
            estado.errorCarga.shouldBeNull()
            estado.producto.shouldBeNull()
            estado.recetas.shouldBeEmpty()
        }
    }

    "sin conexión se muestra el error y Reintentar fuerza la recarga" {
        runTest {
            coEvery { anyConstructed<ProductoRepository>().obtenerProductoPorId(1, false) } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            val vm = ProductoDetalleViewModel()
            vm.cargarProducto(1)
            advanceUntilIdle()
            vm.uiState.value.errorCarga shouldBe MENSAJE_SIN_CONEXION
            vm.uiState.value.noExiste.shouldBeFalse()

            vm.reintentar()
            advanceUntilIdle()
            vm.uiState.value.producto shouldBe cobertura
            coVerify(exactly = 1) { anyConstructed<ProductoRepository>().obtenerProductoPorId(1, true) }
            coVerify(exactly = 1) { anyConstructed<CursoRepository>().obtenerRecetas(true) }
        }
    }

    "si solo fallan las recetas, el producto se muestra igual con el aviso" {
        runTest {
            coEvery { anyConstructed<CursoRepository>().obtenerRecetas(any()) } returns
                Result.failure(ServidorException(500, "Falla interna"))
            val vm = ProductoDetalleViewModel()
            vm.cargarProducto(1)
            advanceUntilIdle()
            vm.uiState.value.producto shouldBe cobertura
            vm.uiState.value.recetas.shouldBeEmpty()
            vm.uiState.value.errorRecetas shouldBe "Falla interna"
        }
    }

    "un producto que ninguna receta usa muestra la lista vacía sin error" {
        runTest {
            coEvery { anyConstructed<ProductoRepository>().obtenerProductoPorId(4, any()) } returns
                Result.success(productosDePrueba[3])
            val vm = ProductoDetalleViewModel()
            vm.cargarProducto(4)
            advanceUntilIdle()
            vm.uiState.value.recetas.shouldBeEmpty()
            vm.uiState.value.errorRecetas.shouldBeNull()
        }
    }

    "Reintentar sin haber abierto un producto no hace nada" {
        runTest {
            val vm = ProductoDetalleViewModel()
            vm.reintentar()
            advanceUntilIdle()
            coVerify(exactly = 0) { anyConstructed<ProductoRepository>().obtenerProductoPorId(any(), any()) }
        }
    }
})
