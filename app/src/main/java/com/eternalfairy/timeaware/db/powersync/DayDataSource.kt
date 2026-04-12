package com.eternalfairy.timeaware.db.powersync

import com.eternalfairy.timeaware.db.data.Day
import com.powersync.PowerSyncDatabase
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject

class DayDataSource @Inject constructor(
    private val powerSyncDatabase: PowerSyncDatabase
) {
    suspend fun insertDay(day: Day): Unit {
        try {
            powerSyncDatabase.writeTransaction { transaction ->
                transaction.execute(
                    sql = "INSERT INTO days (created_at, user_id, date, active_time_start, active_time_end VALUES (?, ?, ?, ?, ?)",
                    parameters = listOf(
                        day.createdAt,
                        day.userId,
                        day.date,
                        day.activeTimeStart,
                        day.activeTimeEnd
                    )
                )
            }
        } catch (e: Exception) {
        }
    }

    fun selectDayByDate(date: String): Flow<Day> {
        return flow {
            try {
                val day = powerSyncDatabase.get(
                    sql = "SELECT * FROM days WHERE date = ?",
                    parameters = listOf(date)
                ) { cursor ->
                    Day(
                        id = UUID.fromString(cursor.getString("id")),
                        createdAt = cursor.getString("created_at"),
                        date = OffsetDateTime.parse(cursor.getString("date")),
                        activeTimeStart = cursor.getString("active_time_start"),
                        activeTimeEnd = cursor.getString("active_time_end"),
                        actualActiveTimeStart = cursor.getString("actual_active_time_start"),
                        actualActiveTimeEnd = cursor.getString("actual_active_time_end"),
                        note = cursor.getString("note")
                    )
                }
                emit(day)
            } catch (e: Exception) {
            }
        }
    }

    suspend fun updateDay(day: Day): Unit {
        try {
            powerSyncDatabase.execute(
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
        }
    }
}