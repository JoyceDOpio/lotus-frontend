package com.eternalfairy.timeaware.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "voice_notes",
    indices = [Index(value = ["activity_id"])],
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["id"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ]
)
class VoiceNote (
    val uri: String,
    val duration: Long,
    val timestamp: Long,
    @ColumnInfo(name = "activity_id")
    val activityId: UUID,
    @PrimaryKey
    val id: UUID
)