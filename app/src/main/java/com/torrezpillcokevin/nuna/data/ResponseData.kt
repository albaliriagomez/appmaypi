package com.torrezpillcokevin.nuna.data

import com.google.gson.annotations.SerializedName


// Modelos para Login
data class Login(val username: String, val password: String)

data class AuthResponse(
    val token: String,
    val token_type: String,
    val user: UserData
)

data class UserData(
    val id: Int,
    val name: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("second_surname") val secondSurname: String,
    val email: String,
    val phone: Long,
    val status: String?,
    val avatar: String?
)

data class UserResponseGet(val message: String, val data: UserData)

// ✅ Respuesta exacta del GET /api/v1/public/missing
// igual que la web Angular: total, page, size, data, links
data class MissingPaginadoResponse(
    val message: String,
    val data: List<ReporteDesaparecido>,
    val total: Int,
    val page: Int,
    val size: Int,
    val links: MissingLinks
)

data class MissingLinks(
    val next: String?,
    val previous: String?,
    val first: String?,
    val last: String?
)

// ✅ Respuesta del POST /api/v1/public/missing
data class MissingPublicoResponse(val message: String, val data: MissingPublicoData)

data class MissingPublicoData(
    val id: Int,
    val name: String,
    @SerializedName("last_name") val lastName: String,
    val age: Int,
    val gender: String
)

// ✅ Respuesta del POST /api/v1/public/register-user
data class UserPublicoResponse(
    val message: String,
    val data: UserData
)

data class MissingResponse(val message: String, val data: MissingData)
data class MissingData(val id: Int, val name: String, val last_name: String)

// ✅ Campos EXACTOS que retorna el backend en listMissing():
// id, name, last_name, gender, age, description, characteristics,
// place_of_disappearance, photo (como base64 "data:image/jpeg;base64,...")
data class ReporteDesaparecido(
    val id: Int,
    @SerializedName("name")                   val nombre: String,
    @SerializedName("last_name")              val apellido: String,
    @SerializedName("age")                    val edad: Int,
    @SerializedName("gender")                 val genero: String,
    @SerializedName("description")            val descripcion: String,
    @SerializedName("characteristics")        val caracteristicas: String,
    @SerializedName("place_of_disappearance") val lugar_desaparicion: String,
    @SerializedName("photo")                  val foto_perfil: String,
    // Opcionales — el endpoint público no los devuelve siempre
    @SerializedName("disappearance_date")     val fecha_desaparicion: String? = null,
    @SerializedName("birthdate")              val fecha_nacimiento: String? = null,
    @SerializedName("investigation_status")   val estado_investigacion: String? = null
)

data class ReportResponse(val message: String, val data: ReportResponseData)
data class ReportResponseData(val id: Int, val name: String, val email: String, val phone: String)

data class ContactoSupportRequest(
    val name: String, val email: String, val title: String,
    val message: String, val phone: String? = null, val user_id: Int?
)
data class ContactoSupportResponse(val message: String, val data: ContactoSupportData)
data class ContactoSupportData(val id: Int, val name: String)

data class Guide(
    val category_id: Int, val author_id: Int, val slug: String,
    val title: String, val subtitle: String, val content: String, val id: Int
)
data class GuideResponse(
    val total_registros: Int, val por_pagina: Int,
    val pagina_actual: Int, val total_paginas: Int,
    val data: List<Guide>
)

data class CategoryResponse(val id: Int, val title: String)
data class Faq(val id: Int, val question: String, val answer: String, val category: CategoryResponse)
data class FaqListResponse(
    val message: String, val data: List<Faq>,
    val total: Int, val page: Int, val size: Int
)
data class FaqItemManual(val question: String, val answer: String)
data class FaqCategoryManual(val title: String, val faqs: List<FaqItemManual>)

data class SupportRequest(
    val name: String, val email: String, val title: String,
    val message: String, val phone: String?, val user_id: Int?
)
data class SupportResponse(val message: String, val data: SupportDetail)
data class SupportDetail(
    val id: Int, val name: String, val email: String,
    val title: String, val message: String, val phone: String?
)

data class GuideCategory(val id: Int, val title: String, val slug: String)
data class GuideCategoryResponse(
    @SerializedName("total_registros") val totalRegistros: Int,
    @SerializedName("por_pagina")      val porPagina: Int,
    @SerializedName("pagina_actual")   val paginaActual: Int,
    @SerializedName("total_paginas")   val totalPaginas: Int,
    val data: List<GuideCategory>
)