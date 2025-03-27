package com.torrezpillcokevin.nuna.data


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/users/")
    suspend fun postUsers(
        @Body user: User
    ): Response<User>

    // Cambiar la respuesta de User a AuthResponse
    @POST("api/login/")
    suspend fun postLogin(
        @Body login: Login
    ): Response<AuthResponse> // Ahora responde con AuthResponse (que contiene el token)

    @GET("api/users/")
    suspend fun getUsers(
        @Query("pagina") page: Int,
        @Query("por_pagina") itemsPerPage: Int
    ): Response<ApiResponse>

}