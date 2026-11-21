package com.example.mastermartini.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// BASE DE DATOS DE LA APP (Room sobre SQLite)
// entities: las tablas que tiene ("favoritos", "checklist_ingredientes" y "checklist_pasos").
// version: cada vez que cambia la estructura (una tabla o columna nueva), hay que subir este número.
//   Pasó de 1 a 2 al agregar la tabla del checklist de ingredientes, y de 2 a 3 al agregar
//   la tabla del checklist de pasos. ¿Por qué? Los celulares que ya tenían la app guardan una base
//   antigua SIN la tabla nueva. Al ver que el código pide una versión mayor, Room sabe que la base
//   guardada está desactualizada y la actualiza (MIGRA). Si no se sube el número, Room detecta
//   que el esquema no coincide y la app se cae al abrir la base de datos.
// exportSchema = false: no guardamos el historial del esquema en un archivo (no lo necesitamos
// en este proyecto y así KSP no muestra una advertencia al compilar).
@Database(
    entities = [
        FavoritoEntity::class,
        ChecklistIngredienteEntity::class,
        ChecklistPasoEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MasterMartiniDatabase : RoomDatabase() {

    // Room genera el código de esta función: entrega el DAO para usar la tabla "favoritos"
    abstract fun favoritoDao(): FavoritoDao

    // DAO para las tablas "checklist_ingredientes" y "checklist_pasos" (también lo genera Room)
    abstract fun checklistDao(): ChecklistDao

    // companion object: lo que está aquí pertenece a la CLASE y no a cada objeto,
    // por eso se llama así: MasterMartiniDatabase.getInstance(context)
    companion object {

        // MIGRACIÓN MANUAL de la versión 2 a la 3.
        // Una migración es el "instructivo" para pasar una base antigua a la estructura nueva
        // SIN borrarla. De la 2 a la 3 lo único nuevo es la tabla "checklist_pasos",
        // así que basta con crearla. Las tablas que ya existían ("favoritos" y
        // "checklist_ingredientes") no se tocan: por eso NO se pierden los favoritos
        // ni los ingredientes marcados que el usuario ya tenía guardados.
        // Migration(2, 3): desde qué versión y hasta cuál sirve.
        // "object :" crea un objeto de una clase anónima que hereda de Migration.
        val MIGRACION_2_3 = object : Migration(2, 3) {
            // Room llama a migrate() UNA sola vez, al abrir una base versión 2 con el código versión 3
            override fun migrate(db: SupportSQLiteDatabase) {
                // SQL que crea la tabla. Debe calzar EXACTO con ChecklistPasoEntity:
                // los Int y Boolean son INTEGER NOT NULL y la clave primaria es (cursoId, indice).
                // Si no calza, Room lo detecta al abrir la base y la app se cae.
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `checklist_pasos` (" +
                        "`cursoId` INTEGER NOT NULL, " +
                        "`indice` INTEGER NOT NULL, " +
                        "`completado` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`cursoId`, `indice`))"
                )
            }
        }

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
                    // De la 2 a la 3: Room usa nuestra migración manual (crea solo la tabla nueva
                    // y conserva los favoritos y el checklist de ingredientes).
                    .addMigrations(MIGRACION_2_3)
                    // PLAN B: si alguien tiene una versión más antigua (la 1), no hay migración
                    // escrita de 1 a 2. En ese caso Room borra todas las tablas y las crea de nuevo
                    // (se pierden los datos, algo aceptable con datos ficticios), en vez de caerse.
                    // Room primero busca una migración en addMigrations y solo si no hay, usa esto.
                    // dropAllTables = true es la forma no deprecada en Room 2.8.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
