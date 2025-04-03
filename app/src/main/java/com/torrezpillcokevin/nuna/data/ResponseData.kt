package com.torrezpillcokevin.nuna.data

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
data class Report(
    val nombre: String,
    val email: String,
    val telefono: Int,
    val fecha_avistamiento: String,
    val ubicacion_avistamiento: String,
    val descripcion: String,
    val imagen: String
)

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

// Clase que representa la respuesta de la API
data class ApiResponse(
    val total_registros: Int,
    val por_pagina: Int,
    val pagina_actual: Int,
    val total_paginas: Int,
    val data: List<User> // Lista de usuarios en la respuesta
)



