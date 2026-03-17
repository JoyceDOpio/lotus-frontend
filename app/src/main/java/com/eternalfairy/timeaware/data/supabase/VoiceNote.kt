package com.eternalfairy.timeaware.data.supabase

import com.eternalfairy.timeaware.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class VoiceNote (
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val createdAt: String,
    val uri: String,
    val duration: String,
    val timestamp: String,
    @Serializable(with = UUIDSerializer::class)
    val activityId: UUID
)