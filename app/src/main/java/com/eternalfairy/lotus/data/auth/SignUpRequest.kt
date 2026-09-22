package com.eternalfairy.lotus.data.auth

data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String
)
