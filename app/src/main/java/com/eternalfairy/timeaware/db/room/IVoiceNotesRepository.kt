package com.eternalfairy.timeaware.db.room

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IVoiceNotesRepository {
    fun getAllVoiceNotesPerActivity(activityId: UUID): Flow<List<VoiceNote>>

    fun getVoiceNote(id: UUID): Flow<VoiceNote>

    suspend fun insertVoiceNotes(vararg voiceNotes: VoiceNote)

    suspend fun deleteVoiceNote(voiceNote: VoiceNote)
}