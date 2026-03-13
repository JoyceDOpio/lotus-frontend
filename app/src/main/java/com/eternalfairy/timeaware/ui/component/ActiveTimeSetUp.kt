package com.eternalfairy.timeaware.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.timeaware.R
import com.eternalfairy.timeaware.data.Time
import com.eternalfairy.timeaware.ui.theme.BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.ERROR_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.SECONDARY_HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.SECONDARY_TEXT_COLOR
import com.eternalfairy.timeaware.ui.viewmodel.DayState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTimeSetUp (
    modifier: Modifier = Modifier,
    dayState: DayState,
    onBack: () -> Unit,
    onClickSaveActiveTime: () -> Unit,
    setActiveTimeStart: (Time) -> Unit,
    setActiveTimeEnd: (Time) -> Unit
){
    var showTimePicker by remember { mutableStateOf(false) }
    var showStartActiveTimePicker by remember { mutableStateOf(false) }
    val startActiveTimePickerState = rememberTimePickerState(
        dayState.activeTimeStart.hour,
        dayState.activeTimeStart.minute
    )
    val endActiveTimePickerState = rememberTimePickerState(
        dayState.activeTimeEnd.hour,
        dayState.activeTimeEnd.minute
    )

    // Validation:
    // - start time must be earlier than end time
    var isStartTimeEarlierThanEndTime by remember { mutableStateOf(true) }
    // - if there are tasks, the active time should not infringe on the tasks' time boundaries
    var isActiveTimeStartBeforeFirstTask by remember { mutableStateOf(true) }
    var isActiveTimeEndAfterLastTask by remember { mutableStateOf(true) }

    // Warning texts
    val startTimeLaterThanEndTimeWarning = "The start time must be earlier than the end time"// TODO: Read string from resource
    val activeTimeStartLaterThanFirstTaskWarning = "The active time start is later than the start of the first task"
    val activeTimeEndEarlierThanLastTaskWarning = "The active time end is earlier than the end of the last task"

    val startTime = Time(startActiveTimePickerState.hour, startActiveTimePickerState.minute)
    val endTime = Time(endActiveTimePickerState.hour, endActiveTimePickerState.minute)

    val tasks = dayState.tasks
    // Check whether the start- and end time have correct values
    isStartTimeEarlierThanEndTime = (startTime.compareTo(endTime) == -1)
    if (!tasks.isEmpty()) {
        isActiveTimeStartBeforeFirstTask = (startTime.compareTo(tasks[0].startTime!!) == -1 || startTime.compareTo(tasks[0].startTime!!) == 0)
        isActiveTimeEndAfterLastTask = (endTime.compareTo(tasks[tasks.size - 1].endTime!!) == 1 || endTime.compareTo(tasks[tasks.size - 1].endTime!!) == 0)
    }

    // Texts
    val headerText = "ACTIVE TIME"// TODO: Read string from resource
    val cancelText = "Cancel"// TODO: Read string from resource
    val okText = "OK"// TODO: Read string from resource

    fun onCancelCloseTimePicker() {
        showTimePicker = false
        showStartActiveTimePicker = false
    }

    fun onSaveCloseTimePicker() {
        if (showStartActiveTimePicker) {
            setActiveTimeStart(Time(startActiveTimePickerState.hour, startActiveTimePickerState.minute))
        } else {
            setActiveTimeEnd(Time(endActiveTimePickerState.hour, endActiveTimePickerState.minute))
        }

        showTimePicker = false
        showStartActiveTimePicker = false
    }

    fun openTimePicker() {
        showTimePicker = true
    }

    Scaffold (
        bottomBar = {
            BottomAppBar (
                containerColor = COMPONENT_BACKGROUND_COLOR,
                actions = {
                    // Close button
                    IconButton(onClick = {
                        // Navigate to previous stack entry
                        onBack()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                            contentDescription = "Cancel",
                            modifier = Modifier.fillMaxSize(0.8F),
                            tint = HEADER_TEXT_COLOR
                        )
                    }

                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))

                    // Save button
                    IconButton(onClick = {
                        if (
                            isStartTimeEarlierThanEndTime
                            && isActiveTimeStartBeforeFirstTask
                            && isActiveTimeEndAfterLastTask
                        ) {
                            onClickSaveActiveTime()
                        }
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.save_alt_svgrepo_com),
                            contentDescription = "Save task",
                            modifier = Modifier.fillMaxSize(0.6F),
                            tint = HEADER_TEXT_COLOR
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .background(COMPONENT_BACKGROUND_COLOR)
                .padding(horizontal = 15.dp)
                .padding(innerPadding)
                .fillMaxSize()
            ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                modifier = Modifier
                    .padding(bottom = 10.dp),
                text = headerText,
                fontSize = 20.sp,
                color = HEADER_TEXT_COLOR
            )

            Row (
                modifier = Modifier.padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active time start
                Box (
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            onClick = {
                                showStartActiveTimePicker = true
                                openTimePicker()
                            }
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .padding(10.dp),
                        text = "%d:%02d".format(
                            startTime.hour,
                            startTime.minute
                        ),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HEADER_TEXT_COLOR
                    )
                }

                Text(
                    " - ",
                    fontWeight = FontWeight.SemiBold,
                    color = HEADER_TEXT_COLOR
                )

                // Active time end
                Box (
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            onClick = {
                                openTimePicker()
                            }
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .padding(10.dp),
                        text = "%d:%02d".format(
                            endTime.hour,
                            endTime.minute
                        ),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HEADER_TEXT_COLOR
                    )
                }
            }

            // Warnings
            AnimatedVisibility(
                visible = !isStartTimeEarlierThanEndTime
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 10.dp)
                    ,
                    text = startTimeLaterThanEndTimeWarning,
                    fontSize = 13.sp,
                    color = ERROR_TEXT_COLOR,
                    textAlign = TextAlign.Start
                )
            }

            // Warnings
            AnimatedVisibility(
                visible = !isActiveTimeStartBeforeFirstTask
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 10.dp)
                    ,
                    text = activeTimeStartLaterThanFirstTaskWarning,
                    fontSize = 13.sp,
                    color = ERROR_TEXT_COLOR,
                    textAlign = TextAlign.Start
                )
            }

            // Warnings
            AnimatedVisibility(
                visible = !isActiveTimeEndAfterLastTask
            ) {
                Text(
                    modifier = Modifier
                        .padding(start = 10.dp)
                    ,
                    text = activeTimeEndEarlierThanLastTaskWarning,
                    fontSize = 13.sp,
                    color = ERROR_TEXT_COLOR,
                    textAlign = TextAlign.Start
                )
            }
        }
    }


    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = {
                onCancelCloseTimePicker()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveCloseTimePicker()
                    }
                ) {
                    Text(
                        text = okText,
                        color = HEADER_TEXT_COLOR
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onCancelCloseTimePicker()
                    }
                ) {
                    Text(
                        text = cancelText,
                        color = HEADER_TEXT_COLOR
                    )
                }
            },
            containerColor = COMPONENT_BACKGROUND_COLOR
        )
        {
            if (showStartActiveTimePicker) {
                TimePicker(
                    state = startActiveTimePickerState,
                    colors = TimePickerDefaults.colors().copy(
                        clockDialColor = BACKGROUND_COLOR,
                        selectorColor = HEADER_TEXT_COLOR,
                        timeSelectorSelectedContainerColor = HEADER_TEXT_COLOR,
                        timeSelectorUnselectedContainerColor = SECONDARY_HEADER_TEXT_COLOR,
                        timeSelectorSelectedContentColor = COMPONENT_BACKGROUND_COLOR,
                        timeSelectorUnselectedContentColor = SECONDARY_TEXT_COLOR
                    )
                )
            } else {
                TimePicker(
                    state = endActiveTimePickerState,
                    colors = TimePickerDefaults.colors().copy(
                        clockDialColor = BACKGROUND_COLOR,
                        selectorColor = HEADER_TEXT_COLOR,
                        timeSelectorSelectedContainerColor = HEADER_TEXT_COLOR,
                        timeSelectorUnselectedContainerColor = SECONDARY_HEADER_TEXT_COLOR,
                        timeSelectorSelectedContentColor = COMPONENT_BACKGROUND_COLOR,
                        timeSelectorUnselectedContentColor = SECONDARY_TEXT_COLOR
                    )
                )
            }
        }
    }
}