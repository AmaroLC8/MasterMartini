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

// Implementación remota de UsuarioRepository conectada vía Retrofit al microservicio de usuarios
class UsuarioRepositoryRemoto : UsuarioRepository {

    private val api = RetrofitClient.usuariosApi

    private fun soloNumeros(telefono: String): String {
        return telefono.filter { letra -> letra.isDigit() }
    }

    // Mapea excepciones de red y HTTP de Retrofit a la jerarquía UsuarioException
    private fun convertirError(e: Exception, esRestablecer: Boolean = false): UsuarioException {
        return when (e) {
            is CancellationException -> throw e
            is IOException -> UsuarioException.SinConexion()
            is HttpException -> convertirErrorHttp(e, esRestablecer)
            else -> UsuarioException.ErrorServidor(null)
        }
    }

    // Traduce el código HTTP y el ErrorDto del backend a la excepción correspondiente
    private fun convertirErrorHttp(e: HttpException, esRestablecer: Boolean): UsuarioException {
        val error = leerErrorDto(e)
        val mensajeServidor = error?.mensaje
        val mensajeParaMostrar = error?.campos?.values?.firstOrNull() ?: mensajeServidor

        return when (e.code()) {
            401 -> UsuarioException.CredencialesInvalidas()
            403 -> UsuarioException.AccesoDenegado()
            404 -> UsuarioException.NoEncontrado()
            409 -> {
                val esRut = mensajeServidor?.contains("rut", ignoreCase = true) == true
                UsuarioException.DatoDuplicado(if (esRut) "RUT" else "correo")
            }
            400 -> if (esRestablecer) {
                UsuarioException.CodigoInvalido()
            } else {
                UsuarioException.ErrorServidor(mensajeParaMostrar)
            }
            else -> UsuarioException.ErrorServidor(mensajeParaMostrar)
        }
    }

    private fun leerErrorDto(e: HttpException): ErrorDto? {
        return try {
            val cuerpo = e.response()?.errorBody()?.string()
            Gson().fromJson(cuerpo, ErrorDto::class.java)
        } catch (ex: Exception) {
            null
        }
    }

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
            throw convertirError(e, esRestablecer = true)
        }
    }

    override suspend fun obtenerPerfil(id: Long): Usuario {
        try {
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
            return api.actualizarPerfil(id = id, idSolicitante = id, cuerpo = cuerpo).aModelo()
        } catch (e: Exception) {
            throw convertirError(e)
        }
    }

    override suspend fun listarUsuarios(idSolicitante: Long, rol: Rol?): List<Usuario> {
        try {
            val respuesta = api.listar(idSolicitante = idSolicitante, rol = rol?.codigo)
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
            if (!respuesta.isSuccessful) {
                throw HttpException(respuesta)
            }
        } catch (e: Exception) {
            throw convertirError(e)
        }
    }
}
