package com.eternalfairy.lotus.model.dao

import com.eternalfairy.lotus.model.data.Task
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface ITaskDAO {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun deleteTask(id: Uuid)

    suspend fun insertTask(task: Task)

    suspend fun getLastPriority(): Int?

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getTask(id: Uuid): Task?

    suspend fun getTasks(date: LocalDate): List<Task>

    suspend fun getTasksWithoutDate(): List<Task>

    suspend fun updateTask(task: Task): Unit
}