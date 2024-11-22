package com.torrezpillcokevin.nuna.data

// Clase que representa un usuario "REGISTRO"
data class User(
        val name: String,
        val password: String,
        val email: String,
        val avatar: String,
        val status: String,
        val role: String,

)
//LOGIN
data class login(
    val password: String,
    val email: String,
    )

// Clase que representa la respuesta de la API
data class ApiResponse(
    val total_registros: Int,
    val por_pagina: Int,
    val pagina_actual: Int,
    val total_paginas: Int,
    val data: List<User> // Lista de usuarios en la respuesta
)



