package com.example.mastermartini.data.local

import androidx.room.Entity

// ENTIDAD DE ROOM: tabla "checklist_ingredientes"
// Cada fila dice si UN ingrediente de UNA receta está marcado como listo.
// No guardamos el texto del ingrediente (ya lo tiene CursoRepository), solo su posición.
// primaryKeys = ["cursoId", "indice"]: CLAVE PRIMARIA COMPUESTA (de dos columnas).
// Lo que no se puede repetir es la PAREJA: puede haber muchas filas con cursoId = 3
// (una por ingrediente), pero solo una con cursoId = 3 e indice = 0.
@Entity(tableName = "checklist_ingredientes", primaryKeys = ["cursoId", "indice"])
data class ChecklistIngredienteEntity(
    // Id del curso al que pertenece el ingrediente
    val cursoId: Int,
    // Posición del ingrediente en la lista "ingredientes" del curso (parte en 0)
    val indice: Int,
    // true = el usuario ya tiene este ingrediente listo (casilla marcada)
    val marcado: Boolean
)
