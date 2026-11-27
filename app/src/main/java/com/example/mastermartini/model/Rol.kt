package com.example.mastermartini.model

private val OPCION_CREAR_CURSO = OpcionPanel(
    titulo = "Crear curso online",
    descripcion = "Publica un curso con video de Cloudinary en el catálogo",
    ruta = "panel/crear-curso"
)
private val OPCION_CLIENTES = OpcionPanel(
    titulo = "Clientes y Plan Pro",
    descripcion = "Busca clientes y activa o quita su Plan Pro",
    ruta = "panel/clientes"
)

private val OPCION_USUARIOS = OpcionPanel(
    titulo = "Usuarios y roles",
    descripcion = "Cambia el rol de un usuario o elimina su cuenta",
    ruta = "panel/usuarios"
)

// Roles de usuario y sus permisos de negocio en la aplicación
sealed class Rol(val codigo: String, val nombreVisible: String) {

    abstract val puedeGestionarPlanPro: Boolean
    abstract val puedeGestionarUsuarios: Boolean
    abstract val puedeResponderPreguntas: Boolean
    abstract fun puedeEditarCurso(instructorIdDelCurso: Long?, miId: Long?): Boolean
    abstract val puedeEliminarCursos: Boolean
    abstract fun opcionesPanel(): List<OpcionPanel>

    data object Cliente : Rol(codigo = "CLIENTE", nombreVisible = "Cliente") {
        override val puedeGestionarPlanPro = false
        override val puedeGestionarUsuarios = false
        override val puedeResponderPreguntas = false
        override fun puedeEditarCurso(instructorIdDelCurso: Long?, miId: Long?) = false
        override val puedeEliminarCursos = false
        override fun opcionesPanel(): List<OpcionPanel> = emptyList()
    }

    data object Instructor : Rol(codigo = "INSTRUCTOR", nombreVisible = "Instructor") {
        override val puedeGestionarPlanPro = false
        override val puedeGestionarUsuarios = false
        override val puedeResponderPreguntas = true
        override fun puedeEditarCurso(instructorIdDelCurso: Long?, miId: Long?): Boolean {
            return instructorIdDelCurso != null && miId != null && instructorIdDelCurso == miId
        }
        override val puedeEliminarCursos = false
        override fun opcionesPanel(): List<OpcionPanel> = listOf(OPCION_CREAR_CURSO)
    }

    data object Ventas : Rol(codigo = "VENTAS", nombreVisible = "Ventas") {
        override val puedeGestionarPlanPro = true
        override val puedeGestionarUsuarios = false
        override val puedeResponderPreguntas = false
        override fun puedeEditarCurso(instructorIdDelCurso: Long?, miId: Long?) = false
        override val puedeEliminarCursos = false
        override fun opcionesPanel(): List<OpcionPanel> = listOf(OPCION_CLIENTES)
    }

    data object Administrador : Rol(codigo = "ADMINISTRADOR", nombreVisible = "Administrador") {
        override val puedeGestionarPlanPro = true
        override val puedeGestionarUsuarios = true
        override val puedeResponderPreguntas = true
        override fun puedeEditarCurso(instructorIdDelCurso: Long?, miId: Long?) = true
        override val puedeEliminarCursos = true
        override fun opcionesPanel(): List<OpcionPanel> = listOf(OPCION_CLIENTES, OPCION_USUARIOS,OPCION_CREAR_CURSO)
    }

    companion object {
        val todos: List<Rol>
            get() = listOf(Cliente, Instructor, Ventas, Administrador)

        // Resuelve un rol desde su código de texto, usando Cliente por defecto
        fun desdeCodigo(codigo: String): Rol {
            return todos.find { rol -> rol.codigo == codigo.trim().uppercase() } ?: Cliente
        }
    }
}
