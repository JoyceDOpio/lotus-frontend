package com.example.circularplanner.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.R
import com.example.circularplanner.data.Goal
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.data.VoiceNote
import com.example.circularplanner.service.StopwatchService
import com.example.circularplanner.ui.component.ActivityGraph
import com.example.circularplanner.ui.component.Calendar
import com.example.circularplanner.ui.component.PopupDialog
import com.example.circularplanner.ui.component.leftBorder
import com.example.circularplanner.ui.navigation.RecordedActivity
import java.util.UUID
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.DayUiState
import com.example.circularplanner.ui.viewmodel.GoalUiState
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.ui.viewmodel.UserInput
import com.example.circularplanner.utils.NoteModePopup
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
fun PlannerScreen(
    context: Context,
    activityUiState: ActivityUiState,
    dayUiState: DayUiState,
    dayState: DayState,
    goals: List<Goal>,
    goalUiState: GoalUiState,
    lastGoalPriority: Int?,
    lastTaskPriority: Int?,
//    recordedActivityUiState: ActivityUiState,
    mainRecordedActivityUiState: ActivityUiState,
    subRecordedActivityUiState: ActivityUiState,
    stopwatchService: StopwatchService,
    nextTaskUiState: TaskUiState,
    previousTaskUiState: TaskUiState,
    taskUiState: TaskUiState,
    toDoTasks: List<Task>,
    userInput: UserInput,
//    clearRecordedActivity: () -> Unit,
    clearMainRecordedActivity: () -> Unit,
    clearSubRecordedActivity: () -> Unit,
    deleteGoal: (Goal) -> Unit,
    deleteTask: () -> Unit,
//    deleteVoiceNote: (VoiceNoteUiState) ->Unit,
    onMoveToToDoList: () -> Unit,
    onNavigateToTaskActivityComparison: () -> Unit,
    onClickSaveActiveTime: () -> Unit,
    onSetDayNote: (String) -> Unit,
    onSetSelectedDate: (LocalDate) -> Unit,
    onSwitchGoals: (UUID, UUID) -> Unit,
    onSwitchScreen: (Boolean) -> Unit,
    saveActivity: () -> Unit,
    saveDay: () -> Unit,
    saveGoal: (Goal) -> Unit,
    saveGoalFromState: () -> Unit,
//    saveRecordedActivity: () -> Unit,
    saveMainRecordedActivity: () -> Unit,
    saveSubRecordedActivity: () -> Unit,
    saveNextTask: () -> Unit,
    saveTask: (Task) -> Unit,
    saveTaskFromState: () -> Unit,
    saveVoiceNote: (VoiceNote) -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectGoal: (UUID?) -> Unit,
    selectNextTask: (Task) -> Unit,
    selectPreviousTask: (Task) -> Unit,
    selectTask: (UUID?) -> Unit,
    setActiveTimeEnd: (Time) -> Unit,
    setActiveTimeStart: (Time) -> Unit,
    setActivityNote: (String) -> Unit,
//    setMainActivityNote: (String) -> Unit,
//    setSubActivityNote: (String) -> Unit,
    setActualActiveTimeEnd: (Time) -> Unit,
    setActualActiveTimeStart: (Time) -> Unit,
    setDayNote: (String) -> Unit,
    setGoalPriority: (Int) -> Unit,
    setGoalTitle: (String) -> Unit,
//    setRecordedActivityEndTime: (Time) -> Unit,
    setMainRecordedActivityEndTime: (Time) -> Unit,
    setSubRecordedActivityEndTime: (Time) -> Unit,
//    setRecordedActivityId: (UUID) -> Unit,
    setMainRecordedActivityId: (UUID) -> Unit,
    setSubRecordedActivityId: (UUID) -> Unit,
//    setRecordedActivityNote: (String) -> Unit,
    setMainRecordedActivityNote: (String) -> Unit,
    setSubRecordedActivityNote: (String) -> Unit,
//    setRecordedActivityStartTime: (Time) -> Unit,
    setMainRecordedActivityStartTime: (Time) -> Unit,
    setSubRecordedActivityStartTime: (Time) -> Unit,
//    setRecordedActivityTitle: (String) -> Unit,
    setMainRecordedActivityTitle: (String) -> Unit,
    setSubRecordedActivityTitle: (String) -> Unit,
    setIsActiveTimeSetUp: (Boolean) -> Unit,
    setNextTaskStartTime: (Time) -> Unit,
    setNextTaskEndTime: (Time) -> Unit,
    setRecordedActivityState: (RecordedActivity) -> Unit,
    setTaskDate: (LocalDate?) -> Unit,
    setTaskDescription: (String) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskPriority: (Int) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskTitle: (String) -> Unit,
    startRecording: (String) -> Unit,
    stopRecording: () -> Unit,
) {
    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
    val selectedDate = userInput.selectedDate
    val showActivityScreen = dayUiState.isActivityDisplay

    var displayState by remember { mutableStateOf(State.Task) }
    var button1State by remember { mutableStateOf(State.Goal) }
    var button2State by remember { mutableStateOf(State.ToDo) }

    var showPopupWindow by remember { mutableStateOf(false) }

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = when (displayState) {
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
                                                ImageVector.vectorResource(id = R.drawable.clock_svgrepo_com)
                                            }
                                            State.ToDo -> {
                                                ImageVector.vectorResource(id = R.drawable.list_svgrepo_com)
                                            }
                                        },
                                    contentDescription = when (button1State) {
                                        State.Goal -> {
                                            "Goals"// TODO: Read string from resource
                                        }
                                        State.Task -> {
                                            "Tasks"// TODO: Read string from resource
                                        }
                                        State.ToDo -> {
                                            "ToDo"// TODO: Read string from resource
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
                                            ImageVector.vectorResource(id = R.drawable.clock_svgrepo_com)
                                        }
                                        State.ToDo -> {
                                            ImageVector.vectorResource(id = R.drawable.list_svgrepo_com)
                                        }
                                    },
                                    contentDescription = when (button2State) {
                                        State.Goal -> {
                                            "Goals"// TODO: Read string from resource
                                        }
                                        State.Task -> {
                                            "Tasks"// TODO: Read string from resource
                                        }
                                        State.ToDo -> {
                                            "ToDo"// TODO: Read string from resource
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
//                verticalArrangement = Arrangement.Center
            ) {
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current

                Row (
                    modifier = Modifier
                        .weight(4f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActivityGraph(
                        dayState = dayState,
                        onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
                        selectActivity = selectActivity,
                        selectTask = selectTask
                    )
                }

//                Row (
//                    modifier = Modifier
//                        .padding(
//                            horizontal = 10.dp,
//                            vertical = 5.dp
//                        ),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Day notes
////                    OutlinedTextField(
//                    TextField(
//                        value = dayUiState.note,
////                        onValueChange = onSetDayNote,
//                        onValueChange = { value ->
//                            onSetDayNote(value)
//                            saveDay()
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(190.dp)
//                            .leftBorder(
//                                color = MaterialTheme.colorScheme.primary,
//                                width = 5f
//                            )
//                            .focusRequester(focusRequester)
//                        ,
//                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
//                        textStyle = TextStyle(
//                            fontSize = 16.sp
//                        ),
//                        label = { Text("DAY NOTES") },//TODO: Read string from resource
//                        singleLine = false,
//                        shape = RoundedCornerShape(15.dp),
//                        colors = TextFieldDefaults.textFieldColors(
//                            containerColor = Color.Transparent,
//                            unfocusedPlaceholderColor = Color(0xFF928BA2),
//                            focusedPlaceholderColor = Color(0xFF928BA2),
//                            focusedIndicatorColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent
//
//                        )
//                    )
//                }

//                // Day notes
//                Row(
//                    modifier = Modifier
//                        .padding(
//                            horizontal = 5.dp
//                        )
//                        .padding(
//                            top = 10.dp,
//                            bottom = 5.dp
//                        )
//                        .fillMaxWidth()
//                        .border(
//                            width = 1.dp,
//                            color = Color.LightGray,
//                            shape = RoundedCornerShape(15.dp)
//                        )
//                    ,
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "DAY NOTES",// TODO: Read text from string resource
//                        modifier = Modifier
//                            .padding(
//                                horizontal = 10.dp
//                            )
//                        ,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//
//                    IconButton(
//                        onClick = {
//                            popupState = NoteModePopup.Day
//                            showPopupWindow = true
//                        }
//                    ) {
//                        Icon(
//                            imageVector = ImageVector.vectorResource(id = R.drawable.add_square_svgrepo_com),
//                            contentDescription = "Add",
//                            modifier = Modifier.fillMaxSize(0.8F),
////                    tint = Color(BOTTOM_BAR_TEXT_COLOR)
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }

                Row (
                    modifier = Modifier
                        .weight(1f)
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
                    dayUiState = dayUiState,
                    goalUiState = goalUiState,
                    goals = goals,
                    lastGoalPriority = lastGoalPriority,
                    lastTaskPriority = lastTaskPriority,
                    state = displayState,
                    nextTaskUiState = nextTaskUiState,
                    previousTaskUiState = previousTaskUiState,
                    taskUiState = taskUiState,
                    toDoTasks = toDoTasks,
                    userInput = userInput,
                    deleteGoal = deleteGoal,
                    deleteTask = deleteTask,
                    onClickSaveActiveTime = onClickSaveActiveTime,
                    onMoveToToDoList = onMoveToToDoList,
                    onSetSelectedDate = onSetSelectedDate,
                    onSwitchGoals = onSwitchGoals,
                    saveGoal = saveGoal,
                    saveGoalFromState = saveGoalFromState,
                    saveNextTask = saveNextTask,
                    saveTask = saveTask,
                    saveTaskFromState = saveTaskFromState,
                    selectGoal = selectGoal,
                    selectNextTask = selectNextTask,
                    selectPreviousTask = selectPreviousTask,
                    selectTask = selectTask,
                    setActiveTimeStart = setActiveTimeStart,
                    setActiveTimeEnd = setActiveTimeEnd,
                    setGoalPriority = setGoalPriority,
                    setGoalTitle = setGoalTitle,
                    setIsActiveTimeSetUp = setIsActiveTimeSetUp,
                    setNextTaskEndTime = setNextTaskEndTime,
                    setNextTaskStartTime = setNextTaskStartTime,
                    setTaskDate = setTaskDate,
                    setTaskDescription = setTaskDescription,
                    setTaskEndTime = setTaskEndTime,
                    setTaskPriority = setTaskPriority,
                    setTaskStartTime = setTaskStartTime,
                    setTaskTitle = setTaskTitle
                )
            }

            // Show actual activities
            AnimatedVisibility(
                visible = showActivityScreen
            ) {
                ActivityScreen(
                    innerPadding = innerPadding,
                    context = context,
                    activityUiState = activityUiState,
                    dayState = dayState,
                    dayUiState = dayUiState,
                    mainRecordedActivityUiState = mainRecordedActivityUiState,
                    subRecordedActivityUiState = subRecordedActivityUiState,
                    stopwatchService = stopwatchService,
                    clearMainRecordedActivity = clearMainRecordedActivity,
                    clearSubRecordedActivity = clearSubRecordedActivity,
                    onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
//                    removeVoiceNote = deleteVoiceNote,
//                    setMainActivityNote = setMainActivityNote,
//                    setSubActivityNote = setSubActivityNote,
                    setActualActiveTimeEnd = setActualActiveTimeEnd,
                    setActualActiveTimeStart = setActualActiveTimeStart,
                    setMainRecordedActivityTitle = setMainRecordedActivityTitle,
                    setSubRecordedActivityTitle = setSubRecordedActivityTitle,
                    saveActivity = saveActivity,
                    saveDay = saveDay,
                    saveMainRecordedActivity = saveMainRecordedActivity,
                    saveSubRecordedActivity = saveSubRecordedActivity,
                    saveVoiceNote = saveVoiceNote,
                    selectActivity = selectActivity,
                    selectTask = selectTask,
                    setActivityNote = setActivityNote,
                    setDayNote = setDayNote,
                    setMainRecordedActivityEndTime = setMainRecordedActivityEndTime,
                    setSubRecordedActivityEndTime = setSubRecordedActivityEndTime,
                    setMainRecordedActivityId = setMainRecordedActivityId,
                    setSubRecordedActivityId = setSubRecordedActivityId,
//                    setMainRecordedActivityNote = setMainRecordedActivityNote,
//                    setSubRecordedActivityNote = setSubRecordedActivityNote,
                    setMainRecordedActivityStartTime = setMainRecordedActivityStartTime,
                    setSubRecordedActivityStartTime = setSubRecordedActivityStartTime,
                    setRecordedActivityState = setRecordedActivityState,
                    startRecording = startRecording,
                    stopRecording = stopRecording
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
            when (displayState) {
//                State.Day -> {
//                    DayNoteEditScreen(
//                        dayUiState = dayUiState,
//                        onBack = {
//                            showPopupWindow = false
//                        },
//                        saveDay = saveDay,
//                        setDayNote = setDayNote
//                    )
//                }
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