package com.eternalfairy.timeaware.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eternalfairy.timeaware.data.Goal
import com.eternalfairy.timeaware.data.Task
import com.eternalfairy.timeaware.data.Time
import com.eternalfairy.timeaware.data.VoiceNote
import com.eternalfairy.timeaware.service.StopwatchService
import com.eternalfairy.timeaware.ui.component.ActiveTimeSetUp
import com.eternalfairy.timeaware.ui.component.ActivityRecorder
import com.eternalfairy.timeaware.ui.component.PopupDialog
import java.util.UUID
import com.eternalfairy.timeaware.ui.viewmodel.ActivityUiState
import com.eternalfairy.timeaware.ui.viewmodel.AudioViewModel
import com.eternalfairy.timeaware.ui.viewmodel.DayState
import com.eternalfairy.timeaware.ui.viewmodel.DayUiState
import com.eternalfairy.timeaware.ui.viewmodel.GoalUiState
import com.eternalfairy.timeaware.ui.viewmodel.TaskUiState
import com.eternalfairy.timeaware.ui.viewmodel.UserInput
import com.eternalfairy.timeaware.utils.WindowSize
import com.eternalfairy.timeaware.utils.WindowType
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//const val TOP_BAR_COLOR = 0xFFffffff
//const val TOP_BAR_TEXT_COLOR = 0xFF6650a4
//const val BOTTOM_BAR_COLOR = 0xFFffffff
//const val BOTTOM_BAR_TEXT_COLOR = 0xFF3D3061
//
enum class SmallScreenState {
    // Comparison of task and activity
    Comparison,
    // Day overview: tasks
    DayTask,
    // Day overview: activities
    DayActivity,
    Goal,
    ToDo
}

enum class SmallScreenButtonState {
    // Day overview
    Day,
    Goal,
    ToDo
}

enum class BigScreenMainPanelState {
    // Comparison of task and activity
    Comparison,
    DayActivity,
    DayTask
}

enum class BigScreenSidePanelState {
    Goal,
    ToDo
}

//enum class ActivityPopupState {
//    ActivityRecorder,
//    Notes
//}

//enum class TaskActivityComparisonModePopup {
//    Activity,
//    Task,
//    VoiceNote
//}

enum class PopupState {
    ActiveTimeSetup,
    ActivityRecorder,
    DeleteActivity,
    DeleteTask,
    DeleteVoiceNote,
    EditActivity,
    EditActivityNotes,
    EditGoal,
    EditTask
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    context: Context,
    activityEditState: ActivityUiState,
    activityUiState: ActivityUiState,
//    dayUiState: DayUiState,
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
    windowSize: WindowSize,
    clearMainRecordedActivity: () -> Unit,
    clearSubRecordedActivity: () -> Unit,
    deleteActivity: () -> Unit,
    deleteGoal: (Goal) -> Unit,
    deleteTask: () -> Unit,
    deleteVoiceNote: () -> Unit,
    onMoveToCalendar: () -> Unit,
    onMoveToToDoList: () -> Unit,
//    onNavigateToTaskActivityComparison: () -> Unit,
    onClickSaveActiveTime: () -> Unit,
    onPinTask: (Boolean) -> Unit,
    onSetSelectedDate: (LocalDate) -> Unit,
//    onSwitchScreen: (Boolean) -> Unit,
    saveActivity: () -> Unit,
    saveDay: () -> Unit,
    saveGoal: (Goal) -> Unit,
    saveGoalFromState: () -> Unit,
    saveMainRecordedActivity: () -> Unit,
    saveSubRecordedActivity: () -> Unit,
    saveTask: (Task) -> Unit,
    saveTaskFromState: () -> Unit,
    saveVoiceNote: (VoiceNote) -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectActivityToBeDeleted: (UUID?) -> Unit,
    selectActivityToBeEdited: (UUID?) -> Unit,
    selectGoal: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
    selectVoiceNoteToBeDeleted: (UUID?) -> Unit,
    setActiveTimeEnd: (Time) -> Unit,
    setActiveTimeStart: (Time) -> Unit,
    setActivityNote: (String) -> Unit,
    setActivityTitle: (String) -> Unit,
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
    stopRecording: () -> Unit,
    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
    val selectedDate = userInput.selectedDate

//    // Small screen state (smartphone)
//    var smallScreenState by remember { mutableStateOf(SmallScreenState.DayTask) }
//    var smallScreenButton1State by remember { mutableStateOf(SmallScreenButtonState.Goal) }
//    var smallScreenButton2State by remember { mutableStateOf(SmallScreenButtonState.ToDo) }
//
//    // Big screen state (tablet)
//    var bigScreenMainPanelState by remember { mutableStateOf(BigScreenMainPanelState.DayTask) }
//    var bigScreenSidePanelState by remember { mutableStateOf(BigScreenSidePanelState.ToDo) }
//
//    var showPopupWindowSmallScreen by remember { mutableStateOf(false) }
//    var showPopupWindowBigScreen by remember { mutableStateOf(false) }
//
////    val showActivityScreen = dayUiState.isActivityDisplay
//    var activityPopupState by remember { mutableStateOf(ActivityPopupState.ActivityRecorder) }

    val isSubActivityTimerRunning  = (recordedSubActivityUiState.id != null)

//    var showActiveTimeSetUp by remember { mutableStateOf(false) }

    // Comparison of task and activity
//    val taskDetails = taskUiState
//    val activityDetails = activityUiState
    var showPopupWindow by remember { mutableStateOf(false) }
//    var popupState by remember { mutableStateOf(TaskActivityComparisonModePopup.Task) }
    var popupState by remember { mutableStateOf(PopupState.EditTask) }
//    var isEditing by remember { mutableStateOf(true) }
    val audioViewModel: AudioViewModel = viewModel(factory = AudioViewModel.Factory)

    // We need a layout for each of 4 cases:
    // - smartphone: portrait view (height: medium, width: compact)
    if (windowSize.height == WindowType.Medium && windowSize.width == WindowType.Compact) {
        SmallScreenPortrait (
            activityUiState = activityUiState,
            audioViewModel = audioViewModel,
            dayState = dayState,
            goals = goals,
            goalUiState = goalUiState,
            lastGoalPriority = lastGoalPriority,
            lastTaskPriority = lastTaskPriority,
            taskUiState = taskUiState,
            toDoTasks = toDoTasks,
            userInput = userInput,
            deleteGoal = deleteGoal,
            deleteTask = deleteTask,
            deleteVoiceNote = deleteVoiceNote,
            onDeleteActivity = { id ->
                // Pass the id of the main activity to mark it for deletion
                selectActivityToBeDeleted(id)
                popupState = PopupState.DeleteActivity
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Activity
//                isEditing = false

            },
            onEditActivity = { id ->
                // Pass the id of the main activity to mark it for editing
                selectActivityToBeEdited(id)
                popupState = PopupState.EditActivity
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Activity
//                isEditing = true
            },
            onDeleteTask = {
                popupState = PopupState.DeleteTask
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Task
//                isEditing = false
            },
            onEditTask = {
                popupState = PopupState.EditTask
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Task
//                isEditing = true
            },
            onDeleteVoiceNote = { id ->
                selectVoiceNoteToBeDeleted(id)
                popupState = PopupState.DeleteVoiceNote
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.VoiceNote
//                isEditing = false
            },
            onMoveToCalendar = onMoveToCalendar,
            onMoveToToDoList = onMoveToToDoList,
            onPinTask = onPinTask,
            onPressActiveTime = {
                popupState = PopupState.ActiveTimeSetup
                showPopupWindow = true
//                showActiveTimeSetUp = true
            },
            onSetSelectedDate = onSetSelectedDate,
            onShowPopupWindow = { state ->
                popupState = state
                showPopupWindow = true
//                showPopupWindowSmallScreen = true
            },
            saveGoal = saveGoal,
            saveGoalFromState = saveGoalFromState,
            saveTask = saveTask,
            saveTaskFromState = saveTaskFromState,
            selectActivity = selectActivity,
            selectGoal = selectGoal,
            selectTask = selectTask,
            selectVoiceNoteToBeDeleted = selectVoiceNoteToBeDeleted,
            setGoalPriority = setGoalPriority,
            setGoalTitle = setGoalTitle,
            setTaskDate = setTaskDate,
            setTaskDescription = setTaskDescription,
            setTaskEndTime = setTaskEndTime,
            setTaskPriority = setTaskPriority,
            setTaskStartTime = setTaskStartTime,
            setTaskTitle = setTaskTitle,
            updateLastPlayedPosition = updateLastPlayedPosition
        )
    }
    // - smartphone: landscape view (height: compact, width: medium)
    else if (windowSize.height == WindowType.Compact && windowSize.width == WindowType.Medium) {
        SmallScreenLandscape (
            activityUiState = activityUiState,
            audioViewModel = audioViewModel,
            dayState = dayState,
            goals = goals,
            goalUiState = goalUiState,
            lastGoalPriority = lastGoalPriority,
            lastTaskPriority = lastTaskPriority,
            taskUiState = taskUiState,
            toDoTasks = toDoTasks,
            userInput = userInput,
            deleteGoal = deleteGoal,
            deleteTask = deleteTask,
            deleteVoiceNote = deleteVoiceNote,
            onDeleteActivity = { id ->
                // Pass the id of the main activity to mark it for deletion
                selectActivityToBeDeleted(id)
                popupState = PopupState.DeleteActivity
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Activity
//                isEditing = false

            },
            onEditActivity = { id ->
                // Pass the id of the main activity to mark it for editing
                selectActivityToBeEdited(id)
                popupState = PopupState.EditActivity
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Activity
//                isEditing = true
            },
            onDeleteTask = {
                popupState = PopupState.DeleteTask
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Task
//                isEditing = false
            },
            onEditTask = {
                popupState = PopupState.EditTask
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Task
//                isEditing = true
            },
            onDeleteVoiceNote = { id ->
                selectVoiceNoteToBeDeleted(id)
                popupState = PopupState.DeleteVoiceNote
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.VoiceNote
//                isEditing = false
            },
            onMoveToCalendar = onMoveToCalendar,
            onMoveToToDoList = onMoveToToDoList,
            onPinTask = onPinTask,
            onPressActiveTime = {
                popupState = PopupState.ActiveTimeSetup
                showPopupWindow = true
//                showActiveTimeSetUp = true
            },
            onSetSelectedDate = onSetSelectedDate,
            onShowPopupWindow = { state ->
                popupState = state
                showPopupWindow = true
//                showPopupWindowSmallScreen = true
            },
            saveGoal = saveGoal,
            saveGoalFromState = saveGoalFromState,
            saveTask = saveTask,
            saveTaskFromState = saveTaskFromState,
            selectActivity = selectActivity,
            selectGoal = selectGoal,
            selectTask = selectTask,
            selectVoiceNoteToBeDeleted = selectVoiceNoteToBeDeleted,
            setGoalPriority = setGoalPriority,
            setGoalTitle = setGoalTitle,
            setTaskDate = setTaskDate,
            setTaskDescription = setTaskDescription,
            setTaskEndTime = setTaskEndTime,
            setTaskPriority = setTaskPriority,
            setTaskStartTime = setTaskStartTime,
            setTaskTitle = setTaskTitle,
            updateLastPlayedPosition = updateLastPlayedPosition
        )
    }
    // - tablet: portrait view (height: expanded, width: medium)
    else if (windowSize.height == WindowType.Expanded && windowSize.width == WindowType.Medium) {
        BigScreenPortrait(
            activityUiState = activityUiState,
            audioViewModel = audioViewModel,
            dayState = dayState,
            goals = goals,
            goalUiState = goalUiState,
            lastGoalPriority = lastGoalPriority,
            lastTaskPriority = lastTaskPriority,
            taskUiState = taskUiState,
            toDoTasks = toDoTasks,
            userInput = userInput,
            deleteGoal = deleteGoal,
            deleteTask = deleteTask,
            deleteVoiceNote = deleteVoiceNote,
            onDeleteActivity = { id ->
                // Pass the id of the main activity to mark it for deletion
                selectActivityToBeDeleted(id)
                popupState = PopupState.DeleteActivity
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Activity
//                isEditing = false

            },
            onEditActivity = { id ->
                // Pass the id of the main activity to mark it for editing
                selectActivityToBeEdited(id)
                popupState = PopupState.EditActivity
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Activity
//                isEditing = true
            },
            onDeleteTask = {
                popupState = PopupState.DeleteTask
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Task
//                isEditing = false
            },
            onEditTask = {
                popupState = PopupState.EditTask
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Task
//                isEditing = true
            },
            onDeleteVoiceNote = { id ->
                selectVoiceNoteToBeDeleted(id)
                popupState = PopupState.DeleteVoiceNote
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.VoiceNote
//                isEditing = false
            },
            onMoveToCalendar = onMoveToCalendar,
            onMoveToToDoList = onMoveToToDoList,
            onPinTask = onPinTask,
            onPressActiveTime = {
                popupState = PopupState.ActiveTimeSetup
                showPopupWindow = true
//                showActiveTimeSetUp = true
            },
            onSetSelectedDate = onSetSelectedDate,
            onShowPopupWindow = { state ->
                popupState = state
                showPopupWindow = true
//                showPopupWindowSmallScreen = true
            },
            saveGoal = saveGoal,
            saveGoalFromState = saveGoalFromState,
            saveTask = saveTask,
            saveTaskFromState = saveTaskFromState,
            selectActivity = selectActivity,
            selectGoal = selectGoal,
            selectTask = selectTask,
            selectVoiceNoteToBeDeleted = selectVoiceNoteToBeDeleted,
            setGoalPriority = setGoalPriority,
            setGoalTitle = setGoalTitle,
            setTaskDate = setTaskDate,
            setTaskDescription = setTaskDescription,
            setTaskEndTime = setTaskEndTime,
            setTaskPriority = setTaskPriority,
            setTaskStartTime = setTaskStartTime,
            setTaskTitle = setTaskTitle,
            updateLastPlayedPosition = updateLastPlayedPosition
        )
    }
    // - tablet: landscape view (height: medium, width: expanded)
    else if (windowSize.height == WindowType.Medium && windowSize.width == WindowType.Expanded) {
        BigScreenLandscape (
            activityUiState = activityUiState,
            audioViewModel = audioViewModel,
            dayState = dayState,
            goals = goals,
            goalUiState = goalUiState,
            lastGoalPriority = lastGoalPriority,
            lastTaskPriority = lastTaskPriority,
            taskUiState = taskUiState,
            toDoTasks = toDoTasks,
            userInput = userInput,
            deleteGoal = deleteGoal,
            deleteTask = deleteTask,
            deleteVoiceNote = deleteVoiceNote,
            onDeleteActivity = { id ->
                // Pass the id of the main activity to mark it for deletion
                selectActivityToBeDeleted(id)
                popupState = PopupState.DeleteActivity
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Activity
//                isEditing = false

            },
            onEditActivity = { id ->
                // Pass the id of the main activity to mark it for editing
                selectActivityToBeEdited(id)
                popupState = PopupState.EditActivity
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Activity
//                isEditing = true
            },
            onDeleteTask = {
                popupState = PopupState.DeleteTask
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Task
//                isEditing = false
            },
            onEditTask = {
                popupState = PopupState.EditTask
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.Task
//                isEditing = true
            },
            onDeleteVoiceNote = { id ->
                selectVoiceNoteToBeDeleted(id)
                popupState = PopupState.DeleteVoiceNote
                showPopupWindow = true
//                popupState = TaskActivityComparisonModePopup.VoiceNote
//                isEditing = false
            },
            onMoveToCalendar = onMoveToCalendar,
            onMoveToToDoList = onMoveToToDoList,
            onPinTask = onPinTask,
            onPressActiveTime = {
                popupState = PopupState.ActiveTimeSetup
                showPopupWindow = true
//                showActiveTimeSetUp = true
            },
            onSetSelectedDate = onSetSelectedDate,
            onShowPopupWindow = { state ->
                popupState = state
                showPopupWindow = true
//                showPopupWindowSmallScreen = true
            },
            saveGoal = saveGoal,
            saveGoalFromState = saveGoalFromState,
            saveTask = saveTask,
            saveTaskFromState = saveTaskFromState,
            selectActivity = selectActivity,
            selectGoal = selectGoal,
            selectTask = selectTask,
            selectVoiceNoteToBeDeleted = selectVoiceNoteToBeDeleted,
            setGoalPriority = setGoalPriority,
            setGoalTitle = setGoalTitle,
            setTaskDate = setTaskDate,
            setTaskDescription = setTaskDescription,
            setTaskEndTime = setTaskEndTime,
            setTaskPriority = setTaskPriority,
            setTaskStartTime = setTaskStartTime,
            setTaskTitle = setTaskTitle,
            updateLastPlayedPosition = updateLastPlayedPosition
        )
    }

//    // FIXME: Optimize
//    if (showPopupWindowSmallScreen) {
//        PopupDialog(
//            onDismissRequest = {
//                showPopupWindowSmallScreen = false
//            }
//        ) {
//            if (smallScreenState == SmallScreenState.DayActivity) {
//                when (activityPopupState) {
//                    ActivityPopupState.ActivityRecorder -> {
//                        ActivityRecorder(
//                            context = context,
//                            modifier = Modifier
//                                .padding(
//                                    horizontal = 5.dp,
//                                    vertical = 5.dp
//                                )
//                            ,
//                            dayState = dayState,
//                            recordedMainActivityUiState = recordedMainActivityUiState,
//                            recordedSubActivityUiState = recordedSubActivityUiState,
//                            stopwatchService = stopwatchService,
//                            clearRecordedMainActivity = clearMainRecordedActivity,
//                            clearRecordedSubActivity = clearSubRecordedActivity,
//                            onNavigateToActivityNoteEdit = {
//                                activityPopupState = ActivityPopupState.Notes
//                            },
//                            saveRecordedMainActivity = saveMainRecordedActivity,
//                            saveRecordedSubActivity = saveSubRecordedActivity,
//                            saveDay = saveDay,
//                            saveVoiceNote = saveVoiceNote,
//                            setActualActiveTimeEnd = setActualActiveTimeEnd,
//                            setActualActiveTimeStart = setActualActiveTimeStart,
//                            setRecordedMainActivityDate = setRecordedMainActivityDate,
//                            setRecordedMainActivityEndTime = setRecordedMainActivityEndTime,
//                            setRecordedMainActivityId = setRecordedMainActivityId,
//                            setRecordedMainActivityStartTime = setRecordedMainActivityStartTime,
//                            setRecordedMainActivityTitle = setRecordedMainActivityTitle,
//                            setRecordedSubActivityEndTime = setRecordedSubActivityEndTime,
//                            setRecordedSubActivityId = setRecordedSubActivityId,
//                            setRecordedSubActivityMainActivityId = setRecordedSubActivityMainActivityId,
//                            setRecordedSubActivityStartTime = setRecordedSubActivityStartTime,
//                            setRecordedSubActivityTitle = setRecordedSubActivityTitle,
//                            startRecording = startRecording,
//                            stopRecording = stopRecording,
//                        )
//                    }
//                    ActivityPopupState.Notes -> {
//                        ActivityEditScreen(
//                            activityEditState = if (isSubActivityTimerRunning) recordedSubActivityUiState else recordedMainActivityUiState,
//                            onBack = {
//                                activityPopupState = ActivityPopupState.ActivityRecorder
//                            },
//                            saveActivity = {
//                                if (isSubActivityTimerRunning) saveSubRecordedActivity() else saveMainRecordedActivity()
//                                activityPopupState = ActivityPopupState.ActivityRecorder
//                            },
//                            setActivityNote = { value ->
//                                if (isSubActivityTimerRunning) setRecordedSubActivityNote(value) else setRecordedMainActivityNote(value)
//                            }
//                        )
//                    }
//                }
//            }
//            else {
//                when (smallScreenState) {
//                    SmallScreenState.Goal -> {
//                        GoalEditScreen(
//                            goalUiState = goalUiState,
//                            lastPriority = lastGoalPriority ?: 0,
//                            onBack = {
//                                showPopupWindowSmallScreen = false
//                            },
//                            saveGoal = saveGoalFromState,
//                            setGoalPriority = setGoalPriority,
//                            setGoalTitle = setGoalTitle
//                        )
//                    }
//                    SmallScreenState.DayTask, SmallScreenState.ToDo -> {
//                        TaskEditScreen(
//                            dayState = dayState,
//                            lastTaskPriority = lastTaskPriority ?: 0,
//                            taskUiState = taskUiState,
//                            onBack = {
//                                showPopupWindowSmallScreen = false
//                            },
//                            saveTask = saveTaskFromState,
//                            setTaskStartTime = setTaskStartTime,
//                            setTaskEndTime = setTaskEndTime,
//                            setTaskDescription = setTaskDescription,
//                            setTaskPriority = setTaskPriority,
//                            setTaskTitle = setTaskTitle
//                        )
//                    }
//
//                    SmallScreenState.Comparison -> TODO()
//                    SmallScreenState.DayActivity -> TODO()
//                }
//            }
//        }
//    }
//
//    if (showPopupWindowBigScreen) {
//        PopupDialog(
//            onDismissRequest = {
//                showPopupWindowBigScreen = false
//            }
//        ) {
//            if (bigScreenMainPanelState == BigScreenMainPanelState.DayActivity) {
//                when (activityPopupState) {
//                    ActivityPopupState.ActivityRecorder -> {
//                        ActivityRecorder(
//                            context = context,
//                            modifier = Modifier
//                                .padding(
//                                    horizontal = 5.dp,
//                                    vertical = 5.dp
//                                )
//                            ,
//                            dayState = dayState,
//                            recordedMainActivityUiState = recordedMainActivityUiState,
//                            recordedSubActivityUiState = recordedSubActivityUiState,
//                            stopwatchService = stopwatchService,
//                            clearRecordedMainActivity = clearMainRecordedActivity,
//                            clearRecordedSubActivity = clearSubRecordedActivity,
//                            onNavigateToActivityNoteEdit = {
//                                activityPopupState = ActivityPopupState.Notes
//                            },
//                            saveRecordedMainActivity = saveMainRecordedActivity,
//                            saveRecordedSubActivity = saveSubRecordedActivity,
//                            saveDay = saveDay,
//                            saveVoiceNote = saveVoiceNote,
//                            setActualActiveTimeEnd = setActualActiveTimeEnd,
//                            setActualActiveTimeStart = setActualActiveTimeStart,
//                            setRecordedMainActivityDate = setRecordedMainActivityDate,
//                            setRecordedMainActivityEndTime = setRecordedMainActivityEndTime,
//                            setRecordedMainActivityId = setRecordedMainActivityId,
//                            setRecordedMainActivityStartTime = setRecordedMainActivityStartTime,
//                            setRecordedMainActivityTitle = setRecordedMainActivityTitle,
//                            setRecordedSubActivityEndTime = setRecordedSubActivityEndTime,
//                            setRecordedSubActivityId = setRecordedSubActivityId,
//                            setRecordedSubActivityMainActivityId = setRecordedSubActivityMainActivityId,
//                            setRecordedSubActivityStartTime = setRecordedSubActivityStartTime,
//                            setRecordedSubActivityTitle = setRecordedSubActivityTitle,
//                            startRecording = startRecording,
//                            stopRecording = stopRecording,
//                        )
//                    }
//                    ActivityPopupState.Notes -> {
//                        ActivityEditScreen(
//                            activityEditState = if (isSubActivityTimerRunning) recordedSubActivityUiState else recordedMainActivityUiState,
//                            onBack = {
//                                activityPopupState = ActivityPopupState.ActivityRecorder
//                            },
//                            saveActivity = {
//                                if (isSubActivityTimerRunning) saveSubRecordedActivity() else saveMainRecordedActivity()
//                                activityPopupState = ActivityPopupState.ActivityRecorder
//                            },
//                            setActivityNote = { value ->
//                                if (isSubActivityTimerRunning) setRecordedSubActivityNote(value) else setRecordedMainActivityNote(value)
//                            }
//                        )
//                    }
//                }
//            }
//            else {
//                when (bigScreenSidePanelState) {
//                    BigScreenSidePanelState.Goal -> {
//                        GoalEditScreen(
//                            goalUiState = goalUiState,
//                            lastPriority = lastGoalPriority ?: 0,
//                            onBack = {
//                                showPopupWindowBigScreen = false
//                            },
//                            saveGoal = saveGoalFromState,
//                            setGoalPriority = setGoalPriority,
//                            setGoalTitle = setGoalTitle
//                        )
//                    }
//                    BigScreenSidePanelState.ToDo -> {
//                        TaskEditScreen(
//                            dayState = dayState,
//                            lastTaskPriority = lastTaskPriority ?: 0,
//                            taskUiState = taskUiState,
//                            onBack = {
//                                showPopupWindowBigScreen = false
//                            },
//                            saveTask = saveTaskFromState,
//                            setTaskStartTime = setTaskStartTime,
//                            setTaskEndTime = setTaskEndTime,
//                            setTaskDescription = setTaskDescription,
//                            setTaskPriority = setTaskPriority,
//                            setTaskTitle = setTaskTitle
//                        )
//                    }
//                }
//            }
//        }
//    }

//    if (showActiveTimeSetUp) {
//        PopupDialog(
//            onDismissRequest = {
//                showActiveTimeSetUp = false
//            }
//        ) {
//            // Show active time setup
//            ActiveTimeSetUp(
//                dayState = dayState,
//                onBack = { showActiveTimeSetUp = false },
//                onClickSaveActiveTime = {
//                    onClickSaveActiveTime()
//                    showActiveTimeSetUp = false
//                },
//                setActiveTimeStart = setActiveTimeStart,
//                setActiveTimeEnd = setActiveTimeEnd
//            )
//        }
//    }

    if (showPopupWindow) {
        PopupDialog(
            onDismissRequest = {
                showPopupWindow = false
            }
        ) {
            when (popupState) {
                PopupState.ActiveTimeSetup -> {
                    // Show active time setup
                    ActiveTimeSetUp(
                        dayState = dayState,
                        onBack = { showPopupWindow = false },
                        onClickSaveActiveTime = {
                            onClickSaveActiveTime()
                            showPopupWindow = false
                        },
                        setActiveTimeStart = setActiveTimeStart,
                        setActiveTimeEnd = setActiveTimeEnd
                    )
                }

                PopupState.ActivityRecorder -> {
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
//                            activityPopupState = ActivityPopupState.Notes
                            popupState = PopupState.EditActivityNotes
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

                PopupState.DeleteActivity -> {
                    DeleteScreen(
                        onBack = {
                            showPopupWindow = false
                        },
                        onDelete = {
                            deleteActivity()
                            selectActivityToBeDeleted(null)
                            showPopupWindow = false
                        },
                        deleteType = DeleteType.Activity
                    )
                }

                PopupState.DeleteTask -> {
                    DeleteScreen(
                        onBack = {
                            showPopupWindow = false
                        },
                        onDelete = {
                            deleteTask()
                            selectTask(null)
                            showPopupWindow = false
                        }
                    )
                }

                PopupState.DeleteVoiceNote -> {
                    DeleteScreen(
                        onBack = {
                            showPopupWindow = false
                        },
                        onDelete = {
                            // FIXME
//                            deleteVoiceNote(voiceNote)
                            deleteVoiceNote()
                            selectVoiceNoteToBeDeleted(null)
                            showPopupWindow = false
                        },
                        deleteType = DeleteType.VoiceNote
                    )
                }

                PopupState.EditActivity -> {
                    ActivityEditScreen(
                        activityEditState = activityEditState,
                        mode = ActivityEditMode.Full,
                        onBack = {
                            showPopupWindow = false
                        },
                        saveActivity = saveActivity,
                        setActivityNote = setActivityNote,
                        setActivityTitle = setActivityTitle
                    )
                }

                PopupState.EditActivityNotes -> {
                    ActivityEditScreen(
                        activityEditState = if (isSubActivityTimerRunning) recordedSubActivityUiState else recordedMainActivityUiState,
                        onBack = {
//                            activityPopupState = ActivityPopupState.ActivityRecorder
                            popupState = PopupState.ActivityRecorder
                        },
                        saveActivity = {
                            if (isSubActivityTimerRunning) saveSubRecordedActivity() else saveMainRecordedActivity()
//                            activityPopupState = ActivityPopupState.ActivityRecorder
                            popupState = PopupState.ActivityRecorder
                        },
                        setActivityNote = { value ->
                            if (isSubActivityTimerRunning) setRecordedSubActivityNote(value) else setRecordedMainActivityNote(value)
                        }
                    )
                }

                PopupState.EditGoal -> {
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

                PopupState.EditTask -> {
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


//                // Task and activity comparison
//                TaskActivityComparisonModePopup.Activity -> {
//                    if (isEditing) {
//                        ActivityEditScreen(
//                            activityEditState = activityEditState,
//                            mode = ActivityEditMode.Full,
//                            onBack = {
//                                showPopupWindow = false
//                            },
//                            saveActivity = saveActivity,
//                            setActivityNote = setActivityNote,
//                            setActivityTitle = setActivityTitle
//                        )
//                    }
//                    else {
//                        DeleteScreen(
//                            onBack = {
//                                showPopupWindow = false
//                            },
//                            onDelete = {
//                                deleteActivity()
//                                selectActivityToBeDeleted(null)
//                                showPopupWindow = false
//                            },
//                            deleteType = DeleteType.Activity
//                        )
//                    }
//                }
//                TaskActivityComparisonModePopup.Task -> {
//                    if (isEditing) {
//                        TaskEditScreen(
//                            dayState = dayState,
//                            lastTaskPriority = 0,
//                            taskUiState = taskUiState,
//                            onBack = {
//                                showPopupWindow = false
//                            },
//                            saveTask = saveTaskFromState,
//                            setTaskEndTime = setTaskEndTime,
//                            setTaskDescription = setTaskDescription,
//                            setTaskPriority = setTaskPriority,
//                            setTaskStartTime = setTaskStartTime,
//                            setTaskTitle = setTaskTitle
//                        )
//                    }
//                    else {
//                        DeleteScreen(
//                            onBack = {
//                                showPopupWindow = false
//                            },
//                            onDelete = {
//                                deleteTask()
//                                selectTask(null)
//                                showPopupWindow = false
//                            }
//                        )
//                    }
//                }
//
//                TaskActivityComparisonModePopup.VoiceNote -> {
//                    DeleteScreen(
//                        onBack = {
//                            showPopupWindow = false
//                        },
//                        onDelete = {
//                            // FIXME
////                            deleteVoiceNote(voiceNote)
//                            deleteVoiceNote()
//                            selectVoiceNoteToBeDeleted(null)
//                            showPopupWindow = false
//                        },
//                        deleteType = DeleteType.VoiceNote
//                    )
//                }
            }
        }
    }
}