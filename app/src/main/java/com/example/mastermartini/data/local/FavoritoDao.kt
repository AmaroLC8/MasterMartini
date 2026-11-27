package com.example.mastermartini.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO de Room para consultar y modificar recetas favoritas
@Dao
interface FavoritoDao {

    @Query("SELECT cursoId FROM favoritos")
    fun obtenerIdsFavoritos(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregar(favorito: FavoritoEntity)

    @Query("DELETE FROM favoritos WHERE cursoId = :cursoId")
    suspend fun quitar(cursoId: Int)

    @Query("SELECT COUNT(*) FROM favoritos")
    fun contarFavoritos(): Flow<Int>
}
