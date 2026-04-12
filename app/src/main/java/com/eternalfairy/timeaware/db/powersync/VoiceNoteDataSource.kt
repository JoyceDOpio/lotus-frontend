package com.eternalfairy.timeaware.db.powersync

import com.eternalfairy.timeaware.db.data.VoiceNote
import com.powersync.PowerSyncDatabase
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class VoiceNoteDataSource @Inject constructor(
    private val powerSyncDatabase: PowerSyncDatabase
) {
    suspend fun deleteVoiceNoteById(id: UUID): Unit {
        try {
            powerSyncDatabase.writeTransaction { transaction ->
                transaction.execute(
                    sql = "DELETE FROM voice_notes WHERE id = ?",
                    parameters = listOf(id)
                )
            }
        } catch (e: Exception) {
        }
    }

    suspend fun insertVoiceNote(voiceNote: VoiceNote): Unit {
        try {
            powerSyncDatabase.writeTransaction { transaction ->
                transaction.execute(
                    sql = "INSERT INTO voice_notes (created_at, user_id, uri, duration, timestamp, activity_id) VALUES (?, ?, ?, ?, ?, ?)",
                    parameters = listOf(
                        voiceNote.createdAt,
                        voiceNote.userId,
                        voiceNote.uri,
                        voiceNote.duration,
                        voiceNote.timestamp,
                        voiceNote.activityId
                    )
                )
            }
        } catch (e: Exception) {
        }
    }

    fun selectVoiceNotesPerActivity(activityId: UUID): Flow<List<VoiceNote>> {
        return flow {
            try {
                val voiceNotes = powerSyncDatabase.getAll(
                    sql = "SELECT * FROM voice_notes WHERE activity_id = ? ORDER BY timestamp ASC",
                    parameters = listOf(activityId)
                ) { cursor ->
                    VoiceNote(
                        id = UUID.fromString(cursor.getString("id")),
                        createdAt = cursor.getString("created_at"),
                        userId = UUID.fromString(cursor.getString("user_id")),
                        uri = cursor.getString("uri"),
                        duration = cursor.getString("duration"),
                        timestamp = cursor.getString("timestamp"),
                        activityId = UUID.fromString(cursor.getString("activity_id"))
                    )
                }
            } catch (e: Exception) {
            }
        }
    }

    fun selectVoiceNoteById(id: UUID): Flow<VoiceNote> {
        return flow {
            try {
                val voiceNote = powerSyncDatabase.get(
                    sql = "SELECT * FROM voice_notes WHERE id = ?",
                    parameters = listOf(id)
                ) { cursor ->
                    VoiceNote(
                        id = UUID.fromString(cursor.getString("id")),
                        createdAt = cursor.getString("created_at"),
                        userId = UUID.fromString(cursor.getString("user_id")),
                        uri = cursor.getString("uri"),
                        duration = cursor.getString("duration"),
                        timestamp = cursor.getString("timestamp"),
                        activityId = UUID.fromString(cursor.getString("activity_id"))
                    )
                }
                emit(voiceNote)
            } catch (e: Exception) {
            }
        }
    }
}