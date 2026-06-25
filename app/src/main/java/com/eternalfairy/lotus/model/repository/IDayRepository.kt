package com.eternalfairy.lotus.model.repository

import com.eternalfairy.lotus.model.dao.postgres.ApiResponse
import com.eternalfairy.lotus.model.data.Day
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface IDayRepository {
    suspend fun addDay(day: Day): ApiResponse<Unit>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun deleteDay(id: Uuid): ApiResponse<Unit>

    suspend fun editDay(day: Day): ApiResponse<Unit>

    suspend fun getDay(date: LocalDate): ApiResponse<Day>
}