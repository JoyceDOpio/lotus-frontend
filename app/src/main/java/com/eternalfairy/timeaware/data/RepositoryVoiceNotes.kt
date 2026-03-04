package com.eternalfairy.timeaware.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RepositoryVoiceNotes(private val voiceNoteDao: DaoVoiceNote) : IVoiceNotesRepository {
    override fun getAllVoiceNotesPerActivity(activityId: UUID): Flow<List<VoiceNote>> {
        return voiceNoteDao.getAllVoiceNotes(activityId)
    }

    override fun getVoiceNote(id: UUID): Flow<VoiceNote> {
        return voiceNoteDao.getVoiceNote(id)
    }

    override suspend fun insertVoiceNotes(vararg voiceNotes: VoiceNote) {
        return voiceNoteDao.insert(*voiceNotes)
    }

    override suspend fun deleteVoiceNote(voiceNote: VoiceNote) {
        return voiceNoteDao.delete(voiceNote)
    }
}