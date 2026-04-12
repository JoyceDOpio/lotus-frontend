package com.eternalfairy.timeaware.db.supabase

import com.eternalfairy.timeaware.db.data.VoiceNote
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class VoiceNotesRepository @Inject constructor(
    private val dataSource: VoiceNoteDataSource
) {
    fun deleteVoiceNote(id: UUID): Flow<ApiResponse<Unit>> {
        return dataSource.deleteVoiceNoteById(id)
    }

    fun getVoiceNotesPerActivityStream(activityId: UUID): Flow<ApiResponse<List<VoiceNote>>> {
        return dataSource.selectVoiceNotesPerActivity(activityId)
    }

    fun getVoiceNoteStream(id: UUID): Flow<ApiResponse<VoiceNote>> {
        return dataSource.selectVoiceNoteById(id)
    }

    fun insertVoiceNote(voiceNote: VoiceNote): Flow<ApiResponse<Unit>> {
        return dataSource.insertVoiceNote(voiceNote)
    }
}