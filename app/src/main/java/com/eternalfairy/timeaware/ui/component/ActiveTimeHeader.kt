package com.eternalfairy.timeaware.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eternalfairy.timeaware.data.Time
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.TERTIARY_TEXT_COLOR
import com.eternalfairy.timeaware.ui.viewmodel.DayState
import com.eternalfairy.timeaware.ui.viewmodel.UserInput
import com.eternalfairy.timeaware.utils.TouchGestureUtils
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime

const val MINUTES_IN_HOUR = 60

@Composable
fun ActiveTimeHeader (
//    componentHeight: Dp = 90.dp,
    componentWidth: Dp = 400.dp,
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
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
            .width(componentWidth)
            .background(COMPONENT_BACKGROUND_COLOR)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp
                )
                .padding(
                    top = 10.dp
                )
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text (
                text = "TOTAL ACTIVE TIME",//TODO: Read string from resource
                color = HEADER_TEXT_COLOR
            )
            Text (
                text = TouchGestureUtils.formatTime(calculateTimeIntervalInMinutes(startTime, endTime)),
                color = TERTIARY_TEXT_COLOR
            )
        }

        if (userInput.selectedDate == today) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 10.dp
                    )
                    .padding(
                        bottom = 10.dp
                    )
                ,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text (
                    text = "ACTIVE TIME LEFT",//TODO: Read string from resource
                    color = HEADER_TEXT_COLOR
                )

                Text (
                    text = TouchGestureUtils.formatTime(if (minutesLeft > 0) minutesLeft else 0),
                    color = TERTIARY_TEXT_COLOR
                )
            }
        }
    }
}