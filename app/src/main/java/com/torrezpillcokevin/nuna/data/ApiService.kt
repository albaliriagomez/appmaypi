package com.torrezpillcokevin.nuna.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody

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

    // ✅ REGISTRO PÚBLICO — POST /api/v1/public/register-user
    @Multipart
    @POST("api/v1/public/register-user")
    suspend fun registrarUsuarioPublico(
        @Part("name") name: RequestBody,
        @Part("last_name") last_name: RequestBody,
        @Part("second_surname") second_surname: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part avatar: MultipartBody.Part
    ): Response<UserPublicoResponse>

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
        @Part("user_status") user_status: RequestBody,
        @Part("token_firebase") token_firebase: RequestBody?,
        @Part avatar: MultipartBody.Part
    ): Response<ResponseBody>

    // ✅ CREAR DESAPARECIDO PÚBLICO — POST /api/v1/public/missing
    @Multipart
    @POST("api/v1/public/missing")
    suspend fun crearMissingPublico(
        @Part("name") name: RequestBody,
        @Part("last_name") last_name: RequestBody,
        @Part("age") age: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part("description") description: RequestBody,
        @Part("birthdate") birthdate: RequestBody,
        @Part("disappearance_date") disappearance_date: RequestBody,
        @Part("place_of_disappearance") place_of_disappearance: RequestBody,
        @Part("characteristics") characteristics: RequestBody,
        @Part("reporter_name") reporter_name: RequestBody,
        @Part("reporter_phone") reporter_phone: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part event_photo: MultipartBody.Part
    ): Response<MissingPublicoResponse>

    @Multipart
    @POST("api/v1/reports")
    suspend fun crearReporte(
        @Header("Authorization") authorization: String,
        @Part("missing_id") missing_id: RequestBody,
        @Part("user_id") user_id: RequestBody,
        @Part("name") name: RequestBody,
        @Part("email") email: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("location") location: RequestBody,
        @Part("description") description: RequestBody,
        @Part("date") date: RequestBody,
        @Part report_file: MultipartBody.Part
    ): Response<ReportResponse>

    // ✅ MURO — GET /api/v1/public/missing
    @GET("api/v1/public/missing")
    suspend fun obtenerDesaparecidos(
        @Query("page") pagina: Int,
        @Query("size") porPagina: Int
    ): Response<MissingPaginadoResponse>

    // ✅ SOPORTE PÚBLICO — POST /api/v1/public/contact-support
    @POST("api/v1/public/contact-support")
    suspend fun enviarContactoPublico(
        @Body request: ContactoSupportRequest
    ): Response<ContactoSupportResponse>

    @POST("api/v1/contacts-support")
    suspend fun postContactoEmergencia(
        @Header("Authorization") token: String,
        @Body request: ContactoSupportRequest
    ): Response<ContactoSupportResponse>

    @POST("api/v1/contacts-support")
    suspend fun createSupportRequest(
        @Body request: SupportRequest,
        @Header("Authorization") token: String
    ): Response<SupportResponse>

    @GET("api/v1/faqs")
    suspend fun getFaqs(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Header("Authorization") authHeader: String
    ): Response<FaqListResponse>

    @GET("api/guides-categories/")
    suspend fun getGuideCategories(
        @Query("pagina") pagina: Int,
        @Query("por_pagina") porPagina: Int,
        @Header("Authorization") token: String
    ): Response<GuideCategoryResponse>

    @GET("api/guides/")
    suspend fun getGuides(
        @Query("pagina") pagina: Int,
        @Query("por_pagina") porPagina: Int,
        @Header("Authorization") authToken: String
    ): Response<GuideResponse>
}