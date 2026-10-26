package com.example.mastermartini.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// BASE DE DATOS DE LA APP (Room sobre SQLite)
// entities: las tablas que tiene (por ahora solo "favoritos").
// version: si mañana se cambia una tabla (ej. agregar una columna), hay que subir este número.
// exportSchema = false: no guardamos el historial del esquema en un archivo (no lo necesitamos
// en este proyecto y así KSP no muestra una advertencia al compilar).
@Database(entities = [FavoritoEntity::class], version = 1, exportSchema = false)
abstract class MasterMartiniDatabase : RoomDatabase() {

    // Room genera el código de esta función: entrega el DAO para usar la tabla "favoritos"
    abstract fun favoritoDao(): FavoritoDao

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
                ).build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
