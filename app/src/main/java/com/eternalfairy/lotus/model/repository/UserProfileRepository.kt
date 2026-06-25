package com.eternalfairy.lotus.model.repository

import com.eternalfairy.lotus.model.dao.IUserProfileDAO
import com.eternalfairy.lotus.model.dao.postgres.ApiResponse
import com.eternalfairy.lotus.model.dao.powersync.UserProfileDAO
import com.eternalfairy.lotus.model.data.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
    override suspend fun getUserProfile(id: Uuid): ApiResponse<UserProfile> {
        return try {
            val userProfile = dao.getUserProfile(id)

            ApiResponse.Success(userProfile)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }
}