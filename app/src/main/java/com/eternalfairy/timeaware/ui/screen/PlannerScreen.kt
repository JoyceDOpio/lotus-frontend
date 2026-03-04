package com.eternalfairy.timeaware.ui.screen

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
import androidx.compose.ui.unit.dp
import com.eternalfairy.timeaware.R
import com.eternalfairy.timeaware.data.Goal
import com.eternalfairy.timeaware.data.Task
import com.eternalfairy.timeaware.data.Time
import com.eternalfairy.timeaware.data.VoiceNote
import com.eternalfairy.timeaware.service.StopwatchService
import com.eternalfairy.timeaware.ui.component.ActivityGraph
import com.eternalfairy.timeaware.ui.component.ActivityRecorder
import com.eternalfairy.timeaware.ui.component.Calendar
import com.eternalfairy.timeaware.ui.component.ComparisonDial
import com.eternalfairy.timeaware.ui.component.PopupDialog
import java.util.UUID
import com.eternalfairy.timeaware.ui.viewmodel.ActivityUiState
import com.eternalfairy.timeaware.ui.viewmodel.DayState
import com.eternalfairy.timeaware.ui.viewmodel.DayUiState
import com.eternalfairy.timeaware.ui.viewmodel.GoalUiState
import com.eternalfairy.timeaware.ui.viewmodel.TaskUiState
import com.eternalfairy.timeaware.ui.viewmodel.UserInput
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

enum class ActivityPopupState {
    ActivityRecorder,
    Notes
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    context: Context,
    dayUiState: DayUiState,
    dayState: DayState,
    goals: List<Goal>,
    goalUiState: GoalUiState,
    lastGoalPriority: Int?,
    lastTaskPriority: Int?,
    recordedMainActivityUiState: ActivityUiState,
    recordedSubActivityUiState: ActivityUiState,
    stopwatchService: StopwatchService,
    taskUiState: TaskUiState,
    toDoTasks: List<Task>,
    userInput: UserInput,
    clearMainRecordedActivity: () -> Unit,
    clearSubRecordedActivity: () -> Unit,
    deleteGoal: (Goal) -> Unit,
    deleteTask: () -> Unit,
    onMoveToCalendar: () -> Unit,
    onMoveToToDoList: () -> Unit,
    onNavigateToTaskActivityComparison: () -> Unit,
    onClickSaveActiveTime: () -> Unit,
    onPinTask: (Boolean) -> Unit,
    onSetSelectedDate: (LocalDate) -> Unit,
    onSwitchScreen: (Boolean) -> Unit,
    saveDay: () -> Unit,
    saveGoal: (Goal) -> Unit,
    saveGoalFromState: () -> Unit,
    saveMainRecordedActivity: () -> Unit,
    saveSubRecordedActivity: () -> Unit,
    saveTask: (Task) -> Unit,
    saveTaskFromState: () -> Unit,
    saveVoiceNote: (VoiceNote) -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectGoal: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
    setActiveTimeEnd: (Time) -> Unit,
    setActiveTimeStart: (Time) -> Unit,
    setActualActiveTimeEnd: (Time) -> Unit,
    setActualActiveTimeStart: (Time) -> Unit,
    setDayNote: (String) -> Unit,
    setGoalPriority: (Int) -> Unit,
    setGoalTitle: (String) -> Unit,
    setRecordedMainActivityDate: (LocalDate) -> Unit,
    setRecordedMainActivityEndTime: (Time) -> Unit,
    setRecordedMainActivityId: (UUID) -> Unit,
    setRecordedMainActivityNote: (String) -> Unit,
    setRecordedMainActivityStartTime: (Time) -> Unit,
    setRecordedMainActivityTitle: (String) -> Unit,
    setRecordedSubActivityEndTime: (Time) -> Unit,
    setRecordedSubActivityId: (UUID) -> Unit,
    setRecordedSubActivityMainActivityId: (UUID) -> Unit,
    setRecordedSubActivityNote: (String) -> Unit,
    setRecordedSubActivityStartTime: (Time) -> Unit,
    setRecordedSubActivityTitle: (String) -> Unit,
    setTaskDate: (LocalDate?) -> Unit,
    setTaskDescription: (String) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskPriority: (Int) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskTitle: (String) -> Unit,
    startRecording: (String) -> Unit,
    stopRecording: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
    val selectedDate = userInput.selectedDate

    var displayState by remember { mutableStateOf(State.Task) }
    var button1State by remember { mutableStateOf(State.Goal) }
    var button2State by remember { mutableStateOf(State.ToDo) }

    var showPopupWindow by remember { mutableStateOf(false) }

    val showActivityScreen = dayUiState.isActivityDisplay
    var activityPopupState by remember { mutableStateOf(ActivityPopupState.ActivityRecorder) }

    val isSubActivityTimerRunning  = (recordedSubActivityUiState.id != null)

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = when (displayState) {
                        State.Goal -> "Goals"// TODO: Read string from resource
                        State.ToDo -> "To Do"// TODO: Read string from resource
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
                    if (showActivityScreen) {
                        IconButton(
                            onClick = { onSwitchScreen(false) }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                                contentDescription = "Back",
                                modifier = Modifier.fillMaxSize(0.8F),
                                tint = Color(BOTTOM_BAR_TEXT_COLOR)
                            )
                        }
                    }
                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar

                    // If the selected date is today or in the future
                    if (!selectedDate.isBefore(LocalDate.now())) {
                        // If the tasks screen is to be shown
                        if (!showActivityScreen) {
                            // Add button
                            IconButton(
                                onClick = {
                                    when (displayState) {
                                        State.Goal -> {
                                            selectGoal(null)
                                        }
                                        State.Task -> {
                                            selectTask(null)
                                            setTaskDate(userInput.selectedDate)
                                        }
                                        State.ToDo -> {
                                            selectTask(null)
                                            setTaskDate(null)
                                        }
                                    }

                                    showPopupWindow = true
                                }
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.add_square_svgrepo_com),
                                    contentDescription = "Add",
                                    modifier = Modifier.fillMaxSize(0.8F),
                                    tint = Color(BOTTOM_BAR_TEXT_COLOR)
                                )
                            }

                            // The Spacer pushes the other icons to the end of the app bar
                            Spacer(Modifier.weight(1f, true))

                            // Button 1
                            IconButton(
                                onClick = {
                                    when (button1State) {
                                        State.Goal -> {
                                            button1State = if (button2State == State.ToDo) State.Task else State.ToDo
                                            displayState = State.Goal
                                        }
                                        State.Task -> {
                                            button1State = if (button2State == State.ToDo) State.Goal else State.ToDo
                                            displayState = State.Task
                                        }
                                        State.ToDo -> {
                                            button1State = if (button2State == State.Task) State.Goal else State.Task
                                            displayState = State.ToDo
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
                                                ImageVector.vectorResource(id = R.drawable.pie_chart_svgrepo_com)
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
                                    modifier = Modifier.fillMaxSize(if (button1State == State.Task) 0.75f else 0.8f),
                                    tint = Color(BOTTOM_BAR_TEXT_COLOR)
                                )
                            }

                            Spacer(Modifier.weight(1f, true))

                            // Button 2
                            IconButton(
                                onClick = {
                                    when (button2State) {
                                        State.Goal -> {
                                            button2State = if (button1State == State.ToDo) State.Task else State.ToDo
                                            displayState = State.Goal
                                        }
                                        State.Task -> {
                                            button2State = if (button1State == State.ToDo) State.Goal else State.ToDo
                                            displayState = State.Task
                                        }
                                        State.ToDo -> {
                                            button2State = if (button1State == State.Task) State.Goal else State.Task
                                            displayState = State.ToDo
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
                                            ImageVector.vectorResource(id = R.drawable.pie_chart_svgrepo_com)
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
                                    modifier = Modifier.fillMaxSize(if (button2State == State.Task) 0.75f else 0.8f),
                                    tint = Color(BOTTOM_BAR_TEXT_COLOR)
                                )
                            }
                        }

                        // If the selected date is in the future, don't show the activities. The activity recording should be reserved only for today.
                        if (!selectedDate.isAfter(LocalDate.now())) {
                            Spacer(Modifier.weight(1f, true))

                            IconButton(onClick = {
                                if (showActivityScreen) {
                                    // Show the activity recorder in a pop-up dialog
                                    showPopupWindow = true
                                }
                                else {
                                    onSwitchScreen(true)
                                }
                            }) {
                                Icon(
                                    imageVector = if (showActivityScreen) ImageVector.vectorResource(id = R.drawable.timer_svgrepo_com) else ImageVector.vectorResource(id = R.drawable.graph_infographic_data_element_2_svgrepo_com),
                                    contentDescription = "Activity recorder",
//                                    modifier = Modifier.fillMaxSize((if (showActivityScreen) 0.8f else 0.7f)),
                                    modifier = Modifier.fillMaxSize(0.75f),
                                    tint = Color(BOTTOM_BAR_TEXT_COLOR)
                                )
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

                Row (
                    modifier = Modifier
                        .weight(1.45f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActivityGraph(
                        dayState = dayState,
                        onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
                        selectActivity = selectActivity,
                        selectTask = selectTask
                    )
                }

                Row (
                    modifier = Modifier
                        .weight(3.5f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ComparisonDial(
                        dayState = dayState,
                        onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
                        selectActivity = selectActivity,
                        selectTask = selectTask
                    )
                }

                Row (
                    modifier = Modifier
                        .weight(1.05f),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Calendar(
                        userInput = userInput,
                        onSetDate = onSetSelectedDate
                    )
                }
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
                    goalUiState = goalUiState,
                    goals = goals,
                    lastGoalPriority = lastGoalPriority,
                    lastTaskPriority = lastTaskPriority,
                    state = displayState,
                    taskUiState = taskUiState,
                    toDoTasks = toDoTasks,
                    userInput = userInput,
                    deleteGoal = deleteGoal,
                    deleteTask = deleteTask,
                    onClickSaveActiveTime = onClickSaveActiveTime,
                    onMoveToCalendar = onMoveToCalendar,
                    onMoveToToDoList = onMoveToToDoList,
                    onPinTask = onPinTask,
                    onSetSelectedDate = onSetSelectedDate,
                    saveGoal = saveGoal,
                    saveGoalFromState = saveGoalFromState,
                    saveTask = saveTask,
                    saveTaskFromState = saveTaskFromState,
                    selectGoal = selectGoal,
                    selectTask = selectTask,
                    setActiveTimeStart = setActiveTimeStart,
                    setActiveTimeEnd = setActiveTimeEnd,
                    setGoalPriority = setGoalPriority,
                    setGoalTitle = setGoalTitle,
                    setTaskDate = setTaskDate,
                    setTaskDescription = setTaskDescription,
                    setTaskEndTime = setTaskEndTime,
                    setTaskPriority = setTaskPriority,
                    setTaskStartTime = setTaskStartTime,
                    setTaskTitle = setTaskTitle,
//                    windowSizeClass = windowSizeClass
                )
            }

            // Show actual activities
            AnimatedVisibility(
                visible = showActivityScreen
            ) {
                ActivityScreen(
                    innerPadding = innerPadding,
                    dayState = dayState,
                    onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
                    selectActivity = selectActivity,
                    selectTask = selectTask,
//                    windowSizeClass = windowSizeClass
                )
            }
        }
    }

    if (showPopupWindow) {
        PopupDialog(
            onDismissRequest = {
                showPopupWindow = false
            }
        ) {
            if (showActivityScreen) {
                when (activityPopupState) {
                    ActivityPopupState.ActivityRecorder -> {
                        ActivityRecorder(
                            context = context,
                            modifier = Modifier
                                .padding(
                                    horizontal = 5.dp,
                                    vertical = 5.dp
                                )
                            ,
                            dayState = dayState,
                            recordedMainActivityUiState = recordedMainActivityUiState,
                            recordedSubActivityUiState = recordedSubActivityUiState,
                            stopwatchService = stopwatchService,
                            clearRecordedMainActivity = clearMainRecordedActivity,
                            clearRecordedSubActivity = clearSubRecordedActivity,
                            onNavigateToActivityNoteEdit = {
                                activityPopupState = ActivityPopupState.Notes
                            },
                            saveRecordedMainActivity = saveMainRecordedActivity,
                            saveRecordedSubActivity = saveSubRecordedActivity,
                            saveDay = saveDay,
                            saveVoiceNote = saveVoiceNote,
                            setActualActiveTimeEnd = setActualActiveTimeEnd,
                            setActualActiveTimeStart = setActualActiveTimeStart,
                            setRecordedMainActivityDate = setRecordedMainActivityDate,
                            setRecordedMainActivityEndTime = setRecordedMainActivityEndTime,
                            setRecordedMainActivityId = setRecordedMainActivityId,
                            setRecordedMainActivityStartTime = setRecordedMainActivityStartTime,
                            setRecordedMainActivityTitle = setRecordedMainActivityTitle,
                            setRecordedSubActivityEndTime = setRecordedSubActivityEndTime,
                            setRecordedSubActivityId = setRecordedSubActivityId,
                            setRecordedSubActivityMainActivityId = setRecordedSubActivityMainActivityId,
                            setRecordedSubActivityStartTime = setRecordedSubActivityStartTime,
                            setRecordedSubActivityTitle = setRecordedSubActivityTitle,
                            startRecording = startRecording,
                            stopRecording = stopRecording,
                        )
                    }
                    ActivityPopupState.Notes -> {
                        ActivityEditScreen(
                            activityEditState = if (isSubActivityTimerRunning) recordedSubActivityUiState else recordedMainActivityUiState,
                            onBack = {
                                activityPopupState = ActivityPopupState.ActivityRecorder
                            },
                            saveActivity = {
                                if (isSubActivityTimerRunning) saveSubRecordedActivity() else saveMainRecordedActivity()
                                activityPopupState = ActivityPopupState.ActivityRecorder
                            },
                            setActivityNote = { value ->
                                if (isSubActivityTimerRunning) setRecordedSubActivityNote(value) else setRecordedMainActivityNote(value)
                            }
                        )
                    }
                }
            }
            else {
                when (displayState) {
                    State.Goal -> {
                        GoalEditScreen(
                            goalUiState = goalUiState,
                            lastPriority = lastGoalPriority ?: 0,
                            onBack = {
                                showPopupWindow = false
                            },
                            saveGoal = saveGoalFromState,
                            setGoalPriority = setGoalPriority,
                            setGoalTitle = setGoalTitle
                        )
                    }
                    State.Task, State.ToDo -> {
                        TaskEditScreen(
                            dayState = dayState,
                            lastTaskPriority = lastTaskPriority ?: 0,
                            taskUiState = taskUiState,
                            onBack = {
                                showPopupWindow = false
                            },
                            saveTask = saveTaskFromState,
                            setTaskStartTime = setTaskStartTime,
                            setTaskEndTime = setTaskEndTime,
                            setTaskDescription = setTaskDescription,
                            setTaskPriority = setTaskPriority,
                            setTaskTitle = setTaskTitle
                        )
                    }
                }
            }
        }
    }
}