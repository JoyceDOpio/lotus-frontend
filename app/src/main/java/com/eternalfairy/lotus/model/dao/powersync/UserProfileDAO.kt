package com.eternalfairy.lotus.model.dao.powersync

import android.util.Log
import com.eternalfairy.lotus.model.data.UserProfile
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileDAO @Inject constructor(
    private val dataSource: OnlineSyncDataSource
) {
    suspend fun deleteUserProfileById(id: String) {
        try {
            dataSource.getDatabase().writeTransaction { transaction ->
                transaction.execute(
                    sql = "DELETE FROM user_profiles WHERE id = ?",
                    parameters = listOf(id)
                )
            }
        } catch (e: Exception) {
            Log.e("UserProfileDAO", e.printStackTrace().toString())
        }
    }

    suspend fun insertUserProfile(userProfile: UserProfile) {
        try {
            dataSource.getDatabase().writeTransaction { transaction ->
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
            Log.e("UserProfileDAO", e.printStackTrace().toString())
        }
    }

    fun selectUserProfileById(id: String): Flow<UserProfile?> {
        return flow {
            try {
                val activity = dataSource.getDatabase().getOptional(
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
                Log.e("UserProfileDAO", e.printStackTrace().toString())
//                emit(ApiResponse.Error(e.message))
            }
        }
    }

    suspend fun updateUserProfile(userProfile: UserProfile) {
        try {
            dataSource.getDatabase().execute(
                sql = "UPDATE user_profiles SET first_name = ?, last_name = ? WHERE id = ?",
                parameters = listOf(
                    userProfile.firstName,
                    userProfile.lastName,
                    userProfile.id
                )
            )
        } catch (e: Exception) {
            Log.e("UserProfileDAO", e.printStackTrace().toString())
        }
    }
}