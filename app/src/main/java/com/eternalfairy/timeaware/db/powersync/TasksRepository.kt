package com.eternalfairy.timeaware.db.powersync

import com.eternalfairy.timeaware.db.data.Task
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class TasksRepository @Inject constructor(
    private val dataSource: TaskDataSource
) {
    suspend fun deleteTask(id: UUID): Unit {
        return dataSource.deleteTaskById(id)
    }

    fun getTasksPerDayStream(date: String): Flow<List<Task>> {
        return dataSource.selectTasksPerDay(date)
    }

    fun getTaskStream(id: UUID): Flow<Task> {
        return dataSource.selectTaskById(id)
    }

    fun getTasksWithoutDateStream(): Flow<List<Task>> {
        return dataSource.selectTasksWithoutDate()
    }

    fun getLastPriority(): Flow<Int> {
        return dataSource.getLastPriority()
    }

    suspend fun insertTask(task: Task): Unit {
        return dataSource.insertTask(task)
    }

    suspend fun updateTask(task: Task): Unit {
        return dataSource.updateTask(task)
    }

}