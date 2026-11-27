package com.example.mastermartini.viewmodel

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

// Usa el catálogo local real de 10 recetas (no necesita mocks)
class RecetasViewModelTest : StringSpec({

    fun minutos(duracion: String) = duracion.filter { it.isDigit() }.toInt()

    "al abrir se ven las 10 recetas en orden alfabético" {
        val estado = RecetasViewModel().uiState.value
        estado.recetas shouldHaveSize 10
        estado.categoriaSeleccionada.shouldBeNull()
        estado.orden shouldBe OrdenRecetas.ALFABETICO
        estado.recetas.map { it.titulo } shouldBe estado.recetas.map { it.titulo }.sorted()
    }

    "las secciones siguen el orden de las categorías del repositorio" {
        val estado = RecetasViewModel().uiState.value
        estado.categorias shouldHaveSize 6
        estado.secciones.keys.toList() shouldBe estado.categorias
        estado.secciones.values.sumOf { it.size } shouldBe 10
    }

    "elegir una categoría muestra solo sus recetas y una sola sección" {
        val vm = RecetasViewModel()
        vm.seleccionarCategoria("Pastelería")
        val estado = vm.uiState.value
        estado.categoriaSeleccionada shouldBe "Pastelería"
        estado.recetas.map { it.id }.sorted() shouldContainExactly listOf(6, 8)
        estado.secciones.keys shouldBe setOf("Pastelería")

        vm.seleccionarCategoria(null)
        vm.uiState.value.recetas shouldHaveSize 10
    }

    "ordenar por duración deja primero las recetas más cortas" {
        val vm = RecetasViewModel()
        vm.cambiarOrden(OrdenRecetas.DURACION)
        val duraciones = vm.uiState.value.recetas.map { minutos(it.duracion) }
        duraciones shouldBe duraciones.sorted()
        vm.uiState.value.orden shouldBe OrdenRecetas.DURACION
    }

    "el filtro de veganas muestra solo recetas veganas y se puede quitar" {
        val vm = RecetasViewModel()
        vm.alternarSoloVeganas()
        vm.uiState.value.soloVeganas.shouldBeTrue()
        vm.uiState.value.recetas.map { it.id }.sorted() shouldContainExactly listOf(5, 8, 9)

        vm.alternarSoloVeganas()
        vm.uiState.value.soloVeganas.shouldBeFalse()
        vm.uiState.value.recetas shouldHaveSize 10
    }

    "los filtros se combinan y se conservan al cambiar el orden" {
        val vm = RecetasViewModel()
        vm.seleccionarCategoria("Pastelería")
        vm.alternarSoloVeganas()
        vm.cambiarOrden(OrdenRecetas.DURACION)
        val estado = vm.uiState.value
        estado.recetas.map { it.id } shouldBe listOf(8)
        estado.categoriaSeleccionada shouldBe "Pastelería"
        estado.soloVeganas.shouldBeTrue()
    }
})
