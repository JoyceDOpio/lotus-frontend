package com.example.circularplanner.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "goals")
data class Goal (
    var title: String,
    @ColumnInfo
    var priority: Int?,
    @PrimaryKey
    val id: UUID = UUID.randomUUID()
)