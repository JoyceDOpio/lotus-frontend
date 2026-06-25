package com.eternalfairy.lotus.model.dao.powersync

import android.util.Log
import com.eternalfairy.lotus.model.data.Activity
import com.eternalfairy.lotus.model.dao.IActivityDAO
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Singleton
class ActivityDAO @Inject constructor(
    private val dataSource: OnlineSyncDataSource
): IActivityDAO {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteActivity(id: Uuid): Unit {
        try {
            dataSource.getDatabase().writeTransaction { transaction ->
                transaction.execute(
                    sql = "DELETE FROM activities WHERE id = ?",
                    parameters = listOf(id)
                )
            }
        } catch (e: Exception) {
            Log.e("ActivityDAO", e.printStackTrace().toString())
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun insertActivity(activity: Activity): Unit {
        try {
            dataSource.getDatabase().writeTransaction { transaction ->
                transaction.execute(
                    sql = "INSERT INTO activities (created_at, date, start_time, end_time, title, note, main_activity) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    parameters = listOf(
                        System.currentTimeMillis(),
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
            Log.e("ActivityDAO", e.printStackTrace().toString())
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun getActivity(id: Uuid): Flow<Activity?> {
        return flow {
            try {
                val activity = dataSource.getDatabase().getOptional(
                    sql = "SELECT * FROM activities WHERE id = ?",
                    parameters = listOf(id)
                ) { cursor ->
                    Activity(
                        id = Uuid.parse(cursor.getString("id")),
                        date = LocalDate.parse(cursor.getString("date")),
                        startTime = LocalTime.parse(cursor.getString("start_time")),
                        endTime = LocalTime.parse(cursor.getString("end_time")),
                        title = cursor.getString("title"),
                        note = cursor.getString("note"),
                        mainActivityId = Uuid.parse(cursor.getString("main_activity_id")),
                    )
                }
                emit(activity)
            } catch (e: Exception) {
                Log.e("ActivityDAO", e.printStackTrace().toString())
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun getActivities(date: LocalDate): Flow<List<Activity>> {
        try {
//                val activities = powerSyncDatabase.getAll(
            val activities = dataSource.getDatabase().watch(
                sql = "SELECT * FROM activities WHERE date = ?",
                parameters = listOf(date)
            ) { cursor ->
                Activity(
                    id = Uuid.parse(cursor.getString("id")),
                    date = LocalDate.parse(cursor.getString("date")),
                    startTime = LocalTime.parse(cursor.getString("start_time")),
                    endTime = LocalTime.parse(cursor.getString("end_time")),
                    title = cursor.getString("title"),
                    note = cursor.getString("note"),
                    mainActivityId = Uuid.parse(cursor.getString("main_activity_id")),
                )
            }
            return activities
        } catch (e: Exception) {
            Log.e("ActivityDAO", e.printStackTrace().toString())
            return emptyFlow()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun getSubActivitiesOfMainActivity(mainActivityId: Uuid): Flow<List<Activity>> {
        return flow {
            try {
                val activities = dataSource.getDatabase().getAll(
                    sql = "SELECT * FROM activities WHERE main_activity_id = ?",
                    parameters = listOf(mainActivityId)
                ) { cursor ->
                    Activity(
                        id = Uuid.parse(cursor.getString("id")),
                        date = LocalDate.parse(cursor.getString("date")),
                        startTime = LocalTime.parse(cursor.getString("start_time")),
                        endTime = LocalTime.parse(cursor.getString("end_time")),
                        title = cursor.getString("title"),
                        note = cursor.getString("note"),
                        mainActivityId = Uuid.parse(cursor.getString("main_activity_id")),
                    )
                }
                emit(activities)
            } catch (e: Exception) {
                Log.e("ActivityDAO", e.printStackTrace().toString())
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun getMainRecordedActivity(): Flow<Activity?> {
        return flow {
            try {
                val activity = dataSource.getDatabase().getOptional(
                    sql = "SELECT * FROM activities WHERE end_time IS NULL AND main_activity_id IS NULL"
                ) { cursor ->
                    Activity(
                        id = Uuid.parse(cursor.getString("id")),
                        date = LocalDate.parse(cursor.getString("date")),
                        startTime = LocalTime.parse(cursor.getString("start_time")),
                        endTime = LocalTime.parse(cursor.getString("end_time")),
                        title = cursor.getString("title"),
                        note = cursor.getString("note"),
                        mainActivityId = Uuid.parse(cursor.getString("main_activity_id")),
                    )
                }
                emit(activity)
            } catch (e: Exception) {
                Log.e("ActivityDAO", e.printStackTrace().toString())
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun getSubRecordedActivity(): Flow<Activity?> {
        return flow {
            try {
                val activity = dataSource.getDatabase().getOptional(
                    sql = "SELECT * FROM activities WHERE end_time IS NULL AND main_activity_id IS NOT NULL"
                ) { cursor ->
                    Activity(
                        id = Uuid.parse(cursor.getString("id")),
                        date = LocalDate.parse(cursor.getString("date")),
                        startTime = LocalTime.parse(cursor.getString("start_time")),
                        endTime = LocalTime.parse(cursor.getString("end_time")),
                        title = cursor.getString("title"),
                        note = cursor.getString("note"),
                        mainActivityId = Uuid.parse(cursor.getString("main_activity_id")),
                    )
                }
                emit(activity)
            } catch (e: Exception) {
                Log.e("ActivityDAO", e.printStackTrace().toString())
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun updateActivity(activity: Activity): Unit {
        try {
            dataSource.getDatabase().execute(
                sql = "UPDATE activities SET end_time = ?, title = ?, note = ? WHERE id = ?",
                parameters = listOf(
                    activity.endTime,
                    activity.title,
                    activity.note,
                    activity.id
                )
            )
        } catch (e: Exception) {
            Log.e("ActivityDAO", e.printStackTrace().toString())
        }
    }
}