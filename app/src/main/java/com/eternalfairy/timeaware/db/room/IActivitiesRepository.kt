package com.eternalfairy.timeaware.db.room

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IActivitiesRepository {
    fun getMainActivitiesPerDayStream(date: String): Flow<List<Activity>>

    fun getActivityStream(id: UUID): Flow<Activity?>

    fun getMainRecordedActivity(): Flow<Activity?>

    fun getSubRecordedActivity(): Flow<Activity?>

    fun getSubActivitiesPerMainActivity(mainActivityId: UUID): Flow<List<Activity>>

    suspend fun insertActivity(activity: Activity, vararg voiceNotes: VoiceNote)

    suspend fun deleteActivity(activity: Activity)

    suspend fun updateActivity(activity: Activity): Int
}