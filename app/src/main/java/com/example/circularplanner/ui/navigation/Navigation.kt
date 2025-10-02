package com.example.circularplanner.ui.navigation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.circularplanner.service.StopwatchService
import com.example.circularplanner.ui.screen.PlannerScreen
import com.example.circularplanner.ui.screen.TaskActivityComparisonScreen
import com.example.circularplanner.ui.screen.WelcomeScreen
import com.example.circularplanner.ui.viewmodel.DayViewModel
import com.example.circularplanner.ui.viewmodel.GoalViewModel
import com.example.circularplanner.ui.viewmodel.toActivity
import com.example.circularplanner.ui.viewmodel.toTask
import com.example.circularplanner.ui.viewmodel.toVoiceNote
import com.example.circularplanner.utils.AudioRecorder
import java.io.File

enum class RecordedActivity {
    Main,
    Sub
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun Navigation(
    navController: NavHostController,
    stopwatchService: StopwatchService
) {
    val dayViewModel: DayViewModel = viewModel(factory = DayViewModel.Factory)

    val activityUiState by dayViewModel.activityUiState.collectAsState()
    val dayState by dayViewModel.dayState.collectAsState()
//    val recordedActivityUiState by dayViewModel.mainRecordedActivityUiState.collectAsState()
    val mainRecordedActivityUiState by dayViewModel.mainRecordedActivityUiState.collectAsState()
    val subRecordedActivityUiState by dayViewModel.subRecordedActivityUiState.collectAsState()
    val dayUiState by dayViewModel.dayUiState.collectAsState()
    val lastTaskPriority by dayViewModel.lastTaskPriority.collectAsState(initial = 0)
    val taskUiState by dayViewModel.taskUiState.collectAsState()
    val toDoTasks by dayViewModel.toDoTasks.collectAsState(emptyList())
    val userInput by dayViewModel.userInput.collectAsState()
    val nextTaskUiState by dayViewModel.nextTaskUiState.collectAsState()
    val previousTaskUiState by dayViewModel.previousTaskUiState.collectAsState()

    val context = LocalContext.current
    val audioRecorder = AudioRecorder()

    val goalViewModel: GoalViewModel = viewModel(factory = GoalViewModel.Factory)
    val goals by goalViewModel.goals.collectAsState()
    val goalUiState by goalViewModel.goalUiState.collectAsState()
    val lastGoalPriority by goalViewModel.lastPriority.collectAsState()

    var recordedActivity = RecordedActivity.Main

    fun setRecordedActivityState(state: RecordedActivity) {
        recordedActivity = state
    }

    NavHost(
        navController = navController,
//        startDestination = WelcomeRoute// TODO: Move welcoming (goals display) to the splash screen
        startDestination = PlannerRoute
    ) {
        composable<PlannerRoute> { backStackEntry ->
            PlannerScreen(
                context = context,
                activityUiState = if (recordedActivity == RecordedActivity.Main) mainRecordedActivityUiState else subRecordedActivityUiState,
                dayUiState = dayUiState,
                dayState = dayState,
                goals = goals,
                goalUiState = goalUiState,
                lastGoalPriority = lastGoalPriority,
                lastTaskPriority = lastTaskPriority,
                mainRecordedActivityUiState = mainRecordedActivityUiState,
                subRecordedActivityUiState = subRecordedActivityUiState,
                stopwatchService = stopwatchService,
                nextTaskUiState = nextTaskUiState,
                previousTaskUiState = previousTaskUiState,
                taskUiState = taskUiState,
                toDoTasks = toDoTasks,
                userInput = userInput,
                clearMainRecordedActivity = dayViewModel::clearMainRecordedActivity,
                clearSubRecordedActivity = dayViewModel::clearSubRecordedActivity,
                deleteGoal = goalViewModel::deleteGoal,
//                deleteVoiceNote = { voiceNoteUiState ->
//                    try {
//                        val file = File(voiceNoteUiState.uri)
//                        if (file.exists()) {
//                            // Delete the voice note audio file from the local storage
//                            file.delete()
//                        }
//                        // The voice note from the database
//                        dayViewModel.deleteVoiceNote(voiceNoteUiState.toVoiceNote())
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        Toast.makeText(context,"Error deleting the file", Toast.LENGTH_SHORT).show()
//                    }
//                },
                deleteTask = {
                    val tasks = dayViewModel.dayState.value.tasks + dayViewModel.toDoTasks.value
                    val task = tasks.find { task -> task.id == dayViewModel.taskUiState.value.id }

                    if (task != null) {
                        dayViewModel.deleteTask(task)
                    }
                },
                onClickSaveActiveTime = {
                    dayViewModel.saveDay()
//                    dayViewModel.setIsActiveTimeSetUp(false)
                },
                onMoveToToDoList = {
                    val task = dayState.tasks.find { task -> task.id == taskUiState.id }

                    if (task != null) {
                        dayViewModel.updateTask(
                            task.copy(
                                date = null,
                                startTime = null,
                                endTime = null,
                                priority = lastTaskPriority
                            )
                        )
                    }
                },
                onNavigateToTaskActivityComparison = { navController.navigate(route = TaskActivityComparisonRoute) },
                onSetDayNote = dayViewModel::setDayNote,
                onSetSelectedDate = { date ->
                    dayViewModel.setSelectedDate(date)
                },
                onSwitchGoals = { firstId, secondId ->
                    goalViewModel.switchPriorities(firstId, secondId)
                },
                onSwitchScreen = dayViewModel::setIsActivityDisplay,
                saveMainRecordedActivity = dayViewModel::saveMainRecordedActivity,
                saveSubRecordedActivity = dayViewModel::saveSubRecordedActivity,
                saveDay = dayViewModel::saveDay,
                saveGoal = goalViewModel::saveGoal,
                saveGoalFromState = goalViewModel::saveGoal,
                saveNextTask = dayViewModel::saveNextTask,
                saveTask = dayViewModel::saveTask,
                saveTaskFromState = dayViewModel::saveTask,
//                saveTaskFromState = {
//                    Log.i("taskUiState", taskUiState.toString())
//                    dayViewModel.saveTask()
//                },
                saveVoiceNote = dayViewModel::saveVoiceNote,
                selectActivity = dayViewModel::selectActivity,
                selectGoal = goalViewModel::selectGoal,
                selectNextTask = dayViewModel::selectNextTask,
                selectPreviousTask = dayViewModel::selectPreviousTask,
                selectTask = dayViewModel::selectTask,
                setActiveTimeStart = dayViewModel::setActiveTimeStart,
                setActiveTimeEnd = dayViewModel::setActiveTimeEnd,
                saveActivity = if (recordedActivity == RecordedActivity.Main) dayViewModel::saveMainRecordedActivity else dayViewModel::saveSubRecordedActivity,
                setActivityNote = if (recordedActivity == RecordedActivity.Main) dayViewModel::setMainRecordedActivityNote else dayViewModel::setSubRecordedActivityNote,
                setActualActiveTimeEnd = dayViewModel::setActualActiveTimeEnd,
                setActualActiveTimeStart = dayViewModel::setActualActiveTimeStart,
                setDayNote = dayViewModel::setDayNote,
                setGoalPriority = goalViewModel::setPriority,
                setGoalTitle = goalViewModel::setTitle,
                setMainRecordedActivityEndTime = dayViewModel::setMainRecordedActivityEndTime,
                setSubRecordedActivityEndTime = dayViewModel::setSubRecordedActivityEndTime,
                setMainRecordedActivityId = dayViewModel::setMainRecordedActivityId,
                setSubRecordedActivityId = dayViewModel::setSubRecordedActivityId,
                setMainRecordedActivityNote = dayViewModel::setMainRecordedActivityNote,
                setSubRecordedActivityNote = dayViewModel::setSubRecordedActivityNote,
                setMainRecordedActivityStartTime = dayViewModel::setMainRecordedActivityStartTime,
                setSubRecordedActivityStartTime = dayViewModel::setSubRecordedActivityStartTime,
                setMainRecordedActivityTitle = dayViewModel::setMainRecordedActivityTitle,
                setSubRecordedActivityTitle = dayViewModel::setSubRecordedActivityTitle,
                setIsActiveTimeSetUp = dayViewModel::setIsActiveTimeSetUp,
                setRecordedActivityState = { state -> setRecordedActivityState(state) },
                setNextTaskEndTime = dayViewModel::setNextTaskEndTime,
                setNextTaskStartTime = dayViewModel::setNextTaskStartTime,
                setTaskDate = dayViewModel::setTaskDate,
                setTaskDescription = dayViewModel::setTaskDescription,
                setTaskEndTime = dayViewModel::setTaskEndTime,
                setTaskPriority = dayViewModel::setTaskPriority,
                setTaskStartTime = dayViewModel::setTaskStartTime,
                setTaskTitle = dayViewModel::setTaskTitle,
                startRecording = audioRecorder::startRecording,
                stopRecording = audioRecorder::stopRecording
            )
        }

        composable<TaskActivityComparisonRoute> { backStackEntry ->
            TaskActivityComparisonScreen(
                activityUiState = activityUiState,
                dayState = dayState,
                taskUiState = taskUiState,
                userInput = userInput,
                deleteActivity = { dayViewModel.deleteActivity(dayViewModel.activityUiState.value.toActivity()) },
                deleteTask = { dayViewModel.deleteTask(dayViewModel.taskUiState.value.toTask()) },
                deleteVoiceNote = {voiceNoteUiState ->
                    try {
                        val file = File(voiceNoteUiState.uri)
                        if (file.exists()) {
                            // Delete the voice note audio file from the local storage
                            file.delete()
                        }
                        // The voice note from the database
                        dayViewModel.deleteVoiceNote(voiceNoteUiState.toVoiceNote())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context,"Error deleting the file", Toast.LENGTH_SHORT).show()
                    }
                },
                onCancel = {
                    navController.popBackStack()
                },
                saveActivity = dayViewModel::saveActivity,
                saveTaskFromState = dayViewModel::saveTask,
                selectActivity = dayViewModel::selectActivity,
                selectTask = dayViewModel::selectTask,
                setActivityNote = dayViewModel::setActivityNote,
                setTaskDescription = dayViewModel::setTaskDescription,
                setTaskEndTime = dayViewModel::setTaskEndTime,
                setTaskPriority = dayViewModel::setTaskPriority,
                setTaskStartTime = dayViewModel::setTaskStartTime,
                setTaskTitle = dayViewModel::setTaskTitle,
                updateLastPlayedPosition = dayViewModel::updateLastPlayedPosition
            )
        }

        composable<WelcomeRoute>{ backStackEntry ->
            WelcomeScreen(
                goals = goals,
                onNext = { navController.navigate(route = PlannerRoute) }
            )
        }
    }
}