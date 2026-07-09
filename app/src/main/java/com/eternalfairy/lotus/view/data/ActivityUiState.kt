package com.eternalfairy.lotus.view.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias VoiceNotesUiState = List<VoiceNoteUiState>
data class ActivityUiState @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid? = null,
    val date: LocalDate = LocalDate.parse(java.time.LocalDate.now().toString()),
    var title: String = "",
    var note: String = "",
    var startTime: LocalTime? = null,
    var endTime: LocalTime? = null,
    var voiceNotesUiState: VoiceNotesUiState = emptyList(),
    val mainActivityId: Uuid? = null,
//    var subActivitiesUiState: List<ActivityUiState> = emptyList()
)