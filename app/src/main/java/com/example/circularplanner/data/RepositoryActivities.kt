package com.example.circularplanner.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
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

@Module
@InstallIn(ActivityComponent::class)
abstract class ActivitiesRepositoryModule {
    @Binds
    abstract fun bindActivitiesRepository(activitiesRepository: RepositoryActivities): IActivitiesRepository
}