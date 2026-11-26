package com.example.mastermartini.data

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.mastermartini.data.remote.CloudinaryApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Repositorio encargado de la subida directa de videos a Cloudinary.
 * Encapsula la configuración de Retrofit/OkHttp y el manejo de archivos temporales
 * para que el ViewModel solo reciba un [Result] con la URL segura del video.
 */
class CloudinaryRepository {

    companion object {
        private const val BASE_URL_CLOUDINARY = "https://api.cloudinary.com/"
        private const val CLOUD_NAME = "ohimrm8m"
        private const val UPLOAD_PRESET = "mastermartini_videos"
    }

    // Cliente HTTP con timeouts amplios pensados para archivos de video pesados.
    // No se agrega logging de cuerpo (BODY) para evitar consumir memoria al subir binarios grandes.
    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    // Instancia Retrofit dedicada a la API de Cloudinary usando Gson.
    private val api: CloudinaryApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_CLOUDINARY)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudinaryApiService::class.java)
    }

    /**
     * Copia el video apuntado por [videoUri] a un archivo temporal en [Context.getCacheDir],
     * lo sube a Cloudinary en un hilo de entrada/salida ([Dispatchers.IO]) y elimina siempre
     * el archivo temporal en el bloque `finally`.
     */
    suspend fun subirVideoDesdeUri(context: Context, videoUri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            var archivoTemporal: File? = null
            try {
                // Detecta el tipo MIME real del video (por defecto video/mp4) y su extensión.
                val mimeType = context.contentResolver.getType(videoUri) ?: "video/mp4"
                val extension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(mimeType)
                    ?.takeIf { it.isNotBlank() }
                    ?: "mp4"

                // Abre el flujo de lectura desde la Uri seleccionada por el usuario.
                val inputStream = context.contentResolver.openInputStream(videoUri)
                    ?: return@withContext Result.failure(
                        IllegalStateException("No se pudo abrir el video seleccionado.")
                    )

                // Crea un archivo temporal en cacheDir y copia el contenido del video.
                archivoTemporal = File.createTempFile("video_curso_", ".$extension", context.cacheDir)
                inputStream.use { entrada ->
                    FileOutputStream(archivoTemporal).use { salida ->
                        entrada.copyTo(salida)
                    }
                }

                // Prepara las partes Multipart requeridas por Cloudinary: archivo y upload_preset.
                val requestBodyVideo = archivoTemporal.asRequestBody(mimeType.toMediaTypeOrNull())
                val parteVideo = MultipartBody.Part.createFormData(
                    name = "file",
                    filename = archivoTemporal.name,
                    body = requestBodyVideo
                )
                val partePreset = UPLOAD_PRESET.toRequestBody("text/plain".toMediaTypeOrNull())

                // Ejecuta la subida a Cloudinary y valida la URL segura devuelta.
                val respuesta = api.subirVideo(
                    cloudName = CLOUD_NAME,
                    file = parteVideo,
                    uploadPreset = partePreset
                )

                if (respuesta.secureUrl.isBlank()) {
                    Result.failure(
                        IllegalStateException("Cloudinary no devolvió un enlace válido para el video.")
                    )
                } else {
                    Result.success(respuesta.secureUrl)
                }
            } catch (e: HttpException) {
                Result.failure(
                    Exception("Error del servidor al subir el video (código ${e.code()}).", e)
                )
            } catch (e: IOException) {
                Result.failure(
                    Exception("Sin conexión o tiempo de espera agotado al subir el video.", e)
                )
            } catch (e: Exception) {
                Result.failure(
                    Exception("Ocurrió un error al procesar el video.", e)
                )
            } finally {
                // Garantiza que el archivo temporal siempre se borre para no llenar el caché.
                archivoTemporal?.let { archivo ->
                    if (archivo.exists()) {
                        archivo.delete()
                    }
                }
            }
        }
}
