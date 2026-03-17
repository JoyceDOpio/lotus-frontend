package com.eternalfairy.timeaware.data.supabase

import com.eternalfairy.timeaware.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Goal (
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val createdAt: String,
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    var title: String,
    var priority: Int?
)