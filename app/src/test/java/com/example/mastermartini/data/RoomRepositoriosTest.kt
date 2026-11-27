package com.example.mastermartini.data

import com.example.mastermartini.data.local.ChecklistDao
import com.example.mastermartini.data.local.ChecklistIngredienteEntity
import com.example.mastermartini.data.local.ChecklistPasoEntity
import com.example.mastermartini.data.local.FavoritoDao
import com.example.mastermartini.data.local.FavoritoEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

// Repositorios de Room: los DAO se simulan con MockK, así no se necesita una base de datos real
class RoomRepositoriosTest : StringSpec({

    val favoritoDao = mockk<FavoritoDao>(relaxed = true)
    val checklistDao = mockk<ChecklistDao>(relaxed = true)

    beforeTest {
        clearMocks(favoritoDao, checklistDao)
    }

    // ---------- FavoritoRepository ----------

    "FavoritoRepository entrega los ids favoritos que guarda Room" {
        runTest {
            every { favoritoDao.obtenerIdsFavoritos() } returns flowOf(listOf(1, 4))
            FavoritoRepository(favoritoDao).obtenerIdsFavoritos().first() shouldBe listOf(1, 4)
        }
    }

    "FavoritoRepository entrega la cantidad de favoritos" {
        runTest {
            every { favoritoDao.contarFavoritos() } returns flowOf(2)
            FavoritoRepository(favoritoDao).contarFavoritos().first() shouldBe 2
        }
    }

    "agregar un favorito guarda una FavoritoEntity con el id del curso" {
        runTest {
            FavoritoRepository(favoritoDao).agregar(7)
            coVerify { favoritoDao.agregar(FavoritoEntity(cursoId = 7)) }
        }
    }

    "quitar un favorito lo borra por el id del curso" {
        runTest {
            FavoritoRepository(favoritoDao).quitar(7)
            coVerify { favoritoDao.quitar(7) }
        }
    }

    // ---------- ChecklistRepository ----------

    "ChecklistRepository entrega los ingredientes marcados de una receta" {
        runTest {
            every { checklistDao.obtenerMarcados(3) } returns flowOf(listOf(0, 2))
            ChecklistRepository(checklistDao).obtenerMarcados(3).first() shouldBe listOf(0, 2)
        }
    }

    "marcar un ingrediente guarda la entidad con su estado" {
        runTest {
            ChecklistRepository(checklistDao).marcar(cursoId = 3, indice = 1, marcado = true)
            coVerify {
                checklistDao.guardar(ChecklistIngredienteEntity(cursoId = 3, indice = 1, marcado = true))
            }
        }
    }

    "reiniciar desmarca los ingredientes de la receta" {
        runTest {
            ChecklistRepository(checklistDao).reiniciar(3)
            coVerify { checklistDao.reiniciar(3) }
        }
    }

    "ChecklistRepository entrega los pasos completados de una receta" {
        runTest {
            every { checklistDao.obtenerPasosCompletados(3) } returns flowOf(listOf(1))
            ChecklistRepository(checklistDao).obtenerPasosCompletados(3).first() shouldBe listOf(1)
        }
    }

    "marcarPaso guarda la entidad del paso con su estado" {
        runTest {
            ChecklistRepository(checklistDao).marcarPaso(cursoId = 3, indice = 4, completado = false)
            coVerify {
                checklistDao.guardarPaso(ChecklistPasoEntity(cursoId = 3, indice = 4, completado = false))
            }
        }
    }

    "reiniciarPasos deja todos los pasos sin completar" {
        runTest {
            ChecklistRepository(checklistDao).reiniciarPasos(3)
            coVerify { checklistDao.reiniciarPasos(3) }
        }
    }
})
