package com.eternalfairy.timeaware.db.powersync

import com.eternalfairy.timeaware.db.data.UserProfile
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class UserProfilesRepository @Inject constructor(
    private val userProfileDataSource: UserProfileDataSource
) {
    suspend fun deleteUserProfile(id: UUID): Unit {
        return userProfileDataSource.deleteUserProfileById(id)
    }

    suspend fun insertUserProfile(userProfile: UserProfile): Unit {
        return userProfileDataSource.insertUserProfile(userProfile)
    }

    fun getUserProfileStream(id: UUID): Flow<UserProfile> {
        return userProfileDataSource.selectUserProfileById(id)
    }

    suspend fun updateUserProfile(userProfile: UserProfile): Unit {
        return userProfileDataSource.updateUserProfile(userProfile)
    }

}