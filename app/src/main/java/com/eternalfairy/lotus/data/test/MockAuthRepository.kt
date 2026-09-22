package com.eternalfairy.lotus.data.test

import android.content.SharedPreferences
import com.eternalfairy.lotus.data.auth.AuthResponse
import com.eternalfairy.lotus.domain.auth.AuthRepository
import com.eternalfairy.lotus.data.auth.TokenResponse
import retrofit2.HttpException

class MockAuthRepository(
    private val preferences: SharedPreferences
): AuthRepository {


    lateinit var nameValue: String
    lateinit var emailValue: String
    lateinit var passwordValue: String



    override suspend fun authenticate(): AuthResponse<Unit> {
        return try {
            val token = preferences.getString("jwt", null) ?: return AuthResponse.Authorized()
//            api.authenticate("Bearer $token")

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
//            if (email != "example@gmail.com"
//                || password != "password") {
//                throw HttpException(Response.error<ResponseBody>(401))
//            }

            val response = TokenResponse(
                token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJ1c2VycyIsImlzcyI6Imh0dHBzOi8vMC4wLjAuMDo4MDgwIiwiZXhwIjoxNzg3NjU4ODI5LCJ1c2VySWQiOiIzNWUzYzZmNS00ZDBhLTQ1Y2UtYThlMS0zZGQ0ODVhMzRjZWYifQ.I1QUwqwaUYWSrngCP9DrqHu0CL2guikaT9NtwrBESTU"
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
//            api.signUp(
//                SignUpRequest(
//                    name = name,
//                    email = email,
//                    password = password
//                )
//            )
            nameValue = name
            emailValue = email
            passwordValue = password

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