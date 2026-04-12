package com.eternalfairy.timeaware.db.supabase

import com.eternalfairy.timeaware.db.data.Activity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import java.util.UUID
import javax.inject.Inject

class ActivityDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    fun deleteActivityById(id: UUID): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.activities").delete {
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

    fun insertActivity(activity: Activity): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.activities").insert(
                    activity
                )
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectActivityById(id: UUID): Flow<ApiResponse<Activity>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val activity = supabaseClient.from("public.activities").select() {
                    filter {
                        Activity::id eq id
                    }
                }.decodeSingle<Activity>()
                emit(ApiResponse.Success(activity))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectMainActivitiesPerDay(date: String): Flow<ApiResponse<List<Activity>>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val activities = supabaseClient.postgrest.rpc(
                    function = "public.get_main_activities_per_day",
                    parameters = buildJsonObject {
                        put("date", Json.encodeToJsonElement(date))
                    }
                ).decodeList<Activity>()
                emit(ApiResponse.Success(activities))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectSubActivitiesPerMainActivity(mainActivityId: UUID): Flow<ApiResponse<List<Activity>>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val activities = supabaseClient.postgrest.rpc(
                    function = "public.get_sub_activities_per_main_activity",
                    parameters = buildJsonObject {
                        put("main_activity_id", Json.encodeToJsonElement(mainActivityId))
                    }
                ).decodeList<Activity>()
                emit(ApiResponse.Success(activities))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectMainRecordedActivity(): Flow<ApiResponse<Activity>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val activity = supabaseClient.postgrest.rpc(
                    function = "public.get_main_recorded_activity"
                ).decodeSingle<Activity>()
                emit(ApiResponse.Success(activity))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectSubRecordedActivity(): Flow<ApiResponse<Activity>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val activity = supabaseClient.postgrest.rpc(
                    function = "public.get_sub_recorded_activity"
                ).decodeSingle<Activity>()
                emit(ApiResponse.Success(activity))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun updateActivity(activity: Activity): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.activities").update(
                    {
                        Activity::title setTo activity.title
                        Activity::note setTo activity.note
                    }
                ) {
                    filter {
                        Activity::id eq activity.id
                    }
                }
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }
}