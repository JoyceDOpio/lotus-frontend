package com.eternalfairy.timeaware.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.eternalfairy.timeaware.ui.component.ComparisonDial
import com.eternalfairy.timeaware.ui.component.DragItemListGoal
import com.eternalfairy.timeaware.ui.component.DragItemListTask
import com.eternalfairy.timeaware.ui.component.DropDownItem
import com.eternalfairy.timeaware.ui.component.MoveToCalendarDial
import com.eternalfairy.timeaware.ui.component.PlannerDial
import com.eternalfairy.timeaware.ui.component.SubActivityList
import com.eternalfairy.timeaware.ui.component.TaskCard
import com.eternalfairy.timeaware.ui.component.TaskDropdownMenu
import com.eternalfairy.timeaware.ui.component.TopBar
import com.eternalfairy.timeaware.ui.component.VoiceNoteList
import com.eternalfairy.timeaware.ui.component.calendar.CalendarMonth
import com.eternalfairy.timeaware.ui.theme.BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.COMMENT_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.viewmodel.ActivityUiState
import com.eternalfairy.timeaware.ui.viewmodel.AudioViewModel
import com.eternalfairy.timeaware.ui.viewmodel.DayState
import com.eternalfairy.timeaware.ui.viewmodel.GoalUiState
import com.eternalfairy.timeaware.ui.viewmodel.TaskUiState
import com.eternalfairy.timeaware.ui.viewmodel.UserInput
import com.eternalfairy.timeaware.ui.viewmodel.toTask
import com.eternalfairy.timeaware.utils.TouchGestureUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun BigScreenPortrait (
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
    setTaskPriority: (Int?) -> Unit,
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
    val goalsTitle = "Goals"//TODO: Read from string resource
    val toDoTitle = "To Do"//TODO: Read from string resource

    // Comparison of task and activity
    val taskDetails = taskUiState
    val activityDetails = activityUiState

    // Move to calendar
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
    val moveToCalendarHeaderText = "Move To Calendar"// TODO: Read string from resource
    val notEnoughTimeSpaceText = "THERE IS NOT ENOUGH TIME WITHIN THE SELECTED DAY TO MOVE THE TASK"// TODO: Read string from resource

    val mainPanelWidth = 750.dp
    val mainPanelHeight = 650.dp
    val sidePanelWidth = 370.dp
    val sidePanelHeight = 700.dp

    // TODO: DUPLICATE - Move to utils
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

    // I know that the width is 600-839 dp, and that the height is 480-899 dp
    // Let's make component width of 550 dp
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOR)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Main panel
        Column (
            modifier = Modifier
                .padding(
                    horizontal = 10.dp
                )
                .height(mainPanelHeight)
                .width(mainPanelWidth)
        ) {
            TopBar(
                componentWidth = mainPanelWidth,
                title = selectedDate.format(formatter)
            )

            // If we are not showing the task-activity comparison
            AnimatedVisibility(
                visible = mainPanelState != BigScreenMainPanelState.Comparison
            ) {
                // If the selected date is in the past, show a comparison of the planned tasks and actual activities
                AnimatedVisibility(
                    visible = selectedDate.isBefore(LocalDate.now())
                            || mainPanelState == BigScreenMainPanelState.DayActivity
                ) {
                    Column(
                        modifier = Modifier
                            .width(mainPanelWidth)
                            .height(mainPanelHeight)
                        ,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        ActivityGraph(
                            componentWidth = mainPanelWidth,
                            dayState = dayState,
                            drawClockHand = mainPanelState == BigScreenMainPanelState.DayActivity,
                            onNavigateToTaskActivityComparison = {
                                mainPanelState = BigScreenMainPanelState.Comparison
                            },
                            selectActivity = selectActivity,
                            selectTask = selectTask
                        )

                        ComparisonDial(
                            componentWidth = mainPanelWidth,
                            componentHeight = 600.dp,
                            dayState = dayState,
                            drawClockHand = mainPanelState == BigScreenMainPanelState.DayActivity,
                            onNavigateToTaskActivityComparison = {
                                mainPanelState = BigScreenMainPanelState.Comparison
                            },
                            selectActivity = selectActivity,
                            selectTask = selectTask
                        )
                    }
                }


                // Show the day planner
                AnimatedVisibility(
                    // If the selected date is today or in the future, show the planning screen
                    visible = mainPanelState == BigScreenMainPanelState.DayTask
                            && !selectedDate.isBefore(LocalDate.now())
                ) {
                    Column(
                        modifier = Modifier
                            .width(mainPanelWidth)
                            .height(mainPanelHeight)
                        ,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        PlannerDial(
                            componentWidth = mainPanelWidth,
                            componentHeight = 600.dp,
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

                AnimatedVisibility(
                    visible = mainPanelState == BigScreenMainPanelState.MoveToCalendar
                ) {
                    findFirstSlot()

                    TopBar(
                        componentWidth = mainPanelWidth,
                        title = moveToCalendarHeaderText
                    )

                    // Handle exception in case taskToBeMoved is null
                    taskToBeMovedToCalendar?.also {
                        if (it.startTime != null && it.endTime != null) {
                            MoveToCalendarDial(
                                componentWidth = mainPanelWidth,
                                componentHeight = 580.dp,
                                dayState = dayState,
                                userInput = userInput,
                                onPressActiveTime = onPressActiveTime,
                                minimalDuration = minimalDuration,
                                tasks = tasks,
                                taskToBeMovedToCalendar = it
                            )
                        }
                    }
                        ?:run {
                            Spacer(modifier = Modifier.weight(1f))

                            Column (
                                modifier = Modifier
                                    .height(580.dp)
                                    .width(mainPanelWidth)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
                                    .background(COMPONENT_BACKGROUND_COLOR)
                                ,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = notEnoughTimeSpaceText,
                                    color = COMMENT_TEXT_COLOR,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))
                        }
                }
            }

            // If we are showing the task-activity comparison
            AnimatedVisibility(
                visible = mainPanelState == BigScreenMainPanelState.Comparison
            ) {
                Row (
                    modifier = Modifier
                        .width(mainPanelWidth)
                        .height(600.dp)
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Task
                    Column (
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
                            .width(350.dp)
                            .height(600.dp)
                            .background(COMPONENT_BACKGROUND_COLOR)
                        ,
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
                                        color = HEADER_TEXT_COLOR,
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
                                    color = COMMENT_TEXT_COLOR,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Activity
                    Column (
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
                            .width(350.dp)
                            .height(600.dp)
                            .background(COMPONENT_BACKGROUND_COLOR)
                        ,
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
                                        color = HEADER_TEXT_COLOR,
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
                                    color = COMMENT_TEXT_COLOR,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // The sub-panels
        Row (
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    top = 5.dp,
                    end = 10.dp,
                    bottom = 50.dp
                )
                .width(mainPanelWidth)
                .height(sidePanelHeight)
            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left sub-panel
            Column (
                modifier = Modifier
                    .width(sidePanelWidth)
                    .height(sidePanelHeight)
                ,
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = userInput.selectedDate.isEqual(LocalDate.now())
                ) {
                    ActiveTimeHeader(
                        componentWidth = sidePanelWidth,
                        dayState = dayState,
                        userInput = userInput
                    )
                }

                // A spacer to push the calendar to the bottom when only the calendar is displayed
                AnimatedVisibility(
                    visible = selectedDate.isBefore(LocalDate.now())
                            || selectedDate.isAfter(LocalDate.now())
                ) {
                    Spacer(Modifier.weight(1f, true))
                }

                CalendarMonth (
                    componentWidth = sidePanelWidth,
                    paddingBottom = if (selectedDate.isBefore(LocalDate.now())) 20.dp else 5.dp,
                    selectedDate = userInput.selectedDate,
                    onSetDate = onSetSelectedDate
                )

                // Task bottom bar
                AnimatedVisibility(
                    visible = mainPanelState == BigScreenMainPanelState.DayTask
                            && !selectedDate.isBefore(LocalDate.now())
                ) {
                    BottomBar(
                        componentWidth = sidePanelWidth,
                        content = {
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
                                    tint = HEADER_TEXT_COLOR
                                )
                            }

                            Spacer(Modifier.weight(1f, true))

                            // If the selected date is in the future, don't show the activities. The activity recording should be reserved only for today.
                            if (selectedDate.isEqual(LocalDate.now())) {
                                IconButton(onClick = {
                                    mainPanelState = BigScreenMainPanelState.DayActivity
                                }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.graph_infographic_data_element_2_svgrepo_com),
                                        contentDescription = "Activity recorder",
                                        modifier = Modifier.fillMaxSize(0.75f),
                                        tint = HEADER_TEXT_COLOR
                                    )
                                }
                            }
                        }
                    )
                }

                // Activity bottom bar
                AnimatedVisibility(
                    visible = mainPanelState == BigScreenMainPanelState.DayActivity
                ) {
                    BottomBar(
                        componentWidth = sidePanelWidth,
                        content = {
                            IconButton(
                                onClick = {
                                    mainPanelState = BigScreenMainPanelState.DayTask
                                }
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                                    contentDescription = "Back",
                                    modifier = Modifier.fillMaxSize(0.8F),
                                    tint = HEADER_TEXT_COLOR
                                )
                            }

                            Spacer(Modifier.weight(1f, true))

                            IconButton(onClick = {
                                // Show the activity recorder in a pop-up dialog
                                val popupState = PopupState.ActivityRecorder
                                onShowPopupWindow(popupState)
                            }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.timer_svgrepo_com),
                                    contentDescription = "Activity recorder",
                                    modifier = Modifier.fillMaxSize(0.75f),
                                    tint = HEADER_TEXT_COLOR
                                )
                            }
                        }
                    )
                }

                // Move-to-calendar bottom bar
                AnimatedVisibility(
                    visible = mainPanelState == BigScreenMainPanelState.MoveToCalendar
                ) {
                    BottomBar(
                        componentWidth = sidePanelWidth,
                        content = {
                            IconButton(
                                onClick = {
                                    mainPanelState = BigScreenMainPanelState.DayTask
                                }
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                                    contentDescription = "Back",
                                    modifier = Modifier.fillMaxSize(0.8F),
                                    tint = HEADER_TEXT_COLOR
                                )
                            }

                            Spacer(Modifier.weight(1f, true))

                            if (taskToBeMovedToCalendar != null) {// FIXME: Optimize null handling
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
                                    mainPanelState = BigScreenMainPanelState.DayTask
                                }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.save_alt_svgrepo_com),
                                        contentDescription = "Save",
                                        modifier = Modifier.fillMaxSize(0.6F),
                                        tint = HEADER_TEXT_COLOR
                                    )
                                }
                            }
                        }
                    )
                }

                // Comparison bottom bar
                AnimatedVisibility(
                    visible = mainPanelState == BigScreenMainPanelState.Comparison
                ) {
                    BottomBar(
                        componentWidth = sidePanelWidth,
                        content = {
                            IconButton(
                                onClick = {
                                    if (selectedDate.isBefore(LocalDate.now())) {
                                        mainPanelState = BigScreenMainPanelState.DayTask
                                    }
                                    else if (selectedDate.isEqual(LocalDate.now())) {
                                        mainPanelState = BigScreenMainPanelState.DayActivity
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                                    contentDescription = "Back",
                                    modifier = Modifier.fillMaxSize(0.8F),
                                    tint = HEADER_TEXT_COLOR
                                )
                            }

                            Spacer(Modifier.weight(1f, true))
                        }
                    )
                }
            }

            // Side panel (right sub-panel)
            Column (
                modifier = Modifier
                    .height(sidePanelHeight)
                    .width(sidePanelWidth)
                ,
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopBar(
                    componentWidth = sidePanelWidth,
                    componentHeight = 60.dp,
                    paddingTop = 5.dp,
                    title = when (sidePanelState) {
                        BigScreenSidePanelState.Goal -> goalsTitle
                        BigScreenSidePanelState.ToDo -> toDoTitle
                    }
                )

                // Show the TO-DO list (list of tasks without specific date and time)
                AnimatedVisibility(
                    visible = sidePanelState == BigScreenSidePanelState.ToDo
                ) {
                    DragItemListTask (
                        componentWidth = sidePanelWidth,
                        componentHeight = 350.dp,
                        dayState = dayState,
                        items = toDoTasks,
                        lastTaskPriority = lastTaskPriority,
                        taskUiState = taskUiState,
                        deleteTask = deleteTask,
                        onMoveToCalendar = {
                            mainPanelState = BigScreenMainPanelState.MoveToCalendar
                        },
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
                    visible = sidePanelState == BigScreenSidePanelState.Goal
                ) {
                    DragItemListGoal(
                        componentWidth = sidePanelWidth,
                        componentHeight = 350.dp,
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

                BottomBar(
                    componentWidth = sidePanelWidth,
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
                                tint = HEADER_TEXT_COLOR
                            )
                        }

                        // The Spacer pushes the other icons to the end of the app bar
                        Spacer(Modifier.weight(1f, true))

                        // Goals/To-Do button
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
                                tint = HEADER_TEXT_COLOR
                            )
                        }
                    }
                )
            }
        }
    }
}