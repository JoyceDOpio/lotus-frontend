package com.eternalfairy.lotus.view.screen.planner

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.lotus.R
import com.eternalfairy.lotus.model.data.Time
import com.eternalfairy.lotus.view.component.ActiveTimeHeader
import com.eternalfairy.lotus.view.component.ActivityGraph
import com.eternalfairy.lotus.view.component.BannerAd
import com.eternalfairy.lotus.view.component.BottomBar
import com.eternalfairy.lotus.view.component.ComparisonDial
import com.eternalfairy.lotus.view.component.DragItemListGoal
import com.eternalfairy.lotus.view.component.DragItemListTask
import com.eternalfairy.lotus.view.component.DropDownItem
import com.eternalfairy.lotus.view.component.MoveToCalendarDial
import com.eternalfairy.lotus.view.component.PlannerDial
import com.eternalfairy.lotus.view.component.SubActivityList
import com.eternalfairy.lotus.view.component.TaskCard
import com.eternalfairy.lotus.view.component.TaskDropdownMenu
import com.eternalfairy.lotus.view.component.TopBar
import com.eternalfairy.lotus.view.component.VoiceNoteList
import com.eternalfairy.lotus.view.component.calendar.CalendarWeek
import com.eternalfairy.lotus.view.data.ActivityUiState
import com.eternalfairy.lotus.view.data.DayUiState
import com.eternalfairy.lotus.view.data.GoalUiState
import com.eternalfairy.lotus.view.data.TaskUiState
import com.eternalfairy.lotus.view.data.UserInput
import com.eternalfairy.lotus.view.theme.BACKGROUND_COLOR
import com.eternalfairy.lotus.view.theme.COMMENT_TEXT_COLOR
import com.eternalfairy.lotus.view.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.lotus.view.theme.HEADER_TEXT_COLOR
import com.eternalfairy.lotus.view.utils.TouchGestureUtils
import com.eternalfairy.lotus.viewmodel.AudioViewModel
import com.eternalfairy.lotus.viewmodel.PlannerViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmallScreenPortrait (
//    activityUiState: ActivityUiState,
//    adView: AdView,
    audioViewModel: AudioViewModel,
    plannerViewModel: PlannerViewModel,
//    context: Context,
//    dayUiState: DayUiState,
//    goals: List<GoalUiState>,
//    goalUiState: GoalUiState,
//    lastGoalPriority: Int?,
//    lastTaskPriority: Int?,
//    taskUiState: TaskUiState,
//    toDoTasks: List<TaskUiState>,
//    userInput: UserInput,
//    deleteGoal: (UUID) -> Unit,
//    deleteTask: () -> Unit,
//    deleteVoiceNote: () -> Unit,
//    onDeleteActivity: (UUID) -> Unit,
//    onEditActivity: (UUID?) -> Unit,
//    onDeleteTask: () -> Unit,
//    onEditTask: () -> Unit,
//    onDeleteVoiceNote: (UUID?) -> Unit,
    onLogout: () -> Unit,
    onMoveToToDoList: () -> Unit,
    onNavigateToLogin: () -> Unit,
//    onPinTask: (Boolean) -> Unit,
//    onPressActiveTime: () -> Unit,
//    onSetSelectedDate: (LocalDate) -> Unit,
//    onShowPopupWindow: (PopupState) -> Unit,
//    saveGoal: (GoalUiState) -> Unit,
//    saveGoalFromState: () -> Unit,
//    saveTask: (TaskUiState) -> Unit,
//    saveTaskFromState: () -> Unit,
//    selectActivity: (UUID?) -> Unit,
//    selectGoal: (UUID?) -> Unit,
//    selectTask: (UUID?) -> Unit,
//    selectVoiceNoteToBeDeleted: (UUID?) -> Unit,
//    setGoalPriority: (Int) -> Unit,
//    setGoalTitle: (String) -> Unit,
//    setTaskDate: (LocalDate?) -> Unit,
//    setTaskDescription: (String) -> Unit,
//    setTaskEndTime: (Time) -> Unit,
//    setTaskPriority: (Int?) -> Unit,
//    setTaskStartTime: (Time) -> Unit,
//    setTaskTitle: (String) -> Unit,
//    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy")
    val selectedDate = userInput.selectedDate

    // Bottom sheet
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
    var taskToBeMovedToCalendar by remember { mutableStateOf<TaskUiState?>(null) }
    // The optimal duration of the task will be 30 minutes and minimum will be 5 minutes
    val optimalDuration = 30
    val minimalDuration = 5
    var movedTaskStartTime: Time? = null
    var movedTaskEndTime: Time? = null
    // Find a slot between tasks to fit in the moved task
    var slotStartTime = if (!userInput.selectedDate.isBefore(LocalDate.now()) && !userInput.selectedDate.isAfter(LocalDate.now())) Time(
        LocalDateTime.now().hour, LocalDateTime.now().minute) else dayUiState.activeTimeStart
    var slotEndTime: Time
    var tasks: List<TaskUiState> = dayUiState.tasks.toList()
    val moveToCalendarHeaderText = "Move To Calendar"// TODO: Read string from resource
    val notEnoughTimeSpaceText = "THERE IS NOT ENOUGH TIME WITHIN THE SELECTED DAY TO MOVE THE TASK"// TODO: Read string from resource

    // Banner ad
//    val adView = remember { AdView(context) }
    val adView = AdView(context)

    adView.adUnitId= "ca-app-pub-3940256099942544/9214589741"

    // Set a large anchored adaptive banner ad size with a given width.
    val deviceWidth = LocalConfiguration.current.screenWidthDp
    val adWidth = 380
    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(LocalContext.current, adWidth)
    adView.setAdSize(adSize)

    adView.adListener =
        object : AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i("SmallScreenPortrait", "onAdLoaded()")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }

    val adRequest = AdRequest.Builder().build()
    adView.loadAd(adRequest)

    // TODO: DUPLICATE - Move to utils
    fun findFirstSlot() {
        if (taskUiState.id != null) {
            // Update the task to be moved to calendar with date and initial start- and end time values
            setTaskDate(dayUiState.date)
            setTaskPriority(null)

            // If there are tasks planned for the day
            if (!dayUiState.tasks.isEmpty()) {
                for (task in dayUiState.tasks) {
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
                taskToBeMovedToCalendar = taskUiState.copy(
                    id = taskUiState.id
                )
                tasks = dayUiState.tasks.toList() + taskToBeMovedToCalendar!!
            }
        }
    }

    fun saveTaskToBeMoved() {
        taskToBeMovedToCalendar?.let {
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
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOR)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TopBar(
            title = when (screenState) {
                SmallScreenState.Goal -> goalsTitle
                SmallScreenState.ToDo -> toDoTitle
                else -> "${selectedDate.format(formatter)}"
            }
        )

        // If we are not showing the task-activity comparison
        AnimatedVisibility(
            visible = screenState != SmallScreenState.Comparison
        ) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedVisibility(
                    visible = screenState == SmallScreenState.MoveToCalendar
                ) {
                    TopBar(
//                        componentHeight = 60.dp,
                        title = moveToCalendarHeaderText
                    )
                }

                // Show the active time header in the day overview of tasks and only today
                AnimatedVisibility(
                    visible = screenState == SmallScreenState.DayTask && selectedDate.isEqual(LocalDate.now())
                ) {
                    ActiveTimeHeader(
                        dayUiState = dayUiState,
                        userInput = userInput
                    )
                }

                // If the selected date is in the future, show the ad banner instead of the Active Time Header
                AnimatedVisibility(
                    visible = screenState == SmallScreenState.DayTask && selectedDate.isAfter(LocalDate.now())
                ) {
                    Column(
                        modifier = Modifier
                            .padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            )
                            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
                            .width(400.dp)
                            .height(80.dp)
                            .background(COMPONENT_BACKGROUND_COLOR)
                    ) {
                        BannerAd(
                            adView,
                            Modifier
                                .width(380.dp)
                                .height(70.dp)
                        )
                    }
                }

                // If the selected date is in the past, show a comparison of the planned tasks and actual activities or if we are to show the activities
                AnimatedVisibility(
                    visible = selectedDate.isBefore(LocalDate.now())
                            || screenState == SmallScreenState.DayActivity
                ) {
                    // INFO: If I don't add this Column here, the ActivityGraph and Comparison components overlap
                    Column (
                        modifier = Modifier
                        ,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        ActivityGraph(
                            dayUiState = dayUiState,
                            drawClockHand = screenState == SmallScreenState.DayActivity,
                            onNavigateToTaskActivityComparison = {
                                screenState = SmallScreenState.Comparison
                            },
                            selectActivity = selectActivity,
                            selectTask = selectTask
                        )

                        ComparisonDial(
                            dayUiState = dayUiState,
                            drawClockHand = screenState == SmallScreenState.DayActivity,
                            onNavigateToTaskActivityComparison = {
                                screenState = SmallScreenState.Comparison
                            },
                            selectActivity = selectActivity,
                            selectTask = selectTask
                        )
                    }
                }

                // If the selected date is today or in the future, show the planning screen
                AnimatedVisibility(
                    visible = !selectedDate.isBefore(LocalDate.now())
                ) {
                    // Show the day planner
                    AnimatedVisibility(
                        visible = screenState == SmallScreenState.DayTask
                    ) {
                        PlannerDial(
                            dayUiState = dayUiState,
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
                            dayUiState = dayUiState,
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
                                dayUiState = dayUiState,
                                userInput = userInput,
                                onCancel = {
                                    screenState = SmallScreenState.DayTask
                                },
                                onPressActiveTime = onPressActiveTime,
                                onSave = { saveTaskToBeMoved() },
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

        // Show the comparison of a given task and an activity
        AnimatedVisibility(
            visible = screenState == SmallScreenState.Comparison
        ) {
            Column (
                modifier = Modifier
                ,
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Task
                Column (
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
                        .width(400.dp)
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
                            dayUiState = dayUiState,
                            endTime = taskDetails.endTime,
                            startTime = taskDetails.startTime,
                            title = taskDetails.title,
                            pinned = taskDetails.pinned
                        ) {
                            // Description
                            Text(
                                text = taskDetails.description ?: "",
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
                        .width(400.dp)
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
                            dayUiState = dayUiState,
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

        // Show calendar in the day overview of tasks or when we're moving a task to the calendar
        AnimatedVisibility(
            visible = screenState == SmallScreenState.DayTask
                    || screenState == SmallScreenState.MoveToCalendar
        ) {
            CalendarWeek(
                paddingBottom = if (selectedDate.isBefore(LocalDate.now())) 20.dp else 5.dp,
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
//                            // Update the task's start- and end times
//                            for (task in tasks) {
//                                if (task.id == taskToBeMovedToCalendar!!.id) {
//                                    // If the task to be moved to the calendar is pinned, we don't want to actually move this task but to copy it to the calendar so that the original task stays in the TO-DO list for further references (i.e. so that the task can be copied over and over again to the calendar)
//                                    if (task.pinned) {
//                                        saveTask(task.copy(id = UUID.randomUUID(), pinned = false))
//                                    }
//                                    else saveTask(task)
//                                }
//                                else saveTask(task)
//                            }
//                            screenState = SmallScreenState.DayTask
                            saveTaskToBeMoved()
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

        // Task-activity comparison bottom bar
        AnimatedVisibility(
            visible = screenState == SmallScreenState.Comparison
        ) {
            BottomBar(
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
                            contentDescription = "Back",// TODO: Read from resource
                            modifier = Modifier.fillMaxSize(0.8F),
                            tint = HEADER_TEXT_COLOR
                        )
                    }

                    Spacer(Modifier.weight(1f, true))
                }
            )
        }

//        // Bottom sheet
//        if (showBottomSheet) {
//            ModalBottomSheet(
//                onDismissRequest = {
//                    showBottomSheet = false
//                },
//                sheetState = sheetState
//            ) {
//                // Sheet content
//                Button(onClick = {
//                    scope.launch { sheetState.hide() }.invokeOnCompletion {
//                        if (!sheetState.isVisible) {
//                            showBottomSheet = false
//                        }
//
//                        if (userInfo?.isAnonymous == false) {
//                            onLogout()
//                        }
//                        onNavigateToLogin()
//                    }
//                }) {
//                    Text(
//                        text = if (userInfo?.isAnonymous == true) "Sign Up/Sign In" else "Log Out"
//                    )
//                }
//            }
//        }
    }
}