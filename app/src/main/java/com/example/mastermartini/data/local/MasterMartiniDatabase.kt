package com.example.mastermartini.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// BASE DE DATOS DE LA APP (Room sobre SQLite)
// entities: las tablas que tiene ("favoritos" y "checklist_ingredientes").
// version: cada vez que cambia la estructura (una tabla o columna nueva), hay que subir este número.
//   Pasó de 1 a 2 al agregar la tabla del checklist. ¿Por qué? Los celulares que ya tenían la app
//   guardan una base versión 1 SIN esa tabla. Al ver que el código pide la versión 2, Room sabe
//   que la base guardada está desactualizada y la actualiza. Si no se sube el número, Room detecta
//   que el esquema no coincide y la app se cae al abrir la base de datos.
// exportSchema = false: no guardamos el historial del esquema en un archivo (no lo necesitamos
// en este proyecto y así KSP no muestra una advertencia al compilar).
@Database(
    entities = [FavoritoEntity::class, ChecklistIngredienteEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MasterMartiniDatabase : RoomDatabase() {

    // Room genera el código de esta función: entrega el DAO para usar la tabla "favoritos"
    abstract fun favoritoDao(): FavoritoDao

    // DAO para la tabla "checklist_ingredientes" (también lo genera Room)
    abstract fun checklistDao(): ChecklistDao

    // companion object: lo que está aquí pertenece a la CLASE y no a cada objeto,
    // por eso se llama así: MasterMartiniDatabase.getInstance(context)
    companion object {

        // Aquí se guarda la única base de datos de la app (null hasta que se crea).
        // @Volatile: todos los hilos ven siempre el valor más reciente de esta variable.
        @Volatile
        private var INSTANCIA: MasterMartiniDatabase? = null

        // PATRÓN SINGLETON: abrir una base de datos es costoso, así que se crea UNA sola vez
        // y después se reutiliza la misma en toda la app.
        fun getInstance(context: Context): MasterMartiniDatabase {
            // Si ya existe, la devuelve. Si no (?:), entra al bloque y la crea.
            return INSTANCIA ?: synchronized(this) {
                // synchronized: si dos partes de la app la piden al mismo tiempo,
                // solo una entra a crearla y así no se crean dos bases de datos.
                val instancia = Room.databaseBuilder(
                    context.applicationContext,   // contexto de la app (no de una pantalla)
                    MasterMartiniDatabase::class.java,
                    "master_martini_db"           // nombre del archivo en el celular
                )
                    // Cómo pasar de la versión 1 a la 2: en vez de escribir una migración a mano,
                    // Room borra todas las tablas antiguas y las crea de nuevo con la estructura nueva.
                    // Se pierden los datos guardados (favoritos), algo aceptable con datos ficticios.
                    // dropAllTables = true es la forma no deprecada en Room 2.8.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
