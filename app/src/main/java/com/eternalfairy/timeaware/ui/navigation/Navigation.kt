package com.eternalfairy.timeaware.ui.navigation

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.eternalfairy.timeaware.service.StopwatchService
import com.eternalfairy.timeaware.ui.screen.login.LoginContainer
import com.eternalfairy.timeaware.ui.screen.planner.PlannerContainer
import com.eternalfairy.timeaware.ui.viewmodel.powersync.AuthViewModel
import com.eternalfairy.timeaware.ui.viewmodel.powersync.PlannerViewModel
import com.eternalfairy.timeaware.ui.viewmodel.supabase.AuthResponse
import com.eternalfairy.timeaware.utils.AudioRecorder
import kotlinx.coroutines.launch
import java.io.File

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun Navigation(
    navController: NavHostController,
    stopwatchService: StopwatchService,
) {
//    val dayViewModel: DayViewModel = viewModel(factory = DayViewModel.Factory)
//    val dayViewModel: DayViewModel = hiltViewModel()
    val plannerViewModel: PlannerViewModel = hiltViewModel()

    val activityEditState by plannerViewModel._activityEditState.collectAsState()// Use the mutable state so that it can be modified by the user
    val dayState by plannerViewModel.dayUiState.collectAsState()
    val mainRecordedActivityUiState by plannerViewModel.recordedMainActivityUiState.collectAsState()
    val subRecordedActivityUiState by plannerViewModel.recordedSubActivityUiState.collectAsState()
//    val dayUiState by dayViewModel.dayUiState.collectAsState()
    val lastTaskPriority by plannerViewModel.lastTaskPriority.collectAsState(initial = 0)
    val taskUiState by plannerViewModel._taskEditState.collectAsState()
    val toDoTasks by plannerViewModel.toDoTasks.collectAsState(emptyList())
    val userInput by plannerViewModel.userInput.collectAsState()
    val voiceNoteToBeDeleted by plannerViewModel.voiceNoteToBeDeletedState.collectAsState()

    val context = LocalContext.current
    val audioRecorder = AudioRecorder(context)

//    val goalViewModel: GoalViewModel = viewModel(factory = GoalViewModel.Factory)
//    val goalViewModel: GoalViewModel = hiltViewModel()
//    val goals by goalViewModel.goals.collectAsState()
//    val goalUiState by goalViewModel.goalUiState.collectAsState()
//    val lastGoalPriority by goalViewModel.lastPriority.collectAsState()
    val goals by plannerViewModel.goals.collectAsState()
    val goalUiState by plannerViewModel._goalEditState.collectAsState()
    val lastGoalPriority by plannerViewModel.lastGoalPriority.collectAsState()

    val activityUiState by plannerViewModel.activityUiState.collectAsState()

    val authViewModel: AuthViewModel = hiltViewModel()
    // Read the user session, i.e. determine whether the user is logged in or not (anonymously or not)
    val sessionStatus by authViewModel.sessionStatus.collectAsState()
    // I wonder whether this will get updated after navigation gets redirected from Login to Planner
    val userInfo by authViewModel.user.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    Log.i("Navigation", "sessionStatus ${sessionStatus.toString()}")

    NavHost(
        navController = navController,
//        startDestination = WelcomeRoute// TODO: Display welcoming (goals) once every day
//        startDestination = PlannerRoute
        // If the user is logged in, show the Planner screen directly; if not, show the login screen
//        startDestination = if (sessionStatus is SessionStatus.Authenticated) PlannerRoute else LoginRoute
        startDestination = LoginRoute
    ) {
        composable<LoginRoute> { backStackEntry ->
            LoginContainer(
                userInfo = userInfo,
//                onNavigateToNext = { navController.navigate(route = PlannerRoute) },
                onSignInAnonymously = {
                    coroutineScope.launch {
                        authViewModel.authManager.signInAnonymously().collect { response ->
                            when (response) {
                                AuthResponse.Success -> {
                                    navController.navigate(route = PlannerRoute)
                                }

                                is AuthResponse.Error -> {

                                }

                                else -> {}
                            }
                        }
                    }
                },
                onSignInWithEmail = { email, password ->
                    coroutineScope.launch {
                        authViewModel.authManager.signInWithEmail(email, password).collect { response ->
                            when (response) {
                                AuthResponse.Success -> {
                                    navController.navigate(route = PlannerRoute)
                                }

                                is AuthResponse.Error -> {

                                }

                                else -> {}
                            }
                        }
                    }
                },
                onSignUpWithEmail = { firstName, lastName, email, password ->
                    coroutineScope.launch {
                        authViewModel.authManager.signUpWithEmail(email, password).collect { response ->
                            when (response) {
                                AuthResponse.Success -> {
                                    // If sign-up was successful, create a user profile
                                    authViewModel.saveUserProfile(
                                        firstName = firstName,
                                        lastName = lastName,
                                        email = email
                                    )

                                    navController.navigate(route = PlannerRoute)
                                }

                                is AuthResponse.Error -> {

                                }

                                else -> {}
                            }
                        }
                    }
                },
            )
        }

        composable<PlannerRoute> { backStackEntry ->
            PlannerContainer(
                activityEditState = activityEditState,
                activityUiState = activityUiState,
                context = context,
                dayUiState = dayState,
                goals = goals,
                goalUiState = goalUiState,
                lastGoalPriority = lastGoalPriority,
                lastTaskPriority = lastTaskPriority,
                recordedMainActivityUiState = mainRecordedActivityUiState,
                recordedSubActivityUiState = subRecordedActivityUiState,
                stopwatchService = stopwatchService,
                taskUiState = taskUiState,
                toDoTasks = toDoTasks,
                // UserInfo should never be null when the PlannerRoute is reached
                userInfo = userInfo!!,
                userInput = userInput,
                clearMainRecordedActivity = plannerViewModel::clearMainRecordedActivity,
                clearSubRecordedActivity = plannerViewModel::clearSubRecordedActivity,
                deleteActivity = {
                    Log.i("Navigation", "activityToBeDeletedState ${plannerViewModel.activityToBeDeletedState.value}")
                    plannerViewModel.deleteActivity(plannerViewModel.activityToBeDeletedState.value.id!!)
                },
                deleteGoal = plannerViewModel::deleteGoal,
                deleteTask = {
                    val tasks = plannerViewModel.dayUiState.value.tasks + plannerViewModel.toDoTasks.value
                    val task = tasks.find { task -> task.id == plannerViewModel._taskEditState.value.id }

                    task?.let {
                        Log.i("Navigation", "task $task")
                        plannerViewModel.deleteTask(task.id!!)
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
                        plannerViewModel.deleteVoiceNote(voiceNoteToBeDeleted.id!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context,"Error deleting the file", Toast.LENGTH_SHORT).show()
                    }
                },
                onClickSaveActiveTime = {
                    plannerViewModel.saveDay()
                },
                onLogout = {
                    authViewModel.authManager.logOut()
                },
                onMoveToToDoList = {
                    val task = dayState.tasks.find { task -> task.id == taskUiState.id }

                    if (task != null) {
                        plannerViewModel.updateTask(
                            task.copy(
                                date = null,
                                startTime = null,
                                endTime = null,
                                priority = lastTaskPriority?.plus(1)
                            )
                        )
                    }
                },
                onNavigateToLogin = { navController.navigate(route = LoginRoute) },
                onPinTask = { value ->
                    plannerViewModel.setTaskPinned(value)
                    plannerViewModel.saveTaskFromState()
                },
                onSetSelectedDate = { date ->
                    plannerViewModel.setSelectedDate(date)
                },
                saveActivity = plannerViewModel::saveActivity,
                saveMainRecordedActivity = plannerViewModel::saveMainRecordedActivity,
                saveSubRecordedActivity = plannerViewModel::saveSubRecordedActivity,
                saveDay = plannerViewModel::saveDay,
                saveGoal = plannerViewModel::saveGoal,
                saveGoalFromState = plannerViewModel::saveGoalFromState,
                saveTask = plannerViewModel::saveTask,
                saveTaskFromState = plannerViewModel::saveTaskFromState,
                saveVoiceNote = plannerViewModel::saveVoiceNote,
                selectActivity = plannerViewModel::selectActivity,
                selectActivityToBeDeleted = plannerViewModel::selectActivityForDeletion,
                selectActivityToBeEdited = plannerViewModel::selectActivityForEditing,
                selectGoal = plannerViewModel::selectGoal,
                selectTask = plannerViewModel::selectTask,
                selectVoiceNoteToBeDeleted = plannerViewModel::selectVoiceNoteForDeletion,
                setActiveTimeStart = plannerViewModel::setActiveTimeStart,
                setActiveTimeEnd = plannerViewModel::setActiveTimeEnd,
                setActivityNote = plannerViewModel::setActivityNote,
                setActivityTitle = plannerViewModel::setActivityTitle,
                setActualActiveTimeEnd = plannerViewModel::setActualActiveTimeEnd,
                setActualActiveTimeStart = plannerViewModel::setActualActiveTimeStart,
                setGoalPriority = plannerViewModel::setGoalPriority,
                setGoalTitle = plannerViewModel::setGoalTitle,
                setRecordedMainActivityDate = plannerViewModel::setRecordedMainActivityDate,
                setRecordedMainActivityEndTime = plannerViewModel::setRecordedMainActivityEndTime,
                setRecordedMainActivityId = plannerViewModel::setRecordedMainActivityId,
                setRecordedMainActivityNote = plannerViewModel::setRecordedMainActivityNote,
                setRecordedMainActivityStartTime = plannerViewModel::setRecordedMainActivityStartTime,
                setRecordedMainActivityTitle = plannerViewModel::setRecordedMainActivityTitle,
                setRecordedSubActivityEndTime = plannerViewModel::setRecordedSubActivityEndTime,
                setRecordedSubActivityId = plannerViewModel::setRecordedSubActivityId,
                setRecordedSubActivityMainActivityId = plannerViewModel::setRecordedSubActivityMainActivityId,
                setRecordedSubActivityNote = plannerViewModel::setRecordedSubActivityNote,
                setRecordedSubActivityStartTime = plannerViewModel::setRecordedSubActivityStartTime,
                setRecordedSubActivityTitle = plannerViewModel::setRecordedSubActivityTitle,
                setTaskDate = plannerViewModel::setTaskDate,
                setTaskDescription = plannerViewModel::setTaskDescription,
                setTaskEndTime = plannerViewModel::setTaskEndTime,
                setTaskPriority = plannerViewModel::setTaskPriority,
                setTaskStartTime = plannerViewModel::setTaskStartTime,
                setTaskTitle = plannerViewModel::setTaskTitle,
                startRecording = audioRecorder::startRecording,
                stopRecording = audioRecorder::stopRecording,
                updateLastPlayedPosition = plannerViewModel::updateLastPlayedPosition
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