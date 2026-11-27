package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.local.FavoritoDao
import com.example.mastermartini.data.local.FavoritoEntity
import com.example.mastermartini.data.local.MasterMartiniDatabase
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
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
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    "al abrir se cargan las 10 recetas" {
        HomeViewModel(app).cursos.value shouldHaveSize 10
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
