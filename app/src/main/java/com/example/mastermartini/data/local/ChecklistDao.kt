package com.example.mastermartini.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// DAO de Room para persistir el progreso de checklists de ingredientes y pasos
@Dao
interface ChecklistDao {

    @Query("SELECT indice FROM checklist_ingredientes WHERE cursoId = :cursoId AND marcado = 1")
    fun obtenerMarcados(cursoId: Int): Flow<List<Int>>

    @Upsert
    suspend fun guardar(item: ChecklistIngredienteEntity)

    @Query("UPDATE checklist_ingredientes SET marcado = 0 WHERE cursoId = :cursoId")
    suspend fun reiniciar(cursoId: Int)

    @Query("SELECT indice FROM checklist_pasos WHERE cursoId = :cursoId AND completado = 1")
    fun obtenerPasosCompletados(cursoId: Int): Flow<List<Int>>

    @Upsert
    suspend fun guardarPaso(item: ChecklistPasoEntity)

    @Query("UPDATE checklist_pasos SET completado = 0 WHERE cursoId = :cursoId")
    suspend fun reiniciarPasos(cursoId: Int)
}
