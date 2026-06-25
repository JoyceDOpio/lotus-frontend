package com.eternalfairy.lotus.view.screen.planner

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eternalfairy.lotus.model.data.Time
import com.eternalfairy.lotus.view.component.ActiveTimeSetUp
import com.eternalfairy.lotus.view.component.ActivityRecorder
import com.eternalfairy.lotus.view.component.MultiWindowSizeLayout
import com.eternalfairy.lotus.view.component.PopupDialog
import com.eternalfairy.lotus.view.data.ActivityUiState
import com.eternalfairy.lotus.view.data.DayUiState
import com.eternalfairy.lotus.view.data.GoalUiState
import com.eternalfairy.lotus.view.data.TaskUiState
import com.eternalfairy.lotus.view.data.UserInput
import com.eternalfairy.lotus.view.data.VoiceNoteUiState
import com.eternalfairy.lotus.view.screen.ActivityEditMode
import com.eternalfairy.lotus.view.screen.ActivityEditScreen
import com.eternalfairy.lotus.view.screen.DeleteScreen
import com.eternalfairy.lotus.view.screen.DeleteType
import com.eternalfairy.lotus.view.screen.GoalEditScreen
import com.eternalfairy.lotus.view.screen.TaskEditScreen
import com.eternalfairy.lotus.view.service.StopwatchService
import com.eternalfairy.lotus.view.theme.Teal74
import com.eternalfairy.lotus.view.theme.White
import com.eternalfairy.lotus.viewmodel.AudioViewModel
import com.eternalfairy.lotus.viewmodel.PlannerViewModel
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
fun PlannerContainer(
    viewModel: PlannerViewModel,
//    context: Context,
//    activityEditState: ActivityUiState,
//    activityUiState: ActivityUiState,
//    dayUiState: DayUiState,
//    goals: List<GoalUiState>,
//    goalUiState: GoalUiState,
//    lastGoalPriority: Int?,
//    lastTaskPriority: Int?,
//    recordedMainActivityUiState: ActivityUiState,
//    recordedSubActivityUiState: ActivityUiState,
    stopwatchService: StopwatchService,
//    taskUiState: TaskUiState,
//    toDoTasks: List<TaskUiState>,
//    userInput: UserInput,
//    clearMainRecordedActivity: () -> Unit,
//    clearSubRecordedActivity: () -> Unit,
//    deleteActivity: () -> Unit,
//    deleteGoal: (UUID) -> Unit,
//    deleteTask: () -> Unit,
//    deleteVoiceNote: () -> Unit,
//    onClickSaveActiveTime: () -> Unit,
    onLogout: () -> Unit,
    onMoveToToDoList: () -> Unit,
    onNavigateToLogin: () -> Unit,
//    onPinTask: (Boolean) -> Unit,
//    onSetSelectedDate: (OffsetDateTime) -> Unit,
//    onSetSelectedDate: (LocalDate) -> Unit,
//    saveActivity: () -> Unit,
//    saveDay: () -> Unit,
//    saveGoal: (GoalUiState) -> Unit,
//    saveGoalFromState: () -> Unit,
//    saveMainRecordedActivity: () -> Unit,
//    saveSubRecordedActivity: () -> Unit,
//    saveTask: (TaskUiState) -> Unit,
//    saveTaskFromState: () -> Unit,
//    saveVoiceNote: (VoiceNoteUiState) -> Unit,
//    selectActivity: (UUID?) -> Unit,
//    selectActivityToBeDeleted: (UUID?) -> Unit,
//    selectActivityToBeEdited: (UUID?) -> Unit,
//    selectGoal: (UUID?) -> Unit,
//    selectTask: (UUID?) -> Unit,
//    selectVoiceNoteToBeDeleted: (UUID?) -> Unit,
//    setActiveTimeEnd: (Time) -> Unit,
//    setActiveTimeStart: (Time) -> Unit,
//    setActivityNote: (String) -> Unit,
//    setActivityTitle: (String) -> Unit,
//    setActualActiveTimeEnd: (Time) -> Unit,
//    setActualActiveTimeStart: (Time) -> Unit,
//    setGoalPriority: (Int) -> Unit,
//    setGoalTitle: (String) -> Unit,
//    setRecordedMainActivityDate: (OffsetDateTime) -> Unit,
//    setRecordedMainActivityDate: (LocalDate) -> Unit,
//    setRecordedMainActivityEndTime: (Time) -> Unit,
//    setRecordedMainActivityId: (UUID) -> Unit,
//    setRecordedMainActivityNote: (String) -> Unit,
//    setRecordedMainActivityStartTime: (Time) -> Unit,
//    setRecordedMainActivityTitle: (String) -> Unit,
//    setRecordedSubActivityEndTime: (Time) -> Unit,
//    setRecordedSubActivityId: (UUID) -> Unit,
//    setRecordedSubActivityMainActivityId: (UUID) -> Unit,
//    setRecordedSubActivityNote: (String) -> Unit,
//    setRecordedSubActivityStartTime: (Time) -> Unit,
//    setRecordedSubActivityTitle: (String) -> Unit,
//    setTaskDate: (OffsetDateTime?) -> Unit,
//    setTaskDate: (LocalDate?) -> Unit,
//    setTaskDescription: (String) -> Unit,
//    setTaskEndTime: (Time) -> Unit,
//    setTaskPriority: (Int?) -> Unit,
//    setTaskStartTime: (Time) -> Unit,
//    setTaskTitle: (String) -> Unit,
//    startRecording: (String) -> Unit,
//    stopRecording: () -> Unit,
//    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    val state = viewModel.state

    val isSubActivityTimerRunning  = (recordedSubActivityUiState.id != null)
    var showPopupWindow by remember { mutableStateOf(false) }
    var popupState by remember { mutableStateOf(PopupState.EditTask) }
    val audioViewModel: AudioViewModel = viewModel(factory = AudioViewModel.Factory)

    // Banner ad
//    val adView = remember { AdView(context) }
    // Set the unique ID for this specific ad unit.
//    val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
//    adView.adUnitId = BANNER_AD_UNIT_ID
// Set a large anchored adaptive banner ad size with a given width.
//    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(LocalContext.current, 360)
//    adView.setAdSize(adSize)

//    var isLoading by remember { mutableStateOf((userInfo == null)) }
//    Log.i("PlannerContainer", "userInfo $userInfo")
//    Log.i("PlannerContainer", "isLoading $isLoading")

    MultiWindowSizeLayout(
        default = {},
        portraitPhone = {
            SmallScreenPortrait(
//                activityUiState = activityUiState,
//                adView = adView,
                audioViewModel = audioViewModel,
                plannerViewModel = viewModel,
//                context = context,
//                dayUiState = dayUiState,
//                goals = goals,
//                goalUiState = goalUiState,
//                lastGoalPriority = lastGoalPriority,
//                lastTaskPriority = lastTaskPriority,
//                taskUiState = taskUiState,
//                toDoTasks = toDoTasks,
////                userInfo = userInfo,
//                userInput = userInput,
//                deleteGoal = deleteGoal,
//                deleteTask = deleteTask,
//                deleteVoiceNote = deleteVoiceNote,
//                onDeleteActivity = { id ->
//                    // Pass the id of the main activity to mark it for deletion
//                    selectActivityToBeDeleted(id)
//                    popupState = PopupState.DeleteActivity
//                    showPopupWindow = true
//
//                },
//                onEditActivity = { id ->
//                    // Pass the id of the main activity to mark it for editing
//                    selectActivityToBeEdited(id)
//                    popupState = PopupState.EditActivity
//                    showPopupWindow = true
//                },
//                onDeleteTask = {
//                    popupState = PopupState.DeleteTask
//                    showPopupWindow = true
//                },
//                onEditTask = {
//                    popupState = PopupState.EditTask
//                    showPopupWindow = true
//                },
//                onDeleteVoiceNote = { id ->
//                    selectVoiceNoteToBeDeleted(id)
//                    popupState = PopupState.DeleteVoiceNote
//                    showPopupWindow = true
//                },
                onLogout = onLogout,
                onMoveToToDoList = onMoveToToDoList,
                onNavigateToLogin = onNavigateToLogin,
//                onPinTask = onPinTask,
//                onPressActiveTime = {
//                    popupState = PopupState.ActiveTimeSetup
//                    showPopupWindow = true
//                },
//                onSetSelectedDate = onSetSelectedDate,
//                onShowPopupWindow = { state ->
//                    popupState = state
//                    showPopupWindow = true
//                },
//                saveGoal = saveGoal,
//                saveGoalFromState = saveGoalFromState,
//                saveTask = saveTask,
//                saveTaskFromState = saveTaskFromState,
//                selectActivity = selectActivity,
//                selectGoal = selectGoal,
//                selectTask = selectTask,
//                selectVoiceNoteToBeDeleted = selectVoiceNoteToBeDeleted,
//                setGoalPriority = setGoalPriority,
//                setGoalTitle = setGoalTitle,
//                setTaskDate = setTaskDate,
//                setTaskDescription = setTaskDescription,
//                setTaskEndTime = setTaskEndTime,
//                setTaskPriority = setTaskPriority,
//                setTaskStartTime = setTaskStartTime,
//                setTaskTitle = setTaskTitle,
//                updateLastPlayedPosition = updateLastPlayedPosition
            )
        },
        landscapePhone = {
            SmallScreenLandscape(
                activityUiState = activityUiState,
//                adView = adView,
                audioViewModel = audioViewModel,
                context = context,
                dayUiState = dayUiState,
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
//                adView = adView,
                audioViewModel = audioViewModel,
                context = context,
                dayUiState = dayUiState,
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
            BigScreenLandscape(
                activityUiState = activityUiState,
//                adView = adView,
                audioViewModel = audioViewModel,
                context = context,
                dayUiState = dayUiState,
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
                        dayUiState = dayUiState,
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
                        dayUiState = dayUiState,
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
                    DeleteScreen(onBack = {
                        showPopupWindow = false
                    }, onDelete = {
                        deleteTask()
                        selectTask(null)
                        showPopupWindow = false
                    })
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
                            if (isSubActivityTimerRunning) setRecordedSubActivityNote(value) else setRecordedMainActivityNote(
                                value
                            )
                        })
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
                        dayUiState = dayUiState,
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

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                strokeWidth = 5.dp,
                color = Teal74
            )
        }
    }
}