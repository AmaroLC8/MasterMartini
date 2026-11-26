package com.example.mastermartini.data

import android.util.Log
import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario
import kotlinx.coroutines.delay

// Implementación en memoria de UsuarioRepository para pruebas locales sin backend
class UsuarioRepositoryFalso : UsuarioRepository {

    private data class Cuenta(val usuario: Usuario, val password: String)

    private data class CodigoRecuperacion(val codigo: String, val venceEn: Long)

    private val cuentas = mutableListOf(
        Cuenta(
            Usuario(
                id = 1, nombre = "Cliente de Prueba", rut = "11111111-1",
                correo = "cliente@example.com", telefono = "911111111", rubro = "Panadería",
                personasNegocio = "2 a 5 personas", rol = Rol.Cliente, planPro = false
            ),
            password = "Demo1234"
        ),
        Cuenta(
            Usuario(
                id = 2, nombre = "Instructor de Prueba", rut = "22222222-2",
                correo = "instructor@example.com", telefono = "922222222", rubro = "Chocolatería",
                personasNegocio = "Solo yo", rol = Rol.Instructor, planPro = true
            ),
            password = "Demo1234"
        ),
        Cuenta(
            Usuario(
                id = 3, nombre = "Ventas de Prueba", rut = "33333333-3",
                correo = "ventas@example.com", telefono = "933333333", rubro = "Pastelería",
                personasNegocio = "6 a 20 personas", rol = Rol.Ventas, planPro = false
            ),
            password = "Demo1234"
        ),
        Cuenta(
            Usuario(
                id = 4, nombre = "Administrador de Prueba", rut = "44444444-4",
                correo = "admin@example.com", telefono = "944444444", rubro = "Panadería",
                personasNegocio = "Más de 20 personas", rol = Rol.Administrador, planPro = true
            ),
            password = "Demo1234"
        )
    )

    private var siguienteId = 5L
    private val codigos = mutableMapOf<String, CodigoRecuperacion>()

    private suspend fun esperarRed() {
        delay(600)
    }

    private fun normalizarRut(rut: String): String {
        return rut.replace(".", "").replace(" ", "").trim().uppercase()
    }

    private fun normalizarCorreo(correo: String): String {
        return correo.trim().lowercase()
    }

    private fun buscarCuenta(id: Long): Cuenta {
        return cuentas.find { cuenta -> cuenta.usuario.id == id }
            ?: throw UsuarioException.NoEncontrado()
    }

    private fun guardarCuenta(cuentaNueva: Cuenta) {
        val posicion = cuentas.indexOfFirst { cuenta -> cuenta.usuario.id == cuentaNueva.usuario.id }
        cuentas[posicion] = cuentaNueva
    }

    // Valida el permiso del solicitante según su rol antes de ejecutar acciones protegidas
    private fun revisarPermiso(idSolicitante: Long, permiso: (Rol) -> Boolean) {
        val solicitante = cuentas.find { cuenta -> cuenta.usuario.id == idSolicitante }
        if (solicitante == null || !permiso(solicitante.usuario.rol)) {
            throw UsuarioException.AccesoDenegado()
        }
    }

    override suspend fun login(usuario: String, password: String): Usuario {
        esperarRed()
        val cuenta = cuentas.find { cuenta ->
            cuenta.usuario.rut == normalizarRut(usuario) ||
                cuenta.usuario.correo == normalizarCorreo(usuario)
        }
        if (cuenta == null || cuenta.password != password) {
            throw UsuarioException.CredencialesInvalidas()
        }
        return cuenta.usuario
    }

    override suspend fun registrar(
        nombre: String,
        rut: String,
        correo: String,
        telefono: String,
        rubro: String,
        personasNegocio: String,
        password: String
    ): Usuario {
        esperarRed()
        val rutLimpio = normalizarRut(rut)
        val correoLimpio = normalizarCorreo(correo)

        if (cuentas.any { cuenta -> cuenta.usuario.correo == correoLimpio }) {
            throw UsuarioException.DatoDuplicado("correo")
        }
        if (cuentas.any { cuenta -> cuenta.usuario.rut == rutLimpio }) {
            throw UsuarioException.DatoDuplicado("RUT")
        }

        val nuevo = Usuario(
            id = siguienteId,
            nombre = nombre.trim(),
            rut = rutLimpio,
            correo = correoLimpio,
            telefono = telefono,
            rubro = rubro,
            personasNegocio = personasNegocio,
            rol = Rol.Cliente,
            planPro = false
        )
        siguienteId++
        cuentas.add(Cuenta(nuevo, password))
        return nuevo
    }

    // Genera un código de recuperación temporal de 10 minutos y lo imprime en Logcat
    override suspend fun solicitarCodigo(correo: String) {
        esperarRed()
        val correoLimpio = normalizarCorreo(correo)
        if (cuentas.none { cuenta -> cuenta.usuario.correo == correoLimpio }) return

        val codigo = (100000..999999).random().toString()
        val venceEn = System.currentTimeMillis() + 10 * 60 * 1000
        codigos[correoLimpio] = CodigoRecuperacion(codigo, venceEn)

        Log.d("RecuperarPassword", "Código para $correoLimpio: $codigo (vence en 10 minutos)")
    }

    override suspend fun restablecerPassword(correo: String, codigo: String, nuevaPassword: String) {
        esperarRed()
        val correoLimpio = normalizarCorreo(correo)
        val guardado = codigos[correoLimpio]

        val vencido = guardado != null && System.currentTimeMillis() > guardado.venceEn
        if (guardado == null || guardado.codigo != codigo.trim() || vencido) {
            throw UsuarioException.CodigoInvalido()
        }

        val cuenta = cuentas.find { it.usuario.correo == correoLimpio }
            ?: throw UsuarioException.NoEncontrado()
        guardarCuenta(cuenta.copy(password = nuevaPassword))
        codigos.remove(correoLimpio)
    }

    override suspend fun obtenerPerfil(id: Long): Usuario {
        esperarRed()
        return buscarCuenta(id).usuario
    }

    override suspend fun actualizarPerfil(
        id: Long,
        nombre: String,
        telefono: String,
        rubro: String,
        personasNegocio: String
    ): Usuario {
        esperarRed()
        val cuenta = buscarCuenta(id)
        val actualizado = cuenta.usuario.copy(
            nombre = nombre.trim(),
            telefono = telefono,
            rubro = rubro,
            personasNegocio = personasNegocio
        )
        guardarCuenta(cuenta.copy(usuario = actualizado))
        return actualizado
    }

    override suspend fun listarUsuarios(idSolicitante: Long, rol: Rol?): List<Usuario> {
        esperarRed()
        revisarPermiso(idSolicitante) { rolSolicitante -> rolSolicitante.puedeGestionarPlanPro }
        val todos = cuentas.map { cuenta -> cuenta.usuario }
        return if (rol == null) todos else todos.filter { usuario -> usuario.rol == rol }
    }

    override suspend fun cambiarPlanPro(idSolicitante: Long, idUsuario: Long, activo: Boolean): Usuario {
        esperarRed()
        revisarPermiso(idSolicitante) { rolSolicitante -> rolSolicitante.puedeGestionarPlanPro }
        val cuenta = buscarCuenta(idUsuario)
        val actualizado = cuenta.usuario.copy(planPro = activo)
        guardarCuenta(cuenta.copy(usuario = actualizado))
        return actualizado
    }

    override suspend fun cambiarRol(idSolicitante: Long, idUsuario: Long, rol: Rol): Usuario {
        esperarRed()
        revisarPermiso(idSolicitante) { rolSolicitante -> rolSolicitante.puedeGestionarUsuarios }
        val cuenta = buscarCuenta(idUsuario)
        val actualizado = cuenta.usuario.copy(rol = rol)
        guardarCuenta(cuenta.copy(usuario = actualizado))
        return actualizado
    }

    override suspend fun eliminarUsuario(idSolicitante: Long, idUsuario: Long) {
        esperarRed()
        revisarPermiso(idSolicitante) { rolSolicitante -> rolSolicitante.puedeGestionarUsuarios }
        val cuenta = buscarCuenta(idUsuario)
        cuentas.remove(cuenta)
    }
}
