package com.eternalfairy.timeaware.db.powersync

import com.eternalfairy.timeaware.db.data.Task
import com.powersync.PowerSyncDatabase
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject

class TaskDataSource @Inject constructor(
    private val powerSyncDatabase: PowerSyncDatabase
) {
    suspend fun deleteTaskById(id: UUID): Unit {
        try {
            powerSyncDatabase.writeTransaction { transaction ->
                transaction.execute(
                    sql = "DELETE FROM tasks WHERE id = ?",
                    parameters = listOf(id)
                )
            }
        } catch (e: Exception) {
        }
    }

    fun getLastPriority(): Flow<Int> {
        return flow {
            try {
                val priority = powerSyncDatabase.get(
                    sql = "SELECT MAX(priority) FROM tasks AS last_priority"
                ) { cursor ->
                    cursor.getString("last_priority").toInt()
                }
                emit(priority)
            } catch (e: Exception) {
            }
        }
    }

    suspend fun insertTask(task: Task): Unit {
        try {
            powerSyncDatabase.writeTransaction { transaction ->
                transaction.execute(
                    sql = "INSERT INTO tasks (created_at, user_id, title, description, date, start_time, end_time, priority, pinned) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    parameters = listOf(
                        task.createdAt,
                        task.userId,
                        task.title,
                        task.description,
                        task.date,
                        task.startTime,
                        task.endTime,
                        task.priority,
                        task.pinned
                    )
                )
            }
        } catch (e: Exception) {
        }
    }

    fun selectTasksWithoutDate(): Flow<List<Task>> {
        try {
            val activities = powerSyncDatabase.watch(
                sql = "SELECT * FROM tasks WHERE date IS NULL ORDER BY priority ASC"
            ) { cursor ->
                Task(
                    id = UUID.fromString(cursor.getString("id")),
                    createdAt = cursor.getString("created_at"),
                    userId = UUID.fromString(cursor.getString("user_id")),
                    title = cursor.getString("title"),
                    description = cursor.getString("description"),
                    date = OffsetDateTime.parse(cursor.getString("date")),
                    startTime = cursor.getString("start_time"),
                    endTime = cursor.getString("end_time"),
                    priority = cursor.getString("priority").toInt(),
                    pinned = cursor.getString("pinned").toBoolean(),
                )
            }
            return activities
        } catch (e: Exception) {
            return emptyFlow()
        }
    }

    fun selectTasksPerDay(date: String): Flow<List<Task>> {
        try {
            val activities = powerSyncDatabase.watch(
                sql = "SELECT * FROM tasks WHERE date = ? ORDER BY start_time ASC",
                parameters = listOf(date)
            ) { cursor ->
                Task(
                    id = UUID.fromString(cursor.getString("id")),
                    createdAt = cursor.getString("created_at"),
                    userId = UUID.fromString(cursor.getString("user_id")),
                    title = cursor.getString("title"),
                    description = cursor.getString("description"),
                    date = OffsetDateTime.parse(cursor.getString("date")),
                    startTime = cursor.getString("start_time"),
                    endTime = cursor.getString("end_time"),
                    priority = cursor.getString("priority").toInt(),
                    pinned = cursor.getString("pinned").toBoolean(),
                )
            }
            return activities
        } catch (e: Exception) {
            return emptyFlow()
        }
    }

    fun selectTaskById(id: UUID): Flow<Task> {
        return flow {
            try {
                val task = powerSyncDatabase.get(
                    sql = "SELECT * FROM tasks WHERE id = ?",
                    parameters = listOf(id)
                ) { cursor ->
                    Task(
                        id = UUID.fromString(cursor.getString("id")),
                        createdAt = cursor.getString("created_at"),
                        userId = UUID.fromString(cursor.getString("user_id")),
                        title = cursor.getString("title"),
                        description = cursor.getString("description"),
                        date = OffsetDateTime.parse(cursor.getString("date")),
                        startTime = cursor.getString("start_time"),
                        endTime = cursor.getString("end_time"),
                        priority = cursor.getString("priority").toInt(),
                        pinned = cursor.getString("pinned").toBoolean()
                    )
                }
                emit(task)
            } catch (e: Exception) {
            }
        }
    }

    suspend fun updateTask(task: Task): Unit {
        try {
            powerSyncDatabase.execute(
                sql = "UPDATE tasks SET title = ?, description = ?, date = ?, start_time = ?, end_time = ?, priority = ?, pinned = ? WHERE id = ?",
                parameters = listOf(
                    task.title,
                    task.description,
                    task.date,
                    task.startTime,
                    task.endTime,
                    task.priority,
                    task.pinned,
                    task.id
                )
            )
        } catch (e: Exception) {
        }
    }
}