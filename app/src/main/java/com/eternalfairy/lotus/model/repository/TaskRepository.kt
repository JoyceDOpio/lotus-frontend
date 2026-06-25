package com.eternalfairy.lotus.model.repository

import com.eternalfairy.lotus.model.dao.ITaskDAO
import com.eternalfairy.lotus.model.dao.postgres.ApiResponse
import com.eternalfairy.lotus.model.dao.powersync.TaskDAO
import com.eternalfairy.lotus.model.data.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Singleton
class TaskRepository @Inject constructor(
    private val dao: ITaskDAO
): ITaskRepository {
    override suspend fun addTask(task: Task): ApiResponse<Unit> {
        return try {
            dao.insertTask(task)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteTask(id: Uuid): ApiResponse<Unit> {
        return try {
            dao.deleteTask(id)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun editTask(task: Task): ApiResponse<Unit> {
        return try {
            dao.updateTask(task)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getLastPriority(): ApiResponse<Int> {
        return try {
            val priority = dao.getLastPriority()

            ApiResponse.Success(priority)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getTask(id: Uuid): ApiResponse<Task> {
        return try {
            val task = dao.getTask(id)

            ApiResponse.Success(task)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getTasksPerDay(date: LocalDate): ApiResponse<List<Task>> {
        return try {
            val tasks = dao.getTasks(date)

            ApiResponse.Success(tasks)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getToDoTasks(): ApiResponse<List<Task>> {
        return try {
            val tasks = dao.getTasksWithoutDate()

            ApiResponse.Success(tasks)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }
}