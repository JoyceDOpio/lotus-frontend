package com.eternalfairy.timeaware.data.supabase

import com.eternalfairy.timeaware.utils.KOffsetDateTimeSerializer
import com.eternalfairy.timeaware.utils.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.UUID

@Serializable
data class Day(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val createdAt: String,
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    @Serializable(with = KOffsetDateTimeSerializer::class)
    val date: OffsetDateTime,
    val activeTimeStart: String,
    val activeTimeEnd: String,
    val actualActiveTimeStart: String?,
    val actualActiveTimeEnd: String?,
    val note: String = ""
)