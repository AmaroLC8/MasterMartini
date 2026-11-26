package com.example.mastermartini.data.local

import androidx.room.Entity

// Entidad Room para registrar pasos completados por receta
@Entity(tableName = "checklist_pasos", primaryKeys = ["cursoId", "indice"])
data class ChecklistPasoEntity(
    val cursoId: Int,
    val indice: Int,
    val completado: Boolean
)
