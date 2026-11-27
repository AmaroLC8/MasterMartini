package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.local.FavoritoDao
import com.example.mastermartini.data.local.MasterMartiniDatabase
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
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
class FavoritosViewModelTest : StringSpec({

    val app = aplicacionDePrueba()
    val favoritoDao = mockk<FavoritoDao>(relaxed = true)
    val baseDeDatos = mockk<MasterMartiniDatabase>()

    beforeTest {
        Dispatchers.setMain(StandardTestDispatcher())
        clearMocks(favoritoDao)
        mockkObject(MasterMartiniDatabase.Companion)
        every { MasterMartiniDatabase.getInstance(any()) } returns baseDeDatos
        every { baseDeDatos.favoritoDao() } returns favoritoDao
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    "muestra las recetas cuyos ids están guardados como favoritos" {
        runTest {
            every { favoritoDao.obtenerIdsFavoritos() } returns flowOf(listOf(3, 1, 99))
            val vm = FavoritosViewModel(app)
            backgroundScope.launch { vm.cursosFavoritos.collect {} }
            advanceUntilIdle()
            // El 99 no existe en el catálogo, así que se ignora
            vm.cursosFavoritos.value.map { it.id } shouldBe listOf(1, 3)
        }
    }

    "sin favoritos la lista queda vacía" {
        runTest {
            every { favoritoDao.obtenerIdsFavoritos() } returns flowOf(emptyList())
            val vm = FavoritosViewModel(app)
            backgroundScope.launch { vm.cursosFavoritos.collect {} }
            advanceUntilIdle()
            vm.cursosFavoritos.value.shouldBeEmpty()
        }
    }

    "quitarFavorito lo borra de Room" {
        runTest {
            every { favoritoDao.obtenerIdsFavoritos() } returns flowOf(listOf(1))
            val vm = FavoritosViewModel(app)
            vm.quitarFavorito(1)
            advanceUntilIdle()
            coVerify { favoritoDao.quitar(1) }
        }
    }
})
