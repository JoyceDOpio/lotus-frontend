package com.eternalfairy.timeaware.db.data

import com.eternalfairy.timeaware.utils.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserProfile (
    @Serializable(with = UUIDSerializer::class)
    // This supposedly can be annotated as null because Supabase will automatically fill this value in
    val id: UUID? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("first_name")
    var firstName: String,
    @SerialName("last_name")
    var lastName: String,
    var email: String
)