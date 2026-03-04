package com.eternalfairy.timeaware.data

import kotlinx.coroutines.flow.Flow

class RepositoryDays(private val dayDao: DaoDay) : IDaysRepository {
    override fun getDayStream(date: String): Flow<Day?> {
        return dayDao.getDay(date)
    }

    override suspend fun insertDay(day: Day) {
        return dayDao.insert(day)
    }

    override suspend fun deleteDay(day: Day) {
        return dayDao.delete(day)
    }

    override suspend fun updateDay(day: Day) {
        return dayDao.update(day)
    }
}