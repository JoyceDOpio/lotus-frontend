package com.eternalfairy.timeaware.db.powersync

import com.eternalfairy.timeaware.db.data.VoiceNote
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class VoiceNotesRepository @Inject constructor(
    private val dataSource: VoiceNoteDataSource
) {
    suspend fun deleteVoiceNote(id: UUID): Unit {
        return dataSource.deleteVoiceNoteById(id)
    }

    fun getVoiceNotesPerActivityStream(activityId: UUID): Flow<List<VoiceNote>> {
        return dataSource.selectVoiceNotesPerActivity(activityId)
    }

    fun getVoiceNoteStream(id: UUID): Flow<VoiceNote> {
        return dataSource.selectVoiceNoteById(id)
    }

    suspend fun insertVoiceNote(voiceNote: VoiceNote): Unit {
        return dataSource.insertVoiceNote(voiceNote)
    }
}