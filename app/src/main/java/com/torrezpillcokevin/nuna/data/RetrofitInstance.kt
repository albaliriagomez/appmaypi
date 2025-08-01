package com.torrezpillcokevin.nuna.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient

import java.util.concurrent.TimeUnit
import java.net.URL

object RetrofitInstance {
    // Configuración para desarrollo
    private const val DEVELOPMENT_BASE_URL = "http://10.0.2.2:8000/"
    private const val TIMEOUT_SECONDS = 30L

    // Client con timeouts y logging para desarrollo
    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        builder.build()
    }

    // Instancia de Retrofit
    private val retrofit: Retrofit by lazy {
        // Validar URL base
        validateBaseUrl(DEVELOPMENT_BASE_URL)

        Retrofit.Builder()
            .baseUrl(DEVELOPMENT_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    //API Service
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    /**
     * Valida que la URL base sea correcta
     * @throws IllegalArgumentException si la URL no es válida
     */
    private fun validateBaseUrl(baseUrl: String) {
        try {
            URL(baseUrl).toURI()
        } catch (e: Exception) {
            throw IllegalArgumentException("La URL base proporcionada no es válida: $baseUrl", e)
        }
    }

    /**
     * Método para cambiar la URL base en tiempo de ejecución (útil para testing)
     */
    @Throws(IllegalArgumentException::class)
    fun changeBaseUrl(newBaseUrl: String) {
        validateBaseUrl(newBaseUrl)
        // Nota: En producción necesitarías recrear Retrofit y ApiService
        throw UnsupportedOperationException("Cambio de URL base no implementado. En producción recrea RetrofitInstance.")
    }
}