package com.example.circularplanner.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import com.example.circularplanner.R
import com.example.circularplanner.data.Goal
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.data.VoiceNote
import com.example.circularplanner.service.StopwatchService
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

const val TOP_BAR_COLOR = 0xFFffffff
const val TOP_BAR_TEXT_COLOR = 0xFF6650a4
const val BOTTOM_BAR_COLOR = 0xFFffffff
const val BOTTOM_BAR_TEXT_COLOR = 0xFF3D3061

enum class State {
    Goal,
    Task,
    ToDo
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    context: Context,
    dayUiState: DayUiState,
    dayState: DayState,
    goals: List<Goal>,
    stopwatchService: StopwatchService,
    recordedActivityUiState: ActivityUiState,
    taskUiState: TaskUiState,
    toDoTasks: List<Task>,
    userInput: UserInput,
    clearRecordedActivity: () -> Unit,
    deleteGoal: (Goal) -> Unit,
    deleteVoiceNote: (VoiceNoteUiState) ->Unit,
    onNavigateToGoalEdit: () -> Unit,
    onNavigateToTaskActivityComparison: () -> Unit,
    onNavigateToTaskEdit: () -> Unit,
    onNavigateToTaskInfo: () -> Unit,
    onClickSaveActiveTime: () -> Unit,
    onSetDayNote: (String) -> Unit,
    onSetSelectedDate: (LocalDate) -> Unit,
    onSwitchGoals: (UUID, UUID) -> Unit,
    onSwitchScreen: (Boolean) -> Unit,
    saveDay: () -> Unit,
    saveRecordedActivity: () -> Unit,
    saveTask: () -> Unit,
    saveVoiceNote: (VoiceNote) -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectGoal: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
    setActiveTimeEnd: (Time) -> Unit,
    setActiveTimeStart: (Time) -> Unit,
    setActualActiveTimeEnd: (Time) -> Unit,
    setActualActiveTimeStart: (Time) -> Unit,
    setRecordedActivityEndTime: (Time) -> Unit,
    setRecordedActivityId: (UUID) -> Unit,
    setRecordedActivityNote: (String) -> Unit,
    setRecordedActivityStartTime: (Time) -> Unit,
    setRecordedActivityTitle: (String) -> Unit,
    setIsActiveTimeSetUp: (Boolean) -> Unit,
    setTaskDate: (LocalDate?) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    startRecording: (String) -> Unit,
    stopRecording: () -> Unit,
) {
    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
    val selectedDate = userInput.selectedDate
    val showActivityScreen = dayUiState.isActivityDisplay

    var state by remember { mutableStateOf(State.Task) }
    var button1State by remember { mutableStateOf(State.Goal) }
    var button2State by remember { mutableStateOf(State.ToDo) }

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = when (state) {
                        State.Goal -> "Goals"// TODO: Read string from resource
                        State.ToDo -> "TO-DO List"// TODO: Read string from resource
                        else -> "${selectedDate.format(formatter)}"
                    },
                    color = Color(TOP_BAR_TEXT_COLOR)
                ) },
                colors = TopAppBarDefaults.topAppBarColors(Color(TOP_BAR_COLOR))
            )
        },
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
                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))

                    // If the selected date is today or in the future
                    if (!selectedDate.isBefore(LocalDate.now())) {
                        // If the tasks screen is to be shown
                        if (!showActivityScreen) {
                            // Add button
                            IconButton(
                                onClick = {
                                    when (state) {
                                        State.Goal -> {
                                            selectGoal(null)
                                            onNavigateToGoalEdit()
                                        }
                                        State.Task -> {
                                            selectTask(null)
                                            setTaskDate(userInput.selectedDate)
                                            onNavigateToTaskEdit()
                                        }
                                        State.ToDo -> {
                                            selectTask(null)
                                            setTaskDate(null)
                                            onNavigateToTaskEdit()
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.add_square_svgrepo_com),
                                    contentDescription = "Add task",
                                    modifier = Modifier.fillMaxSize(0.8F),
                                    tint = Color(BOTTOM_BAR_TEXT_COLOR)
                                )
                            }

                            // Button 1
                            IconButton(
                                onClick = {
                                    when (button1State) {
                                        State.Goal -> {
                                            button1State = if (button2State == State.ToDo) State.Task else State.ToDo
                                            state = State.Goal
                                        }
                                        State.Task -> {
                                            button1State = if (button2State == State.ToDo) State.Goal else State.ToDo
                                            state = State.Task
                                        }
                                        State.ToDo -> {
                                            button1State = if (button2State == State.Task) State.Goal else State.Task
                                            state = State.ToDo
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = when (button1State) {
                                            State.Goal -> {
                                                ImageVector.vectorResource(id = R.drawable.target_love_svgrepo_com)
                                            }
                                            State.Task -> {
                                                ImageVector.vectorResource(id = R.drawable.clock_svgrepo_com)
                                            }
                                            State.ToDo -> {
                                                ImageVector.vectorResource(id = R.drawable.list_svgrepo_com)
                                            }
                                        },
                                    contentDescription = when (button1State) {
                                        State.Goal -> {
                                            "Goals"
                                        }
                                        State.Task -> {
                                            "Tasks"
                                        }
                                        State.ToDo -> {
                                            "ToDo"
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize(0.8F),
                                    tint = Color(BOTTOM_BAR_TEXT_COLOR)
                                )
                            }

                            // Button 2
                            IconButton(
                                onClick = {
                                    when (button2State) {
                                        State.Goal -> {
                                            button2State = if (button1State == State.ToDo) State.Task else State.ToDo
                                            state = State.Goal
                                        }
                                        State.Task -> {
                                            button2State = if (button1State == State.ToDo) State.Goal else State.ToDo
                                            state = State.Task
                                        }
                                        State.ToDo -> {
                                            button2State = if (button1State == State.Task) State.Goal else State.Task
                                            state = State.ToDo
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = when (button2State) {
                                        State.Goal -> {
                                            ImageVector.vectorResource(id = R.drawable.target_love_svgrepo_com)
                                        }
                                        State.Task -> {
                                            ImageVector.vectorResource(id = R.drawable.clock_activity_svgrepo_com)
                                        }
                                        State.ToDo -> {
                                            ImageVector.vectorResource(id = R.drawable.list_svgrepo_com)
                                        }
                                    },
                                    contentDescription = when (button2State) {
                                        State.Goal -> {
                                            "Goals"
                                        }
                                        State.Task -> {
                                            "Tasks"
                                        }
                                        State.ToDo -> {
                                            "ToDo"
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize(0.8F),
                                    tint = Color(BOTTOM_BAR_TEXT_COLOR)
                                )
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
                                        modifier = Modifier.fillMaxSize(0.8F),
                                        tint = Color(BOTTOM_BAR_TEXT_COLOR)
                                    )
                                }
                                else {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.graph_infographic_data_element_2_svgrepo_com),
                                        contentDescription = "Task display",
                                        modifier = Modifier.fillMaxSize(0.8F),
                                        tint = Color(BOTTOM_BAR_TEXT_COLOR)
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
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current

                ActivityGraph(
                    dayState = dayState,
                    onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
                    selectActivity = selectActivity,
                    selectTask = selectTask
                )

//                Row (
//                    modifier = Modifier
//                        .padding(
//                            horizontal = 10.dp,
//                            vertical = 5.dp
//                        ),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Day note
//                    OutlinedTextField(
//                        value = dayUiState.note,
//                        onValueChange = onSetDayNote,
//                        modifier = Modifier
//                            .width(325.dp)
//                            .height(190.dp)
//                            .focusRequester(focusRequester),
//                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
//                        textStyle = TextStyle(
//                            fontSize = 16.sp,
//                            color = Color(HOUR_LABEL_COLOR)
//                        ),
//                        label = { Text("Day Notes") },
//                        singleLine = false,
//                        shape = RoundedCornerShape(15.dp)
//                    )
//
//                    Spacer(Modifier.width(5.dp))
//
//                    Column (
//                        horizontalAlignment = Alignment.End,
//                        verticalArrangement = Arrangement.Center
//                    ) {
//                        // Save button
//                        IconButton(
//                            onClick = {
//                                saveDay()
//                                focusManager.clearFocus()
//                            }
//                        ) {
//                            Icon(
//                                imageVector = ImageVector.vectorResource(id = R.drawable.save_alt_svgrepo_com),
//                                contentDescription = "Save day note",
//                                modifier = Modifier
//                                    .fillMaxSize(0.8f)
//                                ,
//                                tint = Color(BOTTOM_BAR_TEXT_COLOR)
//                            )
//                        }
//                    }
//                }

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
                PlannerScreen(
                    innerPadding = innerPadding,
                    dayState = dayState,
                    dayUiState = dayUiState,
                    goals = goals,
                    state = state,
                    taskUiState = taskUiState,
                    toDoTasks = toDoTasks,
                    userInput = userInput,
                    deleteGoal = deleteGoal,
                    onClickSaveActiveTime = onClickSaveActiveTime,
                    onNavigateToGoalEdit = onNavigateToGoalEdit,
                    onNavigateToTaskEdit = onNavigateToTaskEdit,
                    onNavigateToTaskInfo = onNavigateToTaskInfo,
                    onSwitchGoals = onSwitchGoals,
                    saveTask = saveTask,
                    selectGoal = selectGoal,
                    selectTask = selectTask,
                    setActiveTimeStart = setActiveTimeStart,
                    setActiveTimeEnd = setActiveTimeEnd,
                    setIsActiveTimeSetUp = setIsActiveTimeSetUp,
                    onSetSelectedDate = onSetSelectedDate,
                    setTaskStartTime = setTaskStartTime,
                    setTaskEndTime = setTaskEndTime,
                    setTaskDate = setTaskDate
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
                    recordedActivityUiState = recordedActivityUiState,
                    stopwatchService = stopwatchService,
                    clearRecordedActivity = clearRecordedActivity,
                    onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
                    removeVoiceNote = deleteVoiceNote,
                    setActualActiveTimeEnd = setActualActiveTimeEnd,
                    setActualActiveTimeStart = setActualActiveTimeStart,
                    setRecordedActivityTitle = setRecordedActivityTitle,
                    saveDay = saveDay,
                    saveRecordedActivity = saveRecordedActivity,
                    saveVoiceNote = saveVoiceNote,
                    selectActivity = selectActivity,
                    selectTask = selectTask,
                    setRecordedActivityEndTime = setRecordedActivityEndTime,
                    setRecordedActivityId = setRecordedActivityId,
                    setRecordedActivityNote = setRecordedActivityNote,
                    setRecordedActivityStartTime = setRecordedActivityStartTime,
                    startRecording = startRecording,
                    stopRecording = stopRecording
                )
            }
        }
    }
}