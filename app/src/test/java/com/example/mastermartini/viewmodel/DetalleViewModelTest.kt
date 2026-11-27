package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.local.ChecklistDao
import com.example.mastermartini.data.local.ChecklistIngredienteEntity
import com.example.mastermartini.data.local.ChecklistPasoEntity
import com.example.mastermartini.data.local.MasterMartiniDatabase
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleViewModelTest : StringSpec({

    val app = aplicacionDePrueba()
    val checklistDao = mockk<ChecklistDao>(relaxed = true)
    val baseDeDatos = mockk<MasterMartiniDatabase>()

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        clearMocks(checklistDao)
        mockkObject(MasterMartiniDatabase.Companion)
        every { MasterMartiniDatabase.getInstance(any()) } returns baseDeDatos
        every { baseDeDatos.checklistDao() } returns checklistDao
        // En la receta 1 están marcados los ingredientes 0 y 2, y completado el paso 1
        every { checklistDao.obtenerMarcados(1) } returns flowOf(listOf(0, 2))
        every { checklistDao.obtenerPasosCompletados(1) } returns flowOf(listOf(1))
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    "cargarCurso muestra la receta, su producto y lo guardado en Room" {
        runTest {
            val vm = DetalleViewModel(app)
            vm.cargarCurso(1)
            vm.curso.value?.titulo shouldBe "Curva de Templado en Chocolatería"
            vm.productoInsumo.value?.nombre shouldBe "Chocolate Ariba"
            advanceUntilIdle()
            vm.marcados.value shouldBe listOf(0, 2)
            vm.pasosCompletados.value shouldBe listOf(1)
        }
    }

    "una receta que no existe deja la pantalla vacía" {
        runTest {
            every { checklistDao.obtenerMarcados(99) } returns flowOf(emptyList())
            every { checklistDao.obtenerPasosCompletados(99) } returns flowOf(emptyList())
            val vm = DetalleViewModel(app)
            vm.cargarCurso(99)
            advanceUntilIdle()
            vm.curso.value.shouldBeNull()
            vm.productoInsumo.value.shouldBeNull()
        }
    }

    "tocar un ingrediente marcado lo desmarca y uno sin marcar lo marca" {
        runTest {
            val vm = DetalleViewModel(app)
            vm.cargarCurso(1)
            advanceUntilIdle()
            vm.alternarIngrediente(2)
            vm.alternarIngrediente(1)
            advanceUntilIdle()
            coVerify { checklistDao.guardar(ChecklistIngredienteEntity(cursoId = 1, indice = 2, marcado = false)) }
            coVerify { checklistDao.guardar(ChecklistIngredienteEntity(cursoId = 1, indice = 1, marcado = true)) }
        }
    }

    "tocar un paso cambia su estado completado" {
        runTest {
            val vm = DetalleViewModel(app)
            vm.cargarCurso(1)
            advanceUntilIdle()
            vm.alternarPaso(1)
            vm.alternarPaso(3)
            advanceUntilIdle()
            coVerify { checklistDao.guardarPaso(ChecklistPasoEntity(cursoId = 1, indice = 1, completado = false)) }
            coVerify { checklistDao.guardarPaso(ChecklistPasoEntity(cursoId = 1, indice = 3, completado = true)) }
        }
    }

    "los botones de reiniciar limpian ingredientes y pasos de la receta" {
        runTest {
            val vm = DetalleViewModel(app)
            vm.cargarCurso(1)
            vm.reiniciarChecklist()
            vm.reiniciarPasos()
            advanceUntilIdle()
            coVerify { checklistDao.reiniciar(1) }
            coVerify { checklistDao.reiniciarPasos(1) }
        }
    }

    "sin receta cargada los botones no hacen nada" {
        runTest {
            val vm = DetalleViewModel(app)
            vm.alternarIngrediente(0)
            vm.alternarPaso(0)
            vm.reiniciarChecklist()
            vm.reiniciarPasos()
            advanceUntilIdle()
            coVerify(exactly = 0) { checklistDao.guardar(any()) }
            coVerify(exactly = 0) { checklistDao.guardarPaso(any()) }
            coVerify(exactly = 0) { checklistDao.reiniciar(any()) }
            coVerify(exactly = 0) { checklistDao.reiniciarPasos(any()) }
        }
    }
})
