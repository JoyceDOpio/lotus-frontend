package com.eternalfairy.lotus.model.data

import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class Task @OptIn(ExperimentalUuidApi::class) constructor(
//    @Serializable(with = UUIDSerializer::class)
//    val id: UUID,
    val id: Uuid,
//    val id: UUID = UUID.randomUUID(),// PowerSync will not input the id for us
    // 2026-04-21 08:15:34.082732+00 - this is always stored at UTC time zone
//    @SerialName("created_at")
//    val createdAt: String? = null,
////    val createdAt: String,
//    @SerialName("user_id")
////    @Serializable(with = UUIDSerializer::class)
////    val userId: UUID? = null,
////    val userId: UUID,
//    val userId: String,
//    @Serializable(with = KOffsetDateTimeSerializer::class)
//    @Serializable(with = LocalDateSerializer::class)
//    val date: OffsetDateTime? = null,
    // 2026-04-22
    val date: LocalDate? = null,
//    val date: LocalDate? = null,
    @SerialName("start_time")
    // 10:14:00+02 - this is stored with the local time zone
    var startTime: kotlinx.datetime.LocalTime? = null,
    @SerialName("end_time")
    // 10:58:00+02
    var endTime: kotlinx.datetime.LocalTime? = null,
    var title: String,
    var description: String? = null,
    var priority: Int? = null,
    val pinned: Boolean = false
//    val pinned: Int = 0
) {

    fun compareTo(task: Task): Int {
        //TODO: handle case when task is null

        // return:
        // -1 if the task starts earlier than the task it is compared to
        // 0 the tasks have the same starting time
        // 1 if the task starts later than the task it is compared to
        if (this.startTime != null && task.startTime != null) {
//            val startTimeLocal = LocalTime.parse(startTime)
//            val endTimeLocal = LocalTime.parse(endTime)
//
//            return startTimeLocal.compareTo(endTimeLocal)
            return this.startTime!!.compareTo(task.startTime!!)
        }

        return 0
    }
}