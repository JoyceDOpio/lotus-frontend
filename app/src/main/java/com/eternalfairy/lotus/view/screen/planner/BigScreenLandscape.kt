package com.eternalfairy.lotus.view.screen.planner

import android.content.Context
import android.util.Log
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.lotus.R
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
import com.eternalfairy.lotus.view.component.InfoCard
import com.eternalfairy.lotus.view.component.TaskDropdownMenu
import com.eternalfairy.lotus.view.component.TopBar
import com.eternalfairy.lotus.view.component.VoiceNoteList
import com.eternalfairy.lotus.view.component.calendar.CalendarWeek
import com.eternalfairy.lotus.view.data.TaskUiState
import com.eternalfairy.lotus.view.theme.BACKGROUND_COLOR
import com.eternalfairy.lotus.view.theme.COMMENT_TEXT_COLOR
import com.eternalfairy.lotus.view.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.lotus.view.theme.HEADER_TEXT_COLOR
import com.eternalfairy.lotus.viewmodel.AudioViewModel
import com.eternalfairy.lotus.view.utils.TouchGestureUtils
import com.eternalfairy.lotus.viewmodel.PlannerViewModel
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun BigScreenLandscape (
//    activityUiState: ActivityUiState,
//    adView: AdView,
    audioViewModel: AudioViewModel,
    plannerViewModel: PlannerViewModel,
    context: Context,
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
    onDeleteActivity: (Uuid) -> Unit,
    onEditActivity: (Uuid?) -> Unit,
    onDeleteTask: () -> Unit,
    onEditTask: () -> Unit,
    onDeleteVoiceNote: (Uuid?) -> Unit,
//    onMoveToToDoList: () -> Unit,
//    onPinTask: (Boolean) -> Unit,
    onPressActiveTime: () -> Unit,
//    onSetSelectedDate: (LocalDate) -> Unit,
    onShowPopupWindow: (PopupState) -> Unit,
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
    val state = plannerViewModel.state

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val formatter: DateTimeFormat<kotlinx.datetime.LocalDate> = kotlinx.datetime.LocalDate.Format {
        day()
        char('.')
        char(' ')
        monthName(MonthNames(monthNames))
        char(' ')
        year()

    }
    val selectedDate = state.selectedDate
    val today = kotlinx.datetime.LocalDate.parse(java.time.LocalDate.now().toString())

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
    val activityUiState = state.selectedActivity
    val dayUiState = state.selectedDay
    val subActivities = state.subActivities
    val taskUiState = state.selectedTask

    // Move to calendar
    // The task that is being moved to calendar - if I save the taskUiState under the touchedTask it seems it is not updated in time after touching it. The dial tries to draw it before its value is updated.
    var taskToBeMovedToCalendar by remember { mutableStateOf<TaskUiState?>(null) }
    // The optimal duration of the task will be 30 minutes and minimum will be 5 minutes
    val optimalDuration = 30
    val minimalDuration = 5
    var movedTaskStartTime: LocalTime? = null
    var movedTaskEndTime: LocalTime? = null
    // Find a slot between tasks to fit in the moved task
    var slotStartTime: LocalTime = if (selectedDate == today)
            LocalTime(
                LocalDateTime.now().hour, LocalDateTime.now().minute
            )
    else dayUiState.activeTimeStart
    var slotEndTime: LocalTime
    var tasks: List<TaskUiState> = state.tasks.toList()
    val moveToCalendarHeaderText = "Move To Calendar"// TODO: Read string from resource
    val notEnoughTimeSpaceText = "THERE IS NOT ENOUGH TIME WITHIN THE SELECTED DAY TO MOVE THE TASK"// TODO: Read string from resource

    val mainPanelWidth = 800.dp
    val panelHeight = 780.dp
    val sidePanelWidth = 380.dp
    val comparisonCardWidth = 380.dp
    val comparisonCardHeight = 600.dp

    // Banner ad
//    // Set a large anchored adaptive banner ad size with a given width.
//    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(LocalContext.current, 360)
//    adView.setAdSize(adSize)
    val adView = AdView(context)

    adView.adUnitId= "ca-app-pub-3940256099942544/9214589741"

    // Set a large anchored adaptive banner ad size with a given width.
    val deviceWidth = LocalConfiguration.current.screenWidthDp
    val adWidth = 340
    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(LocalContext.current, adWidth)
    adView.setAdSize(adSize)

    adView?.adListener =
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
//            setTaskDate(dayUiState.date)
//            setTaskPriority(null)
            plannerViewModel.onEvent(PlannerUiEvent.TaskDateChanged(dayUiState.date))
            plannerViewModel.onEvent(PlannerUiEvent.TaskPriorityChanged(null))

            // If there are tasks planned for the day
            if (!state.tasks.isEmpty()) {
                for (task in state.tasks) {
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
//                setTaskStartTime(movedTaskStartTime)
//                setTaskEndTime(movedTaskEndTime)
                plannerViewModel.onEvent(PlannerUiEvent.TaskStartTimeChanged(movedTaskStartTime))
                plannerViewModel.onEvent(PlannerUiEvent.TaskEndTimeChanged(movedTaskEndTime))

                taskToBeMovedToCalendar = taskUiState.copy(
                    id = taskUiState.id
                )
                tasks = state.tasks.toList() + taskToBeMovedToCalendar!!
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
//                        saveTask(task.copy(id = UUID.randomUUID(), pinned = false))
//                        plannerViewModel.onEvent(PlannerUiEvent.TaskIdChanged(Uuid.generateV4()))
                        plannerViewModel.onEvent(PlannerUiEvent.TaskIdChanged(Uuid.random()))
                        plannerViewModel.onEvent(PlannerUiEvent.TaskPinnedChanged(false))
                    }
//                    else saveTask(task)
                }
//                else saveTask(task)
                plannerViewModel.onEvent(PlannerUiEvent.SaveTask)
            }

            mainPanelState = BigScreenMainPanelState.DayTask
        }
    }

    Row (
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOR)
            .padding(
                top = 40.dp,
                bottom = 60.dp
            )
    ) {
        // Side panel
        Column (
            modifier = Modifier
                .width(sidePanelWidth)
                .height(panelHeight)
                .padding(
                    start = 20.dp
                )
            ,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopBar(
                paddingTop = 0.dp,
                componentWidth = sidePanelWidth,
                componentHeight = 50.dp,
                title = selectedDate.format(formatter)
            )

            AnimatedVisibility(
                visible = selectedDate == today
            ) {
                ActiveTimeHeader(
                    componentWidth = sidePanelWidth,
                    dayUiState = dayUiState,
                    selectedDate = selectedDate
//                    userInput = userInput
                )
            }

            // If the selected date is in the past or in the future, show the ad banner instead of the Active Time Header.
            AnimatedVisibility(
                visible = selectedDate != today
            ) {
//                BannerAd(
//                    adView = adView
//                )
                Column(
//                        modifier = Modifier.fillMaxSize(),
//                        verticalArrangement = Arrangement.Bottom
                    modifier = Modifier
                        .padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        )
                        .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
                        .width(sidePanelWidth)
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

//            // If the selected date is in the future, show a spacer
//            AnimatedVisibility(
//                visible = selectedDate.isAfter(LocalDate.now())
//                        || selectedDate.isBefore(LocalDate.now())
//            ) {
//                Spacer(modifier = Modifier.height(80.dp))
//            }

            TopBar(
                paddingTop = 5.dp,
                componentWidth = sidePanelWidth,
                componentHeight = 60.dp,
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
                    componentHeight = 280.dp,
                    viewModel = plannerViewModel,
//                    dayUiState = dayUiState,
//                    items = toDoTasks,
//                    lastTaskPriority = lastTaskPriority,
//                    taskUiState = taskUiState,
//                    deleteTask = deleteTask,
                    onMoveToCalendar = {
                        mainPanelState = BigScreenMainPanelState.MoveToCalendar
                    },
//                    onMoveToToDoList = onMoveToToDoList,
//                    onPinTask = onPinTask,
//                    saveTask = saveTask,
//                    saveTaskFromState = saveTaskFromState,
//                    selectTask = selectTask,
//                    setTaskDescription = setTaskDescription,
//                    setTaskEndTime = setTaskEndTime,
//                    setTaskStartTime = setTaskStartTime,
//                    setTaskPriority = setTaskPriority,
//                    setTaskTitle = setTaskTitle
                )
            }

            // Show the goals
            AnimatedVisibility(
                visible = sidePanelState == BigScreenSidePanelState.Goal
            ) {
                DragItemListGoal(
                    componentWidth = sidePanelWidth,
                    componentHeight = 280.dp,
                    viewModel = plannerViewModel,
//                    items = goals,
//                    goalUiState = goalUiState,
//                    lastGoalPriority = lastGoalPriority,
//                    deleteGoal = deleteGoal,
//                    saveGoal = saveGoal,
//                    saveGoalFromState = saveGoalFromState,
//                    selectGoal = selectGoal,
//                    setGoalPriority = setGoalPriority,
//                    setGoalTitle = setGoalTitle
                )
            }

            CalendarWeek(
                componentWidth = sidePanelWidth,
                selectedDate = java.time.LocalDate.parse(selectedDate.toString()),
//                userInput = userInput,
                onSetDate = { date -> plannerViewModel.onEvent(PlannerUiEvent.SelectedDateChanged(date)) }
//                onSetDate = onSetSelectedDate
            )

            BottomBar(
                componentWidth = sidePanelWidth,
                paddingBottom = 0.dp,
                content = {
                    // Add button
                    IconButton(
                        onClick = {
                            val popupState: PopupState

                            when (sidePanelState) {
                                BigScreenSidePanelState.Goal -> {
//                                    selectGoal(null)
                                    plannerViewModel.onEvent(PlannerUiEvent.SelectedGoalIdChanged(null))

                                    popupState = PopupState.EditGoal
                                }

                                BigScreenSidePanelState.ToDo -> {
//                                    selectTask(null)
//                                    setTaskDate(null)
                                    plannerViewModel.onEvent(PlannerUiEvent.SelectedTaskIdChanged(null))
                                    plannerViewModel.onEvent(PlannerUiEvent.TaskDateChanged(null))

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

        // Main panel
        Column (
            modifier = Modifier
                .height(panelHeight)
                .width(mainPanelWidth)
            ,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // If we are not showing the task-activity comparison
            AnimatedVisibility(
                visible = mainPanelState != BigScreenMainPanelState.Comparison
            ) {
                // If the selected date is in the past, show a comparison of the planned tasks and actual activities
                AnimatedVisibility(
                    visible = selectedDate < today
                            || mainPanelState == BigScreenMainPanelState.DayActivity
                ) {
                    Column(
                        modifier = Modifier
                        ,
                    ) {
                        ActivityGraph(
                            componentWidth = mainPanelWidth,
                            paddingTop = 0.dp,
                            viewModel = plannerViewModel,
//                            dayUiState = dayUiState,
                            drawClockHand = mainPanelState == BigScreenMainPanelState.DayActivity,
                            onNavigateToTaskActivityComparison = {
                                mainPanelState = BigScreenMainPanelState.Comparison
                            },
//                            selectActivity = selectActivity,
//                            selectTask = selectTask
                        )

                        ComparisonDial(
                            componentWidth = mainPanelWidth,
                            componentHeight = if (selectedDate < today) 500.dp else 400.dp,
//                            dayUiState = dayUiState,
                            viewModel = plannerViewModel,
                            drawClockHand = mainPanelState == BigScreenMainPanelState.DayActivity,
                            onNavigateToTaskActivityComparison = {
                                mainPanelState = BigScreenMainPanelState.Comparison
                            },
//                            selectActivity = selectActivity,
//                            selectTask = selectTask
                        )
                    }
                }

                // Show the day planner
                AnimatedVisibility(
                    // If the selected date is today or in the future, show the planning screen
                    visible = mainPanelState == BigScreenMainPanelState.DayTask
                            && selectedDate >= today
                ) {
                        PlannerDial(
                            componentWidth = mainPanelWidth,
                            componentHeight = 580.dp,
                            paddingTop = 0.dp,
                            viewModel = plannerViewModel,
//                            dayUiState = dayUiState,
                            drawClockHand = selectedDate == today,
//                            lastTaskPriority = lastTaskPriority,
//                            taskUiState = taskUiState,
//                            userInput = userInput,
//                            deleteTask = deleteTask,
//                            onMoveToToDoList = onMoveToToDoList,
                            onMoveToCalendar = {
                                mainPanelState = BigScreenMainPanelState.MoveToCalendar
                            },
                            onPressActiveTime = onPressActiveTime,
//                            onPinTask = onPinTask,
//                            saveTask = saveTask,
//                            saveTaskFromState = saveTaskFromState,
//                            selectTask = selectTask,
//                            setTaskEndTime = setTaskEndTime,
//                            setTaskStartTime = setTaskStartTime,
//                            setTaskDate = setTaskDate,
//                            setTaskDescription = setTaskDescription,
//                            setTaskPriority = setTaskPriority,
//                            setTaskTitle = setTaskTitle
                        )
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
                                componentHeight = 740.dp,
                                paddingTop = 0.dp,
                                dayUiState = dayUiState,
                                selectedDate = selectedDate,
//                                userInput = userInput,
                                onCancel = {
                                    mainPanelState = BigScreenMainPanelState.DayTask
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
                                    .height(740.dp)
                                    .width(mainPanelWidth)
                                    .padding(
                                        start = 10.dp,
                                        top = 0.dp,
                                        end = 10.dp,
                                        bottom = 5.dp
                                    )
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
                        .height(comparisonCardHeight)
                    ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Task
                    Column (
                        modifier = Modifier
                            .padding(
                                start = 10.dp,
                                top = 0.dp,
                                end = 10.dp,
                                bottom = 5.dp,
                            )
                            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
                            .width(comparisonCardWidth)
                            .height(comparisonCardHeight)
                            .background(COMPONENT_BACKGROUND_COLOR)
                        ,
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Show task details if there is a task to be shown
                        if (taskUiState.id != null) {
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

                            InfoCard (
                                viewModel = plannerViewModel,
//                                date = userInput.selectedDate,
//                                dayUiState = dayUiState,
//                                endTime = taskDetails.endTime,
//                                startTime = taskDetails.startTime,
//                                title = taskDetails.title,
//                                pinned = taskDetails.pinned
                            ) {
                                // Description
                                Text(
                                    text = taskUiState.description ?: "",
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
                            .padding(
                                start = 10.dp,
                                top = 0.dp,
                                end = 10.dp,
                                bottom = 5.dp,
                            )
                            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
                            .width(comparisonCardWidth)
                            .height(comparisonCardHeight)
                            .background(COMPONENT_BACKGROUND_COLOR)
                        ,
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (activityUiState.id != null) {
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
                                                onEditActivity(activityUiState.id)
                                            }
                                        ),
                                        DropDownItem(
                                            text = deleteText,
                                            iconId = R.drawable.delete_24dp_5f6368_fill0_wght400_grad0_opsz24,
                                            onClick = {
                                                onDeleteActivity(activityUiState.id)
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

                            InfoCard (
                                viewModel = plannerViewModel,
//                                date = userInput.selectedDate,
//                                dayUiState = dayUiState,
//                                endTime = activityDetails.endTime,
//                                startTime = activityDetails.startTime,
//                                title = activityDetails.title,
//                                subActivities = activityDetails.subActivitiesUiState
                            ) {
                                Column (
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState())
                                    ,
                                ) {
                                    if (activityUiState.note != "") {
                                        // Notes
                                        Text(
                                            text = activityUiState.note,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                            ,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 18.sp
                                        )
                                    }

//                                    if (!activityUiState.voiceNotesUiState.isEmpty()) {
//                                        Spacer(modifier = Modifier.height(10.dp))
//
//                                        VoiceNoteList(
//                                            activityUiState = activityUiState,
//                                            audioViewModel = audioViewModel,
//                                            onDeleteItem = { voiceNote ->
//                                                onDeleteVoiceNote(voiceNote.id)
//                                            },
//                                            updateLastPlayedPosition = { position, itemIndex -> plannerViewModel.updateLastPlayedPosition(position, itemIndex) }
////                                            updateLastPlayedPosition = updateLastPlayedPosition
//                                        )
//                                    }

                                    if (!subActivities.isEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))

                                        SubActivityList(
                                            viewModel = plannerViewModel,
//                                            mainActivityUiState = activityDetails,
                                            onDeleteItem = { subActivityId ->
                                                onDeleteActivity(subActivityId)
                                            },
                                            onEditItem = { subActivity ->
                                                onEditActivity(subActivity.id)
                                            },
//                                            removeVoiceNote = { voiceNote ->
//                                                selectVoiceNoteToBeDeleted(voiceNote.id)
//                                                deleteVoiceNote()
//                                            },
//                                            updateLastPlayedPosition = updateLastPlayedPosition
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

            // Tasks bottom bar
            AnimatedVisibility(
                visible = mainPanelState == BigScreenMainPanelState.DayTask
                        && selectedDate >= today
            ) {
                BottomBar(
                    componentWidth = mainPanelWidth,
                    paddingBottom = 0.dp,
                    content = {
                        // Add button
                        IconButton(
                            onClick = {
//                                selectTask(null)
//                                setTaskDate(userInput.selectedDate)
                                plannerViewModel.onEvent(PlannerUiEvent.SelectedTaskIdChanged(null))
                                plannerViewModel.onEvent(PlannerUiEvent.TaskDateChanged(selectedDate))

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

                        if (selectedDate == today) {
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
                    componentWidth = mainPanelWidth,
                    paddingBottom = 0.dp,
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
                    componentWidth = mainPanelWidth,
                    paddingBottom = 0.dp,
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

                        taskToBeMovedToCalendar?.let {// FIXME: Optimize null handling
                            // Save button
                            IconButton(onClick = {
//                                // Update the task's start- and end times
//                                for (task in tasks) {
//                                    if (task.id == taskToBeMovedToCalendar!!.id) {
//                                        // If the task to be moved to the calendar is pinned, we don't want to actually move this task but to copy it to the calendar so that the original task stays in the TO-DO list for further references (i.e. so that the task can be copied over and over again to the calendar)
//                                        if (task.pinned) {
//                                            saveTask(task.copy(id = UUID.randomUUID(), pinned = false))
//                                        }
//                                        else saveTask(task)
//                                    }
//                                    else saveTask(task)
//                                }
//                                mainPanelState = BigScreenMainPanelState.DayTask
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

            // Comparison bottom bar
            AnimatedVisibility(
                visible = mainPanelState == BigScreenMainPanelState.Comparison
            ) {
                BottomBar(
                    componentWidth = mainPanelWidth,
                    paddingBottom = 0.dp,
                    content = {
                        IconButton(
                            onClick = {
                                if (selectedDate < today) {
                                    mainPanelState = BigScreenMainPanelState.DayTask
                                }
                                else if (selectedDate == today) {
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
    }
}