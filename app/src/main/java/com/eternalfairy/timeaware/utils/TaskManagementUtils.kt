package com.eternalfairy.timeaware.utils

import com.eternalfairy.timeaware.data.Time
import com.eternalfairy.timeaware.data.Task
import com.eternalfairy.timeaware.utils.TouchGestureUtils.calculateTotalNumberOfMinutes

class Slot(
    val start: Time,
    val end: Time,
    val durationInMinutes: Int
)

object TaskManagementUtils {
    // Calculate how many and how big slots between tasks are available in given day
    fun calculateSlotsInDay(tasks: List<Task>, activeStartTime: Time, activeEndTime: Time, minDurationInMinutes: Int = 30): List<Slot> {
        var slots = emptyList<Slot>()
        var startTime = activeStartTime
        var endTime = activeStartTime
        var minutes = 0

        for (task in tasks) {
            if (task.startTime != null && task.endTime != null) {
                endTime = task.startTime!!

                minutes = calculateTotalNumberOfMinutes(
                    start = startTime,
                    end = endTime
                )

                if (minutes > minDurationInMinutes) {
                    val slot = Slot(
                        start = startTime,
                        end = endTime,
                        durationInMinutes = minutes
                    )

                    slots = slots + slot
                }

                startTime = task.endTime!!
                endTime = task.endTime!!
            }
        }

        return slots
    }
}