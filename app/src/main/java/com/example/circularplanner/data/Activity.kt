package com.example.circularplanner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "activities")
class Activity (
    val date: LocalDate,
    var title: String,
    var note: String = "",
    @ColumnInfo(name = "start_time")
    var startTime: Time,
    @ColumnInfo(name = "end_time")
    var endTime: Time?,
    @PrimaryKey
    val id: UUID
)