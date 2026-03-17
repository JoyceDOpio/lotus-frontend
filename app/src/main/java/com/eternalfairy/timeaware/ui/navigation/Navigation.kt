package com.eternalfairy.timeaware.ui.navigation

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.eternalfairy.timeaware.service.StopwatchService
import com.eternalfairy.timeaware.ui.screen.planner.PlannerContainer
import com.eternalfairy.timeaware.ui.viewmodel.room.DayViewModel
import com.eternalfairy.timeaware.ui.viewmodel.room.GoalViewModel
import com.eternalfairy.timeaware.ui.viewmodel.room.toActivity
import com.eternalfairy.timeaware.ui.viewmodel.room.toVoiceNote
import com.eternalfairy.timeaware.utils.AudioRecorder
import java.io.File

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun Navigation(
    navController: NavHostController,
    stopwatchService: StopwatchService,
) {
//    val dayViewModel: DayViewModel = viewModel(factory = DayViewModel.Factory)
    val dayViewModel: DayViewModel = hiltViewModel()

    val activityEditState by dayViewModel._activityEditState.collectAsState()// Use the mutable state so that it can be modified by the user
    val dayState by dayViewModel.dayState.collectAsState()
    val mainRecordedActivityUiState by dayViewModel.recordedMainActivityUiState.collectAsState()
    val subRecordedActivityUiState by dayViewModel.recordedSubActivityUiState.collectAsState()
//    val dayUiState by dayViewModel.dayUiState.collectAsState()
    val lastTaskPriority by dayViewModel.lastTaskPriority.collectAsState(initial = 0)
    val taskUiState by dayViewModel.taskUiState.collectAsState()
    val toDoTasks by dayViewModel.toDoTasks.collectAsState(emptyList())
    val userInput by dayViewModel.userInput.collectAsState()
    val voiceNoteToBeDeleted by dayViewModel.voiceNoteToBeDeletedState.collectAsState()

    val context = LocalContext.current
    val audioRecorder = AudioRecorder(context)

//    val goalViewModel: GoalViewModel = viewModel(factory = GoalViewModel.Factory)
    val goalViewModel: GoalViewModel = hiltViewModel()
    val goals by goalViewModel.goals.collectAsState()
    val goalUiState by goalViewModel.goalUiState.collectAsState()
    val lastGoalPriority by goalViewModel.lastPriority.collectAsState()

    val activityUiState by dayViewModel.activityUiState.collectAsState()

    NavHost(
        navController = navController,
//        startDestination = WelcomeRoute// TODO: Display welcoming (goals) once every day
        startDestination = PlannerRoute
    ) {
//        composable<LoginRoute>{ backStackEntry ->
//            LoginScreen(
//                goals = goals,
//                onNext = { navController.navigate(route = PlannerRoute) }
//            )
//        }

        composable<PlannerRoute> { backStackEntry ->
            PlannerContainer(
                activityEditState = activityEditState,
                activityUiState = activityUiState,
                context = context,
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
                deleteActivity = {
                    Log.i("Navigation", "activityToBeDeletedState ${dayViewModel.activityToBeDeletedState.value}")
                    dayViewModel.deleteActivity(dayViewModel.activityToBeDeletedState.value.toActivity())
                },
                deleteGoal = goalViewModel::deleteGoal,
                deleteTask = {
                    val tasks = dayViewModel.dayState.value.tasks + dayViewModel.toDoTasks.value
                    val task = tasks.find { task -> task.id == dayViewModel.taskUiState.value.id }

                    if (task != null) {
                        Log.i("Navigation", "task $task")
                        dayViewModel.deleteTask(task)
                    }
                },
                deleteVoiceNote = {
                    try {
                        val file = File(voiceNoteToBeDeleted.uri)
                        if (file.exists()) {
                            // Delete the voice note audio file from the local storage
                            file.delete()
                        }
                        // The voice note reference from the database
                        dayViewModel.deleteVoiceNote(voiceNoteToBeDeleted.toVoiceNote())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context,"Error deleting the file", Toast.LENGTH_SHORT).show()
                    }
                },
                onClickSaveActiveTime = {
                    dayViewModel.saveDay()
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
                onPinTask = { value ->
                    dayViewModel.setTaskPinned(value)
                    dayViewModel.saveTask()
                },
                onSetSelectedDate = { date ->
                    dayViewModel.setSelectedDate(date)
                },
                saveActivity = dayViewModel::saveActivity,
                saveMainRecordedActivity = dayViewModel::saveMainRecordedActivity,
                saveSubRecordedActivity = dayViewModel::saveSubRecordedActivity,
                saveDay = dayViewModel::saveDay,
                saveGoal = goalViewModel::saveGoal,
                saveGoalFromState = goalViewModel::saveGoal,
                saveTask = dayViewModel::saveTask,
                saveTaskFromState = dayViewModel::saveTask,
                saveVoiceNote = dayViewModel::saveVoiceNote,
                selectActivity = dayViewModel::selectActivity,
                selectActivityToBeDeleted = dayViewModel::selectActivityForDeletion,
                selectActivityToBeEdited = dayViewModel::selectActivityForEditing,
                selectGoal = goalViewModel::selectGoal,
                selectTask = dayViewModel::selectTask,
                selectVoiceNoteToBeDeleted = dayViewModel::selectVoiceNoteForDeletion,
                setActiveTimeStart = dayViewModel::setActiveTimeStart,
                setActiveTimeEnd = dayViewModel::setActiveTimeEnd,
                setActivityNote = dayViewModel::setActivityNote,
                setActivityTitle = dayViewModel::setActivityTitle,
                setActualActiveTimeEnd = dayViewModel::setActualActiveTimeEnd,
                setActualActiveTimeStart = dayViewModel::setActualActiveTimeStart,
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
                stopRecording = audioRecorder::stopRecording,
                updateLastPlayedPosition = dayViewModel::updateLastPlayedPosition
            )
        }

//        composable<WelcomeRoute>{ backStackEntry ->
//            WelcomeScreen(
//                goals = goals,
//                onNext = { navController.navigate(route = PlannerRoute) }
//            )
//        }
    }
}