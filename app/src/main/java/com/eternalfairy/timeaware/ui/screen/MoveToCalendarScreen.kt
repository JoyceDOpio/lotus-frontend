package com.eternalfairy.timeaware.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.timeaware.R
import com.eternalfairy.timeaware.data.Task
import com.eternalfairy.timeaware.data.Time
import com.eternalfairy.timeaware.ui.component.ActiveTimeSetUp
import com.eternalfairy.timeaware.ui.component.Calendar
import com.eternalfairy.timeaware.ui.component.MoveToCalendarDial
import com.eternalfairy.timeaware.ui.component.PopupDialog
import com.eternalfairy.timeaware.ui.viewmodel.DayState
import com.eternalfairy.timeaware.ui.viewmodel.TaskUiState
import com.eternalfairy.timeaware.ui.viewmodel.UserInput
import com.eternalfairy.timeaware.ui.viewmodel.toTask
import com.eternalfairy.timeaware.utils.TouchGestureUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveToCalendarScreen(
    modifier: Modifier = Modifier,
    dayState: DayState,
    taskUiState: TaskUiState,
    userInput: UserInput,
    onBack: () -> Unit,
    onClickSaveActiveTime: () -> Unit,
    onSetSelectedDate: (LocalDate) -> Unit,
    saveTask: (Task) -> Unit,
    setActiveTimeStart: (Time) -> Unit,
    setActiveTimeEnd: (Time) -> Unit,
    setTaskDate: (LocalDate?) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskPriority: (Int?) -> Unit,
    setTaskStartTime: (Time) -> Unit,
) {
    // The task that is being moved to calendar - if I save the taskUiState under the touchedTask it seems it is not updated in time after touching it. The dial tries to draw it before its value is updated.
    var taskToBeMovedToCalendar by remember { mutableStateOf<Task?>(null) }
    // The optimal duration of the task will be 30 minutes and minimum will be 5 minutes
    val optimalDuration = 30
    val minimalDuration = 5
    var movedTaskStartTime: Time? = null
    var movedTaskEndTime: Time? = null
    // Find a slot between tasks to fit in the moved task
    var slotStartTime = if (!userInput.selectedDate.isBefore(LocalDate.now()) && !userInput.selectedDate.isAfter(LocalDate.now())) Time(
        LocalDateTime.now().hour, LocalDateTime.now().minute) else dayState.activeTimeStart
    var slotEndTime: Time
    var tasks: List<Task> = dayState.tasks.toList()

    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
    var showActiveTimeSetUp by remember { mutableStateOf(false) }

    // Text
    val headerText = "Move To Calendar"// TODO: Read string from resource
    val notEnoughTimeSpaceText = "THERE IS NOT ENOUGH TIME WITHIN THE SELECTED DAY TO MOVE THE TASK"// TODO: Read string from resource

    fun findFirstSlot() {
        if (taskUiState.id != null) {
            // Update the task to be moved to calendar with date and initial start- and end time values
            setTaskDate(dayState.date)
            setTaskPriority(null)

            // If there are tasks planned for the day
            if (!dayState.tasks.isEmpty()) {
                for (task in dayState.tasks) {
                    // If the slot start time is after the task's start time, omit that task
                    if (slotStartTime.compareTo(task.startTime!!) == 1) {
                        // If the slot start time is within the task
                        if (slotStartTime.compareTo(task.endTime!!) == -1
                            || slotStartTime.compareTo(task.endTime!!) == 0) {
                            // We're adding 1 minute from the task's end time, so that the tasks don't overlap
                            slotStartTime = TouchGestureUtils.addMinutesToTime(1, task.endTime!!)
                        }
                    }
                    // Else if the slot start time is before the task's start time
                    else {
                        // We're subtracting 1 minute from the task's start time, so that the tasks don't overlap
                        slotEndTime = TouchGestureUtils.addMinutesToTime(-1, task.startTime!!)
                        val availableTime = TouchGestureUtils.calculateTotalNumberOfMinutes(slotStartTime, slotEndTime)

                        if (availableTime >= optimalDuration) {
                            movedTaskStartTime = slotStartTime
                            movedTaskEndTime = TouchGestureUtils.addMinutesToTime(optimalDuration,
                                movedTaskStartTime!!
                            )
                            break
                        }
                        else {
                            // We're adding 1 minute from the task's end time, so that the tasks don't overlap
                            slotStartTime = TouchGestureUtils.addMinutesToTime(1, task.endTime!!)
                        }
                    }
                }
            }
            // Else if there are no tasks
            else {
                movedTaskStartTime = slotStartTime
                movedTaskEndTime = TouchGestureUtils.addMinutesToTime(optimalDuration,
                    movedTaskStartTime
                )
            }

            if (movedTaskStartTime != null && movedTaskEndTime != null) {
                setTaskStartTime(movedTaskStartTime)
                setTaskEndTime(movedTaskEndTime)
                taskToBeMovedToCalendar = taskUiState.toTask().copy(
                    id = taskUiState.id
                )
                tasks = dayState.tasks.toList() + taskToBeMovedToCalendar!!
            }
        }
    }

    findFirstSlot()

    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = headerText,
                    color = Color(TOP_BAR_TEXT_COLOR)
                ) },
                colors = TopAppBarDefaults.topAppBarColors(Color(TOP_BAR_COLOR))
            )
        },
        bottomBar = {
            BottomAppBar (
                containerColor = Color(BOTTOM_BAR_COLOR),
                actions = {
                    // Close button
                    IconButton(onClick = {
                        // Navigate to previous stack entry
                        onBack()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                            contentDescription = "Cancel",
                            modifier = Modifier.fillMaxSize(0.8F),
                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        )
                    }

                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))

                    // Save button
                    IconButton(onClick = {
                        // Update the task's start- and end times
                        for (task in tasks) {
                            if (task.id == taskToBeMovedToCalendar!!.id) {
                                // If the task to be moved to the calendar is pinned, we don't want to actually move this task but to copy it to the calendar so that the original task stays in the TO-DO list for further references (i.e. so that the task can be copied over and over again to the calendar)
                                if (task.pinned) {
                                    saveTask(task.copy(id = UUID.randomUUID(), pinned = false))
                                }
                                else saveTask(task)
                            }
                            else saveTask(task)
                        }
                        onBack()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.save_alt_svgrepo_com),
                            contentDescription = "Save",
                            modifier = Modifier.fillMaxSize(0.6F),
                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column (
            modifier = modifier
                .padding(
                    horizontal = 15.dp
                )
                .padding(innerPadding)
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ){
                Text(
                    text = "${userInput.selectedDate.format(formatter)}",
                    modifier = Modifier.padding(bottom = 10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    thickness = 1.dp,
                )
            }

            // Handle exception in case taskToBeMoved is null
            taskToBeMovedToCalendar?.also {
                if (it.startTime != null && it.endTime != null) {
                    MoveToCalendarDial(
                        dayState = dayState,
                        userInput = userInput,
                        onPressActiveTime = { showActiveTimeSetUp = true },
                        minimalDuration = minimalDuration,
                        tasks = tasks,
                        taskToBeMovedToCalendar = it
                    )
                }
            }
            ?:run {
                Spacer(modifier = Modifier.weight(1f))

                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = notEnoughTimeSpaceText,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Calendar(
                userInput = userInput,
                onSetDate = onSetSelectedDate
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