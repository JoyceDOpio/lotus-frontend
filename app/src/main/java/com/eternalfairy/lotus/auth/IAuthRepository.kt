package com.eternalfairy.lotus.auth

interface IAuthRepository {
    suspend fun authenticate(): AuthResponse<Unit>
    suspend fun logIn(email: String, password: String): AuthResponse<Unit>
    suspend fun signUp(name: String, email: String, password: String): AuthResponse<Unit>
}