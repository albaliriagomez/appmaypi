package com.torrezpillcokevin.nuna.data


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/users/")
    suspend fun postUsers(
        @Body user: User
    ): Response<User>

    @FormUrlEncoded
    @POST("api/login/")
    suspend fun postLogin(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<AuthResponse>

    @GET("api/users/")
    suspend fun getUsers(
        @Query("pagina") page: Int,
        @Query("por_pagina") itemsPerPage: Int
    ): Response<ApiResponse>

    @GET("api/users/{id}")
    suspend fun getUserById(
        @Path("id") userId: Int
    ): Response<User>

    @POST("api/reportes/")
    suspend fun postReportes(
        @Body report: Report
    ): Response<Report>



}