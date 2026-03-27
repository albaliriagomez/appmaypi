package com.torrezpillcokevin.nuna.data

import com.google.gson.annotations.SerializedName

// Modelos para Login
data class Login(
    val username: String,
    val password: String
)

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

data class UserResponseGet(
    val message: String,
    val data: UserData
)

data class MissingPaginadoResponse(
    val total_registros: Int,
    val por_pagina: Int,
    val pagina_actual: Int,
    val total_paginas: Int,
    val data: List<ReporteDesaparecido> // Usa el modelo con los campos corregidos
)

data class MissingResponse(
    val message: String,
    val data: MissingData
)

data class MissingData(
    val id: Int,
    val name: String,
    val last_name: String
)

data class ReporteDesaparecido(
    val id: Int,
    val nombre: String,
    val apellido: String,
    val edad: Int,
    val genero: String,
    val descripcion: String,
    val fecha_nacimiento: String,
    val fecha_desaparicion: String,
    val lugar_desaparicion: String,
    val caracteristicas: String,
    val foto_perfil: String, // URL de la imagen
    val estado_investigacion: String
)


data class ContactoSupportRequest(
    val name: String,         // Nombre del contacto
    val email: String,        // Tu email de usuario (Requerido por backend)
    val title: String,        // Aquí guardaremos la "Línea" (Entel, Viva, etc.)
    val message: String,      // Aquí guardaremos el "Teléfono" y/o "Coordenadas"
    val phone: String? = null, // Campo opcional
    val user_id: Int?         // Tu ID de usuario
)

data class ContactoSupportResponse(
    val message: String,
    val data: ContactoSupportData
)

data class ContactoSupportData(
    val id: Int,
    val name: String
)


//GUIA  ANTIGUO POR EL MOEMNTO SOLO GUIA ESTA ANTIGUO

data class Guide(

    val category_id: Int,

    val author_id: Int,

    val slug: String,

    val title: String,

    val subtitle: String,

    val content: String,

    val id: Int

)



data class GuideResponse(

    val total_registros: Int,

    val por_pagina: Int,

    val pagina_actual: Int,

    val total_paginas: Int,

    val data: List<Guide>

)

data class CategoryResponse(
    val id: Int,
    val title: String
)

data class Faq(
    val id: Int,
    val question: String,
    val answer: String,
    val category: CategoryResponse // Tu backend devuelve un objeto category, no solo un ID
)

data class FaqListResponse(
    val message: String,
    val data: List<Faq>, // Esta es la lista que procesa el ViewModel
    val total: Int,
    val page: Int,
    val size: Int
)


data class SupportRequest(
    val name: String,
    val email: String,
    val title: String,  // El backend usa 'title', no 'subject'
    val message: String,
    val phone: String?,
    val user_id: Int?
)

data class SupportResponse(
    val message: String,
    val data: SupportDetail
)

data class SupportDetail(
    val id: Int,
    val name: String,
    val email: String,
    val title: String,
    val message: String,
    val phone: String?
)


//nuevo guia



data class GuideCategory(

    val id: Int,

    val title: String,

    val slug: String

)



data class GuideCategoryResponse(

    @SerializedName("total_registros")

    val totalRegistros: Int,



    @SerializedName("por_pagina")

    val porPagina: Int,



    @SerializedName("pagina_actual")

    val paginaActual: Int,



    @SerializedName("total_paginas")

    val totalPaginas: Int,



    val data: List<GuideCategory>

)