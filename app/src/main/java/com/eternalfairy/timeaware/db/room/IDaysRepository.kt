package com.eternalfairy.timeaware.db.room

import kotlinx.coroutines.flow.Flow

interface IDaysRepository {
    fun getDayStream(date: String): Flow<Day?>

    suspend fun insertDay(day: Day)

    suspend fun deleteDay(day: Day)

    suspend fun updateDay(day: Day)
}