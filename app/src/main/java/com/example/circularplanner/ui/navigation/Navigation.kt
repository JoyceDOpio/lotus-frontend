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
import com.example.circularplanner.ui.screen.MainScreen
import com.example.circularplanner.ui.screen.TaskActivityComparisonScreen
import com.example.circularplanner.ui.screen.TaskEditScreen
import com.example.circularplanner.ui.screen.TaskInfoScreen
import com.example.circularplanner.ui.viewmodel.AppViewModel
import com.example.circularplanner.ui.viewmodel.toVoiceNote
import com.example.circularplanner.utils.AudioRecorder
import java.io.File

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun Navigation(
    navController: NavHostController
) {
    val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)

    val activityUiState by appViewModel.activityUiState.collectAsState()
    val dayState by appViewModel.dayState.collectAsState()
    val recordedActivityState by appViewModel.recordedActivityState.collectAsState()
    val dayUiState by appViewModel.dayUiState.collectAsState()
    val taskUiState by appViewModel.taskUiState.collectAsState()
    val userInput by appViewModel.userInput.collectAsState()

    val context = LocalContext.current
    val audioRecorder = AudioRecorder()

    NavHost(
        navController = navController,
        startDestination = TaskDisplayRoute
    ) {
        composable<TaskDisplayRoute> {
            MainScreen(
                context = context,
                dayUiState = dayUiState,
                recordedActivityState = recordedActivityState,
                dayState = dayState,
                taskUiState = taskUiState,
                userInput = userInput,
                addVoiceNoteToActivity = appViewModel::addVoiceNoteToActivity,
                clearRecordedActivity = appViewModel::clearRecordedActivity,
                deleteTask = { task ->
                    appViewModel.deleteTask(task)
                },
                deleteVoiceNote = {voiceNoteUiState ->
                    try {
                        val file = File(voiceNoteUiState.uri)
                        if (file.exists()) {
                            // Delete the voice note audio file from the local storage
                            file.delete()
                        }
                        // The voice note from the database
                        appViewModel.deleteVoiceNote(voiceNoteUiState.toVoiceNote())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context,"Error deleting the file", Toast.LENGTH_SHORT).show()
                    }
                },
                onChangeDisplayForm = appViewModel::setIsList,
                onClickSaveActiveTime = {
                    appViewModel.saveDay()
                    appViewModel.setIsActiveTimeSetUp(false)
                },
                onNavigateToTaskActivityComparison = { navController.navigate(route = TaskActivityComparisonRoute) },
                onNavigateToTaskEdit = { navController.navigate(route = TaskEditRoute) },
                onNavigateToTaskInfo = { navController.navigate(route = TaskInfoRoute) },
                onSetSelectedDate = { date ->
                    appViewModel.setSelectedDate(date)
                },
                onSwitchScreen = appViewModel::setIsActivityDisplay,
                saveActivity = appViewModel::saveActivity,
                saveTask = appViewModel::saveTask,
                selectActivity = appViewModel::selectActivity,
                selectTask = appViewModel::selectTask,
                setActiveTimeStart = appViewModel::setActiveTimeStart,
                setActiveTimeEnd = appViewModel::setActiveTimeEnd,
                setActivityStartTime = appViewModel::setActivityStartTime,
                setActivityEndTime = appViewModel::setActivityEndTime,
                setActivityId = appViewModel::setActivityId,
                setActivityTitle = appViewModel::setActivityTitle,
                setIsActiveTimeSetUp = appViewModel::setIsActiveTimeSetUp,
                setIsTimerRunning = appViewModel::setIsTimerRunning,
                setTaskStartTime = appViewModel::setTaskStartTime,
                setTaskEndTime = appViewModel::setTaskEndTime,
                startRecording = audioRecorder::startRecording,
                stopRecording = audioRecorder::stopRecording,
                updateLastPlayedPosition = appViewModel::updateLastPlayedPositionRecordedActivity
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
                        appViewModel.deleteVoiceNote(voiceNoteUiState.toVoiceNote())
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context,"Error deleting the file", Toast.LENGTH_SHORT).show()
                    }
                },
                onCancel = {
                    navController.popBackStack()
                },
                updateLastPlayedPosition = appViewModel::updateLastPlayedPosition
            )
        }

        composable<TaskEditRoute> { backStackEntry ->
            TaskEditScreen(
                taskUiState = taskUiState,
                onBack = {
                    navController.popBackStack()
                },
                saveTask = {
                    appViewModel.saveTask()
                },
                setTaskStartTime = appViewModel::setTaskStartTime,
                setTaskEndTime = appViewModel::setTaskEndTime,
                setTaskDescription = appViewModel::setTaskDescription,
                setTaskTitle = appViewModel::setTaskTitle,
            )
        }

        composable<TaskInfoRoute> { backStackEntry ->
            TaskInfoScreen(
                taskUiState = taskUiState,
                userInput = userInput,
                onCancel = {
                    navController.popBackStack()
                },
                onNavigateToTaskEdit = {
                    navController.navigate(route = TaskEditRoute)
                },
            )
        }
    }
}