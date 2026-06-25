package com.eternalfairy.lotus.model.repository

import com.eternalfairy.lotus.model.dao.postgres.ApiResponse
import com.eternalfairy.lotus.model.data.Task
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface ITaskRepository {
    suspend fun addTask(task: Task): ApiResponse<Unit>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun deleteTask(id: Uuid): ApiResponse<Unit>

    suspend fun editTask(task: Task): ApiResponse<Unit>

    suspend fun getLastPriority(): ApiResponse<Int>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getTask(id: Uuid): ApiResponse<Task>

    suspend fun getTasksPerDay(date: LocalDate): ApiResponse<List<Task>>

    suspend fun getToDoTasks(): ApiResponse<List<Task>>
}