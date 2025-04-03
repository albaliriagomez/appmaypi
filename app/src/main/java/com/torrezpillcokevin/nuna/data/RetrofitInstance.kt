package com.torrezpillcokevin.nuna.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.104.16:8000/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
