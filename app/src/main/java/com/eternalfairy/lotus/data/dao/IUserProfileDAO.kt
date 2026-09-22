package com.eternalfairy.lotus.data.dao

import com.eternalfairy.lotus.data.model.UserProfile

interface IUserProfileDAO {
    suspend fun getUserProfile(): UserProfile?

    suspend fun updateUserProfile(userProfile: UserProfile)
}