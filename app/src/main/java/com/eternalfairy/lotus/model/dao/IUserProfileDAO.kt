package com.eternalfairy.lotus.model.dao

import com.eternalfairy.lotus.model.data.UserProfile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface IUserProfileDAO {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun getUserProfile(id: Uuid): UserProfile?

    suspend fun updateUserProfile(userProfile: UserProfile): Unit
}