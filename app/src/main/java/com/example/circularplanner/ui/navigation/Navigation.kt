package com.example.circularplanner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.circularplanner.ui.screen.TaskDisplayScreen
import com.example.circularplanner.ui.screen.TaskEditScreen
import com.example.circularplanner.ui.screen.TaskInfoScreen
import com.example.circularplanner.ui.viewmodel.TaskDisplayViewModel

@Composable
fun Navigation(
    navController: NavHostController
) {
    val taskViewModel: TaskDisplayViewModel = viewModel(factory = TaskDisplayViewModel.Factory)
    val dayUiState by taskViewModel.dayUiState.collectAsState()
    val dayState by taskViewModel.dayState.collectAsState()
    val taskUiState by taskViewModel.taskUiState.collectAsState()
    val userInput by taskViewModel.userInput.collectAsState()

    NavHost(
        navController = navController,
        startDestination = TaskDisplayRoute
    ) {
        composable<TaskDisplayRoute> {
            TaskDisplayScreen(
                dayUiState = dayUiState,
                dayState = dayState,
                taskUiState = taskUiState,
                userInput = userInput,
                deleteTask = { task ->
                    taskViewModel.deleteTask(task)
                },
                onChangeDisplayForm = taskViewModel::setIsList,
                onNavigateToTaskEdit = { navController.navigate(route = TaskEditRoute) },
                onNavigateToTaskInfo = { navController.navigate(route = TaskInfoRoute) },
                onClickSaveActiveTime = {
                    taskViewModel.saveDay()
                    taskViewModel.setIsActiveTimeSetUp(false)
                },
                onSetSelectedDate = { date ->
                    taskViewModel.setSelectedDate(date)
                },
                saveTask = taskViewModel::saveTask,
                selectTask = taskViewModel::selectTask,
                setActiveTimeStart = taskViewModel::setActiveTimeStart,
                setActiveTimeEnd = taskViewModel::setActiveTimeEnd,
                setIsActiveTimeSetUp = taskViewModel::setIsActiveTimeSetUp,
                setTaskStartTime = taskViewModel::setTaskStartTime,
                setTaskEndTime = taskViewModel::setTaskEndTime
            )
        }

        composable<TaskEditRoute> { backStackEntry ->
            TaskEditScreen(
                taskUiState = taskUiState,
                onBack = {
                    navController.popBackStack()
                },
                saveTask = {
                    taskViewModel.saveTask()
                },
                setTaskStartTime = taskViewModel::setTaskStartTime,
                setTaskEndTime = taskViewModel::setTaskEndTime,
                setTaskDescription = taskViewModel::setTaskDescription,
                setTaskTitle = taskViewModel::setTaskTitle,
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