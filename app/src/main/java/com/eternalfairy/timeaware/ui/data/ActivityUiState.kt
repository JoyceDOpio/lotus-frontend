package com.eternalfairy.timeaware.ui.data

import com.eternalfairy.timeaware.db.Time
import java.time.LocalDate
import java.util.UUID

typealias VoiceNotesUiState = List<VoiceNoteUiState>
data class ActivityUiState (
//    val date: OffsetDateTime = OffsetDateTime.now(),
    val date: LocalDate = LocalDate.now(),
    val id: UUID? = null,
    var title: String = "",
    var note: String = "",
    var startTime: Time = Time(0, 0),// Default value: start of the day//TODO: Maybe I should change the default value to null
    var endTime: Time? = null,
    var voiceNotesUiState: VoiceNotesUiState = emptyList(),
    val mainActivityId: UUID? = null,
    var subActivitiesUiState: List<ActivityUiState> = emptyList()
)