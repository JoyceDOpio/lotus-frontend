package com.eternalfairy.lotus.data.repository

import com.eternalfairy.lotus.data.dao.IUserProfileDAO
import com.eternalfairy.lotus.data.dao.postgres.ApiResponse
import com.eternalfairy.lotus.data.model.UserProfile
import com.eternalfairy.lotus.domain.repository.IUserProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi

@Singleton
class UserProfileRepository @Inject constructor(
    private val dao: IUserProfileDAO
): IUserProfileRepository {
    override suspend fun editUserProfile(userProfile: UserProfile): ApiResponse<Unit> {
        return try {
            dao.updateUserProfile(userProfile)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getUserProfile(): ApiResponse<UserProfile> {
        return try {
            val userProfile = dao.getUserProfile()

            ApiResponse.Success(userProfile)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }
}