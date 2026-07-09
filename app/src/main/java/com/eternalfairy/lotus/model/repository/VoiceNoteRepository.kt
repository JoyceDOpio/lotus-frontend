package com.eternalfairy.lotus.model.repository

import com.eternalfairy.lotus.model.dao.IVoiceNoteDAO
import com.eternalfairy.lotus.model.dao.postgres.ApiResponse
import com.eternalfairy.lotus.model.data.VoiceNote
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Singleton
class VoiceNoteRepository @Inject constructor(
    private val dao: IVoiceNoteDAO
): IVoiceNoteRepository {
    override suspend fun addVoiceNote(voiceNote: VoiceNote): ApiResponse<Unit> {
        return try {
            dao.insertVoiceNote(voiceNote)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteVoiceNote(id: Uuid): ApiResponse<Unit> {
        return try {
            dao.deleteVoiceNote(id)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun editVoiceNote(voiceNote: VoiceNote): ApiResponse<Unit> {
        return try {
            dao.updateVoiceNote(voiceNote)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getVoiceNote(id: Uuid): ApiResponse<VoiceNote> {
        return try {
            val voiceNote = dao.getVoiceNote(id)

            ApiResponse.Success(voiceNote)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getVoiceNotesOfActivity(activityId: Uuid): ApiResponse<List<VoiceNote>> {
        return try {
            val voiceNotes = dao.getVoiceNotesOfActivity(activityId)

            ApiResponse.Success(voiceNotes)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }
}