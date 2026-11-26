package com.example.mastermartini.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Cliente Retrofit singleton que centraliza las instancias API de los microservicios
object RetrofitClient {

    // 10.0.2.2 apunta al localhost del equipo anfitrión desde el emulador Android
    private const val BASE_URL_CURSOS = "http://10.0.2.2:9092/"
    private const val BASE_URL_USUARIOS = "http://10.0.2.2:9091/"
    private const val BASE_URL_INTERACCIONES = "http://10.0.2.2:9093/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val cursosApi: CursosApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_CURSOS)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CursosApiService::class.java)
    }

    val usuariosApi: UsuarioApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_USUARIOS)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuarioApi::class.java)
    }

    val interaccionesApi: InteraccionesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_INTERACCIONES)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InteraccionesApi::class.java)
    }

    val climaApi: ClimaApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApiService::class.java)
    }
}