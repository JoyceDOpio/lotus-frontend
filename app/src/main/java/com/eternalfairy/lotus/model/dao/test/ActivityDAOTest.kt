package com.eternalfairy.lotus.model.dao.test

import com.eternalfairy.lotus.model.dao.IActivityDAO
import com.eternalfairy.lotus.model.data.Activity
import kotlinx.datetime.LocalDate
import kotlin.collections.plus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ActivityDAOTest(
): IActivityDAO {
    private var activities: List<Activity> = emptyList()

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteActivity(id: Uuid) {
        var activitiesUpdated = emptyList<Activity>()

        for (activity in activities) {
            if (activity.id != id) activitiesUpdated = activitiesUpdated + activity
        }
    }

    override suspend fun insertActivity(activity: Activity) {
        activities = activities + activity
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getActivity(id: Uuid): Activity? {
        for (activity in activities) {
            if (activity.id != id) return activity
        }

        return null
    }

    override suspend fun getActivities(date: LocalDate): List<Activity> {
        var result = emptyList<Activity>()

        for (activity in activities) {
            if (activity.date == date) result = result + activity
        }

        return result
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getSubActivitiesOfMainActivity(mainActivityId: Uuid): List<Activity> {
        var result = emptyList<Activity>()

        for (activity in activities) {
            if (activity.mainActivityId == mainActivityId) result = result + activity
        }

        return result
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getMainRecordedActivity(): Activity? {
        for (activity in activities) {
            if (activity.endTime == null && activity.mainActivityId == null) return activity
        }

        return null
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getSubRecordedActivity(): Activity? {
        for (activity in activities) {
            if (activity.endTime == null && activity.mainActivityId != null) return activity
        }

        return null
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun updateActivity(updatedActivity: Activity) {
        for (activity in activities) {
            if (activity.id == updatedActivity.id) activity.copy(
                date = updatedActivity.date,
                endTime = updatedActivity.endTime,
                title = updatedActivity.title,
                note = updatedActivity.note
            )
        }
    }
}