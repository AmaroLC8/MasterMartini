package com.example.mastermartini.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// ENTIDAD DE ROOM (una tabla de la base de datos)
// Cada objeto FavoritoEntity es una FILA de la tabla "favoritos".
// Guardamos solo el id del curso: el resto de los datos (título, chef, etc.)
// ya los tiene CursoRepository, así no se repite información.
@Entity(tableName = "favoritos")
data class FavoritoEntity(
    // @PrimaryKey: no puede haber dos filas con el mismo cursoId,
    // así un curso no puede quedar dos veces como favorito.
    @PrimaryKey
    val cursoId: Int
)
