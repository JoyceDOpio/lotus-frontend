package com.eternalfairy.timeaware.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eternalfairy.timeaware.data.room.VoiceNote
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface DaoVoiceNote {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(vararg voiceNotes: VoiceNote)

    @Delete
    suspend fun delete(voiceNote: VoiceNote)

    // Return a list of VoiceNote entities as Flow. Room keeps this Flow updated for you, which means you only need to explicitly get the data once.
    @Query("SELECT * FROM voice_notes WHERE activity_id = :activityId ORDER BY timestamp ASC")
    fun getAllVoiceNotes(activityId: UUID): Flow<List<VoiceNote>>

    @Query("SELECT * FROM voice_notes WHERE id = :id")
    fun getVoiceNote(id: UUID): Flow<VoiceNote>
//    fun getVoiceNote(id: UUID): Flow<VoiceNote?>
}