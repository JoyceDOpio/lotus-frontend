package com.eternalfairy.lotus.model.dao.postgres

import com.eternalfairy.lotus.model.dao.IUserProfileDAO
import com.eternalfairy.lotus.model.data.UserProfile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UserProfileDAO(
    private val api: UserProfileApi
): IUserProfileDAO {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getUserProfile(id: Uuid): UserProfile? {
        return api.getUserProfile(id)
    }

    override suspend fun updateUserProfile(userProfile: UserProfile) {
        return api.updateUserProfile(userProfile)
    }
}