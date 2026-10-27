package com.example.mastermartini.data

import android.content.Context
import android.content.SharedPreferences

// Recibe el 'context' (el permiso del sistema Android para acceder a la memoria interna del celular)
class AlmacenamientoLocal(context: Context) {

    // 1. Abrimos o creamos el archivo "master_martini_prefs" en modo privado (solo nuestra app puede leerlo)
    private val preferences: SharedPreferences =
        context.getSharedPreferences("master_martini_prefs", Context.MODE_PRIVATE)

    // 2. FUNCIÓN PARA GUARDAR (Escribir en la libreta)
    fun guardarPerfil(
        nombre: String,
        rut: String,
        correo: String,
        rubro: String,
        telefono: String,
        tamanoNegocio: String,
        password: String // Contraseña creada en el registro, la usa el login
    ) {
        // Pedimos un "lápiz" (editor) para escribir en el archivo
        val editor = preferences.edit()

        // Guardamos cada dato con su etiqueta (Clave -> Valor), igual que un mapOf
        editor.putString("KEY_NOMBRE", nombre)
        editor.putString("KEY_RUT", rut)
        editor.putString("KEY_CORREO", correo)
        editor.putString("KEY_RUBRO", rubro)
        editor.putString("KEY_TELEFONO", telefono)
        editor.putString("KEY_TAMANO_NEGOCIO", tamanoNegocio)
        // Se guarda como texto simple porque es un proyecto académico con datos ficticios;
        // en una app real la contraseña se guardaría cifrada o la revisaría un servidor.
        editor.putString("KEY_PASSWORD", password)

        // apply() confirma y guarda los cambios en el disco duro sin congelar la pantalla
        editor.apply()
    }

    // 3. FUNCIONES PARA LEER (Recuperar de la libreta)
    // El segundo texto (ej: "Emprendedor") es el valor por defecto si aún no se ha guardado nada.
    // El operador '?:' (Elvis) de Null Safety asegura que nunca devuelva null.
    // RUT, correo, teléfono y contraseña usan "" (vacío) por defecto: así, si no hay perfil,
    // el formulario no se llena con textos de relleno (como "Sin RUT") y el login no puede coincidir con ellos.
    fun obtenerNombre(): String = preferences.getString("KEY_NOMBRE", "Emprendedor") ?: "Emprendedor"
    fun obtenerRut(): String = preferences.getString("KEY_RUT", "") ?: ""
    fun obtenerCorreo(): String = preferences.getString("KEY_CORREO", "") ?: ""
    fun obtenerRubro(): String = preferences.getString("KEY_RUBRO", "Panadería") ?: "Panadería"
    fun obtenerTelefono(): String = preferences.getString("KEY_TELEFONO", "") ?: ""
    fun obtenerTamanoNegocio(): String = preferences.getString("KEY_TAMANO_NEGOCIO", "Micro") ?: "Micro"
    fun obtenerPassword(): String = preferences.getString("KEY_PASSWORD", "") ?: ""

    // 4. ¿YA HAY UN PERFIL GUARDADO?
    // contains() revisa si la clave existe en el archivo, sin leer su valor.
    // Basta con revisar KEY_NOMBRE porque guardarPerfil() siempre guarda todos los datos juntos.
    fun existePerfil(): Boolean = preferences.contains("KEY_NOMBRE")

    // 5. REVISAR LAS CREDENCIALES DEL LOGIN (la llama LoginViewModel.iniciarSesion)
    // El usuario puede entrar con su RUT o con su correo, más la contraseña del registro.
    // - trim() quita espacios al inicio y al final (por si se escribió " 12345678-5 ").
    // - ignoreCase = true: "Juan@Mail.cl" es igual a "juan@mail.cl" y "-k" igual a "-K".
    // - La contraseña se compara exacta (distingue mayúsculas y minúsculas).
    // Devuelve true solo si el usuario Y la contraseña coinciden.
    fun validarCredenciales(rutOCorreoIngresado: String, passwordIngresada: String): Boolean {
        val rutGuardado = obtenerRut()
        val correoGuardado = obtenerCorreo()
        val passwordGuardada = obtenerPassword()

        val usuarioValido = rutOCorreoIngresado.trim().equals(rutGuardado.trim(), ignoreCase = true) ||
                rutOCorreoIngresado.trim().equals(correoGuardado.trim(), ignoreCase = true)

        val passwordValida = passwordIngresada == passwordGuardada

        return usuarioValido && passwordValida
    }

    // 6. ENCUESTA DE PREFERENCIAS
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