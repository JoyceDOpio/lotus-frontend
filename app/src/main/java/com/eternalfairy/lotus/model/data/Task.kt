package com.eternalfairy.lotus.model.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Task @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid? = null,
    val date: LocalDate? = null,
    var title: String,
    var description: String? = null,
    @SerialName("start_time")
    var startTime: LocalTime? = null,
    @SerialName("end_time")
    var endTime: LocalTime? = null,
    var priority: Int? = null,
    val pinned: Boolean = false
) {

    fun compareTo(task: Task): Int {
        //TODO: handle case when task is null

        // return:
        // -1 if the task starts earlier than the task it is compared to
        // 0 the tasks have the same starting time
        // 1 if the task starts later than the task it is compared to
        if (this.startTime != null && task.startTime != null) {
            return this.startTime!!.compareTo(task.startTime!!)
        }

        return 0
    }
}