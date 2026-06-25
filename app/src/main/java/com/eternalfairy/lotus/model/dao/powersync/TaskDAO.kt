package com.eternalfairy.lotus.model.dao.powersync

import android.util.Log
import com.eternalfairy.lotus.model.data.Task
import com.eternalfairy.lotus.viewmodel.utils.TimeZoneUtils
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskDAO @Inject constructor(
    private val dataSource: OnlineSyncDataSource
) {
//    suspend fun deleteTaskById(id: String) {
//        try {
//            dataSource.getDatabase().writeTransaction { transaction ->
//                transaction.execute(
//                    sql = "DELETE FROM tasks WHERE id = ?",
//                    parameters = listOf(id)
//                )
//            }
//        } catch (e: Exception) {
//            Log.e("TaskDAO", e.printStackTrace().toString())
//        }
//    }
//
//    fun getLastPriority(): Flow<Int?> {
//        return flow {
//            try {
//                val priority = dataSource.getDatabase().getOptional(
//                    sql = "SELECT MAX(priority) FROM tasks"//FIXME: Column last_priority not found
//                ) { cursor ->
//                    cursor.getString(0).toString().toInt()
//                }
//                emit(priority)
//            } catch (e: Exception) {
//                Log.e("TaskDAO", e.printStackTrace().toString())
//            }
//        }
//    }
//
//    suspend fun insertTask(task: Task) {
//        try {
//            dataSource.getDatabase().writeTransaction { transaction ->
//                transaction.execute(
//                    sql = "INSERT INTO tasks (id, created_at, user_id, date, start_time, end_time, title, description, priority, pinned) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
//                    parameters = listOf(
//                        task.id,
//                        task.createdAt,
//                        TimeZoneUtils.createTimestampTz(),
//                        task.userId,
//                        task.date,
//                        task.startTime,
//                        task.endTime,
//                        task.title,
//                        task.description,
//                        task.priority,
//                        task.pinned
//                    )
//                )
//            }
//        } catch (e: Exception) {
//            Log.e("TaskDAO", e.printStackTrace().toString())
//            Log.e("TaskDAO", e.toString())
//        }
//    }
//
//    fun selectTasksWithoutDate(): Flow<List<Task>> {
//        try {
//            val activities = dataSource.getDatabase().watch(
//                sql = "SELECT * FROM tasks WHERE date IS NULL ORDER BY priority ASC"
//            ) { cursor ->
//                Task(
////                    id = UUID.fromString(cursor.getString("id")),
//                    id = cursor.getString("id"),
//                    createdAt = cursor.getString("created_at"),
////                    userId = UUID.fromString(cursor.getString("user_id")),
//                    userId = cursor.getString("user_id"),
////                    date = OffsetDateTime.parse(cursor.getString("date")),
////                    date = cursor.getString(3),
////                    startTime = cursor.getString(4),
////                    endTime = cursor.getString(5),
//                    title = cursor.getString("title"),
////                    description = cursor.getString("description"),
//                    description = cursor.getString(7),
//                    priority = cursor.getString(8)?.toInt(),
//                    pinned = cursor.getString("pinned").toInt()
//                )
//            }
//            return activities
//        } catch (e: Exception) {
//            Log.e("TaskDAO", e.printStackTrace().toString())
//            return emptyFlow()
//        }
//    }
//
//    fun selectTasksPerDay(date: String): Flow<List<Task>> {
//        try {
//            val tasks = dataSource.getDatabase().watch(
//                sql = "SELECT * FROM tasks WHERE date = ? ORDER BY start_time ASC",
//                parameters = listOf(date)
//            ) { cursor ->
//                // 0 - id
//                // 1 - created at "2026-04-21T09:05:07.320962Z"
//                // 2 - user id
//                // 3 - date
//                // 4 - start time
//                // 5 - end time
//                // 6 - title
//                // 7 - description
//                // 8 - priority
//                // 9 - pinned
//                Task(
////                    id = UUID.fromString(cursor.getString("id")),
//                    id = cursor.getString("id"),
//                    createdAt = cursor.getString("created_at"),
////                    userId = UUID.fromString(cursor.getString("user_id")),
//                    userId = cursor.getString("user_id"),
////                    date = OffsetDateTime.parse(cursor.getString("date")),
////                    date = LocalDate.parse(cursor.getString("date")),
//                    date = cursor.getString("date"),
//                    startTime = cursor.getString(4),
//                    endTime = cursor.getString(5),
//                    title = cursor.getString("title"),
////                    description = cursor.getString("description"),
//                    description = cursor.getString(7),
//                    priority = cursor.getString(8)?.toInt(),
//                    pinned = cursor.getString("pinned").toInt()
//                )
//            }
//            return tasks
//        } catch (e: Exception) {
//            Log.e("TaskDAO", e.printStackTrace().toString())
//            return emptyFlow()
//        }
//    }
//
//    fun selectTaskById(id: String): Flow<Task?> {
//        return flow {
//            try {
//                val task = dataSource.getDatabase().getOptional(
//                    sql = "SELECT * FROM tasks WHERE id = ?",
//                    parameters = listOf(id)
//                ) { cursor ->
//                    Task(
////                        id = UUID.fromString(cursor.getString("id")),
//                        id = cursor.getString("id"),
//                        createdAt = cursor.getString("created_at"),
////                        userId = UUID.fromString(cursor.getString("user_id")),
//                        userId = cursor.getString("user_id"),
////                    date = OffsetDateTime.parse(cursor.getString("date")),
////                        date = LocalDate.parse(cursor.getString(3)) ?: null,
//                        date = cursor.getString(3),
//                        startTime = cursor.getString(4),
//                        endTime = cursor.getString(5),
//                        title = cursor.getString("title"),
////                    description = cursor.getString("description"),
//                        description = cursor.getString(7),
//                        priority = cursor.getString(8)?.toInt(),
//                        pinned = cursor.getString("pinned").toInt()
//                    )
//                }
//                emit(task)
//            } catch (e: Exception) {
//                Log.e("TaskDAO", e.printStackTrace().toString())
//            }
//        }
//    }
//
//    suspend fun updateTask(task: Task) {
//        try {
//            dataSource.getDatabase().execute(
//                sql = "UPDATE tasks SET date = ?, start_time = ?, end_time = ?, title = ?, description = ?, priority = ?, pinned = ? WHERE id = ?",
//                parameters = listOf(
//                    task.date,
//                    task.startTime,
//                    task.endTime,
//                    task.title,
//                    task.description,
//                    task.priority,
//                    task.pinned,
//                    task.id
//                )
//            )
//        } catch (e: Exception) {
//            Log.e("TaskDAO", e.printStackTrace().toString())
//        }
//    }
}