package com.eternalfairy.timeaware.db.data

import com.eternalfairy.timeaware.utils.KOffsetDateTimeSerializer
import com.eternalfairy.timeaware.utils.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.UUID

@Serializable
data class Day(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    // The created_at variable is stored as a timestamp with time zone, which is basically a string in the "YYYY-MM-DD HH:mm:ss.SSSS+HH:mm" format, e.g. "1990-01-31 12:15:58.0000+02:00"
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("user_id")
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID? = null,
    @Serializable(with = KOffsetDateTimeSerializer::class)
    // The OffsetDateTime stores information about the time zone in relation to the UTC
    val date: OffsetDateTime,
    // The active_time_start variable is stored as a timestamptz
    @SerialName("active_time_start")
    val activeTimeStart: String,
    // The active_time_end variable is stored as a timestamptz
    @SerialName("active_time_end")
    val activeTimeEnd: String,
    // The actual_active_time_start variable is stored as a timestamptz
    @SerialName("actual_active_time_start")
    val actualActiveTimeStart: String?,
    // The actual_active_time_end variable is stored as a timestamptz
    @SerialName("actual_active_time_end")
    val actualActiveTimeEnd: String?,
    val note: String = ""
)