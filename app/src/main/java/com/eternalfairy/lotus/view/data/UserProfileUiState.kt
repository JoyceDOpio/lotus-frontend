package com.eternalfairy.lotus.view.data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


data class UserProfileUiState @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid? = null,
    var name: String
)