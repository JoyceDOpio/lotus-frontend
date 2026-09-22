package com.eternalfairy.lotus.data.test

import android.util.Log
import com.eternalfairy.lotus.data.dao.postgres.ApiResponse
import com.eternalfairy.lotus.data.model.Day
import com.eternalfairy.lotus.domain.repository.IDayRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.Uuid

class MockDayRepository: IDayRepository {
    var mockDays = mutableListOf<Day>(
        Day(
            id = Uuid.Companion.parse("495502d1-4f23-4c31-8edb-4ec1107f35be"),
            date = LocalDate.Companion.parse(java.time.LocalDate.now().plusDays(-1).toString()),
            activeTimeStart = LocalTime(6, 0),
            activeTimeEnd = LocalTime(22, 0),
            actualActiveTimeStart = null,
            actualActiveTimeEnd = null
        ),
        Day(
            id = Uuid.Companion.parse("5f6c53ca-b5d7-4aec-89af-4f725bc8c50f"),
            date = LocalDate.Companion.parse(java.time.LocalDate.now().toString()),
            activeTimeStart = LocalTime(6, 0),
            activeTimeEnd = LocalTime(22, 0),
            actualActiveTimeStart = LocalTime(5, 30),
            actualActiveTimeEnd = null
        )
    )

    override suspend fun addDay(day: Day): ApiResponse<Unit> {
        return try {
            mockDays += day
            Log.i("MockDayRepository", "addDay() mockDays: $mockDays")

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun deleteDay(id: Uuid): ApiResponse<Unit> {
        return try {
            for (day in mockDays) {
                if (day.id == id) {
                    mockDays.remove(day)
                }
            }
            Log.i("MockDaysRepository", "deleteDay() mockDays: $mockDays")

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun editDay(day: Day): ApiResponse<Unit> {
        return try {
            mockDays.forEach { dayToBeEdited ->
                if (dayToBeEdited.id == day.id) {
                    mockDays.remove(dayToBeEdited)
                    mockDays.add(day)
                }
            }
            Log.i("MockDayRepository", "editDay() mockDays: $mockDays")

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getDay(date: LocalDate): ApiResponse<Day> {
        return try {
            val day = mockDays.find { d -> d.date == date }

            ApiResponse.Success(day)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }
}