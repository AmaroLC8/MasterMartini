package com.example.mastermartini.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class RolTest : StringSpec({

    "Cliente no tiene permisos de gestión ni opciones en el panel" {
        val rol = Rol.Cliente
        rol.codigo shouldBe "CLIENTE"
        rol.nombreVisible shouldBe "Cliente"
        rol.puedeGestionarPlanPro.shouldBeFalse()
        rol.puedeGestionarUsuarios.shouldBeFalse()
        rol.puedeResponderPreguntas.shouldBeFalse()
        rol.puedeEliminarCursos.shouldBeFalse()
        rol.puedeEditarCurso(instructorIdDelCurso = 1, miId = 1).shouldBeFalse()
        rol.opcionesPanel().shouldBeEmpty()
    }

    "Instructor responde preguntas y solo ve la opción de crear curso" {
        val rol = Rol.Instructor
        rol.codigo shouldBe "INSTRUCTOR"
        rol.puedeGestionarPlanPro.shouldBeFalse()
        rol.puedeGestionarUsuarios.shouldBeFalse()
        rol.puedeResponderPreguntas.shouldBeTrue()
        rol.puedeEliminarCursos.shouldBeFalse()
        rol.opcionesPanel().map { it.ruta } shouldContainExactly listOf("panel/crear-curso")
    }

    "Instructor solo puede editar sus propios cursos" {
        val rol = Rol.Instructor
        rol.puedeEditarCurso(instructorIdDelCurso = 7, miId = 7).shouldBeTrue()
        rol.puedeEditarCurso(instructorIdDelCurso = 7, miId = 8).shouldBeFalse()
        rol.puedeEditarCurso(instructorIdDelCurso = null, miId = 7).shouldBeFalse()
        rol.puedeEditarCurso(instructorIdDelCurso = 7, miId = null).shouldBeFalse()
    }

    "Ventas gestiona el Plan Pro y solo ve la opción de clientes" {
        val rol = Rol.Ventas
        rol.codigo shouldBe "VENTAS"
        rol.puedeGestionarPlanPro.shouldBeTrue()
        rol.puedeGestionarUsuarios.shouldBeFalse()
        rol.puedeResponderPreguntas.shouldBeFalse()
        rol.puedeEliminarCursos.shouldBeFalse()
        rol.puedeEditarCurso(instructorIdDelCurso = 1, miId = 1).shouldBeFalse()
        rol.opcionesPanel().map { it.ruta } shouldContainExactly listOf("panel/clientes")
    }

    "Administrador tiene todos los permisos y las tres opciones del panel" {
        val rol = Rol.Administrador
        rol.codigo shouldBe "ADMINISTRADOR"
        rol.puedeGestionarPlanPro.shouldBeTrue()
        rol.puedeGestionarUsuarios.shouldBeTrue()
        rol.puedeResponderPreguntas.shouldBeTrue()
        rol.puedeEliminarCursos.shouldBeTrue()
        rol.puedeEditarCurso(instructorIdDelCurso = null, miId = null).shouldBeTrue()
        rol.opcionesPanel().map { it.ruta } shouldContainExactly
            listOf("panel/clientes", "panel/usuarios", "panel/crear-curso")
    }

    "todos contiene los cuatro roles" {
        Rol.todos shouldContainExactly
            listOf(Rol.Cliente, Rol.Instructor, Rol.Ventas, Rol.Administrador)
    }

    "desdeCodigo reconoce cada código aunque venga en minúsculas o con espacios" {
        Rol.desdeCodigo("CLIENTE") shouldBe Rol.Cliente
        Rol.desdeCodigo("instructor") shouldBe Rol.Instructor
        Rol.desdeCodigo(" Ventas ") shouldBe Rol.Ventas
        Rol.desdeCodigo("ADMINISTRADOR") shouldBe Rol.Administrador
    }

    "desdeCodigo devuelve Cliente si el código es desconocido o vacío" {
        Rol.desdeCodigo("SUPERUSUARIO") shouldBe Rol.Cliente
        Rol.desdeCodigo("") shouldBe Rol.Cliente
    }
})
