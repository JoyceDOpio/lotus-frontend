package com.eternalfairy.lotus.model.dao

import com.eternalfairy.lotus.model.data.Activity
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface IActivityDAO {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun deleteActivity(id: Uuid)

    suspend fun insertActivity(activity: Activity)

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getActivity(id: Uuid): Activity?

    suspend fun getActivities(date: LocalDate): List<Activity>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getSubActivitiesOfMainActivity(mainActivityId: Uuid): List<Activity>

    suspend fun getMainRecordedActivity(): Activity?

    suspend fun getSubRecordedActivity(): Activity?

    suspend fun updateActivity(activity: Activity): Unit
}