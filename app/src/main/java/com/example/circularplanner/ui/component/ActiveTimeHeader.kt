package com.example.circularplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.UserInput
import java.time.LocalDate
import kotlin.math.floor

const val MINUTES_IN_HOUR = 60
const val ACTIVE_TIME_LABEL = 0xFF6650a4
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
    val tasks = dayState.tasks
    val activities = dayState.activities

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

        if (hours > 1) {
            return "$hours hours $minutes min"
        }

        if (hours == 0) {
            return "$minutes min"
        }

        return "$hours hour $minutes min"
    }

    Column (
        modifier = modifier
            .padding(
                vertical = 15.dp,
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
                text = formatTime(calculateTimeIntervalInMinutes(startTime, endTime)),
                color = Color(ACTIVE_TIME_VALUE)
            )
        }

        if (userInput.selectedDate == today) {
//            Row(
//                modifier = modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 15.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text (
//                    text = "ACTIVE TIME LEFT",//TODO: Read string from resource
//                    color = MaterialTheme.colorScheme.primary
//                )
//                // TODO: Calculate time left
//                Text (
//                    text = formatTime(calculateTimeIntervalInMinutes(startTime, endTime)),
//                color = Color.LightGray
//                )
//            }
        } else if (userInput.selectedDate.isBefore(today)) {
            Row(
                modifier = modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text (
                    text = "Tasks",//TODO: Read string from resource
//                    text = "TASKS",//TODO: Read string from resource
                    color = Color(ACTIVE_TIME_LABEL)
                )

                var totalTaskMinutes = 0
                for (task in tasks) {
                    totalTaskMinutes += calculateTimeIntervalInMinutes(task.startTime, task.endTime)
                }

                Text (
                    text = formatTime(totalTaskMinutes),
                    color = Color(ACTIVE_TIME_VALUE)
                )
            }

            Row(
                modifier = modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text (
                    text = "Activities",//TODO: Read string from resource
//                    text = "ACTIVITIES",//TODO: Read string from resource
                    color = Color(ACTIVE_TIME_LABEL)
                )

                var totalActivityMinutes = 0
                for (activity in activities) {
                    totalActivityMinutes += calculateTimeIntervalInMinutes(activity.startTime, activity.endTime)
                }

                Text (
                    text = formatTime(totalActivityMinutes),
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