package com.eternalfairy.timeaware.db.supabase

import com.eternalfairy.timeaware.db.data.Task
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class TasksRepository @Inject constructor(
    private val dataSource: TaskDataSource
) {
    fun deleteTask(id: UUID): Flow<ApiResponse<Unit>> {
        return dataSource.deleteTaskById(id)
    }

    fun getTasksPerDayStream(date: String): Flow<ApiResponse<List<Task>>> {
        return dataSource.selectTasksPerDay(date)
    }

    fun getTaskStream(id: UUID): Flow<ApiResponse<Task>> {
        return dataSource.selectTaskById(id)
    }

    fun getTasksWithoutDateStream(): Flow<ApiResponse<List<Task>>> {
        return dataSource.selectTasksWithoutDate()
    }

    fun getLastPriority(): Flow<ApiResponse<Int>> {
        return dataSource.getLastPriority()
    }

    fun insertTask(task: Task): Flow<ApiResponse<Unit>> {
        return dataSource.insertTask(task)
    }

    fun updateTask(task: Task): Flow<ApiResponse<Unit>> {
        return dataSource.updateTask(task)
    }

}