package com.example.circularplanner.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.circularplanner.R
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.component.ActiveTimeHeader
import com.example.circularplanner.ui.component.ActivityGraph
import com.example.circularplanner.ui.component.Calendar
import java.util.UUID
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.DayUiState
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.ui.viewmodel.UserInput
import com.example.circularplanner.ui.viewmodel.VoiceNoteUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    context: Context,
    dayUiState: DayUiState,
    dayState: DayState,
    recordedActivityState: ActivityUiState,
    taskUiState: TaskUiState,
    userInput: UserInput,
    addVoiceNoteToActivity: (VoiceNoteUiState) -> Unit,
    clearRecordedActivity: () -> Unit,
    deleteTask: (Task) -> Unit,
    deleteVoiceNote: (VoiceNoteUiState) ->Unit,
    onChangeDisplayForm: (Boolean) -> Unit,
    onNavigateToTaskActivityComparison: () -> Unit,
    onNavigateToTaskEdit: () -> Unit,
    onNavigateToTaskInfo: () -> Unit,
    onClickSaveActiveTime: () -> Unit,
    onSetSelectedDate: (LocalDate) -> Unit,
    onSwitchScreen: (Boolean) -> Unit,
    saveActivity: () -> Unit,
    saveTask: () -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
    setActiveTimeEnd: (Time) -> Unit,
    setActiveTimeStart: (Time) -> Unit,
    setActivityEndTime: (Time) -> Unit,
    setActivityId: (UUID) -> Unit,
    setActivityStartTime: (Time) -> Unit,
    setActivityTitle: (String) -> Unit,
    setIsActiveTimeSetUp: (Boolean) -> Unit,
    setIsTimerRunning: (Boolean) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    startRecording: (String) -> Unit,
    stopRecording: () -> Unit,
    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
    val showList = dayUiState.isList
    val selectedDate = userInput.selectedDate
    val showActivityScreen = dayUiState.isActivityDisplay

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${selectedDate.format(formatter)}") },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primaryContainer),
            )
        },
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
                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))

                    // If the selected date is not in the past, i.e. it is today or in the future
                    if (!selectedDate.isBefore(LocalDate.now())) {
                        // If the tasks screen is to be shown
                        if (!showActivityScreen) {
                            IconButton(
                                onClick = {
                                    selectTask(null)
                                    onNavigateToTaskEdit()
                                }
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.add_circle_24dp_5f6368_fill0_wght400_grad0_opsz24),
                                    contentDescription = "Add task",
                                    modifier = Modifier.fillMaxSize(0.8F)
                                )
                            }

                            IconButton(onClick = {
                                onChangeDisplayForm(!showList)
                            }) {
                                if (!showList) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.list_24dp_5f6368_fill0_wght400_grad0_opsz24),
                                        contentDescription = "List view",
                                        modifier = Modifier.fillMaxSize(0.8F)
                                    )
                                }
                                else {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.clock_activity_svgrepo_com),
                                        contentDescription = "Dial view",
                                        modifier = Modifier.fillMaxSize(0.8F)
                                    )
                                }
                            }
                        }

                        // If the selected date is in the future, don't show the activities. The activity recording should be reserved only for today.
                        if (!selectedDate.isAfter(LocalDate.now())) {
                            IconButton(onClick = {
                                onSwitchScreen(!showActivityScreen)
                            }) {
                                if (!showActivityScreen) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.graph_svgrepo_com),
                                        contentDescription = "Activity display",
                                        modifier = Modifier.fillMaxSize(0.8F)
                                    )
                                }
                                else {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.graph_infographic_data_element_2_svgrepo_com),
                                        contentDescription = "Task display",
                                        modifier = Modifier.fillMaxSize(0.8F)
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        // If the selected date is in the past, show a comparison of the planned tasks and actual activities
        AnimatedVisibility(
            visible = selectedDate.isBefore(LocalDate.now())
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ActiveTimeHeader(
                    dayState = dayState,
                    userInput = userInput
                )

                ActivityGraph(
                    dayState = dayState,
                    onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
                    selectActivity = selectActivity,
                    selectTask = selectTask
                )

                Calendar(
                    userInput = userInput,
                    onSetDate = onSetSelectedDate
                )
            }
        }

        // If the selected date is today or in the future, show the planning screen
        AnimatedVisibility(
            visible = !selectedDate.isBefore(LocalDate.now())
        ) {
            // Show tasks
            AnimatedVisibility(
                visible = !showActivityScreen
            ) {
                TaskScreen(
                    innerPadding = innerPadding,
                    dayState = dayState,
                    dayUiState = dayUiState,
                    taskUiState = taskUiState,
                    userInput = userInput,
                    deleteTask = deleteTask,
                    onNavigateToTaskEdit = onNavigateToTaskEdit,
                    onNavigateToTaskInfo = onNavigateToTaskInfo,
                    onClickSaveActiveTime = onClickSaveActiveTime,
                    saveTask = saveTask,
                    selectTask = selectTask,
                    setActiveTimeStart = setActiveTimeStart,
                    setActiveTimeEnd = setActiveTimeEnd,
                    setIsActiveTimeSetUp = setIsActiveTimeSetUp,
                    onSetSelectedDate = onSetSelectedDate,
                    setTaskStartTime = setTaskStartTime,
                    setTaskEndTime = setTaskEndTime
                )
            }

            // Show actual activities
            AnimatedVisibility(
                visible = showActivityScreen
            ) {
                ActivityScreen(
                    innerPadding = innerPadding,
                    context = context,
                    dayState = dayState,
                    recordedActivityState = recordedActivityState,
                    addVoiceNoteToActivity = addVoiceNoteToActivity,
                    clearRecordedActivity = clearRecordedActivity,
                    onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
                    removeVoiceNote = deleteVoiceNote,
                    setActivityTitle = setActivityTitle,
                    saveActivity = saveActivity,
                    selectActivity = selectActivity,
                    selectTask = selectTask,
                    setActivityEndTime = setActivityEndTime,
                    setActivityId = setActivityId,
                    setActivityStartTime = setActivityStartTime,
                    setIsTimerRunning = setIsTimerRunning,
                    startRecording = startRecording,
                    stopRecording = stopRecording,
                    updateLastPlayedPosition = updateLastPlayedPosition
                )
            }
        }
    }
}