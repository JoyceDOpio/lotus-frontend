package com.eternalfairy.lotus.model.data

import com.eternalfairy.lotus.model.utils.KOffsetDateTimeSerializer
import com.eternalfairy.lotus.model.utils.UUIDSerializer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Day @OptIn(ExperimentalUuidApi::class) constructor(
//    @Serializable(with = UUIDSerializer::class)
//    val id: UUID? = null,
    val id: Uuid,
    // The created_at variable is stored as a timestamp with time zone, which is basically a string in the "YYYY-MM-DD HH:mm:ss.SSSS+HH:mm" format, e.g. "1990-01-31 12:15:58.0000+02:00"
//    @SerialName("created_at")
//    val createdAt: String? = null,
//    @SerialName("user_id")
////    @Serializable(with = UUIDSerializer::class)
//    val userId: String,
//    @Serializable(with = KOffsetDateTimeSerializer::class)
    // The OffsetDateTime stores information about the time zone in relation to the UTC
//    val date: OffsetDateTime,
    // 2026-04-22
    val date: LocalDate,
    // 10:14:00+02 - this is stored with the local time zone
    @SerialName("active_time_start")
    val activeTimeStart: LocalTime,
    // 10:58:00+02 - this is stored with the local time zone
    @SerialName("active_time_end")
    val activeTimeEnd: LocalTime,
    // 10:14:00+02 - this is stored with the local time zone
    @SerialName("actual_active_time_start")
    val actualActiveTimeStart: LocalTime? = null,// TODO: Should the default value be null or the activeTimeStart?
    // 10:58:00+02 - this is stored with the local time zone
    @SerialName("actual_active_time_end")
    val actualActiveTimeEnd: LocalTime? = null,// TODO: Should the default value be null or the activeTimeEnd?
    val note: String = ""
)