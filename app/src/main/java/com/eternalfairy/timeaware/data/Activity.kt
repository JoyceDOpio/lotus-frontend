package com.eternalfairy.timeaware.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "activities",
    indices = [Index(value = ["main_activity_id"])],
    // TODO: sub-activities must be deleted when the main activity is deleted
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["id"],
            childColumns = ["main_activity_id"],
            onDelete = ForeignKey.CASCADE,// Delete the sub-activities, if the main activity is deleted
            onUpdate = ForeignKey.NO_ACTION
        )
    ]
)
class Activity (
    val date: LocalDate,
    var title: String,
    var note: String = "",
    @ColumnInfo(name = "start_time")
    var startTime: Time,
    @ColumnInfo(name = "end_time")
    var endTime: Time?,
    @PrimaryKey
    val id: UUID,
    @ColumnInfo(name = "main_activity_id")
    val mainActivityId: UUID?
)