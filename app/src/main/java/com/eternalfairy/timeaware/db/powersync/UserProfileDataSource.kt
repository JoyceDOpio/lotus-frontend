package com.eternalfairy.timeaware.db.powersync

import com.eternalfairy.timeaware.db.data.UserProfile
import com.powersync.PowerSyncDatabase
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class UserProfileDataSource @Inject constructor(
    private val powerSyncDatabase: PowerSyncDatabase
) {
    suspend fun deleteUserProfileById(id: UUID): Unit {
        try {
            powerSyncDatabase.writeTransaction { transaction ->
                transaction.execute(
                    sql = "DELETE FROM user_profiles WHERE id = ?",
                    parameters = listOf(id)
                )
            }
        } catch (e: Exception) {
        }
    }

    suspend fun insertUserProfile(userProfile: UserProfile): Unit {
        try {
            powerSyncDatabase.writeTransaction { transaction ->
                transaction.execute(
                    sql = "INSERT INTO user_profiles (created_at, first_name, last_name, email) VALUES (?, ?, ?, ?)",
                    parameters = listOf(
                        userProfile.createdAt,
                        userProfile.firstName,
                        userProfile.lastName,
                        userProfile.email
                    )
                )
            }
        } catch (e: Exception) {
        }
    }

    fun selectUserProfileById(id: UUID): Flow<UserProfile> {
        return flow {
            try {
                val activity = powerSyncDatabase.get(
                    sql = "SELECT * FROM user_profiles WHERE id = ?",
                    parameters = listOf(id)
                ) { cursor ->
                    UserProfile(
                        id = UUID.fromString(cursor.getString("id")),
                        createdAt = cursor.getString("created_at"),
                        firstName = cursor.getString("first_name"),
                        lastName = cursor.getString("last_name"),
                        email = cursor.getString("email")
                    )
                }
                emit(activity)
            } catch (e: Exception) {
//                emit(ApiResponse.Error(e.message))
            }
        }
    }

    suspend fun updateUserProfile(userProfile: UserProfile): Unit {
        try {
            powerSyncDatabase.execute(
                sql = "UPDATE user_profiles SET first_name = ?, last_name = ? WHERE id = ?",
                parameters = listOf(
                    userProfile.firstName,
                    userProfile.lastName,
                    userProfile.id
                )
            )
        } catch (e: Exception) {
        }
    }
}