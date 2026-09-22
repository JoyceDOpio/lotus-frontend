package com.eternalfairy.lotus.data.model

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class UserProfile @OptIn(ExperimentalUuidApi::class) constructor(
    val id: Uuid? = null,
    var name: String
)