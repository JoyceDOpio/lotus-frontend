package com.eternalfairy.lotus.model.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Day @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid? = null,
    val date: LocalDate,
    @SerialName("active_time_start")
    val activeTimeStart: LocalTime,
    @SerialName("active_time_end")
    val activeTimeEnd: LocalTime,
    @SerialName("actual_active_time_start")
    val actualActiveTimeStart: LocalTime? = null,// TODO: Should the default value be null or the activeTimeStart?
    @SerialName("actual_active_time_end")
    val actualActiveTimeEnd: LocalTime? = null,// TODO: Should the default value be null or the activeTimeEnd?
    val note: String = ""
)