package com.eternalfairy.lotus.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class VoiceNote @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid? = null,
    val uri: String,
    val duration: String,
    @SerialName("recorded_at")
    val recordedAt: String,
    @SerialName("activity_id")
    val activityId: Uuid
)