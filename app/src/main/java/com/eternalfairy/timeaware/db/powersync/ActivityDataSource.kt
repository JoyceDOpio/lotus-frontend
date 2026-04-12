package com.eternalfairy.timeaware.db.powersync

import com.eternalfairy.timeaware.db.data.Activity
import com.powersync.PowerSyncDatabase
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject

class ActivityDataSource @Inject constructor(
    private val powerSyncDatabase: PowerSyncDatabase
) {
    suspend fun deleteActivityById(id: UUID): Unit {
        try {
            powerSyncDatabase.writeTransaction { transaction ->
                transaction.execute(
                    sql = "DELETE FROM activities WHERE id = ?",
                    parameters = listOf(id)
                )
            }
        } catch (e: Exception) {
        }
    }

    suspend fun insertActivity(activity: Activity): Unit {
        try {
            powerSyncDatabase.writeTransaction { transaction ->
                transaction.execute(
                    sql = "INSERT INTO activities (created_at, user_id, date, start_time, end_time, title, note, main_activity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    parameters = listOf(
                        activity.createdAt,
                        activity.userId,
                        activity.date,
                        activity.startTime,
                        activity.endTime,
                        activity.title,
                        activity.note,
                        activity.mainActivityId
                    )
                )
            }
        } catch (e: Exception) {
        }
    }

    fun selectActivityById(id: UUID): Flow<Activity> {
        return flow {
            try {
                val activity = powerSyncDatabase.get(
                    sql = "SELECT * FROM activities WHERE id = ?",
                    parameters = listOf(id)
                ) { cursor ->
                    Activity(
                        id = UUID.fromString(cursor.getString("id")),
                        createdAt = cursor.getString("created_at"),
                        userId = UUID.fromString(cursor.getString("user_id")),
                        date = OffsetDateTime.parse(cursor.getString("date")),
                        startTime = cursor.getString("start_time"),
                        endTime = cursor.getString("end_time"),
                        title = cursor.getString("title"),
                        note = cursor.getString("note"),
                        mainActivityId = UUID.fromString(cursor.getString("main_activity_id")),
                    )
                }
                emit(activity)
            } catch (e: Exception) {
            }
        }
    }

    fun selectMainActivitiesPerDay(date: String): Flow<List<Activity>> {
        try {
//                val activities = powerSyncDatabase.getAll(
            val activities = powerSyncDatabase.watch(
                sql = "SELECT * FROM activities WHERE date = ?",
                parameters = listOf(date)
            ) { cursor ->
                Activity(
                    id = UUID.fromString(cursor.getString("id")),
                    createdAt = cursor.getString("created_at"),
                    userId = UUID.fromString(cursor.getString("user_id")),
                    date = OffsetDateTime.parse(cursor.getString("date")),
                    startTime = cursor.getString("start_time"),
                    endTime = cursor.getString("end_time"),
                    title = cursor.getString("title"),
                    note = cursor.getString("note"),
                    mainActivityId = UUID.fromString(cursor.getString("main_activity_id")),
                )
            }
            return activities
        } catch (e: Exception) {
            return emptyFlow()
        }
    }

    fun selectSubActivitiesPerMainActivity(mainActivityId: UUID): Flow<List<Activity>> {
        return flow {
            try {
                val activities = powerSyncDatabase.getAll(
                    sql = "SELECT * FROM activities WHERE main_activity_id = ?",
                    parameters = listOf(mainActivityId)
                ) { cursor ->
                    Activity(
                        id = UUID.fromString(cursor.getString("id")),
                        createdAt = cursor.getString("created_at"),
                        userId = UUID.fromString(cursor.getString("user_id")),
                        date = OffsetDateTime.parse(cursor.getString("date")),
                        startTime = cursor.getString("start_time"),
                        endTime = cursor.getString("end_time"),
                        title = cursor.getString("title"),
                        note = cursor.getString("note"),
                        mainActivityId = UUID.fromString(cursor.getString("main_activity_id")),
                    )
                }
                emit(activities)
            } catch (e: Exception) {
            }
        }
    }

    fun selectMainRecordedActivity(): Flow<Activity> {
        return flow {
            try {
                val activity = powerSyncDatabase.get(
                    sql = "SELECT * FROM activities WHERE end_time IS NULL AND main_activity_id IS NULL"
                ) { cursor ->
                    Activity(
                        id = UUID.fromString(cursor.getString("id")),
                        createdAt = cursor.getString("created_at"),
                        userId = UUID.fromString(cursor.getString("user_id")),
                        date = OffsetDateTime.parse(cursor.getString("date")),
                        startTime = cursor.getString("start_time"),
                        endTime = cursor.getString("end_time"),
                        title = cursor.getString("title"),
                        note = cursor.getString("note"),
                        mainActivityId = UUID.fromString(cursor.getString("main_activity_id")),
                    )
                }
                emit(activity)
            } catch (e: Exception) {
            }
        }
    }

    fun selectSubRecordedActivity(): Flow<Activity> {
        return flow {
            try {
                val activity = powerSyncDatabase.get(
                    sql = "SELECT * FROM activities WHERE end_time IS NULL AND main_activity_id IS NOT NULL"
                ) { cursor ->
                    Activity(
                        id = UUID.fromString(cursor.getString("id")),
                        createdAt = cursor.getString("created_at"),
                        userId = UUID.fromString(cursor.getString("user_id")),
                        date = OffsetDateTime.parse(cursor.getString("date")),
                        startTime = cursor.getString("start_time"),
                        endTime = cursor.getString("end_time"),
                        title = cursor.getString("title"),
                        note = cursor.getString("note"),
                        mainActivityId = UUID.fromString(cursor.getString("main_activity_id")),
                    )
                }
                emit(activity)
            } catch (e: Exception) {
            }
        }
    }

    suspend fun updateActivity(activity: Activity): Unit {
        try {
            powerSyncDatabase.execute(
                sql = "UPDATE activities SET end_time = ?, title = ?, note = ? WHERE id = ?",
                parameters = listOf(
                    activity.endTime,
                    activity.title,
                    activity.note,
                    activity.id
                )
            )
        } catch (e: Exception) {
        }
    }
}