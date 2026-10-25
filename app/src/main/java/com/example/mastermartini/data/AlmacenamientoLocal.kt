package com.example.mastermartini.data

import android.content.Context
import android.content.SharedPreferences

// Recibe el 'context' (el permiso del sistema Android para acceder a la memoria interna del celular)
class AlmacenamientoLocal(context: Context) {

    // 1. Abrimos o creamos el archivo "master_martini_prefs" en modo privado (solo nuestra app puede leerlo)
    private val preferences: SharedPreferences =
        context.getSharedPreferences("master_martini_prefs", Context.MODE_PRIVATE)

    // 2. FUNCIÓN PARA GUARDAR (Escribir en la libreta)
    fun guardarPerfil(nombre: String, rut: String, correo: String, rubro: String) {
        // Pedimos un "lápiz" (editor) para escribir en el archivo
        val editor = preferences.edit()

        // Guardamos cada dato con su etiqueta (Clave -> Valor), igual que un mapOf
        editor.putString("KEY_NOMBRE", nombre)
        editor.putString("KEY_RUT", rut)
        editor.putString("KEY_CORREO", correo)
        editor.putString("KEY_RUBRO", rubro)

        // apply() confirma y guarda los cambios en el disco duro sin congelar la pantalla
        editor.apply()
    }

    // 3. FUNCIONES PARA LEER (Recuperar de la libreta)
    // El segundo texto (ej: "Emprendedor") es el valor por defecto si aún no se ha guardado nada.
    // El operador '?:' (Elvis) de Null Safety asegura que nunca devuelva null.
    fun obtenerNombre(): String {
        return preferences.getString("KEY_NOMBRE", "Emprendedor") ?: "Emprendedor"
    }
    fun obtenerRut(): String {
        return preferences.getString("KEY_RUT", "Sin RUT") ?: "Sin RUT"
    }
    fun obtenerCorreo(): String {
        return preferences.getString("KEY_CORREO", "Sin correo") ?: "Sin correo"
    }
    fun obtenerRubro(): String {
        return preferences.getString("KEY_RUBRO", "Panadería") ?: "Panadería"
    }

    // 4. ¿YA HAY UN PERFIL GUARDADO?
    // contains() revisa si la clave existe en el archivo, sin leer su valor.
    // Basta con revisar KEY_NOMBRE porque guardarPerfil() siempre guarda los 4 datos juntos.
    fun existePerfil(): Boolean {
        return preferences.contains("KEY_NOMBRE")
    }
}