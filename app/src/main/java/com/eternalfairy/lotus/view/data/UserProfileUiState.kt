package com.eternalfairy.lotus.view.data

import java.util.UUID

data class UserProfileUiState (
    val id: UUID? = null,
    var firstName: String,
    var lastName: String,
    var email: String
)