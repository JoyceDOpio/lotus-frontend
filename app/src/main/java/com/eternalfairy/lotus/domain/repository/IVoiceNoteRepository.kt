package com.eternalfairy.lotus.domain.repository

import com.eternalfairy.lotus.data.dao.postgres.ApiResponse
import com.eternalfairy.lotus.data.model.VoiceNote
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface IVoiceNoteRepository {
    suspend fun addVoiceNote(voiceNote: VoiceNote): ApiResponse<Unit>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun deleteVoiceNote(id: Uuid): ApiResponse<Unit>

    suspend fun editVoiceNote(voiceNote: VoiceNote): ApiResponse<Unit>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getVoiceNote(id: Uuid): ApiResponse<VoiceNote>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getVoiceNotesOfActivity(activityId: Uuid): ApiResponse<List<VoiceNote>>
}