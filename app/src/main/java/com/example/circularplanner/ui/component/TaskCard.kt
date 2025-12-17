package com.example.circularplanner.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.utils.TouchGestureUtils
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class TaskCardDisplayType {
    Popup,
    Full
}

@Composable
fun TaskCard (
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    date: LocalDate?,
    dayState: DayState,
    endTime: Time?,
    startTime: Time?,
    title: String,
    displayType: TaskCardDisplayType = TaskCardDisplayType.Full,
    subActivities: List<ActivityUiState> = emptyList(),
    content: @Composable () -> Unit
) {
    val weekDayFormatter = DateTimeFormatter.ofPattern("EEEE")
    val dateFormatter = DateTimeFormatter.ofPattern("d. MMMM")
    // A value to use in case an activity has not been finished yet
    var endTimeValue = endTime

    if (endTimeValue == null) {
        endTimeValue = Time(LocalDateTime.now().hour, LocalDateTime.now().minute)
    }

    val activeTimeStart = dayState.actualActiveTimeStart ?: dayState.activeTimeStart
    val activeTimeEnd = dayState.actualActiveTimeEnd ?: dayState.activeTimeEnd

    // Time range
    val timeRangeText = if (startTime != null) "%d:%02d - %d:%02d".format(startTime.hour, startTime.minute, endTimeValue.hour, endTimeValue.minute) else ""

    // Time duration
    var totalMinutes = startTime?.let { TouchGestureUtils.calculateTotalNumberOfMinutes(startTime, endTimeValue) } ?: 0

    if (!subActivities.isEmpty()) {
        for (subActivity in subActivities) {
            val subActivityTotalMinutes = TouchGestureUtils.calculateTotalNumberOfMinutes(
                subActivity.startTime,
                subActivity.endTime ?: endTimeValue
            )
            totalMinutes -= subActivityTotalMinutes
        }
    }
    val hours = totalMinutes / TouchGestureUtils.MINUTES_IN_HOUR
    val minutes = totalMinutes % TouchGestureUtils.MINUTES_IN_HOUR
    val timeDurationText = if (hours == 0) {
        "%01d min".format(minutes)
    } else if (minutes == 0) {
        if (hours > 1) {
            "%01d hours".format(hours)
        } else {
            "%01d hour".format(hours)
        }
    } else {
        if (hours > 1) {
            "%01d hours %01d min".format(hours, minutes)
        } else {
            "%01d hour %01d min".format(hours, minutes)
        }
    }
    val totalActiveTimeMinutes = TouchGestureUtils.calculateTotalNumberOfMinutes(activeTimeStart, activeTimeEnd)
    val percentage = totalMinutes.toFloat() / totalActiveTimeMinutes.toFloat() * 100
    val percentageText = "%.2f%%".format(percentage)

    LaunchedEffect(true) {
        if (endTime == null) {
            // Update the clock every minute
            while (true) {
                delay(1000L * SECONDS_IN_MINUTE)
                endTimeValue = Time(LocalDateTime.now().hour, LocalDateTime.now().minute)
            }
        }
    }

    Column (
        modifier = modifier
            .padding(horizontal = if (displayType == TaskCardDisplayType.Popup) 20.dp else 30.dp)
            .padding(top = if (displayType == TaskCardDisplayType.Popup) 15.dp else 0.dp)
            .fillMaxWidth()
            .fillMaxHeight()
        ,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        // If the display width is that of a popup window, narrow down the layout
        if (displayType == TaskCardDisplayType.Popup) {
            Column () {
                // Title
                Row (
                    modifier = modifier
                        .padding(bottom = 5.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        modifier = modifier.weight(2f),
                        fontSize = 28.sp,
                        lineHeight = 32.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                if (date != null) {
                    Column (
                        modifier = modifier
                            .fillMaxWidth()
                    ) {
                        // Date
                        Text(
                            text = date.format(dateFormatter),
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )

                        Text(
                            text = date.format(weekDayFormatter),
                            fontWeight = FontWeight.Thin,
                            fontSize = 18.sp
                        )
                    }
                }

                if (startTime != null && endTime != null) {
                    Column (
                        modifier = modifier
                            .fillMaxWidth()
                    ) {
                        // Time range
                        Text(
                            text = timeRangeText,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )

                        // Time duration
                        Row (
                            modifier = modifier
                                .fillMaxWidth()
                            ,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = timeDurationText,
                                fontWeight = FontWeight.Thin,
                                fontSize = 18.sp
                            )

                            // Percentage
                            Text(
                                text = percentageText,
                                fontWeight = FontWeight.Light,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        // Else, display the content normally
        else {
            // Title
            Row (
                modifier = modifier
                    .padding(bottom = 5.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    modifier = modifier.weight(2f),
                    fontSize = 28.sp,
                    lineHeight = 32.sp
                )
            }

            Row (
                modifier = modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (date != null) {
                    Column (
                        modifier = modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        // Date
                        Text(
                            text = date.format(dateFormatter),
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )

                        Text(
                            text = date.format(weekDayFormatter),
                            fontWeight = FontWeight.Thin,
                            fontSize = 18.sp
                        )
                    }
                }

                if (startTime != null) {
                    Column (
                        modifier = modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        // Time range
                        Text(
                            text = timeRangeText,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )

                        Text(
                            text = timeDurationText,
                            fontWeight = FontWeight.Thin,
                            fontSize = 18.sp
                        )

                        // Percentage
                        Text(
                            text = percentageText,
                            fontWeight = FontWeight.Light,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        content()
    }
}