package com.example.mastermartini.viewmodel

import com.example.mastermartini.data.CursoRepository
import com.example.mastermartini.data.MENSAJE_SIN_CONEXION
import com.example.mastermartini.data.local.FavoritoDao
import com.example.mastermartini.data.local.MasterMartiniDatabase
import com.example.mastermartini.data.recetasDePrueba
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

// Los ids favoritos vienen de Room (simulado) y las recetas del servidor (RecetasDePrueba.kt)
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
        mockkConstructor(CursoRepository::class)
        coEvery { anyConstructed<CursoRepository>().obtenerRecetas(any()) } returns Result.success(recetasDePrueba)
    }

    afterTest {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // Recetas favoritas que se ven cuando ya llegó todo
    fun FavoritosViewModel.idsMostrados(): List<Int> {
        val estado = cursosFavoritos.value
        estado.shouldBeInstanceOf<EstadoUi.Exito<List<com.example.mastermartini.model.Curso>>>()
        return estado.datos.map { it.id }
    }

    "parte en Cargando y luego muestra las recetas cuyos ids están guardados como favoritos" {
        runTest {
            every { favoritoDao.obtenerIdsFavoritos() } returns flowOf(listOf(3, 1, 99))
            val vm = FavoritosViewModel(app)
            backgroundScope.launch { vm.cursosFavoritos.collect {} }
            vm.cursosFavoritos.value shouldBe EstadoUi.Cargando
            advanceUntilIdle()
            // El 99 no está en el servidor, así que se ignora; el orden es el de las recetas
            vm.idsMostrados() shouldBe listOf(1, 3)
        }
    }

    "sin favoritos la lista queda vacía (no es un error)" {
        runTest {
            every { favoritoDao.obtenerIdsFavoritos() } returns flowOf(emptyList())
            val vm = FavoritosViewModel(app)
            backgroundScope.launch { vm.cursosFavoritos.collect {} }
            advanceUntilIdle()
            vm.cursosFavoritos.value shouldBe EstadoUi.Exito(emptyList())
        }
    }

    "al cambiar los favoritos en Room la lista se actualiza sola" {
        runTest {
            val idsEnRoom = MutableStateFlow(listOf(2))
            every { favoritoDao.obtenerIdsFavoritos() } returns idsEnRoom
            val vm = FavoritosViewModel(app)
            backgroundScope.launch { vm.cursosFavoritos.collect {} }
            advanceUntilIdle()
            vm.idsMostrados() shouldBe listOf(2)

            idsEnRoom.value = listOf(2, 5)
            advanceUntilIdle()
            vm.idsMostrados() shouldBe listOf(2, 5)
        }
    }

    "si el servidor no responde se muestra el error, no una lista vacía, y Reintentar recarga" {
        runTest {
            every { favoritoDao.obtenerIdsFavoritos() } returns flowOf(listOf(4))
            coEvery { anyConstructed<CursoRepository>().obtenerRecetas(false) } returns
                Result.failure(Exception(MENSAJE_SIN_CONEXION))
            val vm = FavoritosViewModel(app)
            backgroundScope.launch { vm.cursosFavoritos.collect {} }
            advanceUntilIdle()
            vm.cursosFavoritos.value shouldBe EstadoUi.Error(MENSAJE_SIN_CONEXION)

            vm.reintentar()
            advanceUntilIdle()
            vm.idsMostrados() shouldBe listOf(4)
            coVerify(exactly = 1) { anyConstructed<CursoRepository>().obtenerRecetas(true) }
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
