package com.eternalfairy.lotus.view.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.lotus.R
import com.eternalfairy.lotus.model.data.Time
import com.eternalfairy.lotus.view.component.TimePickerDialog
import com.eternalfairy.lotus.view.data.DayUiState
import com.eternalfairy.lotus.view.data.TaskUiState
import com.eternalfairy.lotus.view.theme.BACKGROUND_COLOR
import com.eternalfairy.lotus.view.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.lotus.view.theme.ERROR_TEXT_COLOR
import com.eternalfairy.lotus.view.theme.HEADER_TEXT_COLOR
import com.eternalfairy.lotus.view.theme.SECONDARY_HEADER_TEXT_COLOR
import com.eternalfairy.lotus.view.theme.SECONDARY_TEXT_COLOR
import com.eternalfairy.lotus.view.theme.SELECTION_COLOR
import com.eternalfairy.lotus.view.utils.TouchGestureUtils
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    modifier: Modifier = Modifier,
    dayUiState: DayUiState,
    lastTaskPriority: Int,
    taskUiState: TaskUiState,
    onBack: () -> Unit,
    saveTask: () -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskDescription: (String) -> Unit,
    setTaskPriority: (Int) -> Unit,
    setTaskTitle: (String) -> Unit
) {
    // Texts
    val createLabelText = "CREATE A TASK"// TODO: Read string from resource
    val editLabelText = "EDIT TASK"// TODO: Read string from resource
    val confirmButtonText = "OK"// TODO: Read string from resource
    val dismissButtonText = "Cancel"// TODO: Read string from resource
    val titlePlaceholderText = "Title"// TODO: Read string from resource
    val descriptionPlaceholderText = "Description"// TODO: Read string from resource

    fun getLabel(taskId: UUID?): String {
        if (taskId == null) {
            return createLabelText
        }

        return editLabelText
    }

    val label: String = getLabel(taskUiState.id)
    var showTimePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    val taskDetails = taskUiState
    var startTimePickerState: TimePickerState? = null
    var endTimePickerState: TimePickerState? = null

    // Validation:
    // - title cannot be empty
    var isTitle by remember { mutableStateOf(true) }
    // - start time and end time must be within active time bounds
    var isStartTimeWithinActiveTime by remember { mutableStateOf(true) }
    var isEndTimeWithinActiveTime by remember { mutableStateOf(true) }
    // - start time must be earlier than end time
    var isStartTimeEarlierThanEndTime by remember { mutableStateOf(true) }
    // - start time and end time cannot overlap with other tasks
    var isStartTimeOverlappingAnotherTask by remember { mutableStateOf(false) }
    var isEndTimeOverlappingAnotherTask by remember { mutableStateOf(false) }
    var isAnotherTaskWithinStartAndEndTime by remember { mutableStateOf(false) }
    // - minimal task duration between start time and end time should be 5 minutes
    val minimalTaskDuration = 5
    var isTaskOfMinimalDuration by remember { mutableStateOf(true) }

    // Warning texts
    val emptyTitleWarning = "The title cannot be empty"// TODO: Read string from resource
    val startTimeLaterThanEndTimeWarning = "The start time must be earlier than the end time"// TODO: Read string from resource
    val startTimeOutsideActiveTimeWarning = "The start time must be within active time: ${dayUiState.activeTimeStart} - ${dayUiState.activeTimeEnd}"// TODO: Read string from resource
    val endTimeOutsideActiveTimeWarning = "The end time must be within active time: ${dayUiState.activeTimeStart} - ${dayUiState.activeTimeEnd}"// TODO: Read string from resource
    val startTimeOverlappingTaskWarning = "The start time is overlapping another task"// TODO: Read string from resource
    val endTimeOverlappingTaskWarning = "The end time is overlapping another task"// TODO: Read string from resource
    val taskWithinStartAndEndTimeWarning = "Another task is within the start time and end time bounds"// TODO: Read string from resource
    val minimalTaskDurationWarning = "A task must be at least $minimalTaskDuration minutes long" // TODO: Read string from resource

    // If the task is a TO-DO task, and it doesn't have a priority value, set the priority
    if (taskDetails.date == null && taskDetails.priority == null) {
        setTaskPriority(lastTaskPriority + 1)
    }

    if (taskDetails.date != null) {
        // Set the initial values for the time pickers
        startTimePickerState = rememberTimePickerState(
            initialHour = taskDetails.startTime?.hour ?: dayUiState.activeTimeStart.hour,
            initialMinute = taskDetails.startTime?.minute ?: dayUiState.activeTimeStart.minute
        )
        endTimePickerState = rememberTimePickerState(
            initialHour = taskDetails.endTime?.hour ?: dayUiState.activeTimeEnd.hour,
            initialMinute = taskDetails.endTime?.minute  ?: dayUiState.activeTimeEnd.minute
        )

        val startTime = Time(startTimePickerState.hour, startTimePickerState.minute)
        val endTime = Time(endTimePickerState.hour, endTimePickerState.minute)

        // Check whether the start- and end time don't overlap with another task
        for (task in dayUiState.tasks) {
            if (task.id != taskDetails.id) {
                isStartTimeOverlappingAnotherTask = ((startTime.compareTo(task.startTime!!) == 0 || startTime.compareTo(task.startTime!!) == 1)
                        && (startTime.compareTo(task.endTime!!) == -1 || startTime.compareTo(task.endTime!!) == 0))
                isEndTimeOverlappingAnotherTask = ((endTime.compareTo(task.startTime!!) == 0 || endTime.compareTo(task.startTime!!) == 1)
                        && (endTime.compareTo(task.endTime!!) == -1 || endTime.compareTo(task.endTime!!) == 0))
                isAnotherTaskWithinStartAndEndTime = (task.startTime!!.compareTo(startTime) == 1 && task.startTime!!.compareTo(endTime) == -1)
            }
        }
    }

    fun onDismissCloseTimePicker() {
        showTimePicker = false
        showStartTimePicker = false
    }

    fun onSaveCloseTimePicker() {
        val startTime = Time(startTimePickerState?.hour ?: 0, startTimePickerState?.minute ?: 0)
        val endTime = Time(endTimePickerState?.hour ?: 0, endTimePickerState?.minute ?: 0)

        // Check whether the start- and end time have correct values
        isStartTimeEarlierThanEndTime = (startTime.compareTo(endTime) == -1)
        isStartTimeWithinActiveTime = ((startTime.compareTo(dayUiState.activeTimeStart) == 0 || startTime.compareTo(dayUiState.activeTimeStart) == 1)
                && startTime.compareTo(dayUiState.activeTimeEnd) == -1)
        isEndTimeWithinActiveTime = (endTime.compareTo(dayUiState.activeTimeStart) == 1
                && (endTime.compareTo(dayUiState.activeTimeEnd) == -1 || endTime.compareTo(dayUiState.activeTimeEnd) == 0))
        isTaskOfMinimalDuration = TouchGestureUtils.calculateTotalNumberOfMinutes(startTime, endTime) >= minimalTaskDuration

        // Check whether the start- and end time don't overlap with another task
        for (task in dayUiState.tasks) {
            if (task.id != taskDetails.id) {
                isStartTimeOverlappingAnotherTask = ((startTime.compareTo(task.startTime!!) == 0 || startTime.compareTo(task.startTime!!) == 1)
                        && (startTime.compareTo(task.endTime!!) == -1 || startTime.compareTo(task.endTime!!) == 0))
                isEndTimeOverlappingAnotherTask = ((endTime.compareTo(task.startTime!!) == 0 || endTime.compareTo(task.startTime!!) == 1)
                        && (endTime.compareTo(task.endTime!!) == -1 || endTime.compareTo(task.endTime!!) == 0))
                isAnotherTaskWithinStartAndEndTime = (task.startTime!!.compareTo(startTime) == 1 && task.startTime!!.compareTo(endTime) == -1)
            }
        }

        setTaskStartTime(startTime)
        setTaskEndTime(endTime)

        showTimePicker = false
        showStartTimePicker = false
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
                    IconButton(
                        onClick = {
                            if (taskDetails.title == "") isTitle = false

                            if (isTitle
                                && isStartTimeWithinActiveTime
                                && isEndTimeWithinActiveTime
                                && isStartTimeEarlierThanEndTime
                                && !isStartTimeOverlappingAnotherTask
                                && !isEndTimeOverlappingAnotherTask
                                && !isAnotherTaskWithinStartAndEndTime
                                && isTaskOfMinimalDuration
                            ) {
                                saveTask()
                                onBack()
                            }
                        }
                    ) {
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
                // INFO: First background, then padding, then fillMax
                .background(COMPONENT_BACKGROUND_COLOR)
                .padding(
                    horizontal = 15.dp
                )
                .padding(innerPadding)
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
            ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label
            Text(
                modifier = Modifier
                    .padding(vertical = 15.dp),
                text = label,
                fontSize = 20.sp,
                color = HEADER_TEXT_COLOR
            )

            if (taskDetails.date != null) {
                Column (
                    modifier = modifier
                        .fillMaxWidth(),
                ) {
                    Row (
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Start time
                        Box (
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(
                                    onClick = {
                                        showStartTimePicker = true
                                        openTimePicker()
                                    }
                                )
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(10.dp),
                                text = "%d:%02d".format(
                                    taskDetails.startTime?.hour ?: dayUiState.activeTimeStart.hour,
                                    taskDetails.startTime?.minute ?: dayUiState.activeTimeStart.minute
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

                        // End time
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
                                    taskDetails.endTime?.hour ?: dayUiState.activeTimeEnd.hour,
                                    taskDetails.endTime?.minute ?: dayUiState.activeTimeEnd.minute
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

                    AnimatedVisibility(
                        visible = !isStartTimeWithinActiveTime
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(start = 10.dp)
                            ,
                            text = startTimeOutsideActiveTimeWarning,
                            fontSize = 13.sp,
                            color = ERROR_TEXT_COLOR,
                            textAlign = TextAlign.Start
                        )
                    }

                    AnimatedVisibility(
                        visible = !isEndTimeWithinActiveTime
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(start = 10.dp)
                            ,
                            text = endTimeOutsideActiveTimeWarning,
                            fontSize = 13.sp,
                            color = ERROR_TEXT_COLOR,
                            textAlign = TextAlign.Start
                        )
                    }

                    AnimatedVisibility(
                        visible = isStartTimeOverlappingAnotherTask
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(start = 10.dp)
                            ,
                            text = startTimeOverlappingTaskWarning,
                            fontSize = 13.sp,
                            color = ERROR_TEXT_COLOR,
                            textAlign = TextAlign.Start
                        )
                    }

                    AnimatedVisibility(
                        visible = isEndTimeOverlappingAnotherTask
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(start = 10.dp)
                            ,
                            text = endTimeOverlappingTaskWarning,
                            fontSize = 13.sp,
                            color = ERROR_TEXT_COLOR,
                            textAlign = TextAlign.Start
                        )
                    }

                    AnimatedVisibility(
                        visible = isAnotherTaskWithinStartAndEndTime
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(start = 10.dp)
                            ,
                            text = taskWithinStartAndEndTimeWarning,
                            fontSize = 13.sp,
                            color = ERROR_TEXT_COLOR,
                            textAlign = TextAlign.Start
                        )
                    }

                    AnimatedVisibility(
                        visible = !isTaskOfMinimalDuration
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(start = 10.dp)
                            ,
                            text = minimalTaskDurationWarning,
                            fontSize = 13.sp,
                            color = ERROR_TEXT_COLOR,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            // Title field
            OutlinedTextField(
                value = taskDetails.title,
                onValueChange = { value ->
                    setTaskTitle(value)
                    if (value != "") isTitle = true
                },
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .fillMaxWidth(),
                textStyle = TextStyle(fontSize = 20.sp),
                label = { Text(titlePlaceholderText) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                shape = RoundedCornerShape(15.dp),
                // TODO: BOILERPLATE - create custom OutlinedTextField
                colors = OutlinedTextFieldDefaults.colors().copy(
                    cursorColor = HEADER_TEXT_COLOR,
                    focusedIndicatorColor = HEADER_TEXT_COLOR,
                    focusedLabelColor = HEADER_TEXT_COLOR,
                    textSelectionColors = TextSelectionColors(
                        handleColor = HEADER_TEXT_COLOR,
                        backgroundColor = SELECTION_COLOR
                    )
                )
            )

            AnimatedVisibility(
                visible = !isTitle
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 10.dp)
                        ,
                        text = emptyTitleWarning,
                        fontSize = 13.sp,
                        color = ERROR_TEXT_COLOR
                    )
                }
            }

            // Description field
            OutlinedTextField(
                value = taskDetails.description ?: "",
                onValueChange = setTaskDescription,
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                textStyle = TextStyle(fontSize = 18.sp),
                label = { Text(descriptionPlaceholderText) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = false,
                shape = RoundedCornerShape(15.dp),
                // TODO: BOILERPLATE - create custom OutlinedTextField
                colors = OutlinedTextFieldDefaults.colors().copy(
                    cursorColor = HEADER_TEXT_COLOR,
                    focusedIndicatorColor = HEADER_TEXT_COLOR,
                    focusedLabelColor = HEADER_TEXT_COLOR,
                    textSelectionColors = TextSelectionColors(
                        handleColor = HEADER_TEXT_COLOR,
                        backgroundColor = SELECTION_COLOR
                    )
                )
            )
        }
    }

    if (showTimePicker) {
        if (startTimePickerState != null && endTimePickerState != null) {
            TimePickerDialog(
                onDismissRequest = {
                    onDismissCloseTimePicker()
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onSaveCloseTimePicker()
                        }
                    ) {
                        Text(
                            text = confirmButtonText,
                            color = HEADER_TEXT_COLOR
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onDismissCloseTimePicker()
                        }
                    ) {
                        Text(
                            text = dismissButtonText,
                            color = HEADER_TEXT_COLOR
                        )
                    }
                }
            )
            {
                if (showStartTimePicker) {
                    TimePicker(
                        state = startTimePickerState,
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
                        state = endTimePickerState,
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
}