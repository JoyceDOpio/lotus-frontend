package com.eternalfairy.lotus.model.dao

import com.eternalfairy.lotus.model.data.Activity
import com.eternalfairy.lotus.model.data.VoiceNote
import kotlinx.datetime.LocalDate
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