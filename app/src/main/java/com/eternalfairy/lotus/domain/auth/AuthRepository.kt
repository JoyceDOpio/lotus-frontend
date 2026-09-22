package com.eternalfairy.lotus.domain.auth

import com.eternalfairy.lotus.data.auth.AuthResponse

interface AuthRepository {
    suspend fun authenticate(): AuthResponse<Unit>
    suspend fun logIn(email: String, password: String): AuthResponse<Unit>
    suspend fun signUp(name: String, email: String, password: String): AuthResponse<Unit>
}