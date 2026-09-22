package com.eternalfairy.lotus.data.dao

import com.eternalfairy.lotus.data.model.VoiceNote
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface IVoiceNoteDAO {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun deleteVoiceNote(id: Uuid)

    suspend fun insertVoiceNote(voiceNote: VoiceNote)

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getVoiceNote(id: Uuid): VoiceNote?

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getVoiceNotesOfActivity(activityId: Uuid): List<VoiceNote>

    suspend fun updateVoiceNote(voiceNote: VoiceNote): Unit
}