package com.example.mastermartini.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO (Data Access Object): la lista de operaciones que se pueden hacer con la tabla "favoritos".
// Es una interfaz: nosotros solo escribimos QUÉ hace cada función (con SQL o anotaciones)
// y Room (con KSP) genera automáticamente el código que la ejecuta.
@Dao
interface FavoritoDao {

    // Devuelve los ids de todos los cursos favoritos.
    // Flow = "flujo vivo": cada vez que la tabla cambia, Room vuelve a enviar la lista nueva
    // sin que tengamos que volver a preguntar. Por eso NO es suspend.
    @Query("SELECT cursoId FROM favoritos")
    fun obtenerIdsFavoritos(): Flow<List<Int>>

    // Agrega un favorito. suspend: escribir en la base de datos toma tiempo y no puede
    // hacerse en el hilo principal (la pantalla se congelaría). Se llama desde una corrutina.
    // IGNORE: si el curso ya era favorito (mismo @PrimaryKey), no hace nada en vez de caerse.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregar(favorito: FavoritoEntity)

    // Quita un favorito por su id. :cursoId se reemplaza por el parámetro de la función.
    @Query("DELETE FROM favoritos WHERE cursoId = :cursoId")
    suspend fun quitar(cursoId: Int)

    // Cuenta cuántos favoritos hay. También es Flow: el número se actualiza solo.
    @Query("SELECT COUNT(*) FROM favoritos")
    fun contarFavoritos(): Flow<Int>
}
