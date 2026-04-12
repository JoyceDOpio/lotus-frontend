package com.eternalfairy.timeaware.db.supabase

import com.eternalfairy.timeaware.db.supabase.ApiResponse
import com.eternalfairy.timeaware.db.data.VoiceNote
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import java.util.UUID
import javax.inject.Inject

class VoiceNoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    fun deleteVoiceNoteById(id: UUID): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("voice_notes").delete {
                    filter {
                        eq("id", id)
                    }
                }
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun insertVoiceNote(voiceNote: VoiceNote): Flow<ApiResponse<Unit>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                supabaseClient.from("voice_notes").insert(
                    voiceNote
                )
                emit(ApiResponse.Success(Unit))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectVoiceNotesPerActivity(activityId: UUID): Flow<ApiResponse<List<VoiceNote>>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val voiceNotes = supabaseClient.postgrest.rpc(
                    function = "get_voice_notes_of_activity",
                    parameters = buildJsonObject { //You can put here any serializable object including your own classes
                        put("activity_id", Json.encodeToJsonElement(activityId))
                    }
                ).decodeList<VoiceNote>()
                emit(ApiResponse.Success(voiceNotes))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }

    fun selectVoiceNoteById(id: UUID): Flow<ApiResponse<VoiceNote>> {
        return flow {
            emit(ApiResponse.Loading)
            try {
                val voiceNote = supabaseClient.from("public.voice_notes").select() {
                    filter {
                        VoiceNote::id eq id
                    }
                }.decodeSingle<VoiceNote>()
                emit(ApiResponse.Success(voiceNote))
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.message))
            }
        }
    }
}