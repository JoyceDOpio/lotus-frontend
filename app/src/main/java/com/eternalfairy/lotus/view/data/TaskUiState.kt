package com.eternalfairy.lotus.view.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class TaskUiState @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid? = null,
    val date: LocalDate? = null,
    var title: String = "",
    var description: String = "",
    var startTime: LocalTime? = null,
    var endTime: LocalTime? = null,
    var priority: Int? = null,
    val pinned: Boolean = false
) {
    fun compareTo(comparedTo: TaskUiState): Int {
        //TODO: handle case when task is null

        // return:
        // -1 if the task starts earlier than the task it is compared to
        // 0 the tasks have the same starting time
        // 1 if the task starts later than the task it is compared to
        if (this.startTime != null && comparedTo.startTime != null) {
            return this.startTime!!.compareTo(comparedTo.startTime!!)
        }

        return 0
    }
}