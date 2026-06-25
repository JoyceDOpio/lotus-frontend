package com.eternalfairy.lotus.model.repository

import com.eternalfairy.lotus.model.dao.IDayDAO
import com.eternalfairy.lotus.model.dao.postgres.ApiResponse
import com.eternalfairy.lotus.model.data.Day
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Singleton
class DayRepository @Inject constructor(
    private val dao: IDayDAO
): IDayRepository {
    override suspend fun addDay(day: Day): ApiResponse<Unit> {
        return try {
            dao.insertDay(day)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteDay(id: Uuid): ApiResponse<Unit> {
        return try {
            dao.deleteDay(id)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun editDay(day: Day): ApiResponse<Unit> {
        return try {
            dao.updateDay(day)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getDay(date: LocalDate): ApiResponse<Day> {
        return try {
            val day = dao.getDay(date)

            ApiResponse.Success(day)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }
}