package com.eternalfairy.lotus.view.data

import com.eternalfairy.lotus.model.data.Time
import java.time.LocalDate
import java.util.UUID

data class TaskUiState (
//    val date: OffsetDateTime? = null,
    val id: UUID? = null,
    val createdAt: String? = null,
    val userId: UUID? = null,
    val date: LocalDate? = null,
    var startTime: Time? = null,
    var endTime: Time? = null,
    var title: String = "",
    var description: String? = null,
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
            return if (this.startTime!!.hour < comparedTo.startTime!!.hour) {
                -1
            } else if (this.startTime!!.hour > comparedTo.startTime!!.hour) {
                1
            } else {
                if (this.startTime!!.minute < comparedTo.startTime!!.minute) {
                    -1
                } else if (this.startTime!!.minute > comparedTo.startTime!!.minute) {
                    1
                } else {
                    0
                }
            }
        }

        return 0
    }
}