package com.example.circularplanner.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.circularplanner.R
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.component.DropDownItem
import com.example.circularplanner.ui.component.PopupDialog
import com.example.circularplanner.ui.component.TaskCard
import com.example.circularplanner.ui.component.TaskCardDisplayType
import com.example.circularplanner.ui.component.TaskDropdownMenu
import com.example.circularplanner.ui.component.VoiceNoteList
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.AudioViewModel
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.ui.viewmodel.UserInput
import com.example.circularplanner.ui.viewmodel.VoiceNoteUiState
import com.example.circularplanner.utils.TaskModePopup
import java.util.UUID

enum class TaskActivityComparisonModePopup {
    Activity,
    Task
}

@Composable
fun TaskActivityComparisonScreen (
    modifier: Modifier = Modifier,
    activityUiState: ActivityUiState,
    dayState: DayState,
    taskUiState: TaskUiState,
    userInput: UserInput,
    deleteActivity: () -> Unit,
    deleteTask: () -> Unit,
    deleteVoiceNote: (VoiceNoteUiState) -> Unit,
    onCancel: () -> Unit,
    saveActivity: () -> Unit,
    saveTaskFromState: () -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
    setActivityNote: (String) -> Unit,
    setTaskDescription: (String) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskPriority: (Int) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskTitle: (String) -> Unit,
    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    val taskDetails = taskUiState
    val activityDetails = activityUiState

    val audioViewModel: AudioViewModel = viewModel(factory = AudioViewModel.Factory)

    val activityLabel = "ACTIVITY"//TODO: Read from string resource
    val taskLabel = "TASK"//TODO: Read from string resource

    var showPopupWindow by remember { mutableStateOf(false) }
    var popupState by remember { mutableStateOf(TaskActivityComparisonModePopup.Task) }

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
                        // Navigate to the previous stack entry
                        onCancel()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                            contentDescription = "Open time picker",
                            modifier = Modifier.fillMaxSize(0.8F),
                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        )
                    }

                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))
                }
            )
        }
    ) { innerPadding ->
        Column (
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Task
            Column (
                modifier = Modifier
                    .fillMaxHeight(0.5f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show task details if there is a task to be shown
                if (taskDetails.id != null) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row (
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Spacer(Modifier.weight(1f))
                        }

                        Row (
                            modifier = Modifier
                                .weight(1f)
                            ,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(vertical = 15.dp)
                                    .weight(1f)
                                ,
                                text = taskLabel,// TODO: Read text from string resource
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Row (
                            modifier = Modifier
                                .weight(1f)
                            ,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {

                            val dropdownItems = listOf<DropDownItem>(
                                DropDownItem(
                                    text = "Edit",
                                    iconId = R.drawable.edit_24dp_5f6368_fill0_wght400_grad0_opsz24,
                                    onClick = {
                                        popupState = TaskActivityComparisonModePopup.Task
                                        showPopupWindow = true
                                    }
                                ),
                                DropDownItem(
                                    text = "Delete",
                                    iconId = R.drawable.delete_24dp_5f6368_fill0_wght400_grad0_opsz24,
                                    onClick = {
                                        deleteTask()
                                        selectTask(null)
//                                    showPopupWindow = true
                                    }
                                )
                            )
                            TaskDropdownMenu(
                                dropdownItems = dropdownItems,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize(0.7F)
                            )
                        }



                    }

                    TaskCard (
                        date = userInput.selectedDate,
                        endTime = taskDetails.endTime,
                        startTime = taskDetails.startTime,
                        title = taskDetails.title
                    ) {
                        // Description
                        Text(
                            text = taskDetails.description,
                            modifier = modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )
                    }
                }
                // Otherwise, show a statement that there is no task planned for the select time
                else {
                    Column (
                        modifier = Modifier
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NO TASK TO DISPLAY",// TODO: Read string from resource
                            color = Color.LightGray
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier
                    .padding(
                        horizontal = 15.dp,
                        vertical = 5.dp)
                    .fillMaxWidth(),
                thickness = 1.dp,
            )

            // Activity
            Column (
                modifier = Modifier
//                    .background(Color(0xff97dde8))// TODO: Add color to a theme
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (activityDetails.id != null) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row (
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Spacer(Modifier.weight(1f).background(Color(0xffFAA18F)))
                        }

                        Row (
                            modifier = Modifier
                                .weight(1f)
                            ,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(vertical = 15.dp)
                                    .weight(1f)
//                                    .background(Color(0xffBBE6FC))// TODO: Add color to a theme
                                ,
                                text = activityLabel,// TODO: Read text from string resource
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Row (
                            modifier = Modifier
                                .weight(1f)
                            ,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {

                            val dropdownItems = listOf<DropDownItem>(
                                DropDownItem(
                                    text = "Edit",
                                    iconId = R.drawable.edit_24dp_5f6368_fill0_wght400_grad0_opsz24,
                                    onClick = {
                                        popupState = TaskActivityComparisonModePopup.Activity
                                        showPopupWindow = true
                                    }
                                ),
                                DropDownItem(
                                    text = "Delete",
                                    iconId = R.drawable.delete_24dp_5f6368_fill0_wght400_grad0_opsz24,
                                    onClick = {
                                        deleteActivity()
                                        selectActivity(null)
//                                    showPopupWindow = true
                                    }
                                )
                            )
                            TaskDropdownMenu(
                                dropdownItems = dropdownItems,
                                modifier = Modifier
                                    .fillMaxSize(0.7F)
                                    .weight(1f)

                            )
                        }
                    }

                    TaskCard (
                        date = userInput.selectedDate,
                        endTime = activityDetails.endTime,
                        startTime = activityDetails.startTime,
                        title = activityDetails.title
                    ) {
                        VoiceNoteList(
                            activityUiState = activityUiState,
                            audioViewModel = audioViewModel,
                            removeVoiceNote = deleteVoiceNote,
                            updateLastPlayedPosition = updateLastPlayedPosition
                        )
                    }
                }
                // Otherwise, show a statement that there is no task planned for the select time
                else {
                    Column (
                        modifier = Modifier
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NO ACTIVITY TO DISPLAY",// TODO: Read string from resource
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }

    if (showPopupWindow) {
        PopupDialog(
            onDismissRequest = {
                showPopupWindow = false
            }
        ) {
            when (popupState) {
                TaskActivityComparisonModePopup.Activity -> {
                    ActivityNoteEditScreen(
                        activityUiState = activityUiState,
                        onBack = {
                            showPopupWindow = false
                        },
                        saveActivity = saveActivity,
                        setActivityNote = setActivityNote
                    )
                }
                TaskActivityComparisonModePopup.Task -> {
                    TaskEditScreen(
                        dayState = dayState,
                        lastTaskPriority = 0,
                        taskUiState = taskUiState,
                        onBack = {
                            showPopupWindow = false
                        },
                        saveTask = saveTaskFromState,
                        setTaskEndTime = setTaskEndTime,
                        setTaskDescription = setTaskDescription,
                        setTaskPriority = setTaskPriority,
                        setTaskStartTime = setTaskStartTime,
                        setTaskTitle = setTaskTitle
                    )
                }
            }
        }
    }
}