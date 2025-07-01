package com.example.circularplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.viewmodel.DayState
import kotlin.math.floor

const val MINUTES_IN_HOUR = 60

@Composable
fun ActiveTimeHeader (
    dayState: DayState,
    modifier: Modifier = Modifier
) {
    val startTime = dayState.activeTimeStart
    val endTime = dayState.activeTimeEnd

    fun calculateTimeIntervalInMinutes(start: Time?, end: Time?): Int {
        var minutes = 0

        if(start != null && end != null) {
            minutes = (end.hour - start.hour) * MINUTES_IN_HOUR
            minutes -= start.minute
            minutes += end.minute
        }

        return minutes
    }

    fun formatTime(minutesTotal: Int): String {
        var hours = minutesTotal / MINUTES_IN_HOUR
        var minutes = 0

        if (minutesTotal % MINUTES_IN_HOUR != 0) {
            hours = floor(hours.toDouble()).toInt()
            minutes = minutesTotal - hours * MINUTES_IN_HOUR
        }

        return "$hours hr $minutes min"
    }

    Column (
        modifier = modifier
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text (text = "Active Time Total: ${formatTime(calculateTimeIntervalInMinutes(startTime, endTime))}")
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text (text = "Active Time Left: ")
        }

        HorizontalDivider(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth(),
            thickness = 1.dp,
        )
    }
}