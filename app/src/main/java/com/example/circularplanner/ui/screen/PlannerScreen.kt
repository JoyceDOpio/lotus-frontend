package com.example.circularplanner.ui.screen

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
import com.example.circularplanner.data.Goal
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.component.ActiveTimeHeader
import com.example.circularplanner.ui.component.ActiveTimeSetUp
import com.example.circularplanner.ui.component.Calendar
import com.example.circularplanner.ui.component.DragItemListGoal
import com.example.circularplanner.ui.component.TaskDial
import com.example.circularplanner.ui.component.DragItemListTask
import com.example.circularplanner.ui.component.PopupDialog
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.DayUiState
import com.example.circularplanner.ui.viewmodel.GoalUiState
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.ui.viewmodel.UserInput
import java.time.LocalDate
import java.util.UUID

@Composable
fun PlannerScreen(
    innerPadding: PaddingValues,
    dayState: DayState,
    dayUiState: DayUiState,
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
    onMoveToToDoList: () -> Unit,
    onClickSaveActiveTime: () -> Unit,
    onSwitchGoals: (UUID, UUID) -> Unit,
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
    setIsActiveTimeSetUp: (Boolean) -> Unit,
    onSetSelectedDate: (LocalDate) -> Unit,
    setTaskDate: (LocalDate?) -> Unit,
    setTaskDescription: (String) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskPriority: (Int) -> Unit,
    setTaskTitle: (String) -> Unit
) {
//    val showActiveTimeSetUp = dayUiState.isActiveTimeSetUp
    var showActiveTimeSetUp by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
//        // Show planner
//        AnimatedVisibility(
//            visible = !showActiveTimeSetUp
//        ) {
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

                    TaskDial(
                        dayState = dayState,
                        drawClockHand = userInput.selectedDate.isEqual(LocalDate.now()),
                        lastTaskPriority = lastTaskPriority,
                        taskUiState = taskUiState,
                        userInput = userInput,
                        deleteTask = deleteTask,
                        onMoveToToDoList = onMoveToToDoList,
//                        onPressActiveTime = { setIsActiveTimeSetUp(true) },
                        onPressActiveTime = { showActiveTimeSetUp = true },
                        setTaskEndTime = setTaskEndTime,
                        setTaskStartTime = setTaskStartTime,
                        saveTask = saveTaskFromState,
                        saveTaskFromState = saveTaskFromState,
                        selectTask = selectTask,
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
                    onMoveToToDoList = onMoveToToDoList,
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
//                    onSwitch = onSwitchGoals,
                    deleteGoal = deleteGoal,
                    saveGoal = saveGoal,
                    saveGoalFromState = saveGoalFromState,
                    selectGoal = selectGoal,
                    setGoalPriority = setGoalPriority,
                    setGoalTitle = setGoalTitle
                )
            }
//        }

//        // Show active time setup
//        AnimatedVisibility(
//            visible = showActiveTimeSetUp
//        ) {
//            ActiveTimeSetUp(
//                dayUiState = dayUiState,
//                onBack = { setIsActiveTimeSetUp(false) },
//                onClickSaveActiveTime = onClickSaveActiveTime,
//                setActiveTimeStart = setActiveTimeStart,
//                setActiveTimeEnd = setActiveTimeEnd
//            )
//        }
    }

    if (showActiveTimeSetUp) {
        PopupDialog(
            onDismissRequest = {
                showActiveTimeSetUp = false
            }
        ) {
            // Show active time setup
            ActiveTimeSetUp(
                dayUiState = dayUiState,
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