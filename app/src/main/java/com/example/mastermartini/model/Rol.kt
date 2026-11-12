package com.example.mastermartini.model

// ROL DEL USUARIO (POO: HERENCIA Y POLIMORFISMO)
//
// "sealed class" (clase sellada) = una clase PADRE cuyas clases hijas están TODAS escritas
// en este mismo archivo. Nadie puede inventar un rol nuevo en otra parte de la app.
// Por eso un "when (rol)" sabe que solo existen estos 4 casos y no necesita un "else".
//
// HERENCIA: Cliente, Instructor, Ventas y Administrador HEREDAN de Rol (": Rol(...)").
// Todos reciben del padre los mismos datos: codigo y nombreVisible.
//
// POLIMORFISMO: el padre declara propiedades "abstract" (sin valor) y CADA hijo las define
// a su manera. Así, quien tenga un Rol puede preguntar "rol.puedeGestionarPlanPro" sin saber
// qué rol es: cada uno responde lo suyo. Ejemplo:
//     val rol: Rol = Rol.Ventas
//     rol.puedeGestionarPlanPro   // true (lo dice Ventas)
//     rol.puedeGestionarUsuarios  // false
// Sin polimorfismo habría que escribir un if por cada rol en cada pantalla
// (if (rol == "VENTAS" || rol == "ADMINISTRADOR") ...), repetido en muchos lugares.
//
// codigo: el texto que usa el backend ("CLIENTE", "VENTAS"...). Se guarda en SharedPreferences.
// nombreVisible: el texto que se muestra en pantalla ("Cliente", "Ventas"...).
sealed class Rol(val codigo: String, val nombreVisible: String) {

    // ¿Puede activar o quitar el Plan Pro y ver la lista de usuarios? (backend: VENTAS o ADMINISTRADOR)
    abstract val puedeGestionarPlanPro: Boolean

    // ¿Puede cambiar el rol de otros usuarios o eliminarlos? (backend: solo ADMINISTRADOR)
    abstract val puedeGestionarUsuarios: Boolean

    // ¿Puede responder las preguntas de los alumnos en los cursos?
    abstract val puedeResponderPreguntas: Boolean

    // Títulos de las secciones de su "Panel de trabajo". Vacía para el Cliente (no tiene panel).
    abstract fun opcionesPanel(): List<String>

    // "data object" = un objeto único (hay UN solo Cliente en toda la app, no se crea con "Cliente()")
    // que además se imprime bonito: println(Rol.Cliente) muestra "Cliente".

    // CLIENTE: el emprendedor que usa la app. No tiene permisos especiales.
    data object Cliente : Rol(codigo = "CLIENTE", nombreVisible = "Cliente") {
        override val puedeGestionarPlanPro = false
        override val puedeGestionarUsuarios = false
        override val puedeResponderPreguntas = false
        override fun opcionesPanel(): List<String> = emptyList()
    }

    // INSTRUCTOR: dicta los cursos online y responde las dudas de sus alumnos
    data object Instructor : Rol(codigo = "INSTRUCTOR", nombreVisible = "Instructor") {
        override val puedeGestionarPlanPro = false
        override val puedeGestionarUsuarios = false
        override val puedeResponderPreguntas = true
        override fun opcionesPanel(): List<String> = listOf("Mis cursos", "Preguntas de alumnos")
    }

    // VENTAS: coordina las suscripciones al Plan Pro con los clientes
    data object Ventas : Rol(codigo = "VENTAS", nombreVisible = "Ventas") {
        override val puedeGestionarPlanPro = true
        override val puedeGestionarUsuarios = false
        override val puedeResponderPreguntas = false
        override fun opcionesPanel(): List<String> = listOf("Clientes", "Suscripciones Plan Pro")
    }

    // ADMINISTRADOR: puede hacer todo
    data object Administrador : Rol(codigo = "ADMINISTRADOR", nombreVisible = "Administrador") {
        override val puedeGestionarPlanPro = true
        override val puedeGestionarUsuarios = true
        override val puedeResponderPreguntas = true
        override fun opcionesPanel(): List<String> =
            listOf("Usuarios y roles", "Suscripciones Plan Pro", "Preguntas de alumnos")
    }

    // companion object = lo que pertenece a la CLASE Rol y no a un rol en particular.
    // Se usa así: Rol.desdeCodigo("VENTAS"), Rol.todos.
    companion object {

        // Los 4 roles. Se escribe con "get()" (se arma cada vez que se pide) y no como
        // "val todos = listOf(...)": los hijos se crean DESPUÉS que el padre, y una lista
        // armada al crear el padre podría quedar con un rol en null.
        val todos: List<Rol>
            get() = listOf(Cliente, Instructor, Ventas, Administrador)

        // Convierte el texto guardado (o el que manda el backend) en un Rol.
        // uppercase() acepta "ventas" o "Ventas". find devuelve null si no hay ninguno con ese código,
        // y en ese caso se usa Cliente (?:): el rol con MENOS permisos, así un dato raro
        // nunca le da permisos de más a nadie.
        fun desdeCodigo(codigo: String): Rol {
            return todos.find { rol -> rol.codigo == codigo.trim().uppercase() } ?: Cliente
        }
    }
}
