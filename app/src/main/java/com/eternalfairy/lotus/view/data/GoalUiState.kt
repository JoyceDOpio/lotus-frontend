package com.eternalfairy.lotus.view.data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class GoalUiState @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid? = null,
    var title: String = "",
    var priority: Int = 1
)
