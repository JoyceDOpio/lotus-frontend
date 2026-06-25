package com.eternalfairy.lotus.model.repository

import com.eternalfairy.lotus.model.dao.postgres.ApiResponse
import com.eternalfairy.lotus.model.data.Activity
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface IActivityRepository {
    suspend fun addActivity(activity: Activity): ApiResponse<Unit>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun deleteActivity(id: Uuid): ApiResponse<Unit>

    suspend fun editActivity(activity: Activity): ApiResponse<Unit>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getActivity(id: Uuid): ApiResponse<Activity>

    suspend fun getMainRecordedActivity(): ApiResponse<Activity>

    suspend fun getMainActivitiesPerDay(date: LocalDate): ApiResponse<List<Activity>>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getSubActivitiesPerMainActivity(mainActivityId: Uuid): ApiResponse<List<Activity>>

    suspend fun getSubRecordedActivity(): ApiResponse<Activity>
}