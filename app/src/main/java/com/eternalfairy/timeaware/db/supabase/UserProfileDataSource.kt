package com.eternalfairy.timeaware.db.supabase

import com.eternalfairy.timeaware.db.data.Activity
import com.eternalfairy.timeaware.db.data.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class UserProfileDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    fun deleteUserProfileById(id: UUID): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.user_profiles").delete {
                    filter {
                        eq("id", id)
                    }
                }
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun insertUserProfile(userProfile: UserProfile): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.user_profiles").insert(
                    userProfile
                )
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectUserProfileById(id: UUID): Flow<ApiResponse<UserProfile>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val userProfile = supabaseClient.from("public.user_profiles").select() {
                    filter {
                        UserProfile::id eq id
                    }
                }.decodeSingle<UserProfile>()
                emit(ApiResponse.Success(userProfile))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun updateUserProfile(userProfile: UserProfile): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.activities").update(
                    {
                        UserProfile::firstName setTo userProfile.firstName
                        UserProfile::lastName setTo userProfile.lastName
//                        UserProfile::email setTo userProfile.email // TODO: I'm not sure this should be updated just like that (OAuth uses email for authentication)
                    }
                ) {
                    filter {
                        Activity::id eq userProfile.id
                    }
                }
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }
}