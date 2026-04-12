package com.eternalfairy.timeaware.db.supabase

import com.eternalfairy.timeaware.db.data.Day
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DaysRepository @Inject constructor(
    private val dataSource: DayDataSource
) {
    fun getDayStream(date: String): Flow<ApiResponse<Day>> {
        return dataSource.selectDayByDate(date)
    }

    fun insertDay(day: Day): Flow<ApiResponse<Unit>> {
        return dataSource.insertDay(day)
    }

    fun updateDay(day: Day): Flow<ApiResponse<Unit>> {
        return dataSource.updateDay(day)
    }

}