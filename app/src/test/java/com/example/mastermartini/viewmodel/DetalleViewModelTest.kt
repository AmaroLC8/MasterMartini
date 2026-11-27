package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.MENSAJE_SIN_CONEXION
import com.example.mastermartini.data.ProductoRepository
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.data.local.ChecklistDao
import com.example.mastermartini.data.local.ChecklistIngredienteEntity
import com.example.mastermartini.data.local.ChecklistPasoEntity
import com.example.mastermartini.data.local.MasterMartiniDatabase
import com.example.mastermartini.data.productosDePrueba
import com.example.mastermartini.data.recetasDePrueba
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
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
        // Receta y producto vienen del servidor simulado (RecetasDePrueba.kt)
        mockkConstructor(CursoRepository::class, ProductoRepository::class)
        coEvery { anyConstructed<CursoRepository>().obtenerRecetaPorId(any(), any()) } returns
            Result.failure(ServidorException(404, "Receta no encontrada"))
        coEvery { anyConstructed<CursoRepository>().obtenerRecetaPorId(1, any()) } returns Result.success(recetasDePrueba[0])
        coEvery { anyConstructed<ProductoRepository>().obtenerProductoPorNombre(any()) } returns Result.success(null)
        coEvery { anyConstructed<ProductoRepository>().obtenerProductoPorNombre("Cobertura Selva Negra") } returns
            Result.success(productosDePrueba[0])
        // En la receta 1 están marcados los ingredientes 0 y 2, y completado el paso 1
        every { checklistDao.obtenerMarcados(1) } returns flowOf(listOf(0, 2))
        every { checklistDao.obtenerPasosCompletados(1) } returns flowOf(listOf(1))
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    "cargarCurso parte en Cargando y muestra la receta, su producto y lo guardado en Room" {
        runTest {
            val vm = DetalleViewModel(app)
            vm.cargarCurso(1)
            vm.carga.value.cargando.shouldBeTrue()
            vm.curso.value.shouldBeNull()
            advanceUntilIdle()

            vm.carga.value shouldBe EstadoCargaDetalle(cargando = false)
            vm.curso.value?.titulo shouldBe "Bombones de maracuyá"
            vm.productoInsumo.value?.nombre shouldBe "Cobertura Selva Negra"
            vm.marcados.value shouldBe listOf(0, 2)
            vm.pasosCompletados.value shouldBe listOf(1)
        }
    }

    "cargar la misma receta dos veces no la vuelve a pedir" {
        runTest {
            val vm = DetalleViewModel(app)
            vm.cargarCurso(1)
            advanceUntilIdle()
            vm.cargarCurso(1)
            advanceUntilIdle()
            coVerify(exactly = 1) { anyConstructed<CursoRepository>().obtenerRecetaPorId(1, false) }
        }
    }

    "si el producto del insumo no se puede cargar, la receta se muestra igual sin producto" {
        runTest {
            coEvery { anyConstructed<ProductoRepository>().obtenerProductoPorNombre(any()) } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            val vm = DetalleViewModel(app)
            vm.cargarCurso(1)
            advanceUntilIdle()
            vm.curso.value?.id shouldBe 1
            vm.productoInsumo.value.shouldBeNull()
            vm.carga.value.errorCarga.shouldBeNull()
        }
    }

    "una receta que no existe (404) muestra que ya no está disponible" {
        runTest {
            every { checklistDao.obtenerMarcados(99) } returns flowOf(emptyList())
            every { checklistDao.obtenerPasosCompletados(99) } returns flowOf(emptyList())
            val vm = DetalleViewModel(app)
            vm.cargarCurso(99)
            advanceUntilIdle()
            vm.carga.value shouldBe EstadoCargaDetalle(cargando = false, noExiste = true)
            vm.curso.value.shouldBeNull()
            vm.productoInsumo.value.shouldBeNull()
        }
    }

    "sin conexión se muestra el error y Reintentar fuerza la recarga" {
        runTest {
            coEvery { anyConstructed<CursoRepository>().obtenerRecetaPorId(1, false) } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            val vm = DetalleViewModel(app)
            vm.cargarCurso(1)
            advanceUntilIdle()
            vm.carga.value shouldBe EstadoCargaDetalle(cargando = false, errorCarga = MENSAJE_SIN_CONEXION)

            vm.reintentar()
            vm.carga.value.cargando.shouldBeTrue()
            advanceUntilIdle()

            vm.curso.value?.id shouldBe 1
            vm.carga.value.errorCarga.shouldBeNull()
            coVerify(exactly = 1) { anyConstructed<CursoRepository>().obtenerRecetaPorId(1, true) }
        }
    }

    "Reintentar sin haber abierto una receta no hace nada" {
        runTest {
            val vm = DetalleViewModel(app)
            vm.reintentar()
            advanceUntilIdle()
            coVerify(exactly = 0) { anyConstructed<CursoRepository>().obtenerRecetaPorId(any(), any()) }
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
            advanceUntilIdle()
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
