package com.example.circularplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.UserInput
import com.example.circularplanner.utils.TouchGestureUtils
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.floor

const val MINUTES_IN_HOUR = 60
const val ACTIVE_TIME_LABEL = 0xFF3D3061
const val ACTIVE_TIME_VALUE = 0xff656468

@Composable
fun ActiveTimeHeader (
    dayState: DayState,
    userInput: UserInput,
    modifier: Modifier = Modifier
) {
    val startTime = dayState.activeTimeStart
    val endTime = dayState.activeTimeEnd
    val today = LocalDate.now()

    fun calculateTimeIntervalInMinutes(start: Time?, end: Time?): Int {
        var minutes = 0

        if(start != null && end != null) {
            minutes = (end.hour - start.hour) * MINUTES_IN_HOUR
            minutes -= start.minute
            minutes += end.minute
        }

        return minutes
    }

    var minutesLeft = calculateTimeIntervalInMinutes(
        Time(
            LocalDateTime.now().hour,
            LocalDateTime.now().minute
        ),
        endTime
    )

    // Every minute update time left
    LaunchedEffect(true) {
        while (true) {
            delay(1000L * SECONDS_IN_MINUTE)
            minutesLeft = calculateTimeIntervalInMinutes(
                Time(
                    LocalDateTime.now().hour,
                    LocalDateTime.now().minute
                ),
                endTime
            )
        }
    }

    Column (
        modifier = modifier
            .padding(
                vertical = 10.dp,
                horizontal = 15.dp
            )
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text (
                text = "TOTAL ACTIVE TIME",//TODO: Read string from resource
                color = Color(ACTIVE_TIME_LABEL)
            )
            Text (
                text = TouchGestureUtils.formatTime(calculateTimeIntervalInMinutes(startTime, endTime)),
                color = Color(ACTIVE_TIME_VALUE)
            )
        }

        if (userInput.selectedDate == today) {
            Row(
                modifier = modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text (
                    text = "ACTIVE TIME LEFT",//TODO: Read string from resource
                    color = Color(ACTIVE_TIME_LABEL)
                )

                Text (
                    text = TouchGestureUtils.formatTime(if (minutesLeft > 0) minutesLeft else 0),
                    color = Color(ACTIVE_TIME_VALUE)
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(),
            thickness = 1.dp,
        )
    }
}