package com.eternalfairy.lotus.model.dao.powersync

import android.util.Log
import com.eternalfairy.lotus.model.data.UserProfile
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
                    sql = "INSERT INTO user_profiles (name) VALUES (?)",
                    parameters = listOf(
                        userProfile.name
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("UserProfileDAO", e.printStackTrace().toString())
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun selectUserProfileById(id: String): Flow<UserProfile?> {
        return flow {
            try {
                val activity = dataSource.getDatabase().getOptional(
                    sql = "SELECT * FROM user_profiles WHERE id = ?",
                    parameters = listOf(id)
                ) { cursor ->
                    UserProfile(
                        id = Uuid.parse(cursor.getString("id")),
                        name = cursor.getString("name")
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
                sql = "UPDATE user_profiles SET name = ? WHERE id = ?",
                parameters = listOf(
                    userProfile.name
                )
            )
        } catch (e: Exception) {
            Log.e("UserProfileDAO", e.printStackTrace().toString())
        }
    }
}