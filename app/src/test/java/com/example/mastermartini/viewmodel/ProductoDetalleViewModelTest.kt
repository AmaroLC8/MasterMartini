package com.example.mastermartini.viewmodel

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

// Usa el catálogo local real de productos y recetas (no necesita mocks)
class ProductoDetalleViewModelTest : StringSpec({

    "cargar un producto muestra las recetas que lo usan" {
        val vm = ProductoDetalleViewModel()
        vm.cargarProducto(9)
        vm.uiState.value.producto?.nombre shouldBe "Chocolate Ariba"
        vm.uiState.value.recetas.map { it.id } shouldBe listOf(1, 6)
    }

    "un producto que no existe deja la pantalla vacía" {
        val vm = ProductoDetalleViewModel()
        vm.cargarProducto(99)
        vm.uiState.value.producto.shouldBeNull()
        vm.uiState.value.recetas.shouldBeEmpty()
    }
})
