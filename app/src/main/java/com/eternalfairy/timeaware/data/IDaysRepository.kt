package com.eternalfairy.timeaware.data

import kotlinx.coroutines.flow.Flow

interface IDaysRepository {
    fun getDayStream(date: String): Flow<Day?>

    suspend fun insertDay(day: Day)

    suspend fun deleteDay(day: Day)

    suspend fun updateDay(day: Day)
}