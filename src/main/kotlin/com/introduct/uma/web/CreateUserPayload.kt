package com.introduct.uma.web

data class CreateUserPayload(

    val name: String,
    val email: String,
    val phone: String
)