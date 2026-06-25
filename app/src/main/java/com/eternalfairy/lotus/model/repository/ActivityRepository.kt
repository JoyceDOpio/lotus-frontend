package com.eternalfairy.lotus.model.repository

import com.eternalfairy.lotus.model.dao.IActivityDAO
import com.eternalfairy.lotus.model.dao.postgres.ApiResponse
import com.eternalfairy.lotus.model.data.Activity
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Singleton
class ActivityRepository @Inject constructor(
    private val dao: IActivityDAO
): IActivityRepository {
    override suspend fun addActivity(activity: Activity): ApiResponse<Unit> {
        return try {
            dao.insertActivity(activity)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteActivity(id: Uuid): ApiResponse<Unit> {
        return try {
            dao.deleteActivity(id)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun editActivity(activity: Activity): ApiResponse<Unit> {
        return try {
            dao.updateActivity(activity)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getActivity(id: Uuid): ApiResponse<Activity> {
        return try {
            val activity = dao.getActivity(id)

            ApiResponse.Success(activity)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getMainRecordedActivity(): ApiResponse<Activity> {
        return try {
            val activity = dao.getMainRecordedActivity()

            ApiResponse.Success(activity)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getMainActivitiesPerDay(date: LocalDate): ApiResponse<List<Activity>> {
        return try {
            val activities = dao.getActivities(date)

            ApiResponse.Success(activities)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getSubActivitiesPerMainActivity(mainActivityId: Uuid): ApiResponse<List<Activity>> {
        return try {
            val activities = dao.getSubActivitiesOfMainActivity(mainActivityId)

            ApiResponse.Success(activities)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getSubRecordedActivity(): ApiResponse<Activity> {
        return try {
            val activity = dao.getSubRecordedActivity()

            ApiResponse.Success(activity)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }
}