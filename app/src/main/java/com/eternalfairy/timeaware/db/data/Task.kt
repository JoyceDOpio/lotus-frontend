package com.eternalfairy.timeaware.db.data

import com.eternalfairy.timeaware.utils.KOffsetDateTimeSerializer
import com.eternalfairy.timeaware.utils.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.UUID

@Serializable
data class Task (
    @Serializable(with = UUIDSerializer::class)
    val id: UUID? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("user_id")
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID? = null,
    var title: String,
    var description: String = "",
    @Serializable(with = KOffsetDateTimeSerializer::class)
    val date: OffsetDateTime? = null,
    @SerialName("start_time")
    var startTime: String? = null,
    @SerialName("end_time")
    var endTime: String? = null,
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
            val startTimeLocal = LocalTime.parse(startTime)
            val endTimeLocal = LocalTime.parse(endTime)

            return startTimeLocal.compareTo(endTimeLocal)
        }

        return 0
    }
}