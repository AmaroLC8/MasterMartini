package com.example.mastermartini.data

import com.example.mastermartini.data.local.ChecklistDao
import com.example.mastermartini.data.local.ChecklistIngredienteEntity
import com.example.mastermartini.data.local.ChecklistPasoEntity
import kotlinx.coroutines.flow.Flow

// Repositorio para gestionar checklists de ingredientes y pasos en Room
class ChecklistRepository(private val dao: ChecklistDao) {

    fun obtenerMarcados(cursoId: Int): Flow<List<Int>> {
        return dao.obtenerMarcados(cursoId)
    }

    suspend fun marcar(cursoId: Int, indice: Int, marcado: Boolean) {
        dao.guardar(
            ChecklistIngredienteEntity(cursoId = cursoId, indice = indice, marcado = marcado)
        )
    }

    suspend fun reiniciar(cursoId: Int) {
        dao.reiniciar(cursoId)
    }

    fun obtenerPasosCompletados(cursoId: Int): Flow<List<Int>> {
        return dao.obtenerPasosCompletados(cursoId)
    }

    suspend fun marcarPaso(cursoId: Int, indice: Int, completado: Boolean) {
        dao.guardarPaso(
            ChecklistPasoEntity(cursoId = cursoId, indice = indice, completado = completado)
        )
    }

    suspend fun reiniciarPasos(cursoId: Int) {
        dao.reiniciarPasos(cursoId)
    }
}
