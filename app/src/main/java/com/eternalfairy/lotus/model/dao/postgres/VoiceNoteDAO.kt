package com.eternalfairy.lotus.model.dao.postgres

import com.eternalfairy.lotus.model.dao.IVoiceNoteDAO
import com.eternalfairy.lotus.model.data.VoiceNote
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class VoiceNoteDAO(
    private val api: VoiceNoteApi
): IVoiceNoteDAO {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteVoiceNote(id: Uuid) {
        return api.deleteVoiceNote(id)
    }

    override suspend fun insertVoiceNote(voiceNote: VoiceNote) {
        return api.insertVoiceNote(voiceNote)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getVoiceNote(id: Uuid): VoiceNote? {
        return api.getVoiceNote(id)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getVoiceNotesOfActivity(activityId: Uuid): List<VoiceNote> {
        return api.getVoiceNotesOfActivity(activityId)
    }

    override suspend fun updateVoiceNote(voiceNote: VoiceNote) {
        return api.updateVoiceNote(voiceNote)
    }
}