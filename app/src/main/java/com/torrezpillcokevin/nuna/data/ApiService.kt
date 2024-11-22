package com.torrezpillcokevin.nuna.data


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/users/")
    suspend fun postUsers(
        @Body user: User
    ): Response<User>

    @POST("api/login/")
    suspend fun postLogin(
        @Body login: login//lo que envia
    ): Response<User> //lo que recibe

    @GET("api/users/")
    suspend fun getUsers(
        @Query("pagina") page: Int,
        @Query("por_pagina") itemsPerPage: Int
    ): Response<ApiResponse>




}