package com.example.circularplanner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "tasks")
data class Task (
    val date: LocalDate,
    var title: String,
    @ColumnInfo(name = "start_time")
    var startTime: Time,
    @ColumnInfo(name = "end_time")
    var endTime: Time,
    var description: String = "",
    @PrimaryKey
    val id: UUID = UUID.randomUUID()
) {
    fun compareTo(task: Task): Int {
        //TODO: handle case when task is null

        // return:
        // -1 if the task starts earlier than the task it is compared to
        // 0 the tasks have the same starting time
        // 1 if the task starts later than the task it is compared to
        if (this.startTime.hour < task.startTime.hour) {
            return -1
        } else if (this.startTime.hour > task.startTime.hour) {
            return 1
        } else {
            if (this.startTime.minute < task.startTime.minute) {
                return -1
            } else if (this.startTime.minute > task.startTime.minute) {
                return 1
            } else {
                return 0
            }
        }
    }
}