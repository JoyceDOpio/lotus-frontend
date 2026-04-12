package com.eternalfairy.timeaware.db.data

import com.eternalfairy.timeaware.utils.KOffsetDateTimeSerializer
import com.eternalfairy.timeaware.utils.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.UUID

@Serializable
data class Activity (
    @Serializable(with = UUIDSerializer::class)
    // This supposedly can be annotated as null because Supabase will automatically fill this value in
    val id: UUID? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("user_id")
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID? = null,
    @Serializable(with = KOffsetDateTimeSerializer::class)
    val date: OffsetDateTime,
    @SerialName("start_time")
    var startTime: String,
    @SerialName("end_time")
    var endTime: String?,
    var title: String,
    var note: String = "",
    @SerialName("main_activity_id")
    @Serializable(with = UUIDSerializer::class)
    val mainActivityId: UUID?,
//    val subActivities: List<Activity> = emptyList()
)