package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.ServidorException
import com.example.mastermartini.data.local.FavoritoDao
import com.example.mastermartini.data.local.FavoritoEntity
import com.example.mastermartini.data.local.MasterMartiniDatabase
import com.example.mastermartini.data.recetasDePrueba
import io.kotest.core.spec.style.StringSpec
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest : StringSpec({

    val app = aplicacionDePrueba()
    val favoritoDao = mockk<FavoritoDao>(relaxed = true)
    val baseDeDatos = mockk<MasterMartiniDatabase>()

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        clearMocks(favoritoDao)
        mockkObject(MasterMartiniDatabase.Companion)
        every { MasterMartiniDatabase.getInstance(any()) } returns baseDeDatos
        every { baseDeDatos.favoritoDao() } returns favoritoDao
        every { favoritoDao.obtenerIdsFavoritos() } returns flowOf(listOf(2))
        every { favoritoDao.contarFavoritos() } returns flowOf(1)
        mockkConstructor(CursoRepository::class)
        coEvery { anyConstructed<CursoRepository>().obtenerRecetas(any()) } returns Result.success(recetasDePrueba)
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    "al abrir parte en Cargando y luego muestra las recetas del servidor" {
        runTest {
            val vm = HomeViewModel(app)
            vm.cursos.value shouldBe EstadoUi.Cargando
            advanceUntilIdle()
            vm.cursos.value shouldBe EstadoUi.Exito(recetasDePrueba)
            coVerify(exactly = 1) { anyConstructed<CursoRepository>().obtenerRecetas(false) }
        }
    }

    "si el servidor no responde se muestra el error y Reintentar fuerza la recarga" {
        runTest {
            coEvery { anyConstructed<CursoRepository>().obtenerRecetas(false) } returns
                Result.failure(ServidorException(503, "Servicio en mantención"))
            val vm = HomeViewModel(app)
            advanceUntilIdle()
            vm.cursos.value shouldBe EstadoUi.Error("Servicio en mantención")

            vm.reintentar()
            vm.cursos.value shouldBe EstadoUi.Cargando
            advanceUntilIdle()
            vm.cursos.value shouldBe EstadoUi.Exito(recetasDePrueba)
            coVerify(exactly = 1) { anyConstructed<CursoRepository>().obtenerRecetas(true) }
        }
    }

    "los favoritos y su total se leen desde Room mientras la pantalla los observa" {
        runTest {
            val vm = HomeViewModel(app)
            // stateIn(WhileSubscribed) solo lee Room si alguien está mirando, como la pantalla
            backgroundScope.launch { vm.favoritos.collect {} }
            backgroundScope.launch { vm.totalFavoritos.collect {} }
            advanceUntilIdle()
            vm.favoritos.value shouldBe listOf(2)
            vm.totalFavoritos.value shouldBe 1
        }
    }

    "el corazón quita una receta favorita y agrega una que no lo es" {
        runTest {
            val vm = HomeViewModel(app)
            backgroundScope.launch { vm.favoritos.collect {} }
            advanceUntilIdle()

            vm.alternarFavorito(2)
            vm.alternarFavorito(5)
            advanceUntilIdle()

            coVerify { favoritoDao.quitar(2) }
            coVerify { favoritoDao.agregar(FavoritoEntity(cursoId = 5)) }
        }
    }
})
