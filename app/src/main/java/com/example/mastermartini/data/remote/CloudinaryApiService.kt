package com.example.mastermartini.data.remote

import com.example.mastermartini.model.CloudinaryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Servicio Retrofit que define el endpoint de Cloudinary para subir videos directamente
 * mediante una petición HTTP Multipart.
 */
interface CloudinaryApiService {

    /**
     * Envía un archivo de video y su preset de subida sin firmar a la cuenta indicada en [cloudName].
     * Devuelve un [CloudinaryResponse] con la URL segura del video alojado en la nube.
     */
    @Multipart
    @POST("v1_1/{cloudName}/video/upload")
    suspend fun subirVideo(
        @Path("cloudName") cloudName: String,
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): CloudinaryResponse
}
