package com.eternalfairy.lotus.data.test

import android.util.Log
import com.eternalfairy.lotus.data.dao.postgres.ApiResponse
import com.eternalfairy.lotus.data.model.Activity
import com.eternalfairy.lotus.domain.repository.IActivityRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.Uuid

class MockActivityRepository: IActivityRepository {
    var mockActivities = mutableListOf(
        Activity(
            id = Uuid.Companion.parse("495502d1-4f23-4c31-8edb-4ec1107f35be"),
            date = LocalDate.Companion.parse(java.time.LocalDate.now().plusDays(-1).toString()),
            title = "predefined activity",
            note = "predefined note",
            startTime = LocalTime(8, 10),
            endTime = LocalTime(9, 55),
            mainActivityId = null
        ),
        Activity(
            id = Uuid.Companion.parse("5f6c53ca-b5d7-4aec-89af-4f725bc8c50f"),
            date = LocalDate.Companion.parse(java.time.LocalDate.now().plusDays(-1).toString()),
            title = "predefined activity 2",
            note = "predefined note",
            startTime = LocalTime(11, 12),
            endTime = LocalTime(12, 47),
            mainActivityId = null
        ),
        Activity(
            id = Uuid.Companion.parse("494623db-021f-4c72-ae05-3a78f7a077f9"),
            date = LocalDate.Companion.parse(java.time.LocalDate.now().plusDays(-1).toString()),
            title = "predefined sub-activity",
            note = "predefined note",
            startTime = LocalTime(8, 46),
            endTime = LocalTime(9, 13),
            mainActivityId = Uuid.Companion.parse("495502d1-4f23-4c31-8edb-4ec1107f35be")
        )
    )

    override suspend fun addActivity(activity: Activity): ApiResponse<Unit> {
        return try {
            mockActivities += activity
            Log.i("MockActivityRepository", "addActivity() mockActivities: $mockActivities")

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun deleteActivity(id: Uuid): ApiResponse<Unit> {
        return try {
            for (activity in mockActivities) {
                if (activity.mainActivityId == id) {
                    mockActivities.remove(activity)
                }
            }
            for (activity in mockActivities) {
                if (activity.id == id) {
                    mockActivities.remove(activity)
                }
                break
            }
            Log.i("MockActivitiesRepository", "deleteActivity() mockActivities: $mockActivities")

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun editActivity(activity: Activity): ApiResponse<Unit> {
        return try {
            Log.i("MockActivityRepository", "editActivity() activity: $activity")

            for (activityToBeEdited in mockActivities) {
                if (activityToBeEdited.id == activity.id) {
                    mockActivities.remove(activityToBeEdited)
                    mockActivities.add(activity)
                    break
                }
            }// fixme: chyba zacina się w tej pętli
            Log.i("MockActivityRepository", "editActivity() mockActivities: $mockActivities")

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getActivity(id: Uuid): ApiResponse<Activity> {
        return try {
            val activity = mockActivities.find { t -> t.id == id }

            ApiResponse.Success(activity)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getRecordedActivityMain(): ApiResponse<Activity> {
        return try {
            val activity = mockActivities.find { a -> a.endTime == null && a.mainActivityId == null }

            ApiResponse.Success(activity)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getMainActivitiesPerDay(date: LocalDate): ApiResponse<List<Activity>> {
        return try {
            var activities = emptyList<Activity>()
            mockActivities.forEach { activity ->
                if (activity.date == date && activity.mainActivityId == null) activities += activity
            }

            ApiResponse.Success(activities)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getSubActivitiesPerMainActivity(mainActivityId: Uuid): ApiResponse<List<Activity>> {
        Log.i("MockActivityRepository", "getSubActivitiesPerMainActivity() mockActivities: $mockActivities")

        return try {
            var activities = emptyList<Activity>()
            mockActivities.forEach { activity ->
                if (activity.mainActivityId == mainActivityId) activities += activity
            }

            ApiResponse.Success(activities)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getRecordedActivitySub(): ApiResponse<Activity> {
        return try {
            val activity = mockActivities.find { a -> a.endTime == null && a.mainActivityId != null }

            ApiResponse.Success(activity)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }
}