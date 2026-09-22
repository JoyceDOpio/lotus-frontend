package com.eternalfairy.lotus.data.dao.postgres

import com.eternalfairy.lotus.data.dao.IDayDAO
import com.eternalfairy.lotus.data.model.Day
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DayDAO(
    private val api: DayApi
): IDayDAO {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteDay(id: Uuid) {
        return api.deleteDay(id)
    }

    override suspend fun insertDay(day: Day) {
        return api.insertDay(day)
    }

    override suspend fun getDay(date: LocalDate): Day? {
        return api.getDay(date)
    }

    override suspend fun updateDay(day: Day) {
        return api.updateDay(day)
    }
}