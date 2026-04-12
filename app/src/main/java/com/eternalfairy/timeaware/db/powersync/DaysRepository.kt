package com.eternalfairy.timeaware.db.powersync

import com.eternalfairy.timeaware.db.data.Day
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DaysRepository @Inject constructor(
    private val dataSource: DayDataSource
) {
    fun getDayStream(date: String): Flow<Day> {
        return dataSource.selectDayByDate(date)
    }

    suspend fun insertDay(day: Day): Unit {
        return dataSource.insertDay(day)
    }

    suspend fun updateDay(day: Day): Unit {
        return dataSource.updateDay(day)
    }

}