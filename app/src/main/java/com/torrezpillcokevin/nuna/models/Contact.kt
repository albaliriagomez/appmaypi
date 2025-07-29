package com.torrezpillcokevin.nuna.models

import java.io.Serializable

data class Contact(
    val id: Int = 0,
    val name: String,
    val phone: String,
    val line: String
) : Serializable
