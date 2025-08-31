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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.circularplanner.data.Goal
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.component.ActiveTimeHeader
import com.example.circularplanner.ui.component.ActiveTimeSetUp
import com.example.circularplanner.ui.component.Calendar
import com.example.circularplanner.ui.component.DragItemList
import com.example.circularplanner.ui.component.TaskDial
import com.example.circularplanner.ui.component.TaskList
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.DayUiState
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
    state: State,
    taskUiState: TaskUiState,
    toDoTasks: List<Task>,
    userInput: UserInput,
    deleteGoal: (Goal) -> Unit,
    onNavigateToGoalEdit: () -> Unit,
    onNavigateToTaskEdit: () -> Unit,
    onNavigateToTaskInfo: () -> Unit,
    onClickSaveActiveTime: () -> Unit,
    onSwitchGoals: (UUID, UUID) -> Unit,
    saveTask: () -> Unit,
    selectGoal: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
    setActiveTimeStart: (Time) -> Unit,
    setActiveTimeEnd: (Time) -> Unit,
    setIsActiveTimeSetUp: (Boolean) -> Unit,
    onSetSelectedDate: (LocalDate) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskDate: (LocalDate?) -> Unit
) {
    val showActiveTimeSetUp = dayUiState.isActiveTimeSetUp

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Show planner
        AnimatedVisibility(
            visible = !showActiveTimeSetUp
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

                    TaskDial(
                        dayState = dayState,
                        drawClockHand = userInput.selectedDate.isEqual(LocalDate.now()),
                        taskUiState = taskUiState,
                        userInput = userInput,
                        onNavigateToTaskEdit = onNavigateToTaskEdit,
                        onNavigateToTaskInfo = onNavigateToTaskInfo,
                        onPressActiveTime = { setIsActiveTimeSetUp(true) },
                        setTaskEndTime = setTaskEndTime,
                        setTaskStartTime = setTaskStartTime,
                        saveTask = saveTask,
                        selectTask = selectTask,
                        setTaskDate = setTaskDate
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
                TaskList (//TODO: Modify the list to allow dragging of items and setting priority
                    tasks = toDoTasks,
                    onNavigateToTaskInfo = onNavigateToTaskInfo,
                    selectTask = selectTask
                )
            }

            // Show the goals
            AnimatedVisibility(
                visible = state == State.Goal,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                // TODO: List of goals that allows dragging of items and setting priority
                DragItemList(
                    items = goals,
                    onNavigateToGoalEdit = onNavigateToGoalEdit,
                    onSwitch = onSwitchGoals,
                    deleteGoal = deleteGoal,
                    selectGoal = selectGoal
//                    listItem = GoalListItem
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
}