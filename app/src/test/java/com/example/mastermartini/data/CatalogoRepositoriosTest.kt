package com.example.mastermartini.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

// Catálogos locales: recetas (CursoRepository), productos y capacitaciones sin red
class CatalogoRepositoriosTest : StringSpec({

    // ---------- CursoRepository ----------

    "CursoRepository tiene 10 recetas, todas con pasos e ingredientes" {
        val cursos = CursoRepository().obtenerCursos()
        cursos shouldHaveSize 10
        cursos.all { it.pasos.isNotEmpty() && it.ingredientes.isNotEmpty() } shouldBe true
    }

    "CursoRepository entrega las seis categorías" {
        CursoRepository().obtenerCategorias() shouldContainExactly
            listOf("Chocolatería", "Pastelería", "Panadería", "Heladería", "Decoración", "Cocina")
    }

    "CursoRepository busca una receta por id y devuelve null si no existe" {
        val repo = CursoRepository()
        repo.obtenerCursoPorId(1)!!.insumo shouldBe "Chocolate Ariba"
        repo.obtenerCursoPorId(99).shouldBeNull()
    }

    // ---------- ProductoRepository ----------

    "ProductoRepository tiene 18 productos y cada uno pertenece a una línea conocida" {
        val repo = ProductoRepository()
        val productos = repo.obtenerProductos()
        productos shouldHaveSize 18
        repo.obtenerLineas() shouldHaveSize 6
        repo.obtenerLineas().shouldContainAll(productos.map { it.linea }.distinct())
    }

    "ProductoRepository busca un producto por id" {
        val repo = ProductoRepository()
        repo.obtenerProductoPorId(9)!!.nombre shouldBe "Chocolate Ariba"
        repo.obtenerProductoPorId(99).shouldBeNull()
    }

    "ProductoRepository busca un producto por nombre exacto" {
        val repo = ProductoRepository()
        repo.obtenerProductoPorNombre("Bravo Crem")!!.id shouldBe 4
        repo.obtenerProductoPorNombre("Producto inventado").shouldBeNull()
    }

    "obtenerRecetasQueUsan encuentra las recetas que usan el producto" {
        val repo = ProductoRepository()
        val chocolate = repo.obtenerProductoPorId(9)!!
        repo.obtenerRecetasQueUsan(chocolate).map { it.id } shouldContainExactly listOf(1, 6)
    }

    "obtenerRecetasQueUsan devuelve una lista vacía si ninguna receta usa el producto" {
        val repo = ProductoRepository()
        val vitaFrit = repo.obtenerProductoPorNombre("Vita Frit")!!
        repo.obtenerRecetasQueUsan(vitaFrit).shouldBeEmpty()
    }

    // ---------- CapacitacionRepository (datos locales) ----------

    "CapacitacionRepository local tiene 6 cursos: 3 gratis y 3 Plan Pro" {
        val capacitaciones = CapacitacionRepository().obtenerCapacitaciones()
        capacitaciones shouldHaveSize 6
        capacitaciones.count { it.esGratis } shouldBe 3
    }

    "CapacitacionRepository entrega las cinco categorías" {
        CapacitacionRepository().obtenerCategorias() shouldHaveSize 5
    }

    "la capacitación 1 tiene 9 lecciones y su primer módulo dura 30 minutos" {
        val capacitacion = CapacitacionRepository().obtenerCapacitacionPorId(1)
        capacitacion.shouldNotBeNull()
        capacitacion.totalLecciones shouldBe 9
        capacitacion.modulos.first().totalMinutos shouldBe 30
    }

    "obtenerCapacitacionPorId devuelve null si el id no existe" {
        CapacitacionRepository().obtenerCapacitacionPorId(99).shouldBeNull()
    }
})
