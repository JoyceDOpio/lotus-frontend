package com.eternalfairy.lotus.model.data

import com.eternalfairy.lotus.model.utils.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Goal @OptIn(ExperimentalUuidApi::class) constructor(
//    @Serializable(with = UUIDSerializer::class)
//    val id: UUID? = null,
    val id: Uuid,
//    @SerialName("created_at")
//    val createdAt: String? = null,
//    @SerialName("user_id")
////    @Serializable(with = UUIDSerializer::class)
//    val userId: String,
    var title: String,
    var priority: Int?
)