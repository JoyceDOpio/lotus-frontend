package com.eternalfairy.lotus.auth

import android.content.SharedPreferences
import retrofit2.HttpException

class AuthRepository(
    private val api: AuthApi,
    private val preferences: SharedPreferences// TODO: Write an abstraction for SharedPreferences
): IAuthRepository {
    override suspend fun authenticate(): AuthResponse<Unit> {
        return try {
            val token = preferences.getString("jwt", null) ?: return AuthResponse.Authorized()
            api.authenticate("Bearer $token")

            AuthResponse.Authorized()
        } catch (e: HttpException) {
            if (e.code() == 401) {
                AuthResponse.Unauthorized()
            } else {
                AuthResponse.UnknownError()
            }
        } catch (e: Exception) {
            AuthResponse.UnknownError()
        }
    }

    override suspend fun logIn(
        email: String,
        password: String
    ): AuthResponse<Unit> {
        return try {
            val response = api.logIn(
                LogInRequest(
                    email = email,
                    password = password
                )
            )

            preferences
                .edit()
                .putString("jwt", response.token)
                .apply()

            AuthResponse.Authorized()

        } catch (e: HttpException) {
            if (e.code() == 401) {
                AuthResponse.Unauthorized()
            } else {
                AuthResponse.UnknownError()
            }
        } catch (e: Exception) {
            AuthResponse.UnknownError()
        }
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String
    ): AuthResponse<Unit> {
       return try {
           api.signUp(
               SignUpRequest(
                   name = name,
                   email = email,
                   password = password
               )
           )

           logIn(email = email, password = password)
       } catch (e: HttpException) {
            if (e.code() == 401) {
                AuthResponse.Unauthorized()
            } else {
                AuthResponse.UnknownError()
            }
       } catch (e: Exception) {
           AuthResponse.UnknownError()
       }
    }
}