package com.example.mastermartini.data

import android.content.Context
import android.content.SharedPreferences

class AlmacenamientoLocal(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("master_martini_prefs", Context.MODE_PRIVATE)

    fun guardarPerfil(
        nombre: String,
        rut: String,
        correo: String,
        rubro: String,
        telefono: String,
        tamanoNegocio: String,
        password: String // 👈 Nuevo parámetro
    ) {
        val editor = preferences.edit()
        editor.putString("KEY_NOMBRE", nombre)
        editor.putString("KEY_RUT", rut)
        editor.putString("KEY_CORREO", correo)
        editor.putString("KEY_RUBRO", rubro)
        editor.putString("KEY_TELEFONO", telefono)
        editor.putString("KEY_TAMANO_NEGOCIO", tamanoNegocio)
        editor.putString("KEY_PASSWORD", password) // 👈 Guarda la contraseña
        editor.apply()
    }

    fun obtenerNombre(): String = preferences.getString("KEY_NOMBRE", "Emprendedor") ?: "Emprendedor"
    fun obtenerRut(): String = preferences.getString("KEY_RUT", "") ?: ""
    fun obtenerCorreo(): String = preferences.getString("KEY_CORREO", "") ?: ""
    fun obtenerRubro(): String = preferences.getString("KEY_RUBRO", "Panadería") ?: "Panadería"
    fun obtenerTelefono(): String = preferences.getString("KEY_TELEFONO", "") ?: ""
    fun obtenerTamanoNegocio(): String = preferences.getString("KEY_TAMANO_NEGOCIO", "Micro") ?: "Micro"
    fun obtenerPassword(): String = preferences.getString("KEY_PASSWORD", "") ?: ""

    fun existePerfil(): Boolean = preferences.contains("KEY_NOMBRE")

    // 👈 FUNCIÓN DE VALIDACIÓN DE LOGIN
    fun validarCredenciales(rutOCorreoIngresado: String, passwordIngresada: String): Boolean {
        val rutGuardado = obtenerRut()
        val correoGuardado = obtenerCorreo()
        val passwordGuardada = obtenerPassword()

        val usuarioValido = rutOCorreoIngresado.trim().equals(rutGuardado.trim(), ignoreCase = true) ||
                rutOCorreoIngresado.trim().equals(correoGuardado.trim(), ignoreCase = true)

        val passwordValida = passwordIngresada == passwordGuardada

        return usuarioValido && passwordValida
    }

    // --- ENCUESTA DE PREFERENCIAS ---
    // Se guarda en el mismo archivo de SharedPreferences que el perfil, con claves propias.

    // Guarda las respuestas de la encuesta.
    // Los intereses son varios (Set<String>), por eso se usa putStringSet en vez de putString.
    fun guardarEncuesta(intereses: Set<String>, nivel: String, ubicacion: String) {
        val editor = preferences.edit()
        editor.putStringSet("KEY_ENCUESTA_INTERESES", intereses)
        editor.putString("KEY_ENCUESTA_NIVEL", nivel)
        editor.putString("KEY_ENCUESTA_UBICACION", ubicacion)
        editor.apply()
    }

    // Devuelve los intereses guardados (o un conjunto vacío si nunca se respondió).
    // toSet() hace una copia: Android pide no modificar directamente el Set que entrega getStringSet.
    fun obtenerInteresesEncuesta(): Set<String> =
        preferences.getStringSet("KEY_ENCUESTA_INTERESES", emptySet())?.toSet() ?: emptySet()

    fun obtenerNivelEncuesta(): String = preferences.getString("KEY_ENCUESTA_NIVEL", "") ?: ""
    fun obtenerUbicacionEncuesta(): String = preferences.getString("KEY_ENCUESTA_UBICACION", "") ?: ""

    // true si la encuesta ya se respondió alguna vez (igual que existePerfil, basta con una clave)
    fun existeEncuesta(): Boolean = preferences.contains("KEY_ENCUESTA_NIVEL")
}