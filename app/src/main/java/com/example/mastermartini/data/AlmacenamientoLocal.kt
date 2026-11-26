package com.example.mastermartini.data

import android.content.Context
import android.content.SharedPreferences
import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario

// Gestiona el almacenamiento local de sesión, preferencias y rutas en SharedPreferences
class AlmacenamientoLocal(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("master_martini_prefs", Context.MODE_PRIVATE)

    // Persiste la sesión activa y los datos del perfil en SharedPreferences
    fun guardarSesion(usuario: Usuario) {
        val editor = preferences.edit()

        editor.putLong("KEY_ID_USUARIO", usuario.id)
        editor.putString("KEY_ROL", usuario.rol.codigo)
        editor.putBoolean("KEY_PLAN_PRO", usuario.planPro)

        editor.putString("KEY_NOMBRE", usuario.nombre)
        editor.putString("KEY_RUT", usuario.rut)
        editor.putString("KEY_CORREO", usuario.correo)
        editor.putString("KEY_RUBRO", usuario.rubro)
        editor.putString("KEY_TELEFONO", usuario.telefono)
        editor.putString("KEY_PERSONAS_NEGOCIO", usuario.personasNegocio)
        editor.remove("KEY_TAMANO_NEGOCIO")
        editor.remove("KEY_PASSWORD")

        editor.apply()
    }

    fun obtenerNombre(): String = preferences.getString("KEY_NOMBRE", "Emprendedor") ?: "Emprendedor"
    fun obtenerRut(): String = preferences.getString("KEY_RUT", "") ?: ""
    fun obtenerCorreo(): String = preferences.getString("KEY_CORREO", "") ?: ""
    fun obtenerRubro(): String = preferences.getString("KEY_RUBRO", "Panadería") ?: "Panadería"
    fun obtenerTelefono(): String = preferences.getString("KEY_TELEFONO", "") ?: ""
    fun obtenerPersonasNegocio(): String = preferences.getString("KEY_PERSONAS_NEGOCIO", "") ?: ""

    fun obtenerIdUsuario(): Long? {
        val id = preferences.getLong("KEY_ID_USUARIO", -1L)
        return if (id == -1L) null else id
    }

    fun obtenerRol(): Rol = Rol.desdeCodigo(preferences.getString("KEY_ROL", "CLIENTE") ?: "CLIENTE")

    fun tienePlanPro(): Boolean = preferences.getBoolean("KEY_PLAN_PRO", false)

    fun existePerfil(): Boolean = preferences.contains("KEY_NOMBRE")

    // Elimina los datos de sesión conservando encuesta y foto del dispositivo
    fun cerrarSesion() {
        val editor = preferences.edit()
        editor.remove("KEY_ID_USUARIO")
        editor.remove("KEY_ROL")
        editor.remove("KEY_PLAN_PRO")
        editor.remove("KEY_NOMBRE")
        editor.remove("KEY_RUT")
        editor.remove("KEY_CORREO")
        editor.remove("KEY_RUBRO")
        editor.remove("KEY_TELEFONO")
        editor.remove("KEY_PERSONAS_NEGOCIO")
        editor.apply()
    }

    // Guarda las preferencias de la encuesta inicial del usuario
    fun guardarEncuesta(intereses: Set<String>, nivel: String, ubicacion: String) {
        val editor = preferences.edit()
        editor.putStringSet("KEY_ENCUESTA_INTERESES", intereses)
        editor.putString("KEY_ENCUESTA_NIVEL", nivel)
        editor.putString("KEY_ENCUESTA_UBICACION", ubicacion)
        editor.apply()
    }

    fun obtenerInteresesEncuesta(): Set<String> =
        preferences.getStringSet("KEY_ENCUESTA_INTERESES", emptySet())?.toSet() ?: emptySet()

    fun obtenerNivelEncuesta(): String = preferences.getString("KEY_ENCUESTA_NIVEL", "") ?: ""
    fun obtenerUbicacionEncuesta(): String = preferences.getString("KEY_ENCUESTA_UBICACION", "") ?: ""

    fun existeEncuesta(): Boolean = preferences.contains("KEY_ENCUESTA_NIVEL")

    fun guardarRutaFoto(ruta: String) {
        preferences.edit().putString("KEY_FOTO_LOCAL", ruta).apply()
    }

    fun obtenerRutaFoto(): String = preferences.getString("KEY_FOTO_LOCAL", "") ?: ""
}