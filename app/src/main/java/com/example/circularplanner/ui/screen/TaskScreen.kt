package com.example.circularplanner.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.component.ActiveTimeHeader
import com.example.circularplanner.ui.component.ActiveTimeSetUp
import com.example.circularplanner.ui.component.Calendar
import com.example.circularplanner.ui.component.TaskDial
import com.example.circularplanner.ui.component.TaskList
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.DayUiState
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.ui.viewmodel.UserInput
import java.time.LocalDate
import java.util.UUID

@Composable
fun TaskScreen(
    innerPadding: PaddingValues,
    dayState: DayState,
    dayUiState: DayUiState,
    taskUiState: TaskUiState,
    userInput: UserInput,
    deleteTask: (Task) -> Unit,
    onNavigateToTaskEdit: () -> Unit,
    onNavigateToTaskInfo: () -> Unit,
    onClickSaveActiveTime: () -> Unit,
    saveTask: () -> Unit,
    selectTask: (UUID?) -> Unit,
    setActiveTimeStart: (Time) -> Unit,
    setActiveTimeEnd: (Time) -> Unit,
    setIsActiveTimeSetUp: (Boolean) -> Unit,
    onSetSelectedDate: (LocalDate) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskEndTime: (Time) -> Unit
) {
    val showList = dayUiState.isList
    val showActiveTimeSetUp = dayUiState.isActiveTimeSetUp

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ActiveTimeHeader(
            dayState = dayState,
            userInput = userInput
        )

        Row(
            modifier = Modifier.fillMaxHeight(0.8f)
        ) {
            // Show planner
            AnimatedVisibility(
                visible = !showActiveTimeSetUp
            ) {
                // Show tasks on the dial
                AnimatedVisibility(
                    visible = !showList,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    TaskDial(
                        dayState = dayState,
                        taskUiState = taskUiState,
                        userInput = userInput,
                        onNavigateToTaskEdit = onNavigateToTaskEdit,
                        onNavigateToTaskInfo = onNavigateToTaskInfo,
                        onPressActiveTime = { setIsActiveTimeSetUp(true) },
                        onSetTaskEndTime = setTaskEndTime,
                        onSetTaskStartTime = setTaskStartTime,
                        saveTask = saveTask,
                        selectTask = selectTask
                    )
                }

                // Show tasks as a list
                AnimatedVisibility(
                    visible = showList,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    TaskList (
                        dayState = dayState,
                        onNavigateToTaskInfo = onNavigateToTaskInfo,
                        selectTask = selectTask,
                        removeTask = deleteTask
                    )
                }
            }

            // Show active time setup
            AnimatedVisibility(
                visible = showActiveTimeSetUp
            ) {
                ActiveTimeSetUp(
                    dayUiState = dayUiState,
                    onBack = { setIsActiveTimeSetUp(false) },
                    onClickSaveActiveTime = onClickSaveActiveTime,
                    setActiveTimeStart = setActiveTimeStart,
                    setActiveTimeEnd = setActiveTimeEnd
                )
            }
        }

        Calendar(
            userInput = userInput,
            onSetDate = onSetSelectedDate
        )
    }
}