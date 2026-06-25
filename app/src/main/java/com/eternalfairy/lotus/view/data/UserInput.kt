package com.eternalfairy.lotus.view.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class UserInput(
//    val selectedDate: OffsetDateTime = OffsetDateTime.now()
    val selectedDate: LocalDate = LocalDate.now()
) : Parcelable