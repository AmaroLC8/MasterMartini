package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.MENSAJE_SIN_CONEXION
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.data.productosDePrueba
import com.example.mastermartini.model.Producto
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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

// Los productos vienen del servidor simulado con los 6 productos inventados de RecetasDePrueba.kt
@OptIn(ExperimentalCoroutinesApi::class)
class ProductosViewModelTest : StringSpec({

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockkConstructor(ProductoRepository::class)
        coEvery { anyConstructed<ProductoRepository>().obtenerProductos(any()) } returns Result.success(productosDePrueba)
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    "parte en Cargando y agrupa los productos por línea en el orden en que llegan" {
        runTest {
            val vm = ProductosViewModel()
            vm.productosPorLinea.value shouldBe EstadoUi.Cargando
            advanceUntilIdle()

            val estado = vm.productosPorLinea.value
            estado.shouldBeInstanceOf<EstadoUi.Exito<Map<String, List<Producto>>>>()
            val porLinea = estado.datos
            porLinea.keys.toList() shouldBe listOf("Chocolates y coberturas", "Cremas vegetales", "Harinas", "Mantecas")
            porLinea.values.sumOf { it.size } shouldBe 6
            porLinea.getValue("Chocolates y coberturas").map { it.id } shouldBe listOf(1, 5)
        }
    }

    "si el servidor no responde se muestra el error y Reintentar fuerza la recarga" {
        runTest {
            coEvery { anyConstructed<ProductoRepository>().obtenerProductos(false) } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            val vm = ProductosViewModel()
            advanceUntilIdle()
            vm.productosPorLinea.value shouldBe EstadoUi.Error(MENSAJE_SIN_CONEXION)

            vm.reintentar()
            vm.productosPorLinea.value shouldBe EstadoUi.Cargando
            advanceUntilIdle()
            vm.productosPorLinea.value.shouldBeInstanceOf<EstadoUi.Exito<Map<String, List<Producto>>>>()
            coVerify(exactly = 1) { anyConstructed<ProductoRepository>().obtenerProductos(true) }
        }
    }
})
