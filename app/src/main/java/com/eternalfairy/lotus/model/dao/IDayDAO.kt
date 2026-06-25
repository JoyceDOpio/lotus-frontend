package com.eternalfairy.lotus.model.dao

import com.eternalfairy.lotus.model.data.Day
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface IDayDAO {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun deleteDay(id: Uuid)

    suspend fun insertDay(day: Day)

    suspend fun getDay(date: LocalDate): Day?

    suspend fun updateDay(day: Day): Unit
}