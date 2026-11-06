package com.example.mastermartini.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// DAO de los checklists de la receta: las operaciones sobre las tablas
// "checklist_ingredientes" y "checklist_pasos".
// Igual que FavoritoDao, es una interfaz y Room (con KSP) genera el código que la ejecuta.
@Dao
interface ChecklistDao {

    // RECUPERAR: devuelve las posiciones de los ingredientes marcados de un curso.
    // En SQLite los Boolean se guardan como números: 1 = true y 0 = false, por eso "marcado = 1".
    // Es Flow: cada vez que la tabla cambia, Room vuelve a enviar la lista (por eso no es suspend).
    @Query("SELECT indice FROM checklist_ingredientes WHERE cursoId = :cursoId AND marcado = 1")
    fun obtenerMarcados(cursoId: Int): Flow<List<Int>>

    // INSERTAR o ACTUALIZAR en una sola función.
    // @Upsert ("update" + "insert") busca la fila por su clave primaria (cursoId + indice):
    // - si no existe, la inserta (la primera vez que se toca ese ingrediente);
    // - si ya existe, la actualiza (cuando se marca o desmarca de nuevo).
    @Upsert
    suspend fun guardar(item: ChecklistIngredienteEntity)

    // ACTUALIZAR varias filas: desmarca todos los ingredientes de un curso.
    // No borra las filas, solo pone marcado en 0 (false).
    @Query("UPDATE checklist_ingredientes SET marcado = 0 WHERE cursoId = :cursoId")
    suspend fun reiniciar(cursoId: Int)

    // ----- CHECKLIST DE PASOS (tabla "checklist_pasos") -----
    // Son las mismas 3 operaciones de arriba, pero sobre la tabla de los pasos.
    // Se dejan en este mismo DAO porque las dos tablas son "checklists" de la receta.

    // RECUPERAR: posiciones de los pasos completados de un curso (completado = 1 es true).
    // Flow: cada vez que la tabla cambia, Room vuelve a enviar la lista.
    @Query("SELECT indice FROM checklist_pasos WHERE cursoId = :cursoId AND completado = 1")
    fun obtenerPasosCompletados(cursoId: Int): Flow<List<Int>>

    // INSERTAR o ACTUALIZAR un paso: inserta la fila la primera vez y después la actualiza
    @Upsert
    suspend fun guardarPaso(item: ChecklistPasoEntity)

    // ACTUALIZAR varias filas: deja todos los pasos del curso como no completados (0 = false)
    @Query("UPDATE checklist_pasos SET completado = 0 WHERE cursoId = :cursoId")
    suspend fun reiniciarPasos(cursoId: Int)
}
