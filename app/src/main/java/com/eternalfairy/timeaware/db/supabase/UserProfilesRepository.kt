package com.eternalfairy.timeaware.db.supabase

import com.eternalfairy.timeaware.db.data.UserProfile
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class UserProfilesRepository @Inject constructor(
    private val userProfileDataSource: UserProfileDataSource
) {
    fun deleteUserProfile(id: UUID): Flow<ApiResponse<Unit>> {
        return userProfileDataSource.deleteUserProfileById(id)
    }

    fun insertUserProfile(userProfile: UserProfile): Flow<ApiResponse<Unit>> {
        return userProfileDataSource.insertUserProfile(userProfile)
    }

    fun getUserProfileStream(id: UUID): Flow<ApiResponse<UserProfile>> {
        return userProfileDataSource.selectUserProfileById(id)
    }

    fun updateUserProfile(userProfile: UserProfile): Flow<ApiResponse<Unit>> {
        return userProfileDataSource.updateUserProfile(userProfile)
    }

}