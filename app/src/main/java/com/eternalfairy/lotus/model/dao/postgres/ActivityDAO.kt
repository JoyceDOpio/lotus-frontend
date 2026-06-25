package com.eternalfairy.lotus.model.dao.postgres

import com.eternalfairy.lotus.model.dao.IActivityDAO
import com.eternalfairy.lotus.model.data.Activity
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ActivityDAO(
    private val api: ActivityApi
): IActivityDAO {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteActivity(id: Uuid) {
        return api.deleteActivity(id)
    }

    override suspend fun insertActivity(activity: Activity) {
        return api.insertActivity(activity)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getActivity(id: Uuid): Activity? {
        return api.getActivity(id)
    }

    override suspend fun getActivities(date: LocalDate): List<Activity> {
        return api.getActivities(date)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getSubActivitiesOfMainActivity(mainActivityId: Uuid): List<Activity> {
        return api.getSubActivitiesOfMainActivity(mainActivityId)
    }

    override suspend fun getMainRecordedActivity(): Activity? {
        return api.getMainRecordedActivity()
    }

    override suspend fun getSubRecordedActivity(): Activity? {
        return api.getSubRecordedActivity()
    }

    override suspend fun updateActivity(activity: Activity) {
        return api.updateActivity(activity)
    }
}