package com.example.mastermartini.data.remote

import com.example.mastermartini.model.CloudinaryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

// Servicio Retrofit para subir videos mediante multipart a Cloudinary
interface CloudinaryApiService {

    @Multipart
    @POST("v1_1/{cloudName}/video/upload")
    suspend fun subirVideo(
        @Path("cloudName") cloudName: String,
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): CloudinaryResponse
}
