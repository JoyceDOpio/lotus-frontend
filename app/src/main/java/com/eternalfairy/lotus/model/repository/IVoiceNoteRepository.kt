package com.eternalfairy.lotus.model.repository

import com.eternalfairy.lotus.model.dao.postgres.ApiResponse
import com.eternalfairy.lotus.model.data.VoiceNote
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface IVoiceNoteRepository {
    suspend fun addVoiceNote(voiceNote: VoiceNote): ApiResponse<Unit>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun deleteVoiceNote(id: Uuid): ApiResponse<Unit>

    suspend fun editVoiceNote(voiceNote: VoiceNote): ApiResponse<Unit>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getVoiceNote(id: Uuid): ApiResponse<VoiceNote>
}