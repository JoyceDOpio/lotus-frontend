package com.example.circularplanner.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.circularplanner.ui.viewmodel.UserInput
//import com.example.circularplanner.ui.viewmodel.TaskDisplayUiState
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditScreen(
    modifier: Modifier = Modifier,
    taskUiState: TaskUiState,
    onBack: () -> Unit,
    saveTask: () -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskDescription: (String) -> Unit,
    setTaskTitle: (String) -> Unit,
) {
    fun getLabel(taskId: UUID?): String {
        if (taskId == null) {
            return "Create new task"
        }

        return "Edit task"
    }

    val label: String = getLabel(taskUiState.id)
    var showTimePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    val taskDetails = taskUiState

    val startTimePickerState: TimePickerState = rememberTimePickerState(
        initialHour = taskDetails.startTime.hour,
        initialMinute = taskDetails.startTime.minute
    )
    val endTimePickerState: TimePickerState = rememberTimePickerState(
        initialHour = taskDetails.endTime.hour,
        initialMinute = taskDetails.endTime.minute
    )

    val confirmButtonText = "OK"
    val dismissButtonText = "Cancel"

    fun onDismissCloseTimePicker() {
        showTimePicker = false
        showStartTimePicker = false
    }

    fun onSaveCloseTimePicker() {
        //TODO: Validate if task start- and end time are within active time bounds
        setTaskStartTime(Time(startTimePickerState.hour, startTimePickerState.minute))
        setTaskEndTime(Time(endTimePickerState.hour, endTimePickerState.minute))
        showTimePicker = false
        showStartTimePicker = false
    }

    fun openTimePicker() {
        showTimePicker = true
    }

    Row (
        modifier = modifier
            .padding(vertical = 10.dp)
            .fillMaxSize(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth(0.85f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // One of the Spacers to center the content after this Spacer and to  make description text field slightly higher than just one line
            Spacer(Modifier.weight(1f, true))

            Column () {
                // Label
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                // Title field
                OutlinedTextField(
                    value = taskDetails.title,
                    onValueChange = setTaskTitle,
                    modifier = Modifier
                        .fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 20.sp),
                    label = { Text("Title") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    singleLine = true
                )

                Row (
                    modifier = modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start time
                    Text(
                        text = String.format(
                            "Start time: %d:%02d",
                            taskDetails.startTime.hour,
                            taskDetails.startTime.minute
                        ),
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = {
                        showStartTimePicker = true
                        openTimePicker()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.schedule_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Open time picker",
                            modifier = Modifier.fillMaxSize(0.8F)
                        )
                    }
                }

                Row (
                    modifier = modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // End time
                    Text(
                        text = String.format(
                            "End time: %d:%02d",
                            taskDetails.endTime.hour,
                            taskDetails.endTime.minute
                        ),
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        openTimePicker()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.schedule_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Open time picker",
                            modifier = Modifier.fillMaxSize(0.8F)
                        )
                    }
                }
            }

            Column (
                modifier = Modifier
                    .weight(1f)
            ) {
                // Description field
                OutlinedTextField(
                    value = taskDetails.description,
                    onValueChange = setTaskDescription,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()

                    .fillMaxHeight(0.5f),
                    textStyle = TextStyle(fontSize = 18.sp),
                    label = { Text("Description") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    singleLine = false
                )
            }

            // One of the Spacers to center the content before this Spacer and to  make description text field slightly higher than just one line
            Spacer(Modifier.weight(1f, true))
        }

        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight(),
            thickness = 2.dp,
        )

        Column (
            modifier = modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            IconButton(onClick = {
                // Navigate to previous stack entry
                onBack()
            }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.cancel_24dp_5f6368_fill0_wght400_grad0_opsz24),
                    contentDescription = "Cancel",
                    modifier = Modifier.fillMaxSize(0.8F)
                )
            }

            // Save button
            IconButton(onClick = {
                //TODO: Validate start- and end-time input - the values should remain within the active time boundaries
                saveTask()
                onBack()
            }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.save_24dp_5f6368_fill0_wght400_grad0_opsz24),
                    contentDescription = "Open time picker",
                    modifier = Modifier.fillMaxSize(0.8F)
                )
            }
        }
    }


    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = {
                onDismissCloseTimePicker()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveCloseTimePicker()
                    }
                ) { Text(confirmButtonText) } },
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