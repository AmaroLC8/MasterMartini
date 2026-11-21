package com.example.mastermartini.data

// ERRORES PROPIOS DEL MANEJO DE USUARIOS (excepciones personalizadas)
//
// Exception es la clase de Kotlin para los errores. Aquí se crea una familia propia:
// UsuarioException HEREDA de Exception, y cada error concreto hereda de UsuarioException.
// Es "sealed" (sellada), igual que Rol: todos los errores posibles están en este archivo.
//
// ¿Para qué? El repositorio LANZA uno de estos errores (throw) cuando algo sale mal, y el
// ViewModel lo ATRAPA con try/catch y muestra e.message en pantalla:
//     try {
//         val usuario = usuarioRepository.login(rut, password)
//     } catch (e: UsuarioException) {
//         errorGeneral = e.message   // ej. "RUT/correo o contraseña incorrectos"
//     }
// Cada error ya trae su mensaje en español, así ninguna pantalla tiene que inventarlo.
//
// Cómo calzan con las respuestas del backend (código HTTP):
//   401 -> CredencialesInvalidas    409 -> DatoDuplicado    403 -> AccesoDenegado
//   404 -> NoEncontrado             400 -> CodigoInvalido   sin internet -> SinConexion
sealed class UsuarioException(mensaje: String) : Exception(mensaje) {

    // El RUT/correo o la contraseña no coinciden. Es el MISMO mensaje si falla el usuario
    // o la contraseña, para no darle pistas a alguien que intenta adivinar cuentas.
    class CredencialesInvalidas :
        UsuarioException("RUT/correo o contraseña incorrectos")

    // Ya existe una cuenta con ese correo o ese RUT. "campo" dice cuál: "correo" o "RUT".
    class DatoDuplicado(campo: String) :
        UsuarioException("Ya existe una cuenta con ese $campo. Inicia sesión o usa otro $campo.")

    // El rol de quien pide la acción no tiene permiso para hacerla (ej. un Cliente quiere ver la lista de usuarios)
    class AccesoDenegado :
        UsuarioException("No tienes permiso para hacer esta acción.")

    // El usuario pedido no existe
    class NoEncontrado :
        UsuarioException("No encontramos ese usuario.")

    // El código para recuperar la contraseña está mal escrito o ya venció (dura 10 minutos)
    class CodigoInvalido :
        UsuarioException("El código no es correcto o ya venció. Pide uno nuevo.")

    // No se pudo conectar con el servidor. El repositorio falso nunca la lanza;
    // la usará el repositorio con Retrofit cuando no haya internet.
    class SinConexion :
        UsuarioException("No pudimos conectarnos. Revisa tu internet e inténtalo otra vez.")
}
