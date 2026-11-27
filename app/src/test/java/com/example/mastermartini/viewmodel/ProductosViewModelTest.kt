package com.example.mastermartini.viewmodel

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

// Usa el catálogo local real de productos (no necesita mocks)
class ProductosViewModelTest : StringSpec({

    "agrupa los 18 productos por línea en el orden del catálogo" {
        val porLinea = ProductosViewModel().productosPorLinea.value
        porLinea.keys.toList() shouldBe listOf(
            "Margarinas", "Mantecas", "Cremas vegetales",
            "Chocolates y coberturas", "Pastelería instantánea", "Fritura"
        )
        porLinea.values.sumOf { it.size } shouldBe 18
        porLinea.getValue("Margarinas").map { it.id } shouldBe listOf(1, 2)
    }
})
