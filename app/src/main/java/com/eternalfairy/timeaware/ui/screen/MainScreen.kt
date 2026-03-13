package com.eternalfairy.timeaware.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
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
import com.eternalfairy.timeaware.ui.component.MultiWindowSizeLayout
import com.eternalfairy.timeaware.ui.component.PopupDialog
import com.eternalfairy.timeaware.ui.viewmodel.ActivityUiState
import com.eternalfairy.timeaware.ui.viewmodel.AudioViewModel
import com.eternalfairy.timeaware.ui.viewmodel.DayState
import com.eternalfairy.timeaware.ui.viewmodel.GoalUiState
import com.eternalfairy.timeaware.ui.viewmodel.TaskUiState
import com.eternalfairy.timeaware.ui.viewmodel.UserInput
import java.time.LocalDate
import java.util.UUID

enum class SmallScreenState {
    // Comparison of task and activity
    Comparison,
    // Day overview: tasks
    DayTask,
    // Day overview: activities
    DayActivity,
    Goal,
    MoveToCalendar,
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
    DayTask,
    MoveToCalendar
}

enum class BigScreenSidePanelState {
    Goal,
    ToDo
}

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
    deleteActivity: () -> Unit,
    deleteGoal: (Goal) -> Unit,
    deleteTask: () -> Unit,
    deleteVoiceNote: () -> Unit,
    onMoveToToDoList: () -> Unit,
    onClickSaveActiveTime: () -> Unit,
    onPinTask: (Boolean) -> Unit,
    onSetSelectedDate: (LocalDate) -> Unit,
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
    setTaskPriority: (Int?) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskTitle: (String) -> Unit,
    startRecording: (String) -> Unit,
    stopRecording: () -> Unit,
    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    val isSubActivityTimerRunning  = (recordedSubActivityUiState.id != null)
    var showPopupWindow by remember { mutableStateOf(false) }
    var popupState by remember { mutableStateOf(PopupState.EditTask) }
    val audioViewModel: AudioViewModel = viewModel(factory = AudioViewModel.Factory)

    MultiWindowSizeLayout(
        default = {},
        portraitPhone = {
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

                },
                onEditActivity = { id ->
                    // Pass the id of the main activity to mark it for editing
                    selectActivityToBeEdited(id)
                    popupState = PopupState.EditActivity
                    showPopupWindow = true
                },
                onDeleteTask = {
                    popupState = PopupState.DeleteTask
                    showPopupWindow = true
                },
                onEditTask = {
                    popupState = PopupState.EditTask
                    showPopupWindow = true
                },
                onDeleteVoiceNote = { id ->
                    selectVoiceNoteToBeDeleted(id)
                    popupState = PopupState.DeleteVoiceNote
                    showPopupWindow = true
                },
                onMoveToToDoList = onMoveToToDoList,
                onPinTask = onPinTask,
                onPressActiveTime = {
                    popupState = PopupState.ActiveTimeSetup
                    showPopupWindow = true
                },
                onSetSelectedDate = onSetSelectedDate,
                onShowPopupWindow = { state ->
                    popupState = state
                    showPopupWindow = true
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
        },
        landscapePhone = {
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

                },
                onEditActivity = { id ->
                    // Pass the id of the main activity to mark it for editing
                    selectActivityToBeEdited(id)
                    popupState = PopupState.EditActivity
                    showPopupWindow = true
                },
                onDeleteTask = {
                    popupState = PopupState.DeleteTask
                    showPopupWindow = true
                },
                onEditTask = {
                    popupState = PopupState.EditTask
                    showPopupWindow = true
                },
                onDeleteVoiceNote = { id ->
                    selectVoiceNoteToBeDeleted(id)
                    popupState = PopupState.DeleteVoiceNote
                    showPopupWindow = true
                },
                onMoveToToDoList = onMoveToToDoList,
                onPinTask = onPinTask,
                onPressActiveTime = {
                    popupState = PopupState.ActiveTimeSetup
                    showPopupWindow = true
                },
                onSetSelectedDate = onSetSelectedDate,
                onShowPopupWindow = { state ->
                    popupState = state
                    showPopupWindow = true
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
        },
        portraitTablet = {
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
                },
                onEditActivity = { id ->
                    // Pass the id of the main activity to mark it for editing
                    selectActivityToBeEdited(id)
                    popupState = PopupState.EditActivity
                    showPopupWindow = true
                },
                onDeleteTask = {
                    popupState = PopupState.DeleteTask
                    showPopupWindow = true
                },
                onEditTask = {
                    popupState = PopupState.EditTask
                    showPopupWindow = true
                },
                onDeleteVoiceNote = { id ->
                    selectVoiceNoteToBeDeleted(id)
                    popupState = PopupState.DeleteVoiceNote
                    showPopupWindow = true
                },
                onMoveToToDoList = onMoveToToDoList,
                onPinTask = onPinTask,
                onPressActiveTime = {
                    popupState = PopupState.ActiveTimeSetup
                    showPopupWindow = true
                },
                onSetSelectedDate = onSetSelectedDate,
                onShowPopupWindow = { state ->
                    popupState = state
                    showPopupWindow = true
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
        },
        landscapeTablet = {
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
                },
                onEditActivity = { id ->
                    // Pass the id of the main activity to mark it for editing
                    selectActivityToBeEdited(id)
                    popupState = PopupState.EditActivity
                    showPopupWindow = true
                },
                onDeleteTask = {
                    popupState = PopupState.DeleteTask
                    showPopupWindow = true
                },
                onEditTask = {
                    popupState = PopupState.EditTask
                    showPopupWindow = true
                },
                onDeleteVoiceNote = { id ->
                    selectVoiceNoteToBeDeleted(id)
                    popupState = PopupState.DeleteVoiceNote
                    showPopupWindow = true
                },
                onMoveToToDoList = onMoveToToDoList,
                onPinTask = onPinTask,
                onPressActiveTime = {
                    popupState = PopupState.ActiveTimeSetup
                    showPopupWindow = true
                },
                onSetSelectedDate = onSetSelectedDate,
                onShowPopupWindow = { state ->
                    popupState = state
                    showPopupWindow = true
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
    )

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
                            popupState = PopupState.ActivityRecorder
                        },
                        saveActivity = {
                            if (isSubActivityTimerRunning) saveSubRecordedActivity() else saveMainRecordedActivity()
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
            }
        }
    }
}