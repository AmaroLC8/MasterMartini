package com.example.mastermartini.data

import android.util.Log
import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario
import kotlinx.coroutines.delay

// REPOSITORIO DE USUARIOS FALSO
// TEMPORAL: se reemplaza por el repositorio con Retrofit cuando el backend esté listo.
// Lo registrado se pierde al cerrar la app (vive en una lista en memoria, no en una base de datos).
//
// Cumple el contrato UsuarioRepository (": UsuarioRepository") y aplica las MISMAS reglas
// que tendrá el backend: datos duplicados, contraseña incorrecta, permisos por rol y
// código de recuperación que vence. Así las pantallas se prueban hoy como si el servidor existiera.
//
// USUARIOS DE PRUEBA (todos con la contraseña Demo1234). DATOS DE EJEMPLO:
//   cliente@example.com     -> Cliente
//   instructor@example.com  -> Instructor
//   ventas@example.com      -> Ventas
//   admin@example.com       -> Administrador
// Los RUT son inventados (11111111-1, 22222222-2...) pero tienen el dígito verificador
// correcto, así pasan la misma validación del formulario de Registro.
class UsuarioRepositoryFalso : UsuarioRepository {

    // Lo que guarda el "servidor" de cada cuenta: el usuario y su contraseña.
    // La contraseña va aparte porque Usuario NO la incluye (nunca se devuelve a la app).
    // Es private: solo este archivo la conoce.
    private data class Cuenta(val usuario: Usuario, val password: String)

    // Código para recuperar la contraseña y el momento en que vence (en milisegundos)
    private data class CodigoRecuperacion(val codigo: String, val venceEn: Long)

    // "Base de datos" en memoria. mutableListOf permite agregar, cambiar y borrar cuentas.
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

    // Id que recibirá la próxima cuenta registrada (los de prueba usan del 1 al 4)
    private var siguienteId = 5L

    // Códigos de recuperación pendientes: clave = correo, valor = código y vencimiento
    private val codigos = mutableMapOf<String, CodigoRecuperacion>()

    // Simula lo que demora la red (600 milisegundos), para ver el indicador de carga
    // igual que con el servidor de verdad. delay() espera SIN congelar la pantalla.
    private suspend fun esperarRed() {
        delay(600)
    }

    // --- AYUDANTES PRIVADOS ---

    // Deja el RUT sin puntos ni espacios y con la K en mayúscula: "11.111.111-k" -> "11111111-K".
    // Así "11.111.111-1" y "11111111-1" se reconocen como el mismo RUT.
    private fun normalizarRut(rut: String): String {
        return rut.replace(".", "").replace(" ", "").trim().uppercase()
    }

    // Correo sin espacios y en minúsculas: "Cliente@Example.com " -> "cliente@example.com"
    private fun normalizarCorreo(correo: String): String {
        return correo.trim().lowercase()
    }

    // Busca la cuenta de un id, o lanza NoEncontrado (404) si no existe
    private fun buscarCuenta(id: Long): Cuenta {
        return cuentas.find { cuenta -> cuenta.usuario.id == id }
            ?: throw UsuarioException.NoEncontrado()
    }

    // Reemplaza una cuenta por su versión nueva (las data class no se cambian: se copian con copy())
    private fun guardarCuenta(cuentaNueva: Cuenta) {
        val posicion = cuentas.indexOfFirst { cuenta -> cuenta.usuario.id == cuentaNueva.usuario.id }
        cuentas[posicion] = cuentaNueva
    }

    // REVISA LOS PERMISOS (403) de quien pide la acción.
    // "permiso" es una función que recibe el Rol y responde true o false, por ejemplo:
    //     revisarPermiso(idSolicitante) { rol -> rol.puedeGestionarUsuarios }
    // Aquí se ve el POLIMORFISMO de Rol: no se pregunta "¿es ADMINISTRADOR?", se le pregunta
    // al rol si PUEDE, y cada rol responde lo suyo.
    // Si quien pide no existe, también es AccesoDenegado.
    private fun revisarPermiso(idSolicitante: Long, permiso: (Rol) -> Boolean) {
        val solicitante = cuentas.find { cuenta -> cuenta.usuario.id == idSolicitante }
        if (solicitante == null || !permiso(solicitante.usuario.rol)) {
            throw UsuarioException.AccesoDenegado()
        }
    }

    // --- FUNCIONES DEL CONTRATO (override = "cumplo lo que pide la interfaz") ---

    override suspend fun login(usuario: String, password: String): Usuario {
        esperarRed()
        // Se busca por RUT o por correo
        val cuenta = cuentas.find { cuenta ->
            cuenta.usuario.rut == normalizarRut(usuario) ||
                cuenta.usuario.correo == normalizarCorreo(usuario)
        }
        // MISMO error si no existe el usuario o si la contraseña no coincide,
        // para no revelar qué cuentas existen. La contraseña distingue mayúsculas.
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

        // 409: no puede haber dos cuentas con el mismo correo ni con el mismo RUT
        if (cuentas.any { cuenta -> cuenta.usuario.correo == correoLimpio }) {
            throw UsuarioException.DatoDuplicado("correo")
        }
        if (cuentas.any { cuenta -> cuenta.usuario.rut == rutLimpio }) {
            throw UsuarioException.DatoDuplicado("RUT")
        }

        // Toda cuenta nueva parte como Cliente y sin Plan Pro (igual que en el backend)
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

    override suspend fun solicitarCodigo(correo: String) {
        esperarRed()
        val correoLimpio = normalizarCorreo(correo)
        // Si el correo no tiene cuenta, no se avisa: se responde igual que si existiera,
        // para no revelar qué correos están registrados.
        if (cuentas.none { cuenta -> cuenta.usuario.correo == correoLimpio }) return

        // Código de 6 dígitos: un número al azar entre 100000 y 999999
        val codigo = (100000..999999).random().toString()
        // Vence en 10 minutos: la hora actual + 10 min (en milisegundos)
        val venceEn = System.currentTimeMillis() + 10 * 60 * 1000
        codigos[correoLimpio] = CodigoRecuperacion(codigo, venceEn)

        // El backend lo enviaría por correo. Aquí se escribe en el Logcat de Android Studio
        // (buscar la etiqueta "RecuperarPassword") para poder probarlo.
        Log.d("RecuperarPassword", "Código para $correoLimpio: $codigo (vence en 10 minutos)")
    }

    override suspend fun restablecerPassword(correo: String, codigo: String, nuevaPassword: String) {
        esperarRed()
        val correoLimpio = normalizarCorreo(correo)
        val guardado = codigos[correoLimpio]

        // El código es inválido si nunca se pidió, si no coincide o si ya pasó su hora de vencimiento
        val vencido = guardado != null && System.currentTimeMillis() > guardado.venceEn
        if (guardado == null || guardado.codigo != codigo.trim() || vencido) {
            throw UsuarioException.CodigoInvalido()
        }

        val cuenta = cuentas.find { it.usuario.correo == correoLimpio }
            ?: throw UsuarioException.NoEncontrado()
        guardarCuenta(cuenta.copy(password = nuevaPassword))
        // El código se usa UNA sola vez
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
        // copy() deja igual el RUT, el correo, el rol y el Plan Pro
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
        // Solo VENTAS o ADMINISTRADOR (los roles que pueden gestionar el Plan Pro)
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
        // Solo ADMINISTRADOR
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
