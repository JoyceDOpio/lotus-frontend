package com.eternalfairy.timeaware.ui.data

import java.util.UUID

data class GoalUiState (
    val id: UUID? = null,
    var title: String = "",
    var priority: Int? = null
)
