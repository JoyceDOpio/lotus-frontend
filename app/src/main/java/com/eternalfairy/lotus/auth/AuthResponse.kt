package com.eternalfairy.lotus.auth

sealed class AuthResponse<T> (val data: T? = null) {
    class Authorized<T>(data: T? = null): AuthResponse<T>(data)
    class Unauthorized<T>: AuthResponse<T>()
    class UnknownError<T>: AuthResponse<T>()
}