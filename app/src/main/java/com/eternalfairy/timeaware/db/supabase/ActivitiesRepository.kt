package com.eternalfairy.timeaware.db.supabase

import com.eternalfairy.timeaware.db.data.Activity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ActivitiesRepository @Inject constructor(
    private val dataSource: ActivityDataSource
) {
    fun deleteActivity(id: UUID): Flow<ApiResponse<Unit>> {
        return dataSource.deleteActivityById(id)
    }

    fun insertActivity(activity: Activity): Flow<ApiResponse<Unit>> {
        return dataSource.insertActivity(activity)
    }

    fun getActivityStream(id: UUID): Flow<ApiResponse<Activity>> {
        return dataSource.selectActivityById(id)
    }

    fun getMainRecordedActivityStream(): Flow<ApiResponse<Activity>> {
        return dataSource.selectMainRecordedActivity()
    }

    fun getMainActivitiesPerDayStream(date: String): Flow<ApiResponse<List<Activity>>> {
        return dataSource.selectMainActivitiesPerDay(date)
    }

    fun getSubActivitiesPerMainActivityStream(mainActivityId: UUID): Flow<ApiResponse<List<Activity>>> {
        return dataSource.selectSubActivitiesPerMainActivity(mainActivityId)
    }

    fun getSubRecordedActivityStream(): Flow<ApiResponse<Activity>> {
        return dataSource.selectSubRecordedActivity()
    }

    fun updateActivity(activity: Activity): Flow<ApiResponse<Unit>> {
        return dataSource.updateActivity(activity)
    }
}