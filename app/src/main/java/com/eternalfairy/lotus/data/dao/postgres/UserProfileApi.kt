package com.eternalfairy.lotus.data.dao.postgres

import com.eternalfairy.lotus.data.auth.InjectAuth
import com.eternalfairy.lotus.data.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserProfileApi {
    @InjectAuth
    @GET("/user-profile")
    suspend fun getUserProfile(): UserProfile?

    @InjectAuth
    @PUT("/user-profile")
    suspend fun updateUserProfile(@Body userProfile: UserProfile)
}