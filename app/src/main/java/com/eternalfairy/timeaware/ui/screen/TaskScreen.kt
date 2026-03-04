package com.eternalfairy.timeaware.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eternalfairy.timeaware.data.Goal
import com.eternalfairy.timeaware.data.Task
import com.eternalfairy.timeaware.data.Time
import com.eternalfairy.timeaware.ui.component.ActiveTimeHeader
import com.eternalfairy.timeaware.ui.component.ActiveTimeSetUp
import com.eternalfairy.timeaware.ui.component.Calendar
import com.eternalfairy.timeaware.ui.component.DragItemListGoal
import com.eternalfairy.timeaware.ui.component.PlannerDial
import com.eternalfairy.timeaware.ui.component.DragItemListTask
import com.eternalfairy.timeaware.ui.component.PopupDialog
import com.eternalfairy.timeaware.ui.viewmodel.DayState
import com.eternalfairy.timeaware.ui.viewmodel.GoalUiState
import com.eternalfairy.timeaware.ui.viewmodel.TaskUiState
import com.eternalfairy.timeaware.ui.viewmodel.UserInput
import java.time.LocalDate
import java.util.UUID

@Composable
fun TaskScreen(
    innerPadding: PaddingValues,
    dayState: DayState,
    goals: List<Goal>,
    goalUiState: GoalUiState,
    lastGoalPriority: Int?,
    lastTaskPriority: Int?,
    state: State,
    taskUiState: TaskUiState,
    toDoTasks: List<Task>,
    userInput: UserInput,
    deleteGoal: (Goal) -> Unit,
    deleteTask: () -> Unit,
    onMoveToCalendar: () -> Unit,
    onMoveToToDoList: () -> Unit,
    onPinTask: (Boolean) -> Unit,
    onClickSaveActiveTime: () -> Unit,
    saveGoal: (Goal) -> Unit,
    saveGoalFromState: () -> Unit,
    saveTask: (Task) -> Unit,
    saveTaskFromState: () -> Unit,
    selectGoal: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
    setActiveTimeStart: (Time) -> Unit,
    setActiveTimeEnd: (Time) -> Unit,
    setGoalPriority: (Int) -> Unit,
    setGoalTitle: (String) -> Unit,
    onSetSelectedDate: (LocalDate) -> Unit,
    setTaskDate: (LocalDate?) -> Unit,
    setTaskDescription: (String) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskPriority: (Int) -> Unit,
    setTaskTitle: (String) -> Unit
) {
    var showActiveTimeSetUp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
            // Show the day planner
            AnimatedVisibility(
                visible = state == State.Task,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    ActiveTimeHeader(
                        dayState = dayState,
                        userInput = userInput
                    )

                    PlannerDial(
                        dayState = dayState,
                        drawClockHand = userInput.selectedDate.isEqual(LocalDate.now()),
                        lastTaskPriority = lastTaskPriority,
                        taskUiState = taskUiState,
                        userInput = userInput,
                        deleteTask = deleteTask,
                        onMoveToToDoList = onMoveToToDoList,
                        onPressActiveTime = { showActiveTimeSetUp = true },
                        onPinTask = onPinTask,
                        saveTask = saveTask,
                        saveTaskFromState = saveTaskFromState,
                        selectTask = selectTask,
                        setTaskEndTime = setTaskEndTime,
                        setTaskStartTime = setTaskStartTime,
                        setTaskDate = setTaskDate,
                        setTaskDescription = setTaskDescription,
                        setTaskPriority = setTaskPriority,
                        setTaskTitle = setTaskTitle
                    )

                    Calendar(
                        userInput = userInput,
                        onSetDate = onSetSelectedDate
                    )
                }
            }

            // Show the TO-DO list (list of tasks without specific date and time)
            AnimatedVisibility(
                visible = state == State.ToDo,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                DragItemListTask (
                    dayState = dayState,
                    items = toDoTasks,
                    lastTaskPriority = lastTaskPriority,
                    taskUiState = taskUiState,
                    deleteTask = deleteTask,
                    onMoveToCalendar = onMoveToCalendar,
                    onMoveToToDoList = onMoveToToDoList,
                    onPinTask = onPinTask,
                    saveTask = saveTask,
                    saveTaskFromState = saveTaskFromState,
                    selectTask = selectTask,
                    setTaskDescription = setTaskDescription,
                    setTaskEndTime = setTaskEndTime,
                    setTaskStartTime = setTaskStartTime,
                    setTaskPriority = setTaskPriority,
                    setTaskTitle = setTaskTitle
                )
            }

            // Show the goals
            AnimatedVisibility(
                visible = state == State.Goal,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                DragItemListGoal(
                    items = goals,
                    goalUiState = goalUiState,
                    lastGoalPriority = lastGoalPriority,
                    deleteGoal = deleteGoal,
                    saveGoal = saveGoal,
                    saveGoalFromState = saveGoalFromState,
                    selectGoal = selectGoal,
                    setGoalPriority = setGoalPriority,
                    setGoalTitle = setGoalTitle
                )
            }
    }

    if (showActiveTimeSetUp) {
        PopupDialog(
            onDismissRequest = {
                showActiveTimeSetUp = false
            }
        ) {
            // Show active time setup
            ActiveTimeSetUp(
                dayState = dayState,
//                dayUiState = dayUiState,
                onBack = { showActiveTimeSetUp = false },
                onClickSaveActiveTime = {
                    onClickSaveActiveTime()
                    showActiveTimeSetUp = false
                },
                setActiveTimeStart = setActiveTimeStart,
                setActiveTimeEnd = setActiveTimeEnd
            )
        }
    }
}