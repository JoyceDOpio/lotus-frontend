package com.eternalfairy.timeaware.db.data

import com.eternalfairy.timeaware.utils.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class VoiceNote (
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("user_id")
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID? = null,
    val uri: String,
    val duration: String,
    val timestamp: String,
    @SerialName("activity_id")
    @Serializable(with = UUIDSerializer::class)
    val activityId: UUID
)