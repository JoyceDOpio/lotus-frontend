package com.eternalfairy.lotus.model.dao.postgres

import com.eternalfairy.lotus.model.dao.ITaskDAO
import com.eternalfairy.lotus.model.data.Task
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TaskDAO(
    private val api: TaskApi
): ITaskDAO {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteTask(id: Uuid) {
        return api.deleteTask(id)
    }

    override suspend fun insertTask(task: Task) {
        return api.insertTask(task)
    }

    override suspend fun getLastPriority(): Int? {
        return api.getLastPriority()
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getTask(id: Uuid): Task? {
        return api.getTask(id)
    }

    override suspend fun getTasks(date: LocalDate): List<Task> {
        return api.getTasks(date)
    }

    override suspend fun getTasksWithoutDate(): List<Task> {
        return api.getWithoutDate()
    }

    override suspend fun updateTask(task: Task) {
        return api.updateTask(task)
    }
}