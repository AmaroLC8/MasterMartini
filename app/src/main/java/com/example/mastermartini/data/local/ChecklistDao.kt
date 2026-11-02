package com.example.mastermartini.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// DAO del checklist de ingredientes: las operaciones sobre la tabla "checklist_ingredientes".
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
}
