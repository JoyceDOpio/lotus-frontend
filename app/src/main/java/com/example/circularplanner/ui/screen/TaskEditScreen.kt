package com.example.circularplanner.ui.screen

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
import com.example.circularplanner.ui.viewmodel.TaskUiState
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
            return "CREATE A TASK"
        }

        return "EDIT TASK"
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

    Scaffold (
        bottomBar = {
            BottomAppBar (
//                contentColor = MaterialTheme.colorScheme.primaryContainer
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
                            imageVector = ImageVector.vectorResource(id = R.drawable.cancel_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Cancel",
                            modifier = Modifier.fillMaxSize(0.8F)
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
                            imageVector = ImageVector.vectorResource(id = R.drawable.save_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Open time picker",
                            modifier = Modifier.fillMaxSize(0.8F)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
//                .weight(0.6f)
                .padding(horizontal = 30.dp)
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
                                taskDetails.startTime.hour,
                                taskDetails.startTime.minute
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
                                taskDetails.endTime.hour,
                                taskDetails.endTime.minute
                            ),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
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
                shape = RoundedCornerShape(15.dp),
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
                shape = RoundedCornerShape(15.dp),
            )
        }
    }

//    Row (
//        modifier = modifier
//            .padding(vertical = 10.dp)
//            .fillMaxSize()
//    ) {
//        Column () {
//            Column(
//                modifier = modifier
//                    .weight(0.6f)
//                    .padding(horizontal = 10.dp)
//                    .fillMaxWidth(0.85f)
//                    .fillMaxHeight()
//                    .verticalScroll(rememberScrollState()),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Label
//                Text(
//                    modifier = Modifier
//                        .padding(bottom = 10.dp),
//                    text = label,
//                    fontSize = 20.sp,
//                    color = MaterialTheme.colorScheme.primary
//                )
//
//                Column (
//                    modifier = modifier
//                        .fillMaxWidth(),
//                ) {
//                    Row (
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        // Start time
//                        Box (
//                            modifier = Modifier
//                                .clip(CircleShape)
//                                .clickable(
//                                    onClick = {
//                                        showStartTimePicker = true
//                                        openTimePicker()
//                                    }
//                                )
//                        ) {
//                            Text(
//                                modifier = Modifier
//                                    .padding(10.dp),
//                                text = String.format(
//                                    "%d:%02d",
//                                    taskDetails.startTime.hour,
//                                    taskDetails.startTime.minute
//                                ),
//                                fontSize = 20.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = MaterialTheme.colorScheme.primary
//                            )
//                        }
//
//                        Text(
//                            " - ",
//                            fontWeight = FontWeight.SemiBold,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//
//                        // End time
//                        Box (
//                            modifier = Modifier
//                                .clip(CircleShape)
//                                .clickable(
//                                    onClick = {
//                                        openTimePicker()
//                                    }
//                                )
//                        ) {
//                            Text(
//                                modifier = Modifier
//                                    .padding(10.dp),
//                                text = String.format(
//                                    "%d:%02d",
//                                    taskDetails.endTime.hour,
//                                    taskDetails.endTime.minute
//                                ),
//                                fontSize = 20.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = MaterialTheme.colorScheme.primary
//                            )
//                        }
//                    }
//                }
//
//                // Title field
//                OutlinedTextField(
//                    value = taskDetails.title,
//                    onValueChange = setTaskTitle,
//                    modifier = Modifier
//                        .padding(vertical = 5.dp)
//                        .fillMaxWidth(),
//                    textStyle = TextStyle(fontSize = 20.sp),
//                    label = { Text("Title") },
//                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
//                    singleLine = true,
//                    shape = RoundedCornerShape(15.dp),
//                )
//
//                // Description field
//                OutlinedTextField(
//                    value = taskDetails.description,
//                    onValueChange = setTaskDescription,
//                    modifier = Modifier
//                        .padding(vertical = 5.dp)
//                        .fillMaxWidth()
//                        .fillMaxHeight(0.5f),
//                    textStyle = TextStyle(fontSize = 18.sp),
//                    label = { Text("Description") },
//                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
//                    singleLine = false,
//                    shape = RoundedCornerShape(15.dp),
//                )
//            }
//        }

//        VerticalDivider(
//            modifier = Modifier
//                .fillMaxHeight(),
//            thickness = 2.dp,
//        )
//
//        Column (
//            modifier = modifier
//                .fillMaxWidth(1f)
//                .fillMaxHeight(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // Close button
//            IconButton(onClick = {
//                // Navigate to previous stack entry
//                onBack()
//            }) {
//                Icon(
//                    imageVector = ImageVector.vectorResource(id = R.drawable.cancel_24dp_5f6368_fill0_wght400_grad0_opsz24),
//                    contentDescription = "Cancel",
//                    modifier = Modifier.fillMaxSize(0.8F)
//                )
//            }
//
//            // Save button
//            IconButton(onClick = {
//                //TODO: Validate start- and end-time input - the values should remain within the active time boundaries
//                saveTask()
//                onBack()
//            }) {
//                Icon(
//                    imageVector = ImageVector.vectorResource(id = R.drawable.save_24dp_5f6368_fill0_wght400_grad0_opsz24),
//                    contentDescription = "Open time picker",
//                    modifier = Modifier.fillMaxSize(0.8F)
//                )
//            }
//        }
//    }

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