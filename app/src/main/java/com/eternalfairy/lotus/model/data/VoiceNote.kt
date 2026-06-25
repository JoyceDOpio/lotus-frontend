package com.eternalfairy.lotus.model.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class VoiceNote @OptIn(ExperimentalUuidApi::class) constructor(
//    @Serializable(with = UUIDSerializer::class)
    val id: Uuid,
//    @SerialName("created_at")
//    val createdAt: String? = null,
//    @SerialName("user_id")
////    @Serializable(with = UUIDSerializer::class)
//    val userId: String,
    val uri: String,
    val duration: String,
    @SerialName("recorded_at")
    val recordedAt: String,
    @SerialName("activity_id")
//    @Serializable(with = UUIDSerializer::class)
    val activityId: Uuid
)