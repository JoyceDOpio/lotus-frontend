package com.example.circularplanner.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RepositoryActivities(private val activityDao: DaoActivity): IActivitiesRepository {
    override fun getAllActivitiesPerDayStream(date: String): Flow<List<Activity>> {
        return activityDao.getAllActivities(date)
    }

    override fun getActivityStream(id: UUID): Flow<Activity?> {
        return activityDao.getActivity(id)
    }

    override fun getRecordedActivity(): Flow<Activity?> {
        return activityDao.getRecordedActivity()
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