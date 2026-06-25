package com.eternalfairy.lotus.view.data

import java.util.UUID

data class GoalUiState (
    val id: UUID? = null,
    var title: String = "",
    var priority: Int? = null
)
