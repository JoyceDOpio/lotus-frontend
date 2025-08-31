package com.example.circularplanner.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface DaoActivity {
    @Transaction
    suspend fun saveActivity(activity: Activity, vararg voiceNotes:VoiceNote) {
        insertActivity(activity)
        insertVoiceNotes(*voiceNotes)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceNotes(vararg voiceNotes: VoiceNote)

    @Update
    suspend fun update(activity: Activity): Int

    @Delete
    suspend fun delete(activity: Activity)

    @Query("SELECT * FROM activities WHERE id = :id")
    fun getActivity(id: UUID): Flow<Activity>

    @Query("SELECT * FROM activities WHERE date = :date ORDER BY start_time ASC")
    fun getAllActivities(date: String): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE end_time IS NULL")
    fun getRecordedActivity(): Flow<Activity>
}