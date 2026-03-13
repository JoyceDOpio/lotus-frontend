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
import com.eternalfairy.timeaware.ui.component.Direction
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
import com.eternalfairy.timeaware.ui.component.calendar.CalendarWeek
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
fun SmallScreenLandscape (
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
    var screenState by remember { mutableStateOf(SmallScreenState.DayTask) }
    var button1State by remember { mutableStateOf(SmallScreenButtonState.Goal) }
    var button2State by remember { mutableStateOf(SmallScreenButtonState.ToDo) }

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

    Row (
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOR)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // If we are not showing the task-activity comparison
        AnimatedVisibility(
            visible = screenState != SmallScreenState.Comparison
        ) {
            Row (
                modifier = Modifier
                    .fillMaxSize()
                ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Column (
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(380.dp)
                    ,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    TopBar(
                        componentHeight = 60.dp,
                        paddingTop = 5.dp,
                        title = when (screenState) {
                            SmallScreenState.Goal -> goalsTitle
                            SmallScreenState.ToDo -> toDoTitle
                            else -> "${selectedDate.format(formatter)}"
                        }
                    )

                    AnimatedVisibility(
                        visible = screenState == SmallScreenState.MoveToCalendar
                    ) {
                        TopBar(
                            paddingTop = 5.dp,
                            title = moveToCalendarHeaderText
                        )
                    }

                    // If the selected date is in the future, show a spacer
                    AnimatedVisibility(
                        visible =
                            selectedDate.isAfter(LocalDate.now())
                                    && screenState != SmallScreenState.DayActivity
                                    && screenState != SmallScreenState.MoveToCalendar
                    ) {
                        Spacer(modifier = Modifier.height(100.dp))
                    }

                    // Show the active time header on the day overview of tasks and only today
                    AnimatedVisibility(
                        visible = screenState == SmallScreenState.DayTask && selectedDate.isEqual(
                            LocalDate.now())
                    ) {
                        ActiveTimeHeader(
                            dayState = dayState,
                            userInput = userInput
                        )
                    }

                    AnimatedVisibility(
                        visible = selectedDate.isBefore(LocalDate.now())
                                || screenState == SmallScreenState.DayActivity
                    ) {
                        Row (
                            modifier = Modifier
                                .weight(1.5f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ActivityGraph(
                                dayState = dayState,
                                drawClockHand = userInput.selectedDate.isEqual(LocalDate.now()),
                                onNavigateToTaskActivityComparison = {
                                    screenState = SmallScreenState.Comparison
                                },
                                selectActivity = selectActivity,
                                selectTask = selectTask
                            )
                        }
                    }

                    // Show the calendar in the day overview of tasks, with goals and with the to-do list
                    AnimatedVisibility(
                        visible = screenState == SmallScreenState.DayTask
                                || screenState == SmallScreenState.MoveToCalendar
                    ) {
                        CalendarWeek(
                            userInput = userInput,
                            onSetDate = onSetSelectedDate
                        )
                    }

                    // Main bottom bar
                    AnimatedVisibility(
                        visible = !selectedDate.isBefore(LocalDate.now())
                                && (screenState == SmallScreenState.DayTask
                                || screenState == SmallScreenState.Goal
                                || screenState == SmallScreenState.ToDo)
                    ) {
                        BottomBar(
                            paddingBottom = 5.dp,
                            content = {
                                // Add button
                                IconButton(
                                    onClick = {
                                        var popupState = PopupState.EditTask

                                        if (screenState == SmallScreenState.DayTask) {
                                            selectTask(null)
                                            setTaskDate(userInput.selectedDate)
                                        }
                                        else if (screenState == SmallScreenState.Goal) {
                                            selectGoal(null)
                                            popupState = PopupState.EditGoal
                                        }
                                        else if (screenState == SmallScreenState.ToDo) {
                                            selectTask(null)
                                            setTaskDate(null)
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

                                // Button 1
                                IconButton(
                                    onClick = {
                                        when (button1State) {
                                            SmallScreenButtonState.Goal -> {
                                                button1State = if (button2State == SmallScreenButtonState.ToDo) SmallScreenButtonState.Day else SmallScreenButtonState.ToDo
                                                screenState = SmallScreenState.Goal
                                            }
                                            SmallScreenButtonState.Day -> {
                                                button1State = if (button2State == SmallScreenButtonState.ToDo) SmallScreenButtonState.Goal else SmallScreenButtonState.ToDo
                                                screenState = SmallScreenState.DayTask
                                            }
                                            SmallScreenButtonState.ToDo -> {
                                                button1State = if (button2State == SmallScreenButtonState.Day) SmallScreenButtonState.Goal else SmallScreenButtonState.Day
                                                screenState = SmallScreenState.ToDo
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = when (button1State) {
                                            SmallScreenButtonState.Goal -> {
                                                ImageVector.vectorResource(id = R.drawable.target_love_svgrepo_com)
                                            }
                                            SmallScreenButtonState.Day -> {
                                                ImageVector.vectorResource(id = R.drawable.pie_chart_svgrepo_com)
                                            }
                                            SmallScreenButtonState.ToDo -> {
                                                ImageVector.vectorResource(id = R.drawable.list_svgrepo_com)
                                            }
                                        },
                                        contentDescription = when (button1State) {
                                            SmallScreenButtonState.Goal -> {
                                                goalsTitle
                                            }
                                            SmallScreenButtonState.Day -> {
                                                dayTitle
                                            }
                                            SmallScreenButtonState.ToDo -> {
                                                toDoTitle
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize(if (button1State == SmallScreenButtonState.Day) 0.75f else 0.8f),
                                        tint = HEADER_TEXT_COLOR
                                    )
                                }

                                Spacer(Modifier.weight(1f, true))

                                // Button 2
                                IconButton(
                                    onClick = {
                                        when (button2State) {
                                            SmallScreenButtonState.Goal -> {
                                                button2State = if (button1State == SmallScreenButtonState.ToDo) SmallScreenButtonState.Day else SmallScreenButtonState.ToDo
                                                screenState = SmallScreenState.Goal
                                            }
                                            SmallScreenButtonState.Day -> {
                                                button2State = if (button1State == SmallScreenButtonState.ToDo) SmallScreenButtonState.Goal else SmallScreenButtonState.ToDo
                                                screenState = SmallScreenState.DayTask
                                            }
                                            SmallScreenButtonState.ToDo -> {
                                                button2State = if (button1State == SmallScreenButtonState.Day) SmallScreenButtonState.Goal else SmallScreenButtonState.Day
                                                screenState = SmallScreenState.ToDo
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = when (button2State) {
                                            SmallScreenButtonState.Goal -> {
                                                ImageVector.vectorResource(id = R.drawable.target_love_svgrepo_com)
                                            }
                                            SmallScreenButtonState.Day -> {
                                                ImageVector.vectorResource(id = R.drawable.pie_chart_svgrepo_com)
                                            }
                                            SmallScreenButtonState.ToDo -> {
                                                ImageVector.vectorResource(id = R.drawable.list_svgrepo_com)
                                            }
                                        },
                                        contentDescription = when (button2State) {
                                            SmallScreenButtonState.Goal -> {
                                                goalsTitle
                                            }
                                            SmallScreenButtonState.Day -> {
                                                dayTitle
                                            }
                                            SmallScreenButtonState.ToDo -> {
                                                toDoTitle
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize(if (button2State == SmallScreenButtonState.Day) 0.75f else 0.8f),
                                        tint = HEADER_TEXT_COLOR
                                    )
                                }

                                // Show the activity button only on today
                                if (selectedDate.isEqual(LocalDate.now())) {
                                    Spacer(Modifier.weight(1f, true))

                                    IconButton(onClick = {
                                        // Show the activity screen
                                        screenState = SmallScreenState.DayActivity
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
                        visible = screenState == SmallScreenState.DayActivity
                    ) {
                        BottomBar(
                            paddingBottom = 5.dp,
                            content = {
                                IconButton(
                                    onClick = {
                                        screenState = SmallScreenState.DayTask
                                    }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                                        contentDescription = "Back",// TODO: Read from resource
                                        modifier = Modifier.fillMaxSize(0.8F),
                                        tint = HEADER_TEXT_COLOR
                                    )
                                }

                                Spacer(Modifier.weight(1f, true))


                                IconButton(onClick = {
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
                        visible = screenState == SmallScreenState.MoveToCalendar
                    ) {
                        BottomBar(
                            paddingBottom = 5.dp,
                            content = {
                                IconButton(
                                    onClick = {
                                        screenState = SmallScreenState.DayTask
                                    }
                                ) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                                        contentDescription = "Back",// TODO: Read from resource
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
                                        screenState = SmallScreenState.DayTask
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
                }

                Column (
                    modifier = Modifier
                        .fillMaxHeight()
                ) {
                    // If the selected date is in the past, show a comparison of the planned tasks and actual activities or show the activities
                    AnimatedVisibility(
                        visible = selectedDate.isBefore(LocalDate.now())
                                || screenState == SmallScreenState.DayActivity
                    ) {
                        ComparisonDial(
                            dayState = dayState,
                            drawClockHand = screenState == SmallScreenState.DayActivity,
                            onNavigateToTaskActivityComparison = {
                                screenState = SmallScreenState.Comparison
                            },
                            selectActivity = selectActivity,
                            selectTask = selectTask
                        )
                    }

                    // If the selected date is today or in the future, show the planning screen
                    AnimatedVisibility(
                        visible = !selectedDate.isBefore(LocalDate.now())
                    ) {
                        // Show tasks
                        AnimatedVisibility(
                            visible = screenState == SmallScreenState.DayTask
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

                        // Show the TO-DO list (list of tasks without specific date and time)
                        AnimatedVisibility(
                            visible = screenState == SmallScreenState.ToDo
                        ) {
                            DragItemListTask (
                                dayState = dayState,
                                items = toDoTasks,
                                lastTaskPriority = lastTaskPriority,
                                taskUiState = taskUiState,
                                deleteTask = deleteTask,
                                onMoveToCalendar = {
                                    screenState = SmallScreenState.MoveToCalendar

                                    if (button1State == SmallScreenButtonState.Day) {
                                        button1State = SmallScreenButtonState.ToDo
                                    }
                                    else if (button2State == SmallScreenButtonState.Day) {
                                        button2State = SmallScreenButtonState.ToDo
                                    }
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
                            visible = screenState == SmallScreenState.Goal
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

                    AnimatedVisibility(
                        visible = screenState == SmallScreenState.MoveToCalendar
                    ) {
                        findFirstSlot()

                        // Handle exception in case taskToBeMoved is null
                        taskToBeMovedToCalendar?.also {
                            if (it.startTime != null && it.endTime != null) {
                                MoveToCalendarDial(
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
                                        .height(450.dp)
                                        .width(400.dp)
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
            }
        }

        // If we are showing the task-activity comparison
        AnimatedVisibility(
            visible = screenState == SmallScreenState.Comparison
        ) {
            Row (
                modifier = Modifier
                    .fillMaxSize()
                ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Task-activity comparison bottom bar
                BottomBar(
                    direction = Direction.Vertical,
                    content = {
                        IconButton(
                            onClick = {
                                if (selectedDate.isBefore(LocalDate.now())) {
                                    screenState = SmallScreenState.DayTask
                                }
                                else if (selectedDate.isEqual(LocalDate.now())) {
                                    screenState = SmallScreenState.DayActivity
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

                // Task
                Column (
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
                        .width(380.dp)
                        .height(330.dp)
                        .background(COMPONENT_BACKGROUND_COLOR)
                    ,
                    verticalArrangement = Arrangement.Center,
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
                        .width(380.dp)
                        .height(330.dp)
                        .background(COMPONENT_BACKGROUND_COLOR)
                    ,
                    verticalArrangement = Arrangement.Center,
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
}