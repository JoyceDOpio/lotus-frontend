package com.eternalfairy.lotus.model.repository

import com.eternalfairy.lotus.model.dao.postgres.ApiResponse
import com.eternalfairy.lotus.model.data.UserProfile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface IUserProfileRepository {
    suspend fun editUserProfile(userProfile: UserProfile): ApiResponse<Unit>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getUserProfile(id: Uuid): ApiResponse<UserProfile>
}