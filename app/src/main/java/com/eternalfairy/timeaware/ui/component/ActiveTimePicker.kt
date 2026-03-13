package com.eternalfairy.timeaware.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

enum class ActiveTime {
    START, END
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTimePicker(
    timePicker: TimePickerState,
    activeTime: ActiveTime
) {
    Column () {
        var title = "Active Time"
        val value: String

        if (activeTime == ActiveTime.START){
            title += " Start"
        } else if (activeTime == ActiveTime.END) {
            title += " End"
        }

        value = "%d:%02d".format(timePicker.hour, timePicker.minute)

        Text (title, fontWeight = FontWeight.Bold)
        Text (value)
    }
}