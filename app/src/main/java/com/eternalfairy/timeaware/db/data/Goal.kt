package com.eternalfairy.timeaware.db.data

import com.eternalfairy.timeaware.utils.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Goal (
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("user_id")
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID? = null,
    var title: String,
    var priority: Int?
)