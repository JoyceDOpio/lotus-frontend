package com.eternalfairy.timeaware.db.supabase

import com.eternalfairy.timeaware.db.supabase.ApiResponse
import com.eternalfairy.timeaware.db.data.Goal
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class GoalDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    fun deleteGoalById(id: UUID): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.goals").delete {
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

    fun getLastPriority(): Flow<ApiResponse<Int>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val priority = supabaseClient.postgrest.rpc(
                    function = "public.get_last_goal_priority"
                ).decodeSingle<Int>()
                emit(ApiResponse.Success(priority))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun insertGoal(goal: Goal): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.goals").insert(
                    goal
                )
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectGoals(): Flow<ApiResponse<List<Goal>>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val goals = supabaseClient.from("public.goals").select().decodeList<Goal>()
                emit(ApiResponse.Success(goals))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectGoalById(id: UUID): Flow<ApiResponse<Goal>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val goal = supabaseClient.from("public.goals").select() {
                    filter {
                        Goal::id eq id
                    }
                }.decodeSingle<Goal>()
                emit(ApiResponse.Success(goal))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun updateGoal(goal: Goal): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.goals").update(
                    {
                        Goal::title setTo goal.title
                        Goal::priority setTo goal.priority
                    }
                ) {
                    filter {
                        Goal::id eq goal.id
                    }
                }
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }
}