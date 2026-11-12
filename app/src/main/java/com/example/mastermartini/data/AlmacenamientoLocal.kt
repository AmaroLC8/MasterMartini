package com.example.mastermartini.data

import android.content.Context
import android.content.SharedPreferences
import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario

// Recibe el 'context' (el permiso del sistema Android para acceder a la memoria interna del celular)
class AlmacenamientoLocal(context: Context) {

    // 1. Abrimos o creamos el archivo "master_martini_prefs" en modo privado (solo nuestra app puede leerlo)
    private val preferences: SharedPreferences =
        context.getSharedPreferences("master_martini_prefs", Context.MODE_PRIVATE)

    // 2. GUARDAR LA SESIÓN (Escribir en la libreta)
    // Se llama cuando el login, el registro o "Editar datos" salen bien, con el Usuario
    // que devolvió el repositorio (el backend). Guarda quién inició sesión (id, rol y Plan Pro)
    // y sus datos, con las MISMAS claves de antes: así Home y Perfil los siguen leyendo igual.
    // La contraseña YA NO se guarda en el celular: la revisa el servidor al iniciar sesión.
    fun guardarSesion(usuario: Usuario) {
        // Pedimos un "lápiz" (editor) para escribir en el archivo
        val editor = preferences.edit()

        // Datos de la sesión. putLong porque el id es Long; el rol se guarda como su
        // código de texto ("VENTAS") y se vuelve a convertir en Rol con Rol.desdeCodigo().
        editor.putLong("KEY_ID_USUARIO", usuario.id)
        editor.putString("KEY_ROL", usuario.rol.codigo)
        editor.putBoolean("KEY_PLAN_PRO", usuario.planPro)

        // Guardamos cada dato con su etiqueta (Clave -> Valor), igual que un mapOf
        editor.putString("KEY_NOMBRE", usuario.nombre)
        editor.putString("KEY_RUT", usuario.rut)
        editor.putString("KEY_CORREO", usuario.correo)
        editor.putString("KEY_RUBRO", usuario.rubro)
        editor.putString("KEY_TELEFONO", usuario.telefono)
        // Clave NUEVA (y no "KEY_TAMANO_NEGOCIO"): así un "Micro" guardado antes nunca se lee
        // como si fuera una respuesta de la pregunta nueva.
        editor.putString("KEY_PERSONAS_NEGOCIO", usuario.personasNegocio)
        // Se borran claves antiguas que ya no se usan (si no existían, no pasa nada).
        // KEY_PASSWORD: las versiones anteriores de la app guardaban aquí la contraseña.
        editor.remove("KEY_TAMANO_NEGOCIO")
        editor.remove("KEY_PASSWORD")

        // apply() confirma y guarda los cambios en el disco duro sin congelar la pantalla
        editor.apply()
    }

    // 3. FUNCIONES PARA LEER (Recuperar de la libreta)
    // El segundo texto (ej: "Emprendedor") es el valor por defecto si aún no se ha guardado nada.
    // El operador '?:' (Elvis) de Null Safety asegura que nunca devuelva null.
    // RUT, correo y teléfono usan "" (vacío) por defecto: así, si no hay perfil,
    // el formulario no se llena con textos de relleno (como "Sin RUT").
    fun obtenerNombre(): String = preferences.getString("KEY_NOMBRE", "Emprendedor") ?: "Emprendedor"
    fun obtenerRut(): String = preferences.getString("KEY_RUT", "") ?: ""
    fun obtenerCorreo(): String = preferences.getString("KEY_CORREO", "") ?: ""
    fun obtenerRubro(): String = preferences.getString("KEY_RUBRO", "Panadería") ?: "Panadería"
    fun obtenerTelefono(): String = preferences.getString("KEY_TELEFONO", "") ?: ""
    // "" (vacío) si todavía no se responde la pregunta nueva
    fun obtenerPersonasNegocio(): String = preferences.getString("KEY_PERSONAS_NEGOCIO", "") ?: ""

    // Id del usuario que inició sesión, o null si no hay sesión.
    // getLong no acepta null como valor por defecto, por eso se usa -1 (ningún id es -1)
    // y después se convierte: si quedó en -1, se devuelve null.
    fun obtenerIdUsuario(): Long? {
        val id = preferences.getLong("KEY_ID_USUARIO", -1L)
        return if (id == -1L) null else id
    }

    // Rol guardado. Si no hay sesión (o el texto es raro), desdeCodigo devuelve Cliente:
    // el rol con menos permisos.
    fun obtenerRol(): Rol = Rol.desdeCodigo(preferences.getString("KEY_ROL", "CLIENTE") ?: "CLIENTE")

    // true si el usuario tiene el Plan Pro activo (false si no hay sesión)
    fun tienePlanPro(): Boolean = preferences.getBoolean("KEY_PLAN_PRO", false)

    // 4. ¿YA HAY UN PERFIL GUARDADO?
    // contains() revisa si la clave existe en el archivo, sin leer su valor.
    // Basta con revisar KEY_NOMBRE porque guardarSesion() siempre guarda todos los datos juntos.
    fun existePerfil(): Boolean = preferences.contains("KEY_NOMBRE")

    // 5. CERRAR SESIÓN
    // Borra la sesión: el id, el rol, el Plan Pro y los datos del usuario.
    // Así, si otra persona usa el celular, no ve los datos del anterior, y el formulario
    // de Registro queda vacío para crear una cuenta nueva. Para volver a entrar, el login
    // le pide los datos al repositorio (el backend) y guardarSesion() los escribe de nuevo.
    // NO se borran la encuesta ni la foto del local (son preferencias de este celular).
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

    // 7. RUTA DE LA FOTO DEL LOCAL
    // La foto en sí se guarda como archivo JPEG (lo hace FotoRepository);
    // aquí solo se anota DÓNDE quedó ese archivo, como un texto más.
    fun guardarRutaFoto(ruta: String) {
        preferences.edit().putString("KEY_FOTO_LOCAL", ruta).apply()
    }

    // "" (vacío) si nunca se guardó una foto
    fun obtenerRutaFoto(): String = preferences.getString("KEY_FOTO_LOCAL", "") ?: ""
}