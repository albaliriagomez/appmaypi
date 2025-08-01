package com.torrezpillcokevin.nuna.data

import com.google.gson.annotations.SerializedName

//Nuevas data class segun las ultimas modificaciones del backend
data class UserRequest(
    @SerializedName("codigo_persona") val codigoPersona: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar_imagen") val avatarImagen: String = "",
    @SerializedName("status") val status: String,
    @SerializedName("role") val role: String,
    @SerializedName("password") val password: String,
    @SerializedName("numero") val numero: String,
    @SerializedName("token_firebase") val tokenFirebase: String,
    @SerializedName("linea_telefonica") val lineaTelefonica: String,
    @SerializedName("username") val username: String,
    @SerializedName("name") val name: String
)

data class UserResponse(
    @SerializedName("id") val id: Int,  // Nota que es Int, no String como en el ejemplo anterior
    @SerializedName("codigo_persona") val codigoPersona: String,
    @SerializedName("email")val email: String,
    @SerializedName("avatar_imagen")   val avatarImagen: String,  // No es nullable según tu ejemplo (viene como "")
    @SerializedName("status") val status: String,
    @SerializedName("role") val role: String,
    @SerializedName("password") // Normalmente no deberías recibir el password hasheado
    val passwordHash: String,   // pero si tu API lo devuelve, lo incluimos
    @SerializedName("numero") val numero: String,
    @SerializedName("token_firebase")val tokenFirebase: String,
    @SerializedName("linea_telefonica")val lineaTelefonica: String,
    @SerializedName("username")val username: String,
    @SerializedName("name")  val name: String
) {
    /**
     * Función de conveniencia para verificar si el usuario está activo
     * (asumiendo que "status" puede ser "active", "inactive", etc.)
     */
    fun isActive(): Boolean = status.equals("1", ignoreCase = true)

    /**
     * Función para obtener la URL completa del avatar si no está vacío
     */
    fun getAvatarUrl(baseUrl: String): String? {
        return if (avatarImagen.isNotEmpty()) {
            "$baseUrl/$avatarImagen"
        } else {
            null
        }
    }
}

//LOGIN
data class Login(
    val username: String,
    val password: String,
)

// Clase que representa la respuesta del login (con JWT)
data class AuthResponse(
    val access_token: String,
    val token_type: String,  // Ejemplo: "bearer"
    val user_id: Int,  // Agregar el user_id que devuelve la API
    val email: String  // Agregar el email que devuelve la API
)

//SOPORTE
data class SupportRequest(
    val user_id: Int,
    val name: String,
    val email: String,
    val subject: String,
    val message: String,
    val sent_at: String
)

data class UserData(
    @SerializedName("usuarios_id") val id: Int,
    val nombres: String,
    val apellidos: String,
    val usuario: String,
    val email: String,
    val estado: Int,
    val role: String,
    val image: String?
)

data class UserResponseGet(
    val message: String,
    val data: UserData
)

//GUIA
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

data class Faq(
    val id: Int,
    val question: String,
    val answer: String,
    val category_id: Int,
    val author_id: Int
)

data class FaqResponse(
    val total_registros: Int,
    val por_pagina: Int,
    val pagina_actual: Int,
    val total_paginas: Int,
    val data: List<Faq>
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


// Clase que representa un usuario
data class User(
    val name: String,
    val password: String,
    val email: String,
    val avatar: String,
    val status: String,
    val role: String,
    val numero:Int,
)

data class UserGet(
    val id: Int,         // Asegúrate de incluir este campo
    val name: String,
    val password: String,
    val email: String,
    val avatar: String,
    val status: String,
    val role: String,
    val numero: Long
)


data class ReporteDesaparecido(
    val nombre: String,
    val apellido: String,
    val genero: String,
    val descripcion: String,
    val fecha_nacimiento: String,
    val fecha_desaparicion: String,
    val lugar_desaparicion: String,
    val estado_investigacion: String,
    val foto_perfil: String,
    val nombre_reportante: String,
    val telefono_reportante: String,
    val foto_suceso: String,
    val coordenadas: String,
    val edad: Int,
    val caracteristicas: String,
    val id_usuario: Int
)
data class DesaparecidoResponse(
    val total_registros: Int,
    val por_pagina: Int,
    val pagina_actual: Int,
    val total_paginas: Int,
    val data: List<ReporteDesaparecido>
)





// Clase que representa la respuesta de la API
data class ApiResponse(
    val total_registros: Int,
    val por_pagina: Int,
    val pagina_actual: Int,
    val total_paginas: Int,
    val data: List<User> // Lista de usuarios en la respuesta
)


data class Report(
    val nombre: String,
    val email: String,
    val telefono: Int,
    val fecha_avistamiento: String,
    val ubicacion_avistamiento: String,
    val descripcion: String,
    val imagen: String
)

data class ContactoRequest(
    val nombre: String,
    val telefono: Long,
    val linea_telefonica: Int,
    val accion: String
)

data class ContactoResponse(
    val id: Int,
    val nombre: String,
    val telefono: Long,
    val linea_telefonica: Int,
    val accion: String
)

data class ContactResponse2(
    val total_registros: Int,
    val por_pagina: Int,
    val pagina_actual: Int,
    val total_paginas: Int,
    val data: List<Contact>
)

data class Contact(
    val nombre: String,
    val telefono: Int,
    val linea_telefonica: Int,
    val accion: String,
    val id: Int
)