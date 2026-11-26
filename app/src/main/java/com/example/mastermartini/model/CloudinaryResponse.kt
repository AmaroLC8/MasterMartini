package com.example.mastermartini.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de respuesta de la API de Cloudinary al subir un video.
 * Mapea el campo "secure_url" del JSON, que entrega el enlace HTTPS público del archivo.
 */
data class CloudinaryResponse(
    @SerializedName("secure_url")
    val secureUrl: String
)
