package com.example.mastermartini.data

import com.example.mastermartini.data.remote.RetrofitClient
import com.example.mastermartini.data.remote.dto.ActualizarPerfilRequest
import com.example.mastermartini.data.remote.dto.CambiarRolRequest
import com.example.mastermartini.data.remote.dto.LoginRequest
import com.example.mastermartini.data.remote.dto.PlanProRequest
import com.example.mastermartini.data.remote.dto.RecuperarRequest
import com.example.mastermartini.data.remote.dto.RegistroRequest
import com.example.mastermartini.data.remote.dto.RestablecerRequest
import com.example.mastermartini.data.remote.dto.aModelo
import com.example.mastermartini.model.ErrorDto
import com.example.mastermartini.model.Rol
import com.example.mastermartini.model.Usuario
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

// REPOSITORIO DE USUARIOS REAL (habla con el microservicio de usuarios con Retrofit)
// Cumple el MISMO contrato que UsuarioRepositoryFalso (": UsuarioRepository"), por eso
// los ViewModels y las pantallas no cambian: solo se elige cuál usar en ProveedorRepositorios.
//
// Cada función hace 3 cosas:
//   1. Arma el cuerpo de la petición (DTO) con los datos que le pasa el ViewModel.
//   2. Llama a la API (usuariosApi) y convierte la respuesta en el modelo de la app (aModelo()).
//   3. Si algo falla, convierte el error en una UsuarioException con convertirError(),
//      que es lo que los ViewModels saben atrapar (catch (e: UsuarioException)).
class UsuarioRepositoryRemoto : UsuarioRepository {

    // Las funciones de Retrofit (UsuarioApi), creadas una sola vez en RetrofitClient
    private val api = RetrofitClient.usuariosApi

    // --- AYUDANTES PRIVADOS ---

    // Deja solo los números del teléfono: "9 1234 5678" -> "912345678".
    // El backend exige 9 dígitos que empiecen con 9, sin espacios ni otros símbolos.
    // filter { it.isDigit() } recorre cada letra y se queda solo con las que son dígitos.
    private fun soloNumeros(telefono: String): String {
        return telefono.filter { letra -> letra.isDigit() }
    }

    // MANEJO DE EXCEPCIONES: convierte cualquier error de la conexión en una UsuarioException.
    // ¿Por qué? Los ViewModels solo atrapan UsuarioException. Si se les escapara otro tipo de
    // error (IOException, HttpException...), nadie lo atraparía y LA APP SE CAERÍA.
    // Así, pase lo que pase, el ViewModel recibe un error con su mensaje en español.
    //
    // "esRestablecer" = true solo en restablecerPassword: ahí un 400 significa que el código
    // de recuperación está mal o venció (en las demás funciones, 400 = datos inválidos).
    private fun convertirError(e: Exception, esRestablecer: Boolean = false): UsuarioException {
        return when (e) {
            // La corrutina se canceló (ej. se cerró la pantalla): NO es un error para mostrar.
            // Se vuelve a lanzar tal cual para que Kotlin termine la corrutina normalmente.
            is CancellationException -> throw e
            // IOException = no se pudo hablar con el servidor: sin internet, servidor apagado,
            // IP o puerto equivocados, o se demoró demasiado en responder.
            is IOException -> UsuarioException.SinConexion()
            // HttpException = el servidor SÍ respondió, pero con un código de error (4xx o 5xx)
            is HttpException -> convertirErrorHttp(e, esRestablecer)
            // Cualquier otro problema (ej. el JSON vino con una forma inesperada)
            else -> UsuarioException.ErrorServidor(null)
        }
    }

    // Elige la UsuarioException según el código HTTP que respondió el servidor.
    // El backend también manda un cuerpo de error: {"mensaje": "...", "estado": 409}
    // (y en los errores de validación, "campos" con el problema de cada dato).
    private fun convertirErrorHttp(e: HttpException, esRestablecer: Boolean): UsuarioException {
        val error = leerErrorDto(e)
        val mensajeServidor = error?.mensaje
        // En los errores de validación el mensaje general es solo "Datos inválidos";
        // el detalle útil está en "campos" (ej. "El teléfono debe tener 9 dígitos..."),
        // por eso se prefiere el primero de esos mensajes si viene.
        val mensajeParaMostrar = error?.campos?.values?.firstOrNull() ?: mensajeServidor

        return when (e.code()) {
            // 401 Unauthorized: RUT/correo o contraseña incorrectos (login)
            401 -> UsuarioException.CredencialesInvalidas()
            // 403 Forbidden: el rol de quien pide no tiene permiso
            403 -> UsuarioException.AccesoDenegado()
            // 404 Not Found: el usuario no existe
            404 -> UsuarioException.NoEncontrado()
            // 409 Conflict: el correo o el RUT ya están registrados.
            // El servidor dice cuál en su mensaje ("El rut ya esta registrado"), así se elige el campo.
            409 -> {
                val esRut = mensajeServidor?.contains("rut", ignoreCase = true) == true
                UsuarioException.DatoDuplicado(if (esRut) "RUT" else "correo")
            }
            // 400 Bad Request: en restablecer = código malo o vencido; en lo demás = dato inválido
            400 -> if (esRestablecer) {
                UsuarioException.CodigoInvalido()
            } else {
                UsuarioException.ErrorServidor(mensajeParaMostrar)
            }
            // Cualquier otro (500 error interno, 503 servicio no disponible...): mensaje del servidor
            else -> UsuarioException.ErrorServidor(mensajeParaMostrar)
        }
    }

    // Lee el cuerpo del error ({"mensaje","estado","campos"}) y lo convierte en ErrorDto con Gson.
    // Tiene su propio try/catch: si el cuerpo viene vacío o no es JSON, devuelve null
    // (y se usará el mensaje genérico), pero nunca se cae.
    private fun leerErrorDto(e: HttpException): ErrorDto? {
        return try {
            val cuerpo = e.response()?.errorBody()?.string()
            Gson().fromJson(cuerpo, ErrorDto::class.java)
        } catch (ex: Exception) {
            null
        }
    }

    // --- FUNCIONES DEL CONTRATO (override = "cumplo lo que pide la interfaz") ---
    // Todas tienen la misma forma:
    //     try { llamar a la API } catch (e: Exception) { throw convertirError(e) }
    // "throw" lanza la UsuarioException hacia el ViewModel, que la atrapa y muestra e.message.

    override suspend fun login(usuario: String, password: String): Usuario {
        try {
            val respuesta = api.login(LoginRequest(usuario = usuario, password = password))
            return respuesta.aModelo()
        } catch (e: Exception) {
            throw convertirError(e)
        }
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
        try {
            val cuerpo = RegistroRequest(
                nombre = nombre,
                rut = rut,
                correo = correo,
                telefono = soloNumeros(telefono),
                rubro = rubro,
                personasNegocio = personasNegocio,
                password = password
            )
            return api.registrar(cuerpo).aModelo()
        } catch (e: Exception) {
            throw convertirError(e)
        }
    }

    override suspend fun solicitarCodigo(correo: String) {
        try {
            // La respuesta solo trae un mensaje de confirmación: no se necesita para nada
            api.recuperar(RecuperarRequest(correo = correo))
        } catch (e: Exception) {
            throw convertirError(e)
        }
    }

    override suspend fun restablecerPassword(correo: String, codigo: String, nuevaPassword: String) {
        try {
            api.restablecer(
                RestablecerRequest(correo = correo, codigo = codigo, nuevaPassword = nuevaPassword)
            )
        } catch (e: Exception) {
            // esRestablecer = true: aquí un 400 significa "código incorrecto o vencido"
            throw convertirError(e, esRestablecer = true)
        }
    }

    override suspend fun obtenerPerfil(id: Long): Usuario {
        try {
            // El usuario pide SU PROPIO perfil: el mismo id va en la ruta y en X-Usuario-Id
            return api.obtenerPorId(id = id, idSolicitante = id).aModelo()
        } catch (e: Exception) {
            throw convertirError(e)
        }
    }

    override suspend fun actualizarPerfil(
        id: Long,
        nombre: String,
        telefono: String,
        rubro: String,
        personasNegocio: String
    ): Usuario {
        try {
            val cuerpo = ActualizarPerfilRequest(
                nombre = nombre,
                telefono = soloNumeros(telefono),
                rubro = rubro,
                personasNegocio = personasNegocio
            )
            // Igual que obtenerPerfil: el usuario cambia SU PROPIO perfil (mismo id en ruta y header)
            return api.actualizarPerfil(id = id, idSolicitante = id, cuerpo = cuerpo).aModelo()
        } catch (e: Exception) {
            throw convertirError(e)
        }
    }

    override suspend fun listarUsuarios(idSolicitante: Long, rol: Rol?): List<Usuario> {
        try {
            // rol?.codigo: si rol es null queda null y Retrofit no agrega "?rol=" (trae todos)
            val respuesta = api.listar(idSolicitante = idSolicitante, rol = rol?.codigo)
            // map convierte cada UsuarioDto de la lista en un Usuario
            return respuesta.map { usuarioDto -> usuarioDto.aModelo() }
        } catch (e: Exception) {
            throw convertirError(e)
        }
    }

    override suspend fun cambiarPlanPro(idSolicitante: Long, idUsuario: Long, activo: Boolean): Usuario {
        try {
            val respuesta = api.cambiarPlanPro(
                id = idUsuario,
                idSolicitante = idSolicitante,
                cuerpo = PlanProRequest(activo = activo)
            )
            return respuesta.aModelo()
        } catch (e: Exception) {
            throw convertirError(e)
        }
    }

    override suspend fun cambiarRol(idSolicitante: Long, idUsuario: Long, rol: Rol): Usuario {
        try {
            val respuesta = api.cambiarRol(
                id = idUsuario,
                idSolicitante = idSolicitante,
                // El rol viaja como texto: Rol.Ventas -> "VENTAS"
                cuerpo = CambiarRolRequest(rol = rol.codigo)
            )
            return respuesta.aModelo()
        } catch (e: Exception) {
            throw convertirError(e)
        }
    }

    override suspend fun eliminarUsuario(idSolicitante: Long, idUsuario: Long) {
        try {
            val respuesta = api.eliminar(id = idUsuario, idSolicitante = idSolicitante)
            // eliminar devuelve Response<Unit> (el servidor responde 204 sin cuerpo), y con
            // Response Retrofit no lanza el error solo: si el código no es 2xx, se lanza aquí
            // la misma HttpException, para que convertirError la trate igual que en las demás.
            if (!respuesta.isSuccessful) {
                throw HttpException(respuesta)
            }
        } catch (e: Exception) {
            throw convertirError(e)
        }
    }
}
