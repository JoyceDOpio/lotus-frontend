package com.eternalfairy.timeaware.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.timeaware.R
import com.eternalfairy.timeaware.data.Goal
import com.eternalfairy.timeaware.data.Task
import com.eternalfairy.timeaware.data.Time
import com.eternalfairy.timeaware.ui.component.ActiveTimeHeader
import com.eternalfairy.timeaware.ui.component.ActivityGraph
import com.eternalfairy.timeaware.ui.component.BottomBar
import com.eternalfairy.timeaware.ui.component.Calendar
import com.eternalfairy.timeaware.ui.component.ComparisonDial
import com.eternalfairy.timeaware.ui.component.DragItemListGoal
import com.eternalfairy.timeaware.ui.component.DragItemListTask
import com.eternalfairy.timeaware.ui.component.DropDownItem
import com.eternalfairy.timeaware.ui.component.PlannerDial
import com.eternalfairy.timeaware.ui.component.SubActivityList
import com.eternalfairy.timeaware.ui.component.TaskCard
import com.eternalfairy.timeaware.ui.component.TaskDropdownMenu
import com.eternalfairy.timeaware.ui.component.TopBar
import com.eternalfairy.timeaware.ui.component.VoiceNoteList
import com.eternalfairy.timeaware.ui.viewmodel.ActivityUiState
import com.eternalfairy.timeaware.ui.viewmodel.AudioViewModel
import com.eternalfairy.timeaware.ui.viewmodel.DayState
import com.eternalfairy.timeaware.ui.viewmodel.GoalUiState
import com.eternalfairy.timeaware.ui.viewmodel.TaskUiState
import com.eternalfairy.timeaware.ui.viewmodel.UserInput
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun BigScreenLandscape (
    activityUiState: ActivityUiState,
    audioViewModel: AudioViewModel,
    dayState: DayState,
    goals: List<Goal>,
    goalUiState: GoalUiState,
    lastGoalPriority: Int?,
    lastTaskPriority: Int?,
    taskUiState: TaskUiState,
    toDoTasks: List<Task>,
    userInput: UserInput,
    deleteGoal: (Goal) -> Unit,
    deleteTask: () -> Unit,
    deleteVoiceNote: () -> Unit,
    onDeleteActivity: (UUID) -> Unit,
    onEditActivity: (UUID?) -> Unit,
    onDeleteTask: () -> Unit,
    onEditTask: () -> Unit,
    onDeleteVoiceNote: (UUID?) -> Unit,
    onMoveToCalendar: () -> Unit,
    onMoveToToDoList: () -> Unit,
    onPinTask: (Boolean) -> Unit,
    onPressActiveTime: () -> Unit,
    onSetSelectedDate: (LocalDate) -> Unit,
    onShowPopupWindow: (PopupState) -> Unit,
    saveGoal: (Goal) -> Unit,
    saveGoalFromState: () -> Unit,
    saveTask: (Task) -> Unit,
    saveTaskFromState: () -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectGoal: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
    selectVoiceNoteToBeDeleted: (UUID?) -> Unit,
    setGoalPriority: (Int) -> Unit,
    setGoalTitle: (String) -> Unit,
    setTaskDate: (LocalDate?) -> Unit,
    setTaskDescription: (String) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskPriority: (Int) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskTitle: (String) -> Unit,
    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
    val selectedDate = userInput.selectedDate

    // Screen state
    var mainPanelState by remember { mutableStateOf(BigScreenMainPanelState.DayTask) }
    var sidePanelState by remember { mutableStateOf(BigScreenSidePanelState.ToDo) }

    // Texts
    val activityLabel = "ACTIVITY"//TODO: Read from string resource
    val taskLabel = "TASK"//TODO: Read from string resource
    val editText = "Edit"//TODO: Read from string resource
    val deleteText = "Delete"//TODO: Read from string resource
    val noTaskText = "NO TASK TO DISPLAY"//TODO: Read from string resource
    val noActivityText = "NO ACTIVITY TO DISPLAY"//TODO: Read from string resource
    val dayTitle = "Day overview"//TODO: Read from string resource
    val goalsTitle = "Goals"//TODO: Read from string resource
    val toDoTitle = "To Do"//TODO: Read from string resource

    // Comparison of task and activity
    val taskDetails = taskUiState
    val activityDetails = activityUiState

    Row (
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Side panel
        Column (
            modifier = Modifier
                .fillMaxWidth(0.25f)
        ) {
            TopBar(
                title = selectedDate.format(formatter)
            )

            AnimatedVisibility(
                visible = userInput.selectedDate.isEqual(LocalDate.now())
            ) {
                ActiveTimeHeader(
                    dayState = dayState,
                    userInput = userInput
                )
            }

            // Show the TO-DO list (list of tasks without specific date and time)
            AnimatedVisibility(
                visible = sidePanelState == BigScreenSidePanelState.ToDo,
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
                visible = sidePanelState == BigScreenSidePanelState.Goal,
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

            Row (
                modifier = Modifier
                    .weight(1.05f),
                verticalAlignment = Alignment.Bottom
            ) {
                Calendar(
                    userInput = userInput,
                    onSetDate = onSetSelectedDate
                )
            }

            BottomBar(
                content = {
                    // Add button
                    IconButton(
                        onClick = {
                            val popupState: PopupState

                            when (sidePanelState) {
                                BigScreenSidePanelState.Goal -> {
                                    selectGoal(null)
                                    popupState = PopupState.EditGoal
                                }

                                BigScreenSidePanelState.ToDo -> {
                                    selectTask(null)
                                    setTaskDate(null)
                                    popupState = PopupState.EditTask
                                }
                            }

                            onShowPopupWindow(popupState)
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.add_square_svgrepo_com),
                            contentDescription = "Add",
                            modifier = Modifier.fillMaxSize(0.8F),
                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        )
                    }

                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))

                    IconButton(
                        onClick = {
                            sidePanelState = when (sidePanelState) {
                                BigScreenSidePanelState.Goal -> {
                                    BigScreenSidePanelState.ToDo
                                }

                                BigScreenSidePanelState.ToDo -> {
                                    BigScreenSidePanelState.Goal
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = when (sidePanelState) {
                                BigScreenSidePanelState.ToDo -> {
                                    ImageVector.vectorResource(id = R.drawable.target_love_svgrepo_com)
                                }
                                BigScreenSidePanelState.Goal -> {
                                    ImageVector.vectorResource(id = R.drawable.list_svgrepo_com)
                                }
                            },
                            contentDescription = when (sidePanelState) {
                                BigScreenSidePanelState.ToDo -> {
                                    goalsTitle
                                }
                                BigScreenSidePanelState.Goal -> {
                                    toDoTitle
                                }
                            },
                            modifier = Modifier.fillMaxSize(0.75f),
                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        )
                    }
                }
            )
        }

        // Main panel
        Column (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // If the selected date is in the past, show a comparison of the planned tasks and actual activities
            AnimatedVisibility(
                visible = selectedDate.isBefore(LocalDate.now())
            ) {// FIXME: This can be removed ('cause it's a double)
                Column(
                    modifier = Modifier
//                        .padding(innerPadding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val focusRequester = remember { FocusRequester() }
                    val focusManager = LocalFocusManager.current

                    Row (
                        modifier = Modifier
                            .weight(1.45f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActivityGraph(
                            dayState = dayState,
                            onNavigateToTaskActivityComparison = {
                                mainPanelState = BigScreenMainPanelState.Comparison
                            },
                            selectActivity = selectActivity,
                            selectTask = selectTask
                        )
                    }

                    Row (
                        modifier = Modifier
                            .weight(3.5f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ComparisonDial(
                            dayState = dayState,
                            onNavigateToTaskActivityComparison = {
                                mainPanelState = BigScreenMainPanelState.Comparison
                            },
                            selectActivity = selectActivity,
                            selectTask = selectTask
                        )
                    }
                }
            }

            // If the selected date is today or in the future, show the planning screen
            AnimatedVisibility(
                visible = !selectedDate.isBefore(LocalDate.now())
            ) {
                // Show the day planner
                AnimatedVisibility(
                    visible = mainPanelState == BigScreenMainPanelState.DayTask,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        PlannerDial(
                            dayState = dayState,
                            drawClockHand = userInput.selectedDate.isEqual(LocalDate.now()),
                            lastTaskPriority = lastTaskPriority,
                            taskUiState = taskUiState,
                            userInput = userInput,
                            deleteTask = deleteTask,
                            onMoveToToDoList = onMoveToToDoList,
                            onPressActiveTime = onPressActiveTime,
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
                    }
                }

                // Show actual activities
                AnimatedVisibility(
                    visible = mainPanelState == BigScreenMainPanelState.DayActivity
                ) {
                    Row (
                        modifier = Modifier
                            .weight(1.5f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActivityGraph(
                            dayState = dayState,
                            drawClockHand = true,
                            onNavigateToTaskActivityComparison = {
                                mainPanelState = BigScreenMainPanelState.Comparison
                            },
                            selectActivity = selectActivity,
                            selectTask = selectTask
                        )
                    }

                    Row (
                        modifier = Modifier
                            .weight(3.5f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ComparisonDial(
                            dayState = dayState,
                            drawClockHand = true,
                            onNavigateToTaskActivityComparison = {
                                mainPanelState = BigScreenMainPanelState.Comparison
                            },
                            selectActivity = selectActivity,
                            selectTask = selectTask
                        )
                    }
                }
            }

            // If we are showing the task-activity comparison
            AnimatedVisibility(
                visible = mainPanelState == BigScreenMainPanelState.Comparison
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Task
                    Column (
                        modifier = Modifier
                            .fillMaxHeight(0.5f),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Show task details if there is a task to be shown
                        if (taskDetails.id != null) {
                            Row (
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row (
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Spacer(Modifier.weight(1f))
                                }

                                Row (
                                    modifier = Modifier
                                        .weight(1f)
                                    ,
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .padding(vertical = 15.dp)
                                            .weight(1f)
                                        ,
                                        text = taskLabel,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Row (
                                    modifier = Modifier
                                        .weight(1f)
                                    ,
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {

                                    val dropdownItems = listOf(
                                        DropDownItem(
                                            text = editText,
                                            iconId = R.drawable.edit_24dp_5f6368_fill0_wght400_grad0_opsz24,
                                            onClick = onEditTask
                                        ),
                                        DropDownItem(
                                            text = deleteText,
                                            iconId = R.drawable.delete_24dp_5f6368_fill0_wght400_grad0_opsz24,
                                            onClick = onDeleteTask
                                        )
                                    )
                                    TaskDropdownMenu(
                                        dropdownItems = dropdownItems,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxSize(0.7F)
                                    )
                                }
                            }

                            TaskCard (
                                date = userInput.selectedDate,
                                dayState = dayState,
                                endTime = taskDetails.endTime,
                                startTime = taskDetails.startTime,
                                title = taskDetails.title,
                                pinned = taskDetails.pinned
                            ) {
                                // Description
                                Text(
                                    text = taskDetails.description,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                    ,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        // Otherwise, show a statement that there is no task planned for the select time
                        else {
                            Column (
                                modifier = Modifier
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = noTaskText,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }

                    VerticalDivider(
                        modifier = Modifier
                            .padding(
                                horizontal = 5.dp, vertical = 15.dp
                            )
                            .fillMaxWidth(),
                        thickness = 1.dp,
                    )

                    // Activity
                    Column (
                        modifier = Modifier
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (activityDetails.id != null) {
                            Row (
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row (
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Spacer(Modifier
                                        .weight(1f)
                                    )
                                }

                                Row (
                                    modifier = Modifier
                                        .weight(1f)
                                    ,
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .padding(vertical = 15.dp)
                                            .weight(1f)
                                        ,
                                        text = activityLabel,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Row (
                                    modifier = Modifier
                                        .weight(1f)
                                    ,
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    val dropdownItems = listOf(
                                        DropDownItem(
                                            text = editText,
                                            iconId = R.drawable.edit_24dp_5f6368_fill0_wght400_grad0_opsz24,
                                            onClick = {
                                                onEditActivity(activityDetails.id)
                                            }
                                        ),
                                        DropDownItem(
                                            text = deleteText,
                                            iconId = R.drawable.delete_24dp_5f6368_fill0_wght400_grad0_opsz24,
                                            onClick = {
                                                onDeleteActivity(activityDetails.id)
                                            }
                                        )
                                    )
                                    TaskDropdownMenu(
                                        dropdownItems = dropdownItems,
                                        modifier = Modifier
                                            .fillMaxSize(0.7F)
                                            .weight(1f)
                                    )
                                }
                            }

                            TaskCard (
                                date = userInput.selectedDate,
                                dayState = dayState,
                                endTime = activityDetails.endTime,
                                startTime = activityDetails.startTime,
                                title = activityDetails.title,
                                subActivities = activityDetails.subActivitiesUiState
                            ) {
                                Column (
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                    ,
                                ) {
                                    if (activityDetails.note != "") {
                                        // Notes
                                        Text(
                                            text = activityDetails.note,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                            ,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 18.sp
                                        )
                                    }

                                    if (!activityDetails.voiceNotesUiState.isEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))

                                        VoiceNoteList(
                                            activityUiState = activityDetails,
                                            audioViewModel = audioViewModel,
                                            onDeleteItem = { voiceNote ->
                                                onDeleteVoiceNote(voiceNote.id)
                                            },
                                            updateLastPlayedPosition = updateLastPlayedPosition
                                        )
                                    }

                                    if (!activityDetails.subActivitiesUiState.isEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))

                                        SubActivityList(
                                            mainActivityUiState = activityDetails,
                                            onDeleteItem = { subActivityId ->
                                                onDeleteActivity(subActivityId)
                                            },
                                            onEditItem = { subActivity ->
                                                onEditActivity(subActivity.id)
                                            },
                                            removeVoiceNote = { voiceNote ->
                                                selectVoiceNoteToBeDeleted(voiceNote.id)
                                                deleteVoiceNote()
                                            },
                                            updateLastPlayedPosition = updateLastPlayedPosition
                                        )
                                    }
                                }
                            }
                        }
                        // Otherwise, show a statement that there was no activity carried out during the selected time
                        else {
                            Column (
                                modifier = Modifier
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = noActivityText,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }

            BottomBar(
                content = {
                    if (mainPanelState == BigScreenMainPanelState.DayActivity
                        || mainPanelState == BigScreenMainPanelState.Comparison) {
                        IconButton(
                            onClick = {
                                if (mainPanelState == BigScreenMainPanelState.DayActivity) {
                                    mainPanelState = BigScreenMainPanelState.DayTask
                                }
                                else if (mainPanelState == BigScreenMainPanelState.Comparison) {
                                    mainPanelState = BigScreenMainPanelState.DayActivity
                                }
                            }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                                contentDescription = "Back",
                                modifier = Modifier.fillMaxSize(0.8F),
                                tint = Color(BOTTOM_BAR_TEXT_COLOR)
                            )
                        }
                    }

                    // If the selected date is today or in the future
                    if (!selectedDate.isBefore(LocalDate.now())) {
                        // If the tasks screen is to be shown
                        if (mainPanelState == BigScreenMainPanelState.DayTask) {
                            // Add button
                            IconButton(
                                onClick = {
                                    selectTask(null)
                                    setTaskDate(userInput.selectedDate)

                                    onShowPopupWindow(PopupState.EditTask)
                                }
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.add_square_svgrepo_com),
                                    contentDescription = "Add",
                                    modifier = Modifier.fillMaxSize(0.8F),
                                    tint = Color(BOTTOM_BAR_TEXT_COLOR)
                                )
                            }
                        }

                        // If the selected date is in the future, don't show the activities. The activity recording should be reserved only for today.
                        if (!selectedDate.isAfter(LocalDate.now())) {
                            Spacer(Modifier.weight(1f, true))

                            IconButton(onClick = {
                                if (mainPanelState == BigScreenMainPanelState.DayActivity) {
                                    // Show the activity recorder in a pop-up dialog
                                    val popupState = PopupState.ActivityRecorder
                                    onShowPopupWindow(popupState)
                                }
                                else {
                                    mainPanelState = BigScreenMainPanelState.DayActivity
                                }
                            }) {
                                Icon(
                                    imageVector = if (mainPanelState == BigScreenMainPanelState.DayActivity) ImageVector.vectorResource(id = R.drawable.timer_svgrepo_com) else ImageVector.vectorResource(id = R.drawable.graph_infographic_data_element_2_svgrepo_com),
                                    contentDescription = "Activity recorder",
//                                    modifier = Modifier.fillMaxSize((if (showActivityScreen) 0.8f else 0.7f)),
                                    modifier = Modifier.fillMaxSize(0.75f),
                                    tint = Color(BOTTOM_BAR_TEXT_COLOR)
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}