package com.example.mastermartini.viewmodel

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

// Usa el catálogo local real de recetas (no necesita mocks)
class BuscarViewModelTest : StringSpec({

    "al abrir no hay resultados y se sugieren las seis categorías" {
        val estado = BuscarViewModel().uiState.value
        estado.resultados.shouldBeEmpty()
        estado.categorias shouldHaveSize 6
    }

    "busca por insumo sin importar mayúsculas" {
        val vm = BuscarViewModel()
        vm.onTextoChange("CHOCOLATE ARIBA")
        vm.uiState.value.texto shouldBe "CHOCOLATE ARIBA"
        vm.uiState.value.resultados.map { it.id } shouldContainAll listOf(1, 6)
    }

    "busca por categoría aunque se escriba sin tilde" {
        val vm = BuscarViewModel()
        vm.onTextoChange("pasteleria")
        vm.uiState.value.resultados.map { it.id } shouldContainAll listOf(6, 8)
    }

    "busca dentro de los ingredientes" {
        val vm = BuscarViewModel()
        vm.onTextoChange("almendras")
        vm.uiState.value.resultados.map { it.id } shouldContainAll listOf(1)
    }

    "un texto vacío o sin coincidencias no muestra resultados" {
        val vm = BuscarViewModel()
        vm.onTextoChange("   ")
        vm.uiState.value.resultados.shouldBeEmpty()
        vm.onTextoChange("pizza napolitana")
        vm.uiState.value.resultados.shouldBeEmpty()
    }

    "limpiar borra el texto y los resultados" {
        val vm = BuscarViewModel()
        vm.onTextoChange("crema")
        vm.limpiar()
        vm.uiState.value.texto shouldBe ""
        vm.uiState.value.resultados.shouldBeEmpty()
    }
})
