package com.eternalfairy.timeaware.db.supabase

import com.eternalfairy.timeaware.db.supabase.ApiResponse
import com.eternalfairy.timeaware.db.data.Task
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

class TaskDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    fun deleteTaskById(id: UUID): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.tasks").delete {
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
                    function = "public.get_last_task_priority"
                ).decodeSingle<Int>()
                emit(ApiResponse.Success(priority))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun insertTask(task: Task): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("public.tasks").insert(
                    task
                )
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectTasksWithoutDate(): Flow<ApiResponse<List<Task>>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val tasks = supabaseClient.postgrest.rpc(
                    function = "public.get_tasks_without_date"
                ).decodeList<Task>()
                emit(ApiResponse.Success(tasks))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectTasksPerDay(date: String): Flow<ApiResponse<List<Task>>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val tasks = supabaseClient.postgrest.rpc(
                    function = "public.get_tasks_per_day",
                    parameters = buildJsonObject { //You can put here any serializable object including your own classes
                        put("date", Json.encodeToJsonElement(date))
                    }
                ).decodeList<Task>()
                emit(ApiResponse.Success(tasks))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectTaskById(id: UUID): Flow<ApiResponse<Task>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val task = supabaseClient.from("public.tasks").select() {
                    filter {
                        Task::id eq id
                    }
                }.decodeSingle<Task>()
                emit(ApiResponse.Success(task))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun updateTask(task: Task): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("tasks").update(
                    {
                        Task::title setTo task.title
                        Task::description setTo task.description
                        Task::date setTo task.date
                        Task::startTime setTo task.startTime
                        Task::endTime setTo task.endTime
                        Task::priority setTo task.priority
                        Task::pinned setTo task.pinned
                    }
                ) {
                    filter {
                        Task::id eq task.id
                    }
                }
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }
}