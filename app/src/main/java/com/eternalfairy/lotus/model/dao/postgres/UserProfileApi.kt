package com.eternalfairy.lotus.model.dao.postgres

import com.eternalfairy.lotus.auth.InjectAuth
import com.eternalfairy.lotus.model.data.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface UserProfileApi {
    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @GET("/user-profile/{id}")
    suspend fun getUserProfile(@Path("id") id: Uuid): UserProfile?

    @InjectAuth
    @PUT("/user-profile")
    suspend fun updateUserProfile(@Body userProfile: UserProfile)
}