package com.torrezpillcokevin.nuna.data


import okhttp3.Call
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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


    @GET("api/users/{user_id}")
    suspend fun getUser(
        @Path("user_id") userId: Int
    ): Response<UserGet>


    @POST("api/reportes/")
    suspend fun createReport(
        @Header("Authorization") token: String,
        @Body report: Report
    ): Response<Report>

    @POST("api/contactoss/")
    suspend fun createContacto(
        @Header("Authorization") token: String,
        @Body contacto: ContactoRequest
    ): Response<ContactoResponse>

    @GET("api/contactoss/")
    suspend fun getContactos(
        @Header("Authorization") token: String,
        @Query("pagina") pagina: Int,
        @Query("por_pagina") porPagina: Int
    ): Response<ContactResponse2>






}