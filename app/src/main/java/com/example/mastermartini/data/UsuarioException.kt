package com.example.mastermartini.data

// Jerarquía de excepciones de dominio para operaciones de autenticación y usuarios
sealed class UsuarioException(mensaje: String) : Exception(mensaje) {

    class CredencialesInvalidas :
        UsuarioException("RUT/correo o contraseña incorrectos")

    class DatoDuplicado(campo: String) :
        UsuarioException("Ya existe una cuenta con ese $campo. Inicia sesión o usa otro $campo.")

    class AccesoDenegado :
        UsuarioException("No tienes permiso para hacer esta acción.")

    class NoEncontrado :
        UsuarioException("No encontramos ese usuario.")

    class CodigoInvalido :
        UsuarioException("El código no es correcto o ya venció. Pide uno nuevo.")

    class SinConexion :
        UsuarioException(MENSAJE_SIN_CONEXION)

    class ErrorServidor(mensaje: String?) :
        UsuarioException(
            mensaje?.ifBlank { null } ?: "Tuvimos un problema con el servidor. Inténtalo más tarde."
        )
}
