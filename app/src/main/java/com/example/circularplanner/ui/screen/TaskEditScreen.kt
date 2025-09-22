package com.example.circularplanner.ui.screen

import android.util.Log
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.R
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.component.TimePickerDialog
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.TaskUiState
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    modifier: Modifier = Modifier,
    dayState: DayState,
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
    fun getLabel(taskId: UUID?): String {
        if (taskId == null) {
            return "CREATE A TASK"
        }

        return "EDIT TASK"
    }

    val label: String = getLabel(taskUiState.id)
    var showTimePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    val taskDetails = taskUiState
Log.i("taskUiState", taskUiState.toString())
    var startTimePickerState: TimePickerState? = null
    var endTimePickerState: TimePickerState? = null

    if (taskDetails.date != null) {
        startTimePickerState = rememberTimePickerState(
            initialHour = taskDetails.startTime?.hour ?: dayState.activeTimeStart.hour,
            initialMinute = taskDetails.startTime?.minute ?: dayState.activeTimeStart.minute
        )
        endTimePickerState = rememberTimePickerState(
            initialHour = taskDetails.endTime?.hour ?: dayState.activeTimeEnd.hour,
            initialMinute = taskDetails.endTime?.minute  ?: dayState.activeTimeEnd.minute
        )
    }

    val confirmButtonText = "OK"
    val dismissButtonText = "Cancel"

    // If the task is a TO-DO task and it doesn't have a priority value, set the priority
    if (taskDetails.date == null && taskDetails.priority == null) {
        setTaskPriority(lastTaskPriority + 1)
    }

    fun onDismissCloseTimePicker() {
        showTimePicker = false
        showStartTimePicker = false
    }

    fun onSaveCloseTimePicker() {
        //TODO: Validate if task start- and end time are within active time bounds
        setTaskStartTime(Time(startTimePickerState?.hour ?: 0, startTimePickerState?.minute ?: 0))
        setTaskEndTime(Time(endTimePickerState?.hour ?: 0, endTimePickerState?.minute ?: 0))
        showTimePicker = false
        showStartTimePicker = false
    }

    fun openTimePicker() {
        showTimePicker = true
    }

    Scaffold (
        bottomBar = {
            BottomAppBar (
                containerColor = Color(BOTTOM_BAR_COLOR),
                actions = {
//                    // Leading icons should typically have a high content alpha
//                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
//                        IconButton(onClick = { /* doSomething() */ }) {
//                            Icon(Icons.Filled.Menu, contentDescription = "Localized description")
//                        }
//                    }

                    // Close button
                    IconButton(onClick = {
                        // Navigate to previous stack entry
                        onBack()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                            contentDescription = "Cancel",
                            modifier = Modifier.fillMaxSize(0.8F),
                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        )
                    }

                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))

                    // Save button
                    IconButton(onClick = {
                        //TODO: Validate start- and end-time input - the values should remain within the active time boundaries
                        saveTask()
                        onBack()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.save_alt_svgrepo_com),
                            contentDescription = "Save task",
                            modifier = Modifier.fillMaxSize(0.6F),
                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(horizontal = 15.dp)
                .padding(innerPadding)
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label
            Text(
                modifier = Modifier
                    .padding(bottom = 10.dp),
                text = label,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
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
                                    taskDetails.startTime?.hour ?: dayState.activeTimeStart.hour,
                                    taskDetails.startTime?.minute ?: dayState.activeTimeStart.minute
                                ),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            " - ",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
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
                                    taskDetails.endTime?.hour ?: dayState.activeTimeEnd.hour,
                                    taskDetails.endTime?.minute ?: dayState.activeTimeEnd.minute
                                ),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Title field
            OutlinedTextField(
                value = taskDetails.title,
                onValueChange = setTaskTitle,
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .fillMaxWidth(),
                textStyle = TextStyle(fontSize = 20.sp),
                label = { Text("Title") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                shape = RoundedCornerShape(15.dp)
            )

            // Description field
            OutlinedTextField(
                value = taskDetails.description,
                onValueChange = setTaskDescription,
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                textStyle = TextStyle(fontSize = 18.sp),
                label = { Text("Description") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = false,
                shape = RoundedCornerShape(15.dp)
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
                    ) { Text(confirmButtonText) }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onDismissCloseTimePicker()
                        }
                    ) { Text(dismissButtonText) }
                }
            )
            {
                if (showStartTimePicker) {
                    TimePicker(state = startTimePickerState)
                } else {
                    TimePicker(state = endTimePickerState)
                }
            }
        }
    }
}