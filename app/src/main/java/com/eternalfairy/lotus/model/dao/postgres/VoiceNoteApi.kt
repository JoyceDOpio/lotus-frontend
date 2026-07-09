package com.eternalfairy.lotus.model.dao.postgres

import com.eternalfairy.lotus.auth.InjectAuth
import com.eternalfairy.lotus.model.data.VoiceNote
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface VoiceNoteApi {
    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @DELETE("/voice-note/{id}")
    suspend fun deleteVoiceNote(@Path("id") id: Uuid)

    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @GET("/voice-note/{id}")
    suspend fun getVoiceNote(@Path("id") id: Uuid): VoiceNote?

    @InjectAuth
    @OptIn(ExperimentalUuidApi::class)
    @GET("/voice-note/activity/{id}")
    suspend fun getVoiceNotesOfActivity(@Path("id") id: Uuid): List<VoiceNote>

    @InjectAuth
    @POST("/voice-note")
    suspend fun insertVoiceNote(@Body voiceNote: VoiceNote)

    @InjectAuth
    @PUT("/voice-note")
    suspend fun updateVoiceNote(@Body voiceNote: VoiceNote)
}