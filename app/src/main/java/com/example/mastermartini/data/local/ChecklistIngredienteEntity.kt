package com.example.mastermartini.data.local

import androidx.room.Entity

// Entidad Room para registrar ingredientes marcados por receta
@Entity(tableName = "checklist_ingredientes", primaryKeys = ["cursoId", "indice"])
data class ChecklistIngredienteEntity(
    val cursoId: Int,
    val indice: Int,
    val marcado: Boolean
)
