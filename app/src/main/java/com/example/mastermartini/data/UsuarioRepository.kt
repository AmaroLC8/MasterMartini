package com.example.mastermartini.data

import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario

// REPOSITORIO DE USUARIOS (INTERFAZ)
//
// Una "interface" es un CONTRATO: dice QUÉ funciones existen, pero no CÓMO se hacen.
// Hoy lo cumple UsuarioRepositoryFalso (una lista en memoria) y mañana lo cumplirá
// un repositorio con Retrofit que habla con el backend. Los ViewModels solo conocen
// esta interfaz, así que al cambiar uno por otro NO se toca ninguna pantalla ni ViewModel:
// se cambia una sola línea en ProveedorRepositorios.
// Además, en las pruebas se le puede pasar al ViewModel un repositorio de mentira (MockK).
//
// Todas las funciones son "suspend": hablar con un servidor demora, y una función suspend
// puede esperar sin congelar la pantalla. Se llaman dentro de viewModelScope.launch { }.
//
// Si todo sale bien DEVUELVEN el dato. Si algo sale mal LANZAN una UsuarioException
// (ver UsuarioException.kt), que el ViewModel atrapa con try/catch.
//
// "idSolicitante" = el id de quien PIDE la acción. El backend revisa su rol y responde
// AccesoDenegado (403) si no tiene permiso.
interface UsuarioRepository {

    // POST /api/usuarios/login. "usuario" puede ser el RUT o el correo.
    // Lanza CredencialesInvalidas (401) si no coinciden.
    suspend fun login(usuario: String, password: String): Usuario

    // POST /api/usuarios/registro. Crea la cuenta como Cliente y sin Plan Pro.
    // Lanza DatoDuplicado (409) si el correo o el RUT ya existen.
    suspend fun registrar(
        nombre: String,
        rut: String,
        correo: String,
        telefono: String,
        rubro: String,
        personasNegocio: String,
        password: String
    ): Usuario

    // POST /api/usuarios/recuperar. Genera un código de 6 dígitos que vence en 10 minutos.
    suspend fun solicitarCodigo(correo: String)

    // POST /api/usuarios/restablecer. Lanza CodigoInvalido si el código está mal o venció.
    suspend fun restablecerPassword(correo: String, codigo: String, nuevaPassword: String)

    // GET /api/usuarios/{id}. Lanza NoEncontrado si no existe.
    suspend fun obtenerPerfil(id: Long): Usuario

    // PUT /api/usuarios/{id}. Solo cambia estos datos: el RUT, el correo,
    // el rol y el Plan Pro no se cambian desde el perfil.
    suspend fun actualizarPerfil(
        id: Long,
        nombre: String,
        telefono: String,
        rubro: String,
        personasNegocio: String
    ): Usuario

    // GET /api/usuarios?rol=... Solo VENTAS o ADMINISTRADOR. rol = null devuelve todos.
    suspend fun listarUsuarios(idSolicitante: Long, rol: Rol?): List<Usuario>

    // PUT /api/usuarios/{id}/plan-pro. Solo VENTAS o ADMINISTRADOR.
    suspend fun cambiarPlanPro(idSolicitante: Long, idUsuario: Long, activo: Boolean): Usuario

    // PUT /api/usuarios/{id}/rol. Solo ADMINISTRADOR.
    suspend fun cambiarRol(idSolicitante: Long, idUsuario: Long, rol: Rol): Usuario

    // DELETE /api/usuarios/{id}. Solo ADMINISTRADOR.
    suspend fun eliminarUsuario(idSolicitante: Long, idUsuario: Long)
}
