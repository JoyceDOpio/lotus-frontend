package com.eternalfairy.timeaware.data

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RepositoryActivities @Inject constructor(private val activityDao: DaoActivity): IActivitiesRepository {
    override fun getMainActivitiesPerDayStream(date: String): Flow<List<Activity>> {
        return activityDao.getMainActivities(date)
    }

    override fun getActivityStream(id: UUID): Flow<Activity?> {
        return activityDao.getActivity(id)
    }

    override fun getMainRecordedActivity(): Flow<Activity?> {
        return activityDao.getMainRecordedActivity()
    }

    override fun getSubRecordedActivity(): Flow<Activity?> {
        return activityDao.getSubRecordedActivity()
    }

    override fun getSubActivitiesPerMainActivity(mainActivityId: UUID): Flow<List<Activity>> {
        return activityDao.getSubActivities(mainActivityId)
    }

    override suspend fun insertActivity(activity: Activity, vararg voiceNotes: VoiceNote) {
        return activityDao.saveActivity(activity, *voiceNotes)
    }

    override suspend fun deleteActivity(activity: Activity) {
        return activityDao.delete(activity)
    }

    override suspend fun updateActivity(activity: Activity): Int {
        return activityDao.update(activity)
    }
}