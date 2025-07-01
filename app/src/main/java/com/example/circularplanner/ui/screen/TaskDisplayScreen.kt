package com.example.circularplanner.ui.screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.circularplanner.R
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.component.ActiveTimeHeader
import com.example.circularplanner.ui.component.ActiveTimeSetUp
import com.example.circularplanner.ui.component.Calendar
import java.util.UUID
import com.example.circularplanner.ui.component.TaskDial
import com.example.circularplanner.ui.component.TaskList
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.DayUiState
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.ui.viewmodel.UserInput
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDisplayScreen(
    dayUiState: DayUiState,
    dayState: DayState,
    taskUiState: TaskUiState,
    userInput: UserInput,
    deleteTask: (Task) -> Unit,
    onChangeDisplayForm: (Boolean) -> Unit,
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
    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
    var showList = dayUiState.isList
    var showActiveTimeSetUp = dayUiState.isActiveTimeSetUp
    val selectedDate = userInput.selectedDate

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${selectedDate.format(formatter)}") },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primaryContainer),
            )
        },
        bottomBar = {
            BottomAppBar (
//                contentColor = MaterialTheme.colorScheme.primaryContainer
                actions = {
//                    // Leading icons should typically have a high content alpha
//                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
//                        IconButton(onClick = { /* doSomething() */ }) {
//                            Icon(Icons.Filled.Menu, contentDescription = "Localized description")
//                        }
//                    }
                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))

                    IconButton(
                        onClick = {
                            selectTask(null)
                            onNavigateToTaskEdit()
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.add_circle_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Add task",
                            modifier = Modifier.fillMaxSize(0.8F)
                        )
                    }

                    IconButton(onClick = {
                        onChangeDisplayForm(!showList)
                    }) {
                        if (!showList) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.list_24dp_5f6368_fill0_wght400_grad0_opsz24),
                                contentDescription = "List view",
                                modifier = Modifier.fillMaxSize(0.8F)
                            )
                        }
                        else {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.data_usage_24dp_5f6368_fill0_wght400_grad0_opsz24),
                                contentDescription = "Dial view",
                                modifier = Modifier.fillMaxSize(0.8F)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ActiveTimeHeader(
                dayState = dayState
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
}