package com.eternalfairy.lotus.model.data

import com.eternalfairy.lotus.model.utils.KOffsetDateTimeSerializer
import com.eternalfairy.lotus.model.utils.UUIDSerializer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Activity @OptIn(ExperimentalUuidApi::class) constructor(
//    @Serializable(with = UUIDSerializer::class)
    // This supposedly can be annotated as null because Supabase will automatically fill this value in
//    val id: UUID? = null,
    val id: Uuid,
//    @SerialName("created_at")
//    val createdAt: String? = null,
//    @SerialName("user_id")
////    @Serializable(with = UUIDSerializer::class)
////    val userId: UUID? = null,
//    val userId: String,
//    @Serializable(with = KOffsetDateTimeSerializer::class)
//    val date: OffsetDateTime,
    val date: LocalDate,
    @SerialName("start_time")
    var startTime: LocalTime,
    @SerialName("end_time")
    var endTime: LocalTime?,
    var title: String,
    var note: String = "",
    @SerialName("main_activity_id")
//    @Serializable(with = UUIDSerializer::class)
//    val mainActivityId: UUID?,
    val mainActivityId: Uuid? = null,
//    val subActivities: List<Activity> = emptyList()
)