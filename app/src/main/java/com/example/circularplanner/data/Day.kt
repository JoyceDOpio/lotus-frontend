package com.example.circularplanner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "days")
data class Day(
    @PrimaryKey
    val date: LocalDate,
    @ColumnInfo(name = "active_time_start")
    val activeTimeStart: Time,
    @ColumnInfo(name = "active_time_end")
    val activeTimeEnd: Time,
    @ColumnInfo(name = "actual_active_time_start")
    val actualActiveTimeStart: Time?,
    @ColumnInfo(name = "actual_active_time_end")
    val actualActiveTimeEnd: Time?,
    val note: String = ""
)