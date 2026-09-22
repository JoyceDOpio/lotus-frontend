package com.eternalfairy.lotus.data.dao.postgres

import com.eternalfairy.lotus.data.dao.IUserProfileDAO
import com.eternalfairy.lotus.data.model.UserProfile

class UserProfileDAO(
    private val api: UserProfileApi
): IUserProfileDAO {
    override suspend fun getUserProfile(): UserProfile? {
        return api.getUserProfile()
    }

    override suspend fun updateUserProfile(userProfile: UserProfile) {
        return api.updateUserProfile(userProfile)
    }
}