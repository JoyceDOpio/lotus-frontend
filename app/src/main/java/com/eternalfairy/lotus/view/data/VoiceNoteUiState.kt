package com.eternalfairy.lotus.view.data

import java.util.UUID

data class VoiceNoteUiState (
    val id: UUID? = null,
    val uri: String = "",
    val duration: Long = 0L,
    val recordedAt: Long? = null,
    val activityId: UUID? = null,
    val lastPlayedPosition: Long = 0L
)