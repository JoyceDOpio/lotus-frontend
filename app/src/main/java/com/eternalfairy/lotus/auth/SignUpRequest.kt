package com.eternalfairy.lotus.auth

data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String
)
