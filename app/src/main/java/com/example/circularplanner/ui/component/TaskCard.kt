package com.example.circularplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.data.Time
import com.example.circularplanner.utils.TouchGestureUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TaskCard (
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    date: LocalDate?,
    endTime: Time?,
    startTime: Time?,
    title: String,
    content: @Composable () -> Unit
) {
    val weekDayFormatter = DateTimeFormatter.ofPattern("EEEE")
    val dateFormatter = DateTimeFormatter.ofPattern("d. MMMM")

    val configuration = LocalConfiguration.current
    val isPopupWidth = configuration.screenWidthDp < 250

    Column (
        modifier = modifier
            .padding(horizontal = if (isPopupWidth) 10.dp else 30.dp)
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        // If the display width is that of a popup window, narrow down the layout
        if (isPopupWidth) {// TODO: Adjust layout for popup window width
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

            Column (
                modifier = modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
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

                if (startTime != null && endTime != null) {
                    Column (
                        modifier = modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        // Time range
                        val timeRangeText = "%d:%02d - %d:%02d".format(startTime.hour, startTime.minute, endTime.hour, endTime.minute)
                        Text(
                            text = timeRangeText,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )

                        // Time duration
                        val totalMinutes = TouchGestureUtils.calculateTotalNumberOfMinutes(startTime, endTime)
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
                        Text(
                            text = timeDurationText,
                            fontWeight = FontWeight.Thin,
                            fontSize = 18.sp
                        )
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

                if (startTime != null && endTime != null) {
                    Column (
                        modifier = modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        // Time range
                        val timeRangeText = "%d:%02d - %d:%02d".format(startTime.hour, startTime.minute, endTime.hour, endTime.minute)
                        Text(
                            text = timeRangeText,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )

                        // Time duration
                        val totalMinutes = TouchGestureUtils.calculateTotalNumberOfMinutes(startTime, endTime)
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
                        Text(
                            text = timeDurationText,
                            fontWeight = FontWeight.Thin,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        content()
    }
}