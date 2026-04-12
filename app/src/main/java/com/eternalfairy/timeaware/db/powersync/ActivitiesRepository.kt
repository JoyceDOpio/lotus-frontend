package com.eternalfairy.timeaware.db.powersync

import com.eternalfairy.timeaware.db.data.Activity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ActivitiesRepository @Inject constructor(
    private val dataSource: ActivityDataSource
) {
    suspend fun deleteActivity(id: UUID): Unit {
        return dataSource.deleteActivityById(id)
    }

    suspend fun insertActivity(activity: Activity): Unit {
        return dataSource.insertActivity(activity)
    }

    fun getActivityStream(id: UUID): Flow<Activity> {
        return dataSource.selectActivityById(id)
    }

    fun getMainRecordedActivityStream(): Flow<Activity> {
        return dataSource.selectMainRecordedActivity()
    }

    suspend fun getMainActivitiesPerDayStream(date: String): Flow<List<Activity>> {
        return dataSource.selectMainActivitiesPerDay(date)
    }

    fun getSubActivitiesPerMainActivityStream(mainActivityId: UUID): Flow<List<Activity>> {
        return dataSource.selectSubActivitiesPerMainActivity(mainActivityId)
    }

    fun getSubRecordedActivityStream(): Flow<Activity> {
        return dataSource.selectSubRecordedActivity()
    }

    suspend fun updateActivity(activity: Activity): Unit {
        return dataSource.updateActivity(activity)
    }
}