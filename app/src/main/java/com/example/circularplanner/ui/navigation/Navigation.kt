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
import com.example.circularplanner.data.Task
import com.example.circularplanner.service.StopwatchService
import com.example.circularplanner.ui.screen.ActivityNoteEditScreen
import com.example.circularplanner.ui.screen.DayNoteEditScreen
import com.example.circularplanner.ui.screen.GoalEditScreen
import com.example.circularplanner.ui.screen.MainScreen
import com.example.circularplanner.ui.screen.TaskActivityComparisonScreen
import com.example.circularplanner.ui.screen.TaskEditScreen
import com.example.circularplanner.ui.screen.TaskInfoScreen
import com.example.circularplanner.ui.screen.WelcomeScreen
import com.example.circularplanner.ui.viewmodel.DayViewModel
import com.example.circularplanner.ui.viewmodel.GoalViewModel
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
        startDestination = WelcomeRoute
    ) {
        composable<ActivityNoteEditRoute> { backStackEntry ->
            ActivityNoteEditScreen(
                activityUiState = if (recordedActivity == RecordedActivity.Main) mainRecordedActivityUiState else subRecordedActivityUiState,
                onBack = {
                    navController.popBackStack()
                },
                saveActivity = if (recordedActivity == RecordedActivity.Main) dayViewModel::saveMainRecordedActivity else dayViewModel::saveSubRecordedActivity,
                setActivityNote = if (recordedActivity == RecordedActivity.Main) dayViewModel::setMainRecordedActivityNote else dayViewModel::setSubRecordedActivityNote
            )
        }

        composable<DayNoteEditRoute> { backStackEntry ->
            DayNoteEditScreen(
                dayUiState = dayUiState,
                onBack = {
                    navController.popBackStack()
                },
                saveDay = dayViewModel::saveDay,
                setDayNote = dayViewModel::setDayNote
            )
        }

        composable<GoalEditRoute> { backStackEntry ->
            GoalEditScreen(
                goalUiState = goalUiState,
                lastPriority = lastGoalPriority ?: 0,
                onBack = {
                    navController.popBackStack()
                },
                saveGoal = goalViewModel::saveGoal,
                setGoalPriority = goalViewModel::setPriority,
                setGoalTitle = goalViewModel::setTitle
            )
        }

        composable<TaskActivityComparisonRoute> { backStackEntry ->
            TaskActivityComparisonScreen(
                activityUiState = activityUiState,
                taskUiState = taskUiState,
                userInput = userInput,
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
                updateLastPlayedPosition = dayViewModel::updateLastPlayedPosition
            )
        }

        composable<TaskDisplayRoute> { backStackEntry ->
            MainScreen(
                context = context,
                dayUiState = dayUiState,
                dayState = dayState,
                goals = goals,
                mainRecordedActivityUiState = mainRecordedActivityUiState,
                subRecordedActivityUiState = subRecordedActivityUiState,
                stopwatchService = stopwatchService,
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
                onClickSaveActiveTime = {
                    dayViewModel.saveDay()
                    dayViewModel.setIsActiveTimeSetUp(false)
                },
                onNavigateToActivityNoteEdit = { navController.navigate( route = ActivityNoteEditRoute )},
                onNavigateToDayNoteEdit = { navController.navigate( route = DayNoteEditRoute )},
                onNavigateToGoalEdit = { navController.navigate(route = GoalEditRoute) },
                onNavigateToTaskActivityComparison = { navController.navigate(route = TaskActivityComparisonRoute) },
                onNavigateToTaskEdit = { navController.navigate(route = TaskEditRoute) },
                onNavigateToTaskInfo = { navController.navigate(route = TaskInfoRoute) },
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
                saveTask = dayViewModel::saveTask,
                saveTaskFromState = dayViewModel::saveTask,
                saveVoiceNote = dayViewModel::saveVoiceNote,
                selectActivity = dayViewModel::selectActivity,
                selectGoal = goalViewModel::selectGoal,
                selectTask = dayViewModel::selectTask,
                setActiveTimeStart = dayViewModel::setActiveTimeStart,
                setActiveTimeEnd = dayViewModel::setActiveTimeEnd,
                setActualActiveTimeEnd = dayViewModel::setActualActiveTimeEnd,
                setActualActiveTimeStart = dayViewModel::setActualActiveTimeStart,
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
                setTaskDate = dayViewModel::setTaskDate,
                setTaskStartTime = dayViewModel::setTaskStartTime,
                setTaskEndTime = dayViewModel::setTaskEndTime,
                startRecording = audioRecorder::startRecording,
                stopRecording = audioRecorder::stopRecording,
            )
        }

        composable<TaskEditRoute> { backStackEntry ->
            TaskEditScreen(
                dayState = dayState,
                lastTaskPriority = lastTaskPriority ?: 0,
                taskUiState = taskUiState,
                onBack = {
                    navController.popBackStack()
                },
                saveTask = { dayViewModel.saveTask() },
                setTaskStartTime = dayViewModel::setTaskStartTime,
                setTaskEndTime = dayViewModel::setTaskEndTime,
                setTaskDescription = dayViewModel::setTaskDescription,
                setTaskPriority = dayViewModel::setTaskPriority,
                setTaskTitle = dayViewModel::setTaskTitle,
            )
        }

        composable<TaskInfoRoute> { backStackEntry ->
            TaskInfoScreen(
                taskUiState = taskUiState,
                deleteTask = {
                    var task: Task?

                    // TODO: Find a more optimal way to find the task
                    task = dayViewModel.toDoTasks.value.find { task -> task.id == dayViewModel.taskUiState.value.id }
                    if (task == null) {
                        task = dayViewModel.dayState.value.tasks.find { task -> task.id == dayViewModel.taskUiState.value.id }
                    }

                    if (task != null) {
                        dayViewModel.deleteTask(task)
                    }
                },
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToMoveToCalendar = {
//                    val task = toDoTasks.find { task -> task.id == taskUiState.id }
                },
                onMoveToToDoList = {
                    val task = dayState.tasks.find { task -> task.id == taskUiState.id }

                    if (task != null) {
                        dayViewModel.updateTask(
                            task.copy(
                                date = null,
                                startTime = null,
                                endTime = null
                            )
                        )
                    }
                },
                onNavigateToTaskEdit = {
                    navController.navigate(route = TaskEditRoute)
                },
            )
        }

        composable<WelcomeRoute>{ backStackEntry ->
            WelcomeScreen(
                goals = goals,
                onNext = { navController.navigate(route = TaskDisplayRoute) }
            )
        }
    }
}