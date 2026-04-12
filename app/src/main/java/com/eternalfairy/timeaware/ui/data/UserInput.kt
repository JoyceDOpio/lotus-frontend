package com.eternalfairy.timeaware.ui.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.OffsetDateTime

@Parcelize
data class UserInput(
//    val selectedDate: OffsetDateTime = OffsetDateTime.now()
    val selectedDate: LocalDate = LocalDate.now()
) : Parcelable