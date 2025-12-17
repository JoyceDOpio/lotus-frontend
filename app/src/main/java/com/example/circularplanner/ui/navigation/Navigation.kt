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
import com.example.circularplanner.service.StopwatchService
import com.example.circularplanner.ui.screen.MoveToCalendarScreen
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

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun Navigation(
    navController: NavHostController,
    stopwatchService: StopwatchService
) {
    val dayViewModel: DayViewModel = viewModel(factory = DayViewModel.Factory)

    val activityEditState by dayViewModel.activityEditState.collectAsState()
    val dayState by dayViewModel.dayState.collectAsState()
    val mainRecordedActivityUiState by dayViewModel.recordedMainActivityUiState.collectAsState()
    val subRecordedActivityUiState by dayViewModel.recordedSubActivityUiState.collectAsState()
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

    val activityUiState by dayViewModel.activityUiState.collectAsState()

    NavHost(
        navController = navController,
//        startDestination = WelcomeRoute// TODO: Display welcoming (goals) once every day
        startDestination = PlannerRoute
    ) {
        composable<MoveToCalendarRoute> { backStackEntry ->
            MoveToCalendarScreen(
                dayState = dayState,
                taskUiState = taskUiState,
                userInput = userInput,
                onBack = { navController.popBackStack() },
                onClickSaveActiveTime = {
                    dayViewModel.saveDay()
                },
                onSetSelectedDate = { date ->
                    dayViewModel.setSelectedDate(date)
                },
                saveTask = dayViewModel::saveTask,
                setActiveTimeStart = dayViewModel::setActiveTimeStart,
                setActiveTimeEnd = dayViewModel::setActiveTimeEnd,
                setTaskDate = dayViewModel::setTaskDate,
                setTaskEndTime = dayViewModel::setTaskEndTime,
                setTaskPriority = dayViewModel::setTaskPriority,
                setTaskStartTime = dayViewModel::setTaskStartTime
            )
        }

        composable<PlannerRoute> { backStackEntry ->
            PlannerScreen(
                context = context,
                dayUiState = dayUiState,
                dayState = dayState,
                goals = goals,
                goalUiState = goalUiState,
                lastGoalPriority = lastGoalPriority,
                lastTaskPriority = lastTaskPriority,
                recordedMainActivityUiState = mainRecordedActivityUiState,
                recordedSubActivityUiState = subRecordedActivityUiState,
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
                        Log.i("Navigation", "task $task")
                        dayViewModel.deleteTask(task)
                    }
                },
                onClickSaveActiveTime = {
                    dayViewModel.saveDay()
                },
                onMoveToCalendar = {
                    navController.navigate(route = MoveToCalendarRoute)
                },
                onMoveToToDoList = {
                    val task = dayState.tasks.find { task -> task.id == taskUiState.id }

                    if (task != null) {
                        dayViewModel.updateTask(
                            task.copy(
                                date = null,
                                startTime = null,
                                endTime = null,
                                priority = lastTaskPriority?.plus(1)
                            )
                        )
                    }
                },
                onNavigateToTaskActivityComparison = { navController.navigate(route = TaskActivityComparisonRoute) },
                onSetSelectedDate = { date ->
                    dayViewModel.setSelectedDate(date)
                },
                onSwitchScreen = dayViewModel::setIsActivityDisplay,
                saveMainRecordedActivity = dayViewModel::saveMainRecordedActivity,
                saveSubRecordedActivity = dayViewModel::saveSubRecordedActivity,
                saveDay = dayViewModel::saveDay,
                saveGoal = goalViewModel::saveGoal,
                saveGoalFromState = goalViewModel::saveGoal,
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
                setDayNote = dayViewModel::setDayNote,
                setGoalPriority = goalViewModel::setPriority,
                setGoalTitle = goalViewModel::setTitle,
                setRecordedMainActivityDate = dayViewModel::setRecordedMainActivityDate,
                setRecordedMainActivityEndTime = dayViewModel::setRecordedMainActivityEndTime,
                setRecordedMainActivityId = dayViewModel::setRecordedMainActivityId,
                setRecordedMainActivityNote = dayViewModel::setRecordedMainActivityNote,
                setRecordedMainActivityStartTime = dayViewModel::setRecordedMainActivityStartTime,
                setRecordedMainActivityTitle = dayViewModel::setRecordedMainActivityTitle,
                setRecordedSubActivityEndTime = dayViewModel::setRecordedSubActivityEndTime,
                setRecordedSubActivityId = dayViewModel::setRecordedSubActivityId,
                setRecordedSubActivityMainActivityId = dayViewModel::setRecordedSubActivityMainActivityId,
                setRecordedSubActivityNote = dayViewModel::setRecordedSubActivityNote,
                setRecordedSubActivityStartTime = dayViewModel::setRecordedSubActivityStartTime,
                setRecordedSubActivityTitle = dayViewModel::setRecordedSubActivityTitle,
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
                activityEditState = activityEditState,
                activityUiState = activityUiState,
                dayState = dayState,
                taskUiState = taskUiState,
                userInput = userInput,
                deleteActivity = { dayViewModel.deleteActivity(dayViewModel.activityEditState.value.toActivity()) },
                deleteTask = { dayViewModel.deleteTask(dayViewModel.taskUiState.value.toTask().copy(
                    id = dayViewModel.taskUiState.value.id!!
                )) },
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
                setActivityTitle = dayViewModel::setActivityTitle,
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