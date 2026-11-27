package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.AlmacenamientoLocal
import com.example.mastermartini.data.CapacitacionRepository
import com.example.mastermartini.data.InscripcionRepository
import com.example.mastermartini.model.Inscripcion
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CapacitacionesViewModelTest : StringSpec({

    val app = aplicacionDePrueba()
    val catalogoLocal = CapacitacionRepository().obtenerCapacitaciones()
    // El servidor devuelve solo 2 cursos: así se nota que se usó la lista remota
    val catalogoRemoto = catalogoLocal.filter { it.id == 1 || it.id == 4 }

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        mockkConstructor(CapacitacionRepository::class, InscripcionRepository::class, AlmacenamientoLocal::class)
        coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionesRemotas() } returns catalogoRemoto
        every { anyConstructed<AlmacenamientoLocal>().obtenerIdUsuario() } returns 5L
        coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns Result.success(emptyList())
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    "antes de responder el servidor se ve el catálogo local con todos los filtros" {
        val estado = CapacitacionesViewModel(app).uiState.value
        estado.capacitaciones shouldHaveSize 6
        estado.filtroSeleccionado shouldBe FILTRO_TODOS
        estado.filtros shouldBe listOf(
            "Todos", "Gratis", "Plan Pro", "Chocolatería", "Pastelería", "Panadería", "Heladería", "Decoración"
        )
        estado.precioPlanPro shouldBe "$9.990"
    }

    "al responder el servidor se muestra el catálogo remoto" {
        runTest {
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.uiState.value.capacitaciones.map { it.id } shouldBe listOf(1, 4)
        }
    }

    "los filtros Gratis, Plan Pro y categoría muestran los cursos correctos" {
        runTest {
            coEvery { anyConstructed<CapacitacionRepository>().obtenerCapacitacionesRemotas() } returns catalogoLocal
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()

            vm.seleccionarFiltro(FILTRO_GRATIS)
            vm.uiState.value.capacitaciones.map { it.id } shouldBe listOf(1, 2, 3)
            vm.seleccionarFiltro(FILTRO_PLAN_PRO)
            vm.uiState.value.capacitaciones.map { it.id } shouldBe listOf(4, 5, 6)
            vm.seleccionarFiltro("Panadería")
            vm.uiState.value.capacitaciones.map { it.id } shouldBe listOf(2, 4)
            vm.uiState.value.filtroSeleccionado shouldBe "Panadería"
        }
    }

    "recargar conserva el filtro elegido" {
        runTest {
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.seleccionarFiltro(FILTRO_PLAN_PRO)
            vm.recargarCapacitaciones()
            advanceUntilIdle()
            vm.uiState.value.filtroSeleccionado shouldBe FILTRO_PLAN_PRO
            vm.uiState.value.capacitaciones.map { it.id } shouldBe listOf(4)
        }
    }

    "Mis cursos muestra el título y el avance de cada inscripción" {
        runTest {
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns Result.success(
                listOf(
                    // Sin título del servidor: se usa el del catálogo. Curso 1 tiene 9 lecciones
                    Inscripcion(id = 1, cursoId = 1, leccionesCompletadas = listOf("0-0", "0-1", "0-2")),
                    Inscripcion(id = 2, cursoId = 4, cursoTitulo = "Título del servidor"),
                    // Curso que ya no está en el catálogo
                    Inscripcion(id = 3, cursoId = 77)
                )
            )
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()

            vm.uiState.value.misCursos shouldBe listOf(
                MiCurso(cursoId = 1, titulo = catalogoLocal.first().titulo, porcentaje = 33),
                MiCurso(cursoId = 4, titulo = "Título del servidor", porcentaje = 0),
                MiCurso(cursoId = 77, titulo = "Curso Master Martini", porcentaje = 0)
            )
        }
    }

    "sin sesión Mis cursos queda vacío y no se consulta el servidor" {
        runTest {
            every { anyConstructed<AlmacenamientoLocal>().obtenerIdUsuario() } returns null
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.uiState.value.misCursos.shouldBeEmpty()
            coVerify(exactly = 0) { anyConstructed<InscripcionRepository>().misInscripciones(any()) }
        }
    }

    "si falla la consulta de inscripciones el catálogo se muestra igual" {
        runTest {
            coEvery { anyConstructed<InscripcionRepository>().misInscripciones(5) } returns
                Result.failure(Exception("sin conexión"))
            val vm = CapacitacionesViewModel(app)
            advanceUntilIdle()
            vm.uiState.value.capacitaciones shouldHaveSize 2
            vm.uiState.value.misCursos.shouldBeEmpty()
        }
    }
})
