package com.example.mastermartini.data.local

import androidx.room.Entity

// ENTIDAD DE ROOM: tabla "checklist_pasos"
// Cada fila dice si UN paso de UNA receta ya está completado.
// Es igual a ChecklistIngredienteEntity, pero para los pasos de la receta.
// No guardamos el texto del paso (ya lo tiene CursoRepository), solo su posición.
// primaryKeys = ["cursoId", "indice"]: CLAVE PRIMARIA COMPUESTA (de dos columnas).
// Lo que no se puede repetir es la PAREJA: el curso 3 puede tener muchos pasos,
// pero solo una fila con cursoId = 3 e indice = 0.
@Entity(tableName = "checklist_pasos", primaryKeys = ["cursoId", "indice"])
data class ChecklistPasoEntity(
    // Id del curso al que pertenece el paso
    val cursoId: Int,
    // Posición del paso en la lista "pasos" del curso (parte en 0)
    val indice: Int,
    // true = el usuario ya hizo este paso
    val completado: Boolean
)
