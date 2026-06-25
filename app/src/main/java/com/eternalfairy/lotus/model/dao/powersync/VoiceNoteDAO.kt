package com.eternalfairy.lotus.model.dao.powersync

import android.util.Log
import com.eternalfairy.lotus.model.data.VoiceNote
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceNoteDAO @Inject constructor(
    private val dataSource: OnlineSyncDataSource
) {
    suspend fun deleteVoiceNoteById(id: String) {
        try {
            dataSource.getDatabase().writeTransaction { transaction ->
                transaction.execute(
                    sql = "DELETE FROM voice_notes WHERE id = ?",
                    parameters = listOf(id)
                )
            }
        } catch (e: Exception) {
            Log.e("VoiceNoteDAO", e.printStackTrace().toString())
        }
    }

    suspend fun insertVoiceNote(voiceNote: VoiceNote) {
        try {
            dataSource.getDatabase().writeTransaction { transaction ->
                transaction.execute(
                    sql = "INSERT INTO voice_notes (created_at, user_id, uri, duration, timestamp, activity_id) VALUES (?, ?, ?, ?, ?, ?)",
                    parameters = listOf(
                        voiceNote.createdAt,
                        voiceNote.userId,
                        voiceNote.uri,
                        voiceNote.duration,
                        voiceNote.recordedAt,
                        voiceNote.activityId
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("VoiceNoteDAO", e.printStackTrace().toString())
        }
    }

    fun selectVoiceNotesPerActivity(activityId: String): Flow<List<VoiceNote>> {
        return flow {
            try {
                val voiceNotes = dataSource.getDatabase().getAll(
                    sql = "SELECT * FROM voice_notes WHERE activity_id = ? ORDER BY timestamp ASC",
                    parameters = listOf(activityId)
                ) { cursor ->
                    VoiceNote(
                        id = UUID.fromString(cursor.getString("id")),
                        createdAt = cursor.getString("created_at"),
                        userId = UUID.fromString(cursor.getString("user_id")),
                        uri = cursor.getString("uri"),
                        duration = cursor.getString("duration"),
                        recordedAt = cursor.getString("timestamp"),
                        activityId = UUID.fromString(cursor.getString("activity_id"))
                    )
                }
            } catch (e: Exception) {
                Log.e("VoiceNoteDAO", e.printStackTrace().toString())
            }
        }
    }

    fun selectVoiceNoteById(id: String): Flow<VoiceNote?> {
        return flow {
            try {
                val voiceNote = dataSource.getDatabase().getOptional(
                    sql = "SELECT * FROM voice_notes WHERE id = ?",
                    parameters = listOf(id)
                ) { cursor ->
                    VoiceNote(
                        id = UUID.fromString(cursor.getString("id")),
                        createdAt = cursor.getString("created_at"),
                        userId = UUID.fromString(cursor.getString("user_id")),
                        uri = cursor.getString("uri"),
                        duration = cursor.getString("duration"),
                        recordedAt = cursor.getString("timestamp"),
                        activityId = UUID.fromString(cursor.getString("activity_id"))
                    )
                }
                emit(voiceNote)
            } catch (e: Exception) {
                Log.e("VoiceNoteDAO", e.printStackTrace().toString())
            }
        }
    }
}