package com.eternalfairy.lotus.view.data

import com.eternalfairy.lotus.model.data.Time
import java.time.LocalDate

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

data class DayUiState (
//    val date: OffsetDateTime = OffsetDateTime.now(),
    val date: LocalDate = LocalDate.now(),
    val activeTimeStart: Time = Time(6, 0),
    val activeTimeEnd: Time = Time(22, 0),
    val actualActiveTimeStart: Time? = null,
    val actualActiveTimeEnd: Time? = null,
    val tasks: Tasks = emptyList(),
    val activities: Activities = emptyList(),
    val isActiveTimeValid: Boolean = false,
    val isActiveTimeSetUp: Boolean = false,
    val isActivityDisplay: Boolean = false,
    val note: String = ""
)

// There is data I only want to read and there is data which I want to modify in response to user's actions (without saving this data into the database)
//typealias Tasks = List<Task>
//typealias Activities = List<Activity>
typealias Tasks = List<TaskUiState>
typealias Activities = List<ActivityUiState>
data class DayState (
    val date: LocalDate = LocalDate.now(),
    val activeTimeStart: String = Time(6, 0).parseToTimestampTz(LocalDate.now()),
    val activeTimeEnd: String = Time(22, 0).parseToTimestampTz(LocalDate.now()),
    val actualActiveTimeStart: String? = null,
    val actualActiveTimeEnd: String? = null,
//    val tasks: Tasks = emptyList(),
//    val activities: Activities = emptyList(),
    val tasks: Tasks = emptyList(),
    val activities: Activities = emptyList(),
    val note: String = ""
)