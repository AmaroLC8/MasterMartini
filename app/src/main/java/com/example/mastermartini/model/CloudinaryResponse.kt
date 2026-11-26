package com.example.mastermartini.model

import com.google.gson.annotations.SerializedName

// Respuesta de Cloudinary con la URL HTTPS del video subido
data class CloudinaryResponse(
    @SerializedName("secure_url")
    val secureUrl: String
)
