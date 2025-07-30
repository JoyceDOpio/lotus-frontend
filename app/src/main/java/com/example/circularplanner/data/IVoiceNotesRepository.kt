package com.example.circularplanner.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IVoiceNotesRepository {
    fun getAllVoiceNotesPerActivity(activityId: UUID): Flow<List<VoiceNote>>

    suspend fun insertVoiceNotes(vararg voiceNotes: VoiceNote)

    suspend fun deleteVoiceNote(voiceNote: VoiceNote)
}