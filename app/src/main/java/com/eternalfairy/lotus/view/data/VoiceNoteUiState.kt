package com.eternalfairy.lotus.view.data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class VoiceNoteUiState @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid? = null,
    val uri: String? = null,
    val duration: Long = 0L,
    val recordedAt: Long? = null,
    val activityId: Uuid? = null,
    val lastPlayedPosition: Long = 0L
)