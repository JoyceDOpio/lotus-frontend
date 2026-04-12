package com.eternalfairy.timeaware.ui.data

import java.util.UUID

data class UserProfileUiState (
    val id: UUID? = null,
    var firstName: String,
    var lastName: String,
    var email: String
)