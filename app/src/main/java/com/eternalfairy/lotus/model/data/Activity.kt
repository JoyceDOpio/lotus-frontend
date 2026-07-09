package com.eternalfairy.lotus.model.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Activity @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid? = null,
    val date: LocalDate,
    @SerialName("start_time")
    var startTime: LocalTime,
    @SerialName("end_time")
    var endTime: LocalTime?,
    var title: String,
    var note: String? = null,
    @SerialName("main_activity_id")
    val mainActivityId: Uuid? = null
)