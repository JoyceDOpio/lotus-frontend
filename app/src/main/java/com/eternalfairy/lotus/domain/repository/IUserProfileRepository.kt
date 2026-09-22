package com.eternalfairy.lotus.domain.repository

import com.eternalfairy.lotus.data.dao.postgres.ApiResponse
import com.eternalfairy.lotus.data.model.UserProfile

interface IUserProfileRepository {
    suspend fun editUserProfile(userProfile: UserProfile): ApiResponse<Unit>

    suspend fun getUserProfile(): ApiResponse<UserProfile>
}