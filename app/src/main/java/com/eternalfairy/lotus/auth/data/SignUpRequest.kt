package com.eternalfairy.lotus.auth.data

data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String
)