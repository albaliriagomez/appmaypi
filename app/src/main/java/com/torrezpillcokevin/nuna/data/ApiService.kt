package com.torrezpillcokevin.nuna.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun postLogin(
        @Field("username") phone: String,
        @Field("password") pass: String
    ): Response<AuthResponse>

    @GET("api/v1/users/{id}")
    suspend fun getUserById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<UserResponseGet>

    @Multipart
    @POST("api/v1/users")
    suspend fun createUser(
        @Part("code") code: RequestBody,
        @Part("name") name: RequestBody,
        @Part("last_name") lastName: RequestBody,
        @Part("second_surname") secondSurname: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("user_status") status: RequestBody,
        @Part("token_firebase") tokenFirebase: RequestBody?,
        @Part avatar: MultipartBody.Part
    ): Response<UserResponseGet>

    @Multipart
    @POST("api/v1/public/missing")
    suspend fun registrarDesaparecidoPublico(
        @Part("name") name: RequestBody,
        @Part("last_name") lastName: RequestBody,
        @Part("age") age: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("description") description: RequestBody,
        @Part("birthdate") birthdate: RequestBody,
        @Part("disappearance_date") disappearance_date: RequestBody,
        @Part("place_of_disappearance") place_of_disappearance: RequestBody,
        @Part("characteristics") characteristics: RequestBody,
        @Part("reporter_name") reporter_name: RequestBody,
        @Part("reporter_phone") reporter_phone: RequestBody,
        @Part("id_usuario") id_usuario: RequestBody, // <-- Este es el que faltaba
        @Part photo: MultipartBody.Part,
        @Part event_photo: MultipartBody.Part
    ): Response<MissingResponse>

    // AGREGAR ESTA FUNCIÓN AL FINAL:
    @GET("api/v1/public/missing")
    suspend fun obtenerDesaparecidos(
        @Query("pagina") pagina: Int,
        @Query("por_pagina") porPagina: Int,
        @Header("Authorization") token: String
    ): Response<MissingPaginadoResponse>


    @GET("api/v1/faqs")
    suspend fun getFaqs(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("Authorization") authHeader: String
    ): Response<FaqListResponse>




    @POST("api/v1/contacts-support")
    suspend fun postContactoEmergencia(
        @Header("Authorization") token: String, // Bearer Token
        @Body request: ContactoSupportRequest
    ): Response<ContactoSupportResponse>

    @POST("api/v1/contacts-support")
    suspend fun createSupportRequest(
        @Body request: SupportRequest,
        @Header("Authorization") token: String
    ): Response<SupportResponse>


    // CORREGIDO: "guides" en minúsculas para evitar el 404

    @GET("api/guides-categories/")

    suspend fun getGuideCategories(

        @Query("pagina") pagina: Int,

        @Query("por_pagina") porPagina: Int,

        @Header("Authorization") token: String

    ): Response<GuideCategoryResponse>

    // CORREGIDO: "guides" en minúsculas (tenías "Guides")

    @GET("api/guides/")

    suspend fun getGuides(

        @Query("pagina") pagina: Int,

        @Query("por_pagina") porPagina: Int,

        @Header("Authorization") authToken: String

    ): Response<GuideResponse>


}