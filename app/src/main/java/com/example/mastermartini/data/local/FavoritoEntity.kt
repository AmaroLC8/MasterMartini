package com.example.mastermartini.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entidad Room que almacena los IDs de recetas favoritas
@Entity(tableName = "favoritos")
data class FavoritoEntity(
    @PrimaryKey
    val cursoId: Int
)
