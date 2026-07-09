package com.eternalfairy.lotus.view.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

//data class toDayState (
//    val activeTimeStart: Time = Time(6, 0),
//    val activeTimeEnd: Time = Time(22, 0),
//    val actualActiveTimeStart: Time? = null,
//    val actualActiveTimeEnd: Time? = null,
//    val isActiveTimeValid: Boolean = false,
//    val isActiveTimeSetUp: Boolean = false,
//    val isActivityDisplay: Boolean = false,
//    val note: String = ""
//)

data class DayUiState @OptIn(ExperimentalUuidApi::class) constructor(
//    val date: OffsetDateTime = OffsetDateTime.now(),
    val id: Uuid? = null,
    val date: LocalDate = LocalDate.parse(java.time.LocalDate.now().toString()),
    val activeTimeStart: LocalTime = LocalTime(6, 0),
    val activeTimeEnd: LocalTime = LocalTime(22, 0),
    val actualActiveTimeStart: LocalTime? = null,
    val actualActiveTimeEnd: LocalTime? = null,
//    val tasks: Tasks = emptyList(),
//    val activities: Activities = emptyList(),
//    val isActiveTimeValid: Boolean = false,
//    val isActiveTimeSetUp: Boolean = false,
//    val isActivityDisplay: Boolean = false,
//    val note: String = ""
)

// There is data I only want to read and there is data which I want to modify in response to user's actions (without saving this data into the database)
//typealias Tasks = List<Task>
//typealias Activities = List<Activity>
typealias Tasks = List<TaskUiState>
typealias Activities = List<ActivityUiState>
data class DayState (
    val date: LocalDate = LocalDate.parse(java.time.LocalDate.now().toString()),
    val activeTimeStart: LocalTime = LocalTime(6, 0),
    val activeTimeEnd: LocalTime = LocalTime(22, 0),
    val actualActiveTimeStart: LocalTime? = null,
    val actualActiveTimeEnd: LocalTime? = null,
    val tasks: Tasks = emptyList(),
    val activities: Activities = emptyList(),
    val note: String? = null
)