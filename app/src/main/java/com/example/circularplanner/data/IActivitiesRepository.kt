package com.example.circularplanner.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IActivitiesRepository {
    fun getAllActivitiesPerDayStream(date: String): Flow<List<Activity>>

    fun getActivityStream(id: UUID): Flow<Activity?>

    suspend fun insertActivity(activity: Activity, vararg voiceNotes: VoiceNote)

    suspend fun deleteActivity(activity: Activity)

    suspend fun updateActivity(activity: Activity): Int
}