package com.example.mastermartini.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 10.0.2.2 apunta al localhost de tu PC desde el emulador de Android Studio.
    // Si usas BlueStacks con adb reverse, cámbialo a "http://localhost:9092/"
    private const val BASE_URL_CURSOS = "http://10.0.2.2:9092/"

    // Microservicio de USUARIOS (Spring Boot, puerto 9091). 10.0.2.2 = el PC visto desde el emulador.
    private const val BASE_URL_USUARIOS = "http://10.0.2.2:9091/"

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

    // Endpoints de usuarios (login, registro, perfil, roles...). Lo usa UsuarioRepositoryRemoto.
    // "by lazy" = se crea la primera vez que se usa, y después se reutiliza siempre el mismo.
    val usuariosApi: UsuarioApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_USUARIOS)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuarioApi::class.java)
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