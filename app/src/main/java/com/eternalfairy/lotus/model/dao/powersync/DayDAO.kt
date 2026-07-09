package com.eternalfairy.lotus.model.dao.powersync

import android.util.Log
import com.eternalfairy.lotus.model.data.Day
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Singleton
class DayDAO @Inject constructor(
    private val dataSource: OnlineSyncDataSource
) {
    suspend fun insertDay(day: Day): Unit {
        try {
            dataSource.getDatabase().writeTransaction { transaction ->
                transaction.execute(
                    sql = "INSERT INTO days (created_at, user_id, date, active_time_start, active_time_end VALUES (?, ?, ?, ?, ?)",
                    parameters = listOf(
                        day.date,
                        day.activeTimeStart,
                        day.activeTimeEnd
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("DayDAO", e.printStackTrace().toString())
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun selectDayByDate(date: String): Flow<Day?> {
        return flow {
            try {
                val day = dataSource.getDatabase().getOptional(
                    sql = "SELECT * FROM days WHERE date = ?",
                    parameters = listOf(date)
                ) { cursor ->
                    Day(
                        id = Uuid.parse(cursor.getString("id")),
                        date = LocalDate.parse(cursor.getString("date")),
                        activeTimeStart = LocalTime.parse(cursor.getString("active_time_start")),
                        activeTimeEnd = LocalTime.parse(cursor.getString("active_time_end")),
                        actualActiveTimeStart = LocalTime.parse(cursor.getString("actual_active_time_start")),
                        actualActiveTimeEnd = LocalTime.parse(cursor.getString("actual_active_time_end")),
                        note = cursor.getString("note")
                    )
                }
                emit(day)
            } catch (e: Exception) {
                Log.e("DayDAO", e.printStackTrace().toString())
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun updateDay(day: Day): Unit {
        try {
            dataSource.getDatabase().execute(
                sql = "UPDATE days SET active_time_start = ?, active_time_end = ?, actual_active_time_start = ?, actual_active_time_end = ? WHERE id = ?",
                parameters = listOf(
                    day.activeTimeStart,
                    day.activeTimeEnd,
                    day.actualActiveTimeStart,
                    day.actualActiveTimeEnd,
                    day.id
                )
            )
        } catch (e: Exception) {
            Log.e("DayDAO", e.printStackTrace().toString())
        }
    }
}