package com.eternalfairy.timeaware.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.eternalfairy.timeaware.data.room.VoiceNote
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface DaoActivity {
    @Transaction
    suspend fun saveActivity(activity: Activity, vararg voiceNotes: VoiceNote) {
        insertActivity(activity)
        insertVoiceNotes(*voiceNotes)
    }

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertActivity(activity: Activity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertVoiceNotes(vararg voiceNotes: VoiceNote)

    @Update
    suspend fun update(activity: Activity): Int

    @Delete
    suspend fun delete(activity: Activity)

    @Query("SELECT * FROM activities WHERE id = :id")
//    fun getActivity(id: UUID): Flow<Activity>
    fun getActivity(id: UUID): Flow<Activity?>

//    @Query("SELECT * FROM activities WHERE id = :id")
//    fun getActivity(id: UUID): Flow<ActivityUiState>

    @Query("SELECT * FROM activities WHERE date = :date AND main_activity_id IS NULL ORDER BY start_time ASC")
    fun getMainActivities(date: String): Flow<List<Activity>>

    @Query("SELECT * FROM activities WHERE end_time IS NULL AND main_activity_id IS NULL")
//    fun getMainRecordedActivity(): Flow<Activity>
    fun getMainRecordedActivity(): Flow<Activity?>

    @Query("SELECT * FROM activities WHERE end_time IS NULL AND main_activity_id IS NOT NULL")
//    fun getSubRecordedActivity(): Flow<Activity>
    fun getSubRecordedActivity(): Flow<Activity?>

    @Query("SELECT * FROM activities WHERE main_activity_id = :mainActivityId")
    fun getSubActivities(mainActivityId: UUID): Flow<List<Activity>>
}