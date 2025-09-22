package com.example.circularplanner.ui.navigation

import android.os.Build
import android.util.Log
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
//        startDestination = WelcomeRoute// TODO: Move welcoming (goals display) to the splash screen
        startDestination = TaskDisplayRoute
    ) {
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
                saveTask = dayViewModel::saveTask,
//                saveTaskFromState = dayViewModel::saveTask,
                saveTaskFromState = {
                    Log.i("taskUiState", taskUiState.toString())
                    dayViewModel.saveTask()
                },
                saveVoiceNote = dayViewModel::saveVoiceNote,
                selectActivity = dayViewModel::selectActivity,
                selectGoal = goalViewModel::selectGoal,
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

        composable<WelcomeRoute>{ backStackEntry ->
            WelcomeScreen(
                goals = goals,
                onNext = { navController.navigate(route = TaskDisplayRoute) }
            )
        }
    }
}