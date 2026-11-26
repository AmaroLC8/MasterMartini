package com.example.mastermartini.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Base de datos Room de la aplicación con tablas de favoritos y checklists
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

    abstract fun favoritoDao(): FavoritoDao

    abstract fun checklistDao(): ChecklistDao

    companion object {

        // Crea la tabla checklist_pasos conservando los datos previos de la versión 2
        val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `checklist_pasos` (" +
                        "`cursoId` INTEGER NOT NULL, " +
                        "`indice` INTEGER NOT NULL, " +
                        "`completado` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`cursoId`, `indice`))"
                )
            }
        }

        @Volatile
        private var INSTANCIA: MasterMartiniDatabase? = null

        // Devuelve la instancia singleton de la base de datos Room
        fun getInstance(context: Context): MasterMartiniDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    MasterMartiniDatabase::class.java,
                    "master_martini_db"
                )
                    .addMigrations(MIGRACION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
