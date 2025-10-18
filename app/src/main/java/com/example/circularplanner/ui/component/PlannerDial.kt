package com.example.circularplanner.ui.component

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.R
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.screen.DeleteScreen
import com.example.circularplanner.ui.screen.TaskEditScreen
import com.example.circularplanner.ui.screen.TaskInfoScreen
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.ui.viewmodel.UserInput
import com.example.circularplanner.utils.AngleMode
import com.example.circularplanner.utils.DrawScopeUtils.drawClockCenter
import com.example.circularplanner.utils.DrawScopeUtils.drawClockHand
import com.example.circularplanner.utils.DrawScopeUtils.drawHourStepsAndLabels
import com.example.circularplanner.utils.DrawScopeUtils.drawMinuteSteps
import com.example.circularplanner.utils.DrawScopeUtils.drawTask
import com.example.circularplanner.utils.TaskModePopup
import com.example.circularplanner.utils.TouchGestureUtils
import com.example.circularplanner.utils.TouchGestureUtils.TOUCH_STROKE
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import kotlin.math.min

const val SECONDS_IN_MINUTE = 60
const val HOUR_LABEL_COLOR = 0xFF6650a4
const val MINUTE_STEP_COLOR = 0xFFcac3de
const val CLOCK_LABEL_COLOR = 0xff592687
const val CLOCK_CENTER_COLOR = 0xffffffff

enum class DragDirection {
    Backward,
    Forward,
    None
}
// Are we creating a new task or editing an existing task
enum class TaskMode {
    Create,
    EditTimeRange,
    EditStartTime,
    View// Default value
}

@Composable
fun PlannerDial(
    dayState: DayState,
    drawClockHand: Boolean = false,
    lastTaskPriority: Int?,
    nextTaskUiState: TaskUiState,
    previousTaskUiState: TaskUiState,
    taskUiState: TaskUiState,
    userInput: UserInput,
    deleteTask: () -> Unit,
    onMoveToToDoList: () -> Unit,
    onPressActiveTime: () -> Unit,
    setTaskDescription: (String) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskPriority: (Int) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskTitle: (String) -> Unit,
    saveTask: (Task) -> Unit,
    saveTaskFromState: () -> Unit,
    selectNextTask: (Task) -> Unit,
    selectPreviousTask: (Task) -> Unit,
    selectTask: (UUID?) -> Unit,
    setNextTaskStartTime: (Time) -> Unit,
    setNextTaskEndTime: (Time) -> Unit,
    saveNextTask: () -> Unit,
    setTaskDate: (LocalDate?) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val activeTimeStart: Time = dayState.activeTimeStart
    val activeTimeEnd: Time = dayState.activeTimeEnd
    var taskMode: TaskMode by remember { mutableStateOf(TaskMode.View) }
    var angleMode: AngleMode by remember { mutableStateOf(AngleMode.None) }
    val activeTimeColor = MaterialTheme.colorScheme.primary

    val totalMinutes: Int = TouchGestureUtils.calculateTotalNumberOfMinutes(
        Time(
            activeTimeStart.hour,
            activeTimeStart.minute
        ),
        Time(
            activeTimeEnd.hour,
            activeTimeEnd.minute
        )
    )
    // The angle that corresponds to 1 minute
    val minuteAngle: Float = 360 / totalMinutes.toFloat()

    // Note the start and end angle of where the finger touched the screen to get the supposed new start and end time values of a task:
    // - values used to trace the finger movement
    var startAngle by remember { mutableStateOf(0f) }
    var endAngle by remember { mutableStateOf(0f) }
    // - values used to draw the angles
    var tmpStartAngle by remember { mutableStateOf(0f) }
    var tmpEndAngle by remember { mutableStateOf(0f) }

    // The width and height of the Canvas
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    var angle by remember { mutableStateOf(0f) }
    var touchNearTheDialEdge by remember { mutableStateOf(false) }
    var touchInsideTheDial by remember { mutableStateOf(false) }
    var drawNewTaskTimeRange by remember { mutableStateOf(false) }
    var center by remember { mutableStateOf(Offset.Zero) }
    // Task area padding from the end of the dial's outer radius
    val taskPadding = 40f
    val clockHandPadding = 0f

    // Basically the width of the finger touch on the screen
    val touchStroke: Float = TOUCH_STROKE

    // The radius of the dial (from the center to the end of the clock steps)
    var outerRadius by remember { mutableStateOf(0f) }
    // The radius from the center to the clock steps
    var innerRadius by remember { mutableStateOf(0f) }
    // The radius of the clock center (the one that displays time)
    var centerRadius by remember { mutableStateOf(0f) }

    // Variables to identify whether the dial was touched within an existing task area
    var touchWithinTaskArea by remember { mutableStateOf(true) }
    // If I save the taskUiState under the touchedTask it seems it is not updated in time after touching it. The dial tries to draw it before its value is updated.
    var touchedTask by remember { mutableStateOf<Task?>(null) }
    var touchedTaskIndex by remember { mutableStateOf<Int?>(null) }
    var nextTask by remember { mutableStateOf<Task?>(null) }
    var previousTask by remember { mutableStateOf<Task?>(null) }

    var clockTime by remember { mutableStateOf(Time(LocalTime.now().hour, LocalTime.now().minute)) }
    var taskClockTime by remember { mutableStateOf(Time(LocalTime.now().hour, LocalTime.now().minute)) }

//    val tasks = dayState.tasks.sortedWith{ a, b -> a.compareTo(b)}
//    val tasks = dayState.tasks
    val tasks = dayState.tasks.toList()

    var showPopupWindow by remember { mutableStateOf(false) }
    var popupState by remember { mutableStateOf(TaskModePopup.Info) }

    // DRAGGING
    // The duration of the dragged task in minutes
    var draggedTaskDuration by remember { mutableIntStateOf(0) }
    var dragDirection by remember { mutableStateOf(DragDirection.None) }
    var initialAngleTranslated by remember { mutableFloatStateOf(0f) }
    // The angles which the dragged task should "jump to" should the dragging end while the dragged task still overlaps another task
    var jumpToStartAngle: Float? by remember { mutableStateOf(null) }
    var jumpToEndAngle: Float? by remember { mutableStateOf(null) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val scaleDownButtonAlpha by animateFloatAsState(
        targetValue = if (scale > 1f || offset != Offset.Zero) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = ""
    )

    fun checkIfTouchWithinTasks(angle: Float, tasks: List<Task>): Boolean {
        // The task stores the appropriate angle values, i.e. values corresponding to how the circle is drawn (the 0 degree starts at the right-hand side (east) of the circle). We want to 'correct' these angles as if 0 degree starts at the top of the circle (north)
        var isTouchWithinAnyTask: Boolean

        for ((index, task) in tasks.withIndex()) {
            val taskStartAngle = TouchGestureUtils.calculateAngleFromTime (
                activeTimeStart,
                task.startTime!!,
                minuteAngle
            )
            val taskEndAngle = TouchGestureUtils.calculateAngleFromTime (
                activeTimeStart,
                task.endTime!!,
                minuteAngle
            )
            isTouchWithinAnyTask =
                TouchGestureUtils.checkIfTouchWithinAngleRange(angle, taskStartAngle, taskEndAngle)

            // We haven't found the task yet
            if (!isTouchWithinAnyTask && touchedTask == null) {
                previousTask = task.copy()
            }

            // We found the task
            if (touchedTask != null && !isTouchWithinAnyTask && nextTask == null) {
                nextTask = task.copy()
            }

            // We found the task
            if (isTouchWithinAnyTask) {
                selectTask(task.id)
                selectNextTask(task)
                selectPreviousTask(task)

                touchedTask = task.copy()
                touchedTaskIndex = index
            }
        }

        if (touchedTask != null) return true
        else {
            previousTask = null
            return false
        }
    }

    fun moveTaskBackward(index: Int, angleChange: Float) {
        val taskTobeMoved = tasks[index]
//        Log.i("PlannerDial", "moveTaskBackward")
//        Log.i("PlannerDial", "taskTobeMoved.title ${taskTobeMoved.title}")
//        Log.i("PlannerDial", "angleChange $angleChange")
//        Log.i("PlannerDial", "index $index")

        if (taskTobeMoved.id != touchedTask!!.id) {
            val taskToBeMovedStartAngle =
                TouchGestureUtils.calculateAngleFromTime(
                    activeTimeStart = activeTimeStart,
                    time = taskTobeMoved.startTime!!,
                    minuteAngle = minuteAngle
                )
            val taskToBeMovedEndAngle =
                TouchGestureUtils.calculateAngleFromTime(
                    activeTimeStart = activeTimeStart,
                    time = taskTobeMoved.endTime!!,
                    minuteAngle = minuteAngle
                )
            var taskToBeMovedNewStartAngle = taskToBeMovedStartAngle - angleChange
            var taskToBeMovedNewEndAngle = taskToBeMovedEndAngle - angleChange

            // Since the values are not translated, they have to be corrected (because we might end up subtracting a positive value from 0)
            if (taskToBeMovedNewStartAngle < 0f) taskToBeMovedNewStartAngle += 360f
            if (taskToBeMovedNewEndAngle < 0f) taskToBeMovedNewEndAngle += 360f
            if (taskToBeMovedNewStartAngle > 360f) taskToBeMovedNewStartAngle -= 360f
            if (taskToBeMovedNewEndAngle > 360f) taskToBeMovedNewEndAngle -= 360f

            val taskTobeMovedNewStartTime = TouchGestureUtils.calculateTimeFromAngle(taskToBeMovedNewStartAngle, minuteAngle, activeTimeStart)
            val taskTobeMovedNewEndTime = TouchGestureUtils.calculateTimeFromAngle(taskToBeMovedNewEndAngle, minuteAngle, activeTimeStart)
            val duration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                taskTobeMoved.startTime!!,
                taskTobeMoved.endTime!!
            )

//            Log.i("PlannerDial", "duration $duration")

            val rangeStart = activeTimeStart
            val rangeEnd = taskTobeMovedNewEndTime
            val rangeCapacity = TouchGestureUtils.calculateTotalNumberOfMinutes(
                start = rangeStart,
                end = rangeEnd
            )

//            Log.i("PlannerDial", "rangeStart $rangeStart")
//            Log.i("PlannerDial", "rangeEnd $rangeEnd")
//            Log.i("PlannerDial", "rangeCapacity $rangeCapacity")

            if (rangeCapacity >= duration) {
                // Check if the task that is moved starts to overlap an adjacent task while it's moving (omit the dragged task)
                var adjacentTaskIndex = index - 1

                if (adjacentTaskIndex >= 0) {
                    if (tasks[adjacentTaskIndex].id == touchedTask!!.id) {
                        if (adjacentTaskIndex > 0) {
                            adjacentTaskIndex--
                        }
                    }

                    if (tasks[adjacentTaskIndex].id != touchedTask!!.id) {
                        val adjacentTask = tasks[adjacentTaskIndex]

                        // If the tasks start to overlap, start moving backwards the adjacent task
                        if (taskTobeMovedNewStartTime.compareTo(adjacentTask.endTime!!) == -1 || taskTobeMovedNewStartTime.compareTo(adjacentTask.endTime!!) == 0) {
                            // Re-adjust the angle change - calculate by how much the tasks are overlapping
                            val adjacentTaskEndAngle =
                                TouchGestureUtils.calculateAngleFromTime(
                                    activeTimeStart = activeTimeStart,
                                    time = adjacentTask.endTime!!,
                                    minuteAngle = minuteAngle
                                )
                            val adjacentTaskEndAngleTranslated = TouchGestureUtils.translateAngle270To0(adjacentTaskEndAngle)
                            val taskToBeMovedNewStartAngleTranslated = TouchGestureUtils.translateAngle270To0(taskToBeMovedNewStartAngle)
                            val angleChangeAdjusted = adjacentTaskEndAngleTranslated - taskToBeMovedNewStartAngleTranslated

                            moveTaskBackward(adjacentTaskIndex, angleChangeAdjusted)
                        }
                        else {
                            tasks.find { task -> task.id == taskTobeMoved.id }?.apply {
                                startTime = taskTobeMovedNewStartTime
                                endTime = taskTobeMovedNewEndTime
                            }
                        }
                    }
                    else {
                        tasks.find { task -> task.id == taskTobeMoved.id }?.apply {
                            startTime = taskTobeMovedNewStartTime
                            endTime = taskTobeMovedNewEndTime
                        }
                    }
                }
                // If there is no more tasks before the task to be moved
                else {
                    tasks.find { task -> task.id == taskTobeMoved.id }?.apply {
                        startTime = taskTobeMovedNewStartTime
                        endTime = taskTobeMovedNewEndTime
                    }
                }
            }
        }
    }

    // The value of angleChange will be negative
    fun moveTaskForward(index: Int, angleChange: Float) {
        val taskTobeMoved = tasks[index]
        Log.i("PlannerDial", "moveTaskForward")
        Log.i("PlannerDial", "taskTobeMoved.title ${taskTobeMoved.title}")
        Log.i("PlannerDial", "taskTobeMoved.startTime ${taskTobeMoved.startTime}")
        Log.i("PlannerDial", "angleChange $angleChange")
        Log.i("PlannerDial", "index $index")

        if (taskTobeMoved.id != touchedTask!!.id) {
            val taskToBeMovedStartAngle =
                TouchGestureUtils.calculateAngleFromTime(
                    activeTimeStart = activeTimeStart,
                    time = taskTobeMoved.startTime!!,
                    minuteAngle = minuteAngle
                ) + 0.33f// It seems that during the TouchGestureUtils.calculateAngleFromTime() conversion around 0.33f is lost (subtracted from the angle). This makes it difficult for the task to move forward, so we're adding the 0.33f back. If I add this value back in the TouchGestureUtils.calculateAngleFromTime() method, the task will have difficulty moving backward (in the moveTaskBackward() method).
            val taskToBeMovedEndAngle =
                TouchGestureUtils.calculateAngleFromTime(
                    activeTimeStart = activeTimeStart,
                    time = taskTobeMoved.endTime!!,
                    minuteAngle = minuteAngle
                ) + 0.33f
            var taskToBeMovedNewStartAngle = taskToBeMovedStartAngle - angleChange
            var taskToBeMovedNewEndAngle = taskToBeMovedEndAngle - angleChange

            if (taskToBeMovedNewStartAngle > 360f) taskToBeMovedNewStartAngle -= 360f
            if (taskToBeMovedNewEndAngle > 360f) taskToBeMovedNewEndAngle -= 360f
            if (taskToBeMovedNewStartAngle < 0f) taskToBeMovedNewStartAngle += 360f
            if (taskToBeMovedNewEndAngle < 0f) taskToBeMovedNewEndAngle += 360f

//            Log.i("PlannerDial", "taskToBeMovedStartAngle ${taskToBeMovedStartAngle}")
//            Log.i("PlannerDial", "taskToBeMovedEndAngle ${taskToBeMovedEndAngle}")
//            Log.i("PlannerDial", "taskToBeMovedNewStartAngle ${taskToBeMovedNewStartAngle}")
//            Log.i("PlannerDial", "taskToBeMovedNewEndAngle ${taskToBeMovedNewEndAngle}")

            val taskTobeMovedNewStartTime = TouchGestureUtils.calculateTimeFromAngle(taskToBeMovedNewStartAngle, minuteAngle, activeTimeStart)
            val taskTobeMovedNewEndTime = TouchGestureUtils.calculateTimeFromAngle(taskToBeMovedNewEndAngle, minuteAngle, activeTimeStart)

//            Log.i("PlannerDial", "taskTobeMovedNewStartTime ${taskTobeMovedNewStartTime}")
//            Log.i("PlannerDial", "taskTobeMovedNewEndTime ${taskTobeMovedNewEndTime}")

            val duration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                taskTobeMoved.startTime!!,
                taskTobeMoved.endTime!!
            )

//            Log.i("PlannerDial", "duration $duration")

            val rangeStart = taskTobeMovedNewStartTime
            val rangeEnd = activeTimeEnd
            val rangeCapacity = TouchGestureUtils.calculateTotalNumberOfMinutes(
                start = rangeStart,
                end = rangeEnd
            )
//            Log.i("PlannerDial", "rangeStart $rangeStart")
//            Log.i("PlannerDial", "rangeEnd $rangeEnd")
//            Log.i("PlannerDial", "rangeCapacity $rangeCapacity")

            if (rangeCapacity >= duration) {
                // Check if the task that is moved starts to overlap an adjacent task while it's moving (omit the dragged task)
                var adjacentTaskIndex = index + 1

                if (adjacentTaskIndex < tasks.size) {
                    if (tasks[adjacentTaskIndex].id == touchedTask!!.id) {
                        if (adjacentTaskIndex < tasks.size - 1) {
                            adjacentTaskIndex++
                        }
                    }

                    if (tasks[adjacentTaskIndex].id != touchedTask!!.id) {
                        val adjacentTask = tasks[adjacentTaskIndex]

                        // If the tasks start to overlap, start moving forwards the adjacent task
                        if (taskTobeMovedNewEndTime.compareTo(adjacentTask.startTime!!) == 1 || taskTobeMovedNewEndTime.compareTo(adjacentTask.startTime!!) == 0) {
                            // Re-adjust the angle change - calculate by how much the tasks are overlapping
                            val adjacentTaskStartAngle =
                                TouchGestureUtils.calculateAngleFromTime(
                                    activeTimeStart = activeTimeStart,
                                    time = adjacentTask.startTime!!,
                                    minuteAngle = minuteAngle
                                ) + 0.33f// It seems that during the TouchGestureUtils.calculateAngleFromTime() conversion around 0.33f is lost (subtracted from the angle). This makes it difficult for the task to move forward, so we're adding the 0.33f back. If I add this value back in the TouchGestureUtils.calculateAngleFromTime() method, the task will have difficulty moving backward (in the moveTaskBackward() method).
                            val adjacentTaskStartAngleTranslated = TouchGestureUtils.translateAngle270To0(adjacentTaskStartAngle)
                            val taskToBeMovedNewEndAngleTranslated = TouchGestureUtils.translateAngle270To0(taskToBeMovedNewEndAngle)
                            val angleChangeAdjusted = adjacentTaskStartAngleTranslated - taskToBeMovedNewEndAngleTranslated

                            moveTaskForward(adjacentTaskIndex, angleChangeAdjusted)
                        }
                        else {
                            tasks.find { task -> task.id == taskTobeMoved.id }?.apply {
                                startTime = taskTobeMovedNewStartTime
                                endTime = taskTobeMovedNewEndTime
                            }
                        }
                    }
                    else {
                        tasks.find { task -> task.id == taskTobeMoved.id }?.apply {
                            startTime = taskTobeMovedNewStartTime
                            endTime = taskTobeMovedNewEndTime
                        }
                    }
                }
                // If there are no more tasks after the task to be moved
                else {
                    tasks.find { task -> task.id == taskTobeMoved.id }?.apply {
                        startTime = taskTobeMovedNewStartTime
                        endTime = taskTobeMovedNewEndTime
                    }
                }
            }
        }
    }

    fun resetDialParameters() {
        touchNearTheDialEdge = false
        drawNewTaskTimeRange = false
        touchWithinTaskArea = false
        selectTask(null)
    }

    // Reset the start and end angles of where the finger touched the dial
    fun resetStartAndEndAngles() {
        startAngle = 0f
        endAngle = 0f
        tmpStartAngle = 0f
        tmpEndAngle = 0f
    }

    fun resetTask() {
        touchedTask = null
        selectTask(null)
        nextTask = null
        previousTask = null
    }

    fun reset() {
        resetDialParameters()
        resetStartAndEndAngles()
        resetTask()
        taskMode = TaskMode.View
        popupState = TaskModePopup.Info
        dragDirection = DragDirection.None
    }

    // Update the clock every minute
    LaunchedEffect(true) {
        while (true) {
            delay(1000L * SECONDS_IN_MINUTE)
            clockTime = Time(LocalTime.now().hour, LocalTime.now().minute)
        }
    }

    // Clear the task UI state when the component is loaded for the first time
    LaunchedEffect(Unit) {
        selectTask(null)// TODO: This should clear the task UI state after coming back from the TaskInfoScreen, but it will not do anything when the user drags task along the dial, since then the component is not drawn for the first time (instead, it's redrawn). This could be solved if for example I cleared the task UI state based on the component's state
    }

    LaunchedEffect(userInput.selectedDate) {
        // Resets the zoom among others
        reset()
    }

    Column (
        modifier = Modifier
            .width(380.dp)
            .height(440.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Center
        ) {
            Column (
                modifier = Modifier
                    .weight(2f)
            ) {}

            Column (
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box (
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(activeTimeColor)
                        .aspectRatio(1f)
                        .fillMaxSize(1f)
                        .clickable {
                            // Show active time setup
                            onPressActiveTime()
                        }
                ) {
                    val label: AnnotatedString = if (taskMode == TaskMode.View) {
                        buildAnnotatedString {
                            append("%d:%02d".format(activeTimeStart.hour, activeTimeStart.minute))
                            appendLine()
                            append("%d:%02d".format(activeTimeEnd.hour, activeTimeEnd.minute))
                        }
                    } else {
                        buildAnnotatedString {
                            append("%d:%02d".format(taskClockTime.hour, taskClockTime.minute))
                        }
                    }

                    Text(
                        text = label,
                        modifier = Modifier
                            .align(Alignment.Center),
                        color = Color.White,
                        fontSize = if (taskMode != TaskMode.View) 18.sp else 14.sp,
                        fontWeight = if (taskMode != TaskMode.View) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            Column (
                modifier = Modifier
                    .weight(2f),
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                    modifier = Modifier
                        .alpha(scaleDownButtonAlpha)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.scale_down_svgrepo_com),
                        contentDescription = "Scale down",
                        modifier = Modifier.fillMaxSize(1F),
//                    tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Canvas (
            modifier = Modifier
//                .background(Color(0xffFBE7FE))// TODO: Save the color to a theme
                .fillMaxSize()
                .onGloballyPositioned {
                    width = it.size.width
                    height = it.size.height
                    center = Offset(width / 2f, height / 2f)
                    // The radius of the dial
                    outerRadius = min(width.toFloat(), height.toFloat()) / 2f * 0.9f
                    // The radius from the center to the clock steps
                    innerRadius = outerRadius * 0.8f
                    // The radius of the clock center (the one that displays time)
                    centerRadius = outerRadius * 0.3f
                }
                .clipToBounds()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x * scale,
                    translationY = offset.y * scale
                )
                .pointerInput(Unit) {
                    detectTransformGestures(
                        onGesture = { centroid, pan, zoom, _ ->
                            Log.i("TaskDial", "onGesture")
                            if (scale > 1f) {
                                offset += pan
                            }
                            scale = (scale * zoom).coerceIn(1f, 10f)
                        }
                    )
                }
                .pointerInput(dayState, taskUiState, userInput) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
//                            Log.i("PlannerDial", "detectDragGesturesAfterLongPress onDragStart")

                            // Get the starting coordinates and determine if the touch is:
                            // 1. within the dial
                            // 2. within any existing task area
                            val distance = TouchGestureUtils.distance(offset, center)
                            angle = TouchGestureUtils.angle(center, offset)

                            // Clear the task UI state
                            selectTask(null)

                            // If the touch is within a task area, we will want to drag that task along the dial. Therefore, the mode will be changed to EDIT_TIME_START.
                            touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                distance,
                                centerRadius,
                                innerRadius,
                                touchStroke
                            )

                            // Check if touch is near the dial edge
                            touchNearTheDialEdge =
                                TouchGestureUtils.checkIfTouchNearDialEdge(
                                    distance,
                                    innerRadius,
                                    outerRadius,
                                    touchStroke
                                )

                            if (touchInsideTheDial || touchNearTheDialEdge) {
                                touchWithinTaskArea =
                                    checkIfTouchWithinTasks(
                                        angle,
                                        tasks
                                    )

                                // If the long press was within a task, switch to the edit-the-task's-start-time mode
                                if (touchWithinTaskArea) {
                                    taskMode = TaskMode.EditStartTime

                                    tmpStartAngle = TouchGestureUtils.calculateAngleFromTime(
                                        activeTimeStart,
                                        touchedTask!!.startTime!!,
                                        minuteAngle
                                    )

                                    tmpEndAngle = TouchGestureUtils.calculateAngleFromTime(
                                        activeTimeStart,
                                        touchedTask!!.endTime!!,
                                        minuteAngle
                                    )

                                    draggedTaskDuration =
                                        TouchGestureUtils.calculateTotalNumberOfMinutes(
                                            touchedTask!!.startTime!!,
                                            touchedTask!!.endTime!!
                                        )

                                    // Display the task's start time on the task clock
                                    taskClockTime = TouchGestureUtils.calculateTimeFromAngle(
                                        angle = tmpStartAngle,
                                        clockStart = activeTimeStart,
                                        minuteAngle = minuteAngle
                                    )
                                } else {
                                    taskMode = TaskMode.Create

                                    // Calculate the time represented by the angle to display it in the clock center
                                    taskClockTime = TouchGestureUtils.calculateTimeFromAngle(
                                        angle = angle,
                                        clockStart = activeTimeStart,
                                        minuteAngle = minuteAngle
                                    )

                                    startAngle = angle
                                    endAngle = angle
                                    tmpStartAngle = angle
                                    tmpEndAngle = angle

                                    val currentAngleTranslated =
                                        TouchGestureUtils.translateAngle270To0(angle)

                                    initialAngleTranslated = currentAngleTranslated

                                    // Find the next and previous tasks, if any
                                    for (task in tasks) {
                                        val taskEndAngle = TouchGestureUtils.calculateAngleFromTime(
                                            activeTimeStart,
                                            task.endTime!!,
                                            minuteAngle
                                        )
                                        val taskEndAngleTranslated =
                                            TouchGestureUtils.translateAngle270To0(taskEndAngle)

                                        if (taskEndAngleTranslated < currentAngleTranslated) {
                                            previousTask = task.copy()
                                        } else {
                                            if (nextTask == null) {
                                                nextTask = task.copy()
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
//                            Log.i("PlannerDial", "detectDragGesturesAfterLongPress onDrag")
                            // If touch is within dial, keep track of the coordinates
                            val offset = change.position
                            val distance = TouchGestureUtils.distance(offset, center)

                            touchNearTheDialEdge =
                                TouchGestureUtils.checkIfTouchNearDialEdge(
                                    distance,
                                    innerRadius,
                                    outerRadius,
                                    touchStroke
                                )
                            touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                distance,
                                centerRadius,
                                innerRadius,
                                touchStroke
                            )

                            if (touchInsideTheDial || touchNearTheDialEdge) {
                                // Determine the direction of the drag
                                val currentAngle = TouchGestureUtils.angle(center, offset)
                                angle = currentAngle
                                val currentAngleTranslated =
                                    TouchGestureUtils.translateAngle270To0(currentAngle)
                                val startAngleTranslated =
                                    TouchGestureUtils.translateAngle270To0(startAngle)

                                angleMode =
                                    if (currentAngleTranslated < startAngleTranslated) {
                                        AngleMode.Start
                                    } else {
                                        AngleMode.End
                                    }

                                // If we're creating a new task
                                if (taskMode == TaskMode.Create) {
                                    drawNewTaskTimeRange = true

                                    if (angleMode == AngleMode.Start) {
                                        if (previousTask != null) {
                                            val previousTaskEndAngle =
                                                TouchGestureUtils.calculateAngleFromTime(
                                                    activeTimeStart,
                                                    previousTask!!.endTime!!,
                                                    minuteAngle
                                                )
                                            val previousTaskEndAngleTranslated =
                                                TouchGestureUtils.translateAngle270To0(
                                                    previousTaskEndAngle
                                                )

                                            if (currentAngleTranslated > previousTaskEndAngleTranslated) {
                                                tmpStartAngle = currentAngle
                                                tmpEndAngle = startAngle
                                            }
                                        } else {
                                            tmpStartAngle = currentAngle
                                            tmpEndAngle = startAngle
                                        }
                                    } else if (angleMode == AngleMode.End) {
                                        if (nextTask != null) {
                                            val nextTaskStartAngle =
                                                TouchGestureUtils.calculateAngleFromTime(
                                                    activeTimeStart,
                                                    nextTask!!.startTime!!,
                                                    minuteAngle
                                                )
                                            val nextTaskStartAngleTranslated =
                                                TouchGestureUtils.translateAngle270To0(
                                                    nextTaskStartAngle
                                                )

                                            if (currentAngleTranslated < nextTaskStartAngleTranslated) {
                                                tmpEndAngle = currentAngle
                                                tmpStartAngle = startAngle
                                            }
                                        } else {
                                            tmpEndAngle = currentAngle
                                            tmpStartAngle = startAngle
                                        }
                                    }

                                    // Calculate the time represented by the angle to display it in the clock center
                                    taskClockTime = TouchGestureUtils.calculateTimeFromAngle(
                                        angle = angle,
                                        clockStart = activeTimeStart,
                                        minuteAngle = minuteAngle
                                    )
                                }
                                // This code is not executed. After long-pressing on a task area the drag is not detected by the .detectDragGesturesAfterLongPress() - for some reason the finger has to be lifted and then the drag gestures are detected by the .detectDragGestures() instead
                                else if (taskMode == TaskMode.EditStartTime) {
                                }
                            }
                        },
                        onDragEnd = {
//                            Log.i("PlannerDial", "detectDragGesturesAfterLongPress onDragEnd")
                            when (taskMode) {
                                TaskMode.Create -> {
                                    // The number of minutes from start active start time
                                    val clockTaskStartTime =
                                        TouchGestureUtils.calculateTimeFromAngle(
                                            angle = tmpStartAngle,
                                            clockStart = activeTimeStart,
                                            minuteAngle = minuteAngle
                                        )
                                    val clockTaskEndTime = TouchGestureUtils.calculateTimeFromAngle(
                                        angle = tmpEndAngle,
                                        clockStart = activeTimeStart,
                                        minuteAngle = minuteAngle
                                    )

                                    setTaskStartTime(clockTaskStartTime)
                                    setTaskEndTime(clockTaskEndTime)
                                    setTaskDate(userInput.selectedDate)

                                    angleMode = AngleMode.None
                                }
                                // This code is not executed. After long-pressing on a task area the drag is not detected by the .detectDragGesturesAfterLongPress() - for some reason the finger has to be lifted and then the drag gestures are detected by the .detectDragGestures() instead
                                TaskMode.EditStartTime -> {
                                    // Calculate the time represented by the angle
                                    val taskNewStartTime = TouchGestureUtils.calculateTimeFromAngle(
                                        angle = tmpStartAngle,
                                        clockStart = activeTimeStart,
                                        minuteAngle = minuteAngle
                                    )
                                    val taskNewEndTime = TouchGestureUtils.calculateTimeFromAngle(
                                        angle = tmpEndAngle,
                                        clockStart = activeTimeStart,
                                        minuteAngle = minuteAngle
                                    )

                                    // Update the task's start- and end times
                                    setTaskStartTime(taskNewStartTime)
                                    setTaskEndTime(taskNewEndTime)
                                    saveTaskFromState()
                                    reset()
                                }

                                else -> {
                                    reset()
                                }
                            }
                        }
                    )
                }
                .pointerInput(dayState, taskUiState, userInput) {
                    detectTapGestures(
                        onTap = { offset ->
//                            Log.i("PlannerDial", "detectTapGestures onTap")
                            val distance = TouchGestureUtils.distance(offset, center)
                            touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                distance,
                                centerRadius,
                                innerRadius,
                                touchStroke
                            )
                            angle = TouchGestureUtils.angle(center, offset)

                            if (touchInsideTheDial) {
                                if (taskMode == TaskMode.View) {
                                    // Check whether the touch is within the ANY task area
                                    touchWithinTaskArea =
                                        checkIfTouchWithinTasks(
                                            angle,
                                            tasks
                                        )

                                    if (touchWithinTaskArea) {
                                        // Show task info
                                        showPopupWindow = true
                                    }
                                } else if (taskMode == TaskMode.EditTimeRange || taskMode == TaskMode.EditStartTime) {
                                    // Check whether the touch is within the GIVEN task area
                                    touchWithinTaskArea =
                                        TouchGestureUtils.checkIfTouchWithinAngleRange(
                                            angle,
                                            tmpStartAngle,
                                            tmpEndAngle
                                        )

                                    if (touchWithinTaskArea) {
                                        // Start angle carries now the supposedly new value for start time of the given task, the end angle the value for the end time, respectively. Update the task:
                                        // - start time
                                        val clockTaskStartTime =
                                            TouchGestureUtils.calculateTimeFromAngle(
                                                angle = tmpStartAngle,
                                                clockStart = activeTimeStart,
                                                minuteAngle = minuteAngle
                                            )
                                        // - end time
                                        val clockTaskEndTime =
                                            TouchGestureUtils.calculateTimeFromAngle(
                                                angle = tmpEndAngle,
                                                clockStart = activeTimeStart,
                                                minuteAngle = minuteAngle
                                            )

                                        setTaskStartTime(clockTaskStartTime)
                                        setTaskEndTime(clockTaskEndTime)
                                        // Set the task's date
                                        setTaskDate(userInput.selectedDate)
                                        saveTaskFromState()

                                        // Reset the dial
                                        reset()
                                    }
                                } else if (taskMode == TaskMode.Create) {
                                    // Check whether the touch is within the new task area
                                    touchWithinTaskArea =
                                        TouchGestureUtils.checkIfTouchWithinTaskArea(
                                            angle,
                                            clockStart = activeTimeStart,
                                            minuteAngle = minuteAngle,
                                            taskStart = taskUiState.startTime!!,
                                            taskEnd = taskUiState.endTime!!
                                        )

                                    if (touchWithinTaskArea) {
                                        // Show task creation screen
                                        popupState = TaskModePopup.Edit
                                        showPopupWindow = true
                                    } else {
                                        reset()
                                    }
                                }
                            } else {
                                // Cancel everything if touch is outside the dial
                                reset()
                            }
                        },
                        onDoubleTap = { offset ->
//                            Log.i("PlannerDial", "detectTapGestures onDoubleTap")
                            val distance = TouchGestureUtils.distance(offset, center)
                            touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                distance,
                                centerRadius,
                                innerRadius,
                                touchStroke
                            )
                            angle = TouchGestureUtils.angle(center, offset)

                            if (touchInsideTheDial) {
                                // Check if touch is within ANY task area
                                touchWithinTaskArea =
                                    checkIfTouchWithinTasks(
                                        angle,
                                        tasks
                                    )

                                if (touchWithinTaskArea) {
                                    taskMode = TaskMode.EditTimeRange
                                    val taskStartAngle = TouchGestureUtils.calculateAngleFromTime(
                                        activeTimeStart,
                                        touchedTask!!.startTime!!,
                                        minuteAngle
                                    )
                                    val taskEndAngle = TouchGestureUtils.calculateAngleFromTime(
                                        activeTimeStart,
                                        touchedTask!!.endTime!!,
                                        minuteAngle
                                    )
                                    tmpStartAngle = taskStartAngle
                                    tmpEndAngle = taskEndAngle
                                }
                            } else {
                                // Cancel everything if touch is outside the dial
                                reset()
                            }
                        },
                    )
                }
                .pointerInput(dayState, taskUiState, userInput) {
                    detectDragGestures(
                        onDragStart = { offset ->
//                            Log.i("PlannerDial", "detectDragGestures onDragStart")
                            // Get the starting coordinates and determine if the touch is:
                            // 1. within the dial
                            // 2. within any existing task area
                            val distance = TouchGestureUtils.distance(offset, center)
                            touchNearTheDialEdge = TouchGestureUtils.checkIfTouchNearDialEdge(
                                distance,
                                innerRadius,
                                outerRadius,
                                touchStroke
                            )
                            touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                distance,
                                centerRadius,
                                innerRadius,
                                touchStroke
                            )

                            if (touchNearTheDialEdge || touchInsideTheDial) {
                                val currentAngle = TouchGestureUtils.angle(center, offset)
                                angle = currentAngle
                            }
                        },
                        onDrag = { change, dragAmount ->
//                            Log.i("PlannerDial", "detectDragGestures onDrag")
                            if (taskMode == TaskMode.Create || taskMode == TaskMode.EditTimeRange) {
                                val offset = change.position
                                val distance = TouchGestureUtils.distance(offset, center)
                                touchNearTheDialEdge =
                                    TouchGestureUtils.checkIfTouchNearDialEdge(
                                        distance,
                                        innerRadius,
                                        outerRadius,
                                        touchStroke
                                    )
                                touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                    distance,
                                    centerRadius,
                                    innerRadius,
                                    touchStroke
                                )

                                if (touchNearTheDialEdge || touchInsideTheDial) {
                                    val currentAngle = TouchGestureUtils.angle(center, offset)
                                    angle = currentAngle
                                    val currentAngleTranslated =
                                        TouchGestureUtils.translateAngle270To0(currentAngle)

                                    // If we don't know which time boundary of the given task we are setting
                                    if (angleMode == AngleMode.None) {
                                        // Determine which time boundary of the given task we are setting: start or end time
                                        // It seems that an angle == tmpStartAngle comparison does not cut it - the app is not able to detect the moment when the two angles are equal
//                                        if (angle in (tmpStartAngle - touchStroke / 2f)..(tmpStartAngle + touchStroke / 2f)) {
//                                        if (angle in (tmpStartAngle - touchStroke / 4f)..(tmpStartAngle + touchStroke / 2f)) {
                                        if (currentAngleTranslated in (TouchGestureUtils.translateAngle270To0(
                                                tmpStartAngle
                                            ) - touchStroke / 4f)..(TouchGestureUtils.translateAngle270To0(
                                                tmpStartAngle
                                            ) + touchStroke / 4f)
                                        ) {
                                            angleMode = AngleMode.Start
                                            initialAngleTranslated =
                                                TouchGestureUtils.translateAngle270To0(
                                                    TouchGestureUtils.calculateAngleFromTime(
                                                        activeTimeStart,
                                                        // We're using the end time angle as a boundary
                                                        taskUiState.endTime!!,
                                                        minuteAngle
                                                    )
                                                )
//                                        } else if (angle in (tmpEndAngle - touchStroke / 2f)..(tmpEndAngle + touchStroke / 2f)) {
//                                        } else if (angle in (tmpEndAngle - touchStroke / 4f)..(tmpEndAngle + touchStroke / 2f)) {
                                        } else if (currentAngleTranslated in (TouchGestureUtils.translateAngle270To0(
                                                tmpEndAngle
                                            ) - touchStroke / 4f)..(TouchGestureUtils.translateAngle270To0(
                                                tmpEndAngle
                                            ) + touchStroke / 4f)
                                        ) {
                                            angleMode = AngleMode.End
                                            initialAngleTranslated =
                                                TouchGestureUtils.translateAngle270To0(
                                                    TouchGestureUtils.calculateAngleFromTime(
                                                        activeTimeStart,
                                                        // We're using the start time angle as a boundary
                                                        taskUiState.startTime!!,
                                                        minuteAngle
                                                    )
                                                )
                                        }
                                    }
                                    // If we do know which time boundary of the given task we are setting
                                    else {
                                        // Show the clock time the angle corresponds to
                                        taskClockTime = TouchGestureUtils.calculateTimeFromAngle(
                                            angle = angle,
                                            clockStart = activeTimeStart,
                                            minuteAngle = minuteAngle
                                        )

                                        // If we are setting the start time of the given task
                                        if (angleMode == AngleMode.Start) {
                                            // If the current angle is smaller than the angle of the task's end time
                                            if (currentAngleTranslated < initialAngleTranslated) {
                                                // If there is a previous task
                                                if (previousTask != null) {
                                                    val previousTaskEndAngle =
                                                        TouchGestureUtils.calculateAngleFromTime(
                                                            activeTimeStart,
                                                            previousTask!!.endTime!!,
                                                            minuteAngle
                                                        )
                                                    val previousTaskEndAngleTranslated =
                                                        TouchGestureUtils.translateAngle270To0(
                                                            previousTaskEndAngle
                                                        )
                                                    // If the angle does not cross the previous task's end time
                                                    if (currentAngleTranslated > previousTaskEndAngleTranslated) {
                                                        startAngle = angle
                                                        tmpStartAngle = startAngle
                                                    }
                                                }
                                                // Else, if there is no previous task
                                                else {
                                                    startAngle = angle
                                                    tmpStartAngle = startAngle
                                                }
                                            }
                                        }
                                        // If we are setting the end time of the given task
                                        else if (angleMode == AngleMode.End) {
                                            // If the current angle is greater than the angle of the task's start time
                                            if (currentAngleTranslated > initialAngleTranslated) {
                                                if (nextTask != null) {
                                                    val nextTaskStartAngle =
                                                        TouchGestureUtils.calculateAngleFromTime(
                                                            activeTimeStart,
                                                            nextTask!!.startTime!!,
                                                            minuteAngle
                                                        )
                                                    val nextTaskStartAngleTranslated =
                                                        TouchGestureUtils.translateAngle270To0(
                                                            nextTaskStartAngle
                                                        )
                                                    // If the angle does not cross the previous task's end time
                                                    if (currentAngleTranslated < nextTaskStartAngleTranslated) {
                                                        endAngle = angle
                                                        tmpEndAngle = endAngle
                                                    }
                                                } else {
                                                    endAngle = angle
                                                    tmpEndAngle = endAngle
                                                }
                                            }
                                        }

                                        // Ask whether the task should be deleted
                                        if (currentAngleTranslated in initialAngleTranslated - minuteAngle..initialAngleTranslated + minuteAngle) {
                                            popupState = TaskModePopup.Delete
                                            showPopupWindow = true
                                        }
                                    }

                                }
                            }
                            // This code is executed instead of the onDrag() in .detectDragGesturesAfterLongPress() - for some reason the finger has to be lifted after a long-press and the drag gestures are then detected by the .detectDragGestures()
                            else if (taskMode == TaskMode.EditStartTime) {
                                val currentAngle = TouchGestureUtils.angle(center, change.position)
                                val previousAngle =
                                    TouchGestureUtils.angle(center, change.previousPosition)
                                val currentAngleTranslated =
                                    TouchGestureUtils.translateAngle270To0(currentAngle)
                                val previousAngleTranslated =
                                    TouchGestureUtils.translateAngle270To0(previousAngle)

                                dragDirection =
                                    if (currentAngleTranslated > previousAngleTranslated) {
                                        DragDirection.Forward
                                    } else {
                                        DragDirection.Backward
                                    }

                                var touchedTaskNewStartAngle =
                                    tmpStartAngle + currentAngle - previousAngle
                                var touchedTaskNewEndAngle =
                                    tmpStartAngle + (draggedTaskDuration * minuteAngle)

                                // Correct the angle if its value exceeds 360 degrees
                                if (touchedTaskNewStartAngle > 360f) touchedTaskNewStartAngle -= 360f
                                if (touchedTaskNewEndAngle > 360f) touchedTaskNewEndAngle -= 360f

                                val touchedTaskNewStartAngleTranslated =
                                    TouchGestureUtils.translateAngle270To0(touchedTaskNewStartAngle)
                                val touchedTaskNewEndAngleTranslated =
                                    TouchGestureUtils.translateAngle270To0(touchedTaskNewEndAngle)

                                // If the task's end time doesn't pass the 0/360 degree mark, move the task
                                if (touchedTaskNewEndAngleTranslated < 360f && touchedTaskNewStartAngleTranslated >= 0) {
                                    if (touchedTaskNewEndAngleTranslated > touchedTaskNewStartAngleTranslated) {
                                        tmpStartAngle = touchedTaskNewStartAngle
                                        tmpEndAngle = touchedTaskNewEndAngle

                                        // Update the selected task with new start- and end time - These values are not updated on time during task swapping but they are updated on time before onDragEnd()
                                        tasks.find { task -> task.id == touchedTask!!.id }?.apply {
                                            startTime = TouchGestureUtils.calculateTimeFromAngle(
                                                tmpStartAngle,
                                                minuteAngle,
                                                activeTimeStart
                                            )
                                            endTime = TouchGestureUtils.calculateTimeFromAngle(
                                                tmpEndAngle,
                                                minuteAngle,
                                                activeTimeStart
                                            )
                                        }

                                        // TASK SWAPPING
                                        // Check if the middle angle of the dragged task reaches the start time of an adjacent task. If it does, the later task should get the start time of the earlier task and the earlier task should get the first start time available after the later task.
                                        var touchedTaskMiddleAngle =
                                            touchedTaskNewStartAngle + ((draggedTaskDuration * minuteAngle) * 0.5f)
                                        // Correct the middle angle if due to constant addition its value exceeds 360 degrees
                                        if (touchedTaskMiddleAngle > 360f) touchedTaskMiddleAngle -= 360f

                                        val touchedTaskMiddleAngleTranslated =
                                            TouchGestureUtils.translateAngle270To0(
                                                touchedTaskMiddleAngle
                                            )

//                                        // If we're moving forward, we may cross the next task's start time angle
//                                        if (dragDirection == DragDirection.Forward) {
//                                            // Find the task the touched task overlaps (if any)
//                                            for ((index, task) in tasks.withIndex()) {
//                                                if (task.id != touchedTask!!.id) {
//                                                    val taskStartAngle =
//                                                        TouchGestureUtils.calculateAngleFromTime(
//                                                            activeTimeStart = activeTimeStart,
//                                                            time = task.startTime!!,
//                                                            minuteAngle = minuteAngle
//                                                        )
//                                                    val taskStartAngleTranslated =
//                                                        TouchGestureUtils.translateAngle270To0(
//                                                            taskStartAngle
//                                                        )
//                                                    val taskEndAngle =
//                                                        TouchGestureUtils.calculateAngleFromTime(
//                                                            activeTimeStart = activeTimeStart,
//                                                            time = task.endTime!!,
//                                                            minuteAngle = minuteAngle
//                                                        )
//                                                    val taskEndAngleTranslated =
//                                                        TouchGestureUtils.translateAngle270To0(
//                                                            taskEndAngle
//                                                        )
//                                                    val duration = TouchGestureUtils.calculateTotalNumberOfMinutes(
//                                                        task.startTime!!,
//                                                        task.endTime!!
//                                                    )
//                                                    var taskMiddleAngle =
//                                                        taskStartAngle + ((duration * minuteAngle) * 0.5f)
//
//                                                    // Correct the middle angle if due to constant addition its value exceeds 360 degrees
//                                                    if (taskMiddleAngle > 360f) taskMiddleAngle -= 360f
//
//                                                    val taskMiddleAngleTranslated =
//                                                        TouchGestureUtils.translateAngle270To0(
//                                                            taskMiddleAngle
//                                                        )
//
//                                                    // If the tasks overlap - if the dragged task is within the neighbouring task or the neighbouring task is within the dragged task
//                                                    if (touchedTaskNewEndAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated ||
//                                                        touchedTaskNewStartAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated ||
//                                                        taskStartAngleTranslated in touchedTaskNewStartAngleTranslated..touchedTaskNewEndAngleTranslated ||
//                                                        taskEndAngleTranslated in touchedTaskNewStartAngleTranslated..touchedTaskNewEndAngleTranslated) {
//                                                        val overlappedTaskIndex = index
//                                                        val angleChange = currentAngle - previousAngle
//
//                                                        // If at least half of the dragged task overlaps with the next task
//                                                        if (touchedTaskMiddleAngleTranslated > taskStartAngleTranslated || taskMiddleAngleTranslated < touchedTaskNewEndAngleTranslated) {
//                                                            // Start moving the overlapped task backwards as the dragged task moves forward until the two tasks stop overlapping/the overlapped task cannot be moved further
//                                                            moveTaskBackward(
//                                                                overlappedTaskIndex,
//                                                                angleChange
//                                                            )
//
//                                                            // Set the jump-to-position to just after the next task (should the dragging end while the tasks overlap)
//                                                            jumpToStartAngle =
//                                                                taskEndAngle + minuteAngle
//                                                            jumpToEndAngle =
//                                                                jumpToStartAngle!! + (draggedTaskDuration * minuteAngle)
//
//                                                            if (jumpToStartAngle!! > 360f) jumpToStartAngle =
//                                                                jumpToStartAngle!! - 360f
//                                                            if (jumpToEndAngle!! > 360f) jumpToEndAngle =
//                                                                jumpToEndAngle!! - 360f
//                                                        }
//                                                        // If the dragged task overlaps with the next task only slightly
//                                                        else {
//                                                            // Set the jump-to-position to just before the next task (should the dragging end while the tasks overlap)
//                                                            jumpToEndAngle =
//                                                                taskStartAngle - minuteAngle
//                                                            jumpToStartAngle =
//                                                                jumpToEndAngle!! - (draggedTaskDuration * minuteAngle)
//
//                                                            if (jumpToEndAngle!! < 0f) jumpToEndAngle =
//                                                                jumpToEndAngle!! + 360f
//                                                            if (jumpToStartAngle!! < 0f) jumpToStartAngle =
//                                                                jumpToStartAngle!! + 360f
//                                                        }
//
//                                                        break
//                                                    }
//                                                    // If there is no overlap, clear the jump-to-position values
//                                                    else {
//                                                        jumpToStartAngle = null
//                                                        jumpToEndAngle = null
//                                                    }
//                                                }
//                                            }
//                                        }
//                                        else if (dragDirection == DragDirection.Backward) {
//                                            // Find the task the touched task overlaps (if any)
//                                            for ((index, task) in tasks.withIndex()) {
//                                                if (task.id != touchedTask!!.id) {
//                                                    val taskStartAngle =
//                                                        TouchGestureUtils.calculateAngleFromTime(
//                                                            activeTimeStart = activeTimeStart,
//                                                            time = task.startTime!!,
//                                                            minuteAngle = minuteAngle
//                                                        )
//                                                    val taskStartAngleTranslated =
//                                                        TouchGestureUtils.translateAngle270To0(
//                                                            taskStartAngle
//                                                        )
//                                                    val taskEndAngle =
//                                                        TouchGestureUtils.calculateAngleFromTime(
//                                                            activeTimeStart = activeTimeStart,
//                                                            time = task.endTime!!,
//                                                            minuteAngle = minuteAngle
//                                                        )
//                                                    val taskEndAngleTranslated =
//                                                        TouchGestureUtils.translateAngle270To0(
//                                                            taskEndAngle
//                                                        )
//                                                    val duration = TouchGestureUtils.calculateTotalNumberOfMinutes(
//                                                        task.startTime!!,
//                                                        task.endTime!!
//                                                    )
//                                                    var taskMiddleAngle =
//                                                        taskStartAngle + ((duration * minuteAngle) * 0.5f)
//
//                                                    // Correct the middle angle if due to constant addition its value exceeds 360 degrees
//                                                    if (taskMiddleAngle > 360f) taskMiddleAngle -= 360f
//
//                                                    val taskMiddleAngleTranslated =
//                                                        TouchGestureUtils.translateAngle270To0(
//                                                            taskMiddleAngle
//                                                        )
//
//                                                    // If the tasks overlap - if the dragged task is within the neighbouring task or the neighbouring task is within the dragged task
//                                                    if (touchedTaskNewEndAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated ||
//                                                        touchedTaskNewStartAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated ||
//                                                        taskStartAngleTranslated in touchedTaskNewStartAngleTranslated..touchedTaskNewEndAngleTranslated ||
//                                                        taskEndAngleTranslated in touchedTaskNewStartAngleTranslated..touchedTaskNewEndAngleTranslated) {
//                                                        val overlappedTaskIndex = index
//                                                        val angleChange = currentAngle - previousAngle
//
//                                                        // If at least half of the dragged task overlaps with the previous task
//                                                        if (touchedTaskMiddleAngleTranslated < taskEndAngleTranslated || taskMiddleAngleTranslated > touchedTaskNewStartAngleTranslated) {
//                                                            Log.i("PlannerDial", "touchedTaskMiddleAngleTranslated $touchedTaskMiddleAngleTranslated")
//                                                            // Start moving the overlapped task backwards as the dragged task moves forward until the two tasks stop overlapping/the overlapped task cannot be moved further
////                                                            val angleChange = -(currentAngle - previousAngle)
//                                                            moveTaskForward(overlappedTaskIndex, angleChange)
//
//                                                            // Set the jump-to-position to just after the next task (should the dragging end while the tasks overlap)
//                                                            jumpToEndAngle = taskStartAngle - minuteAngle
//                                                            jumpToStartAngle = jumpToEndAngle!! - (draggedTaskDuration * minuteAngle)
//
//                                                            if (jumpToStartAngle!! < 0f) jumpToStartAngle = jumpToStartAngle!! + 360f
//                                                            if (jumpToEndAngle!! < 0f) jumpToEndAngle = jumpToEndAngle!! + 360f
//                                                        }
//                                                        // If the dragged task overlaps with the next task only slightly
//                                                        else {
//                                                            // Set the jump-to-position to just after the previous task (should the dragging end while the tasks overlap)
//                                                            jumpToStartAngle =
//                                                                taskEndAngle + minuteAngle
//                                                            jumpToEndAngle =
//                                                                jumpToStartAngle!! + (draggedTaskDuration * minuteAngle)
//
//                                                            if (jumpToEndAngle!! > 360f) jumpToEndAngle =
//                                                                jumpToEndAngle!! - 360f
//                                                            if (jumpToStartAngle!! > 360f) jumpToStartAngle =
//                                                                jumpToStartAngle!! - 360f
//                                                        }
//
//                                                        break
//                                                    }
//                                                    // If there is no overlap, clear the jump-to-position values
//                                                    else {
//                                                        jumpToStartAngle = null
//                                                        jumpToEndAngle = null
//                                                    }
//                                                }
//                                            }
//                                        }
// ----------------------------------------
                                        // Find the task the touched task overlaps (if any)
                                        for ((index, task) in tasks.withIndex()) {
                                            if (task.id != touchedTask!!.id) {
                                                val taskStartAngle =
                                                    TouchGestureUtils.calculateAngleFromTime(
                                                        activeTimeStart = activeTimeStart,
                                                        time = task.startTime!!,
                                                        minuteAngle = minuteAngle
                                                    )
                                                val taskStartAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        taskStartAngle
                                                    )
                                                val taskEndAngle =
                                                    TouchGestureUtils.calculateAngleFromTime(
                                                        activeTimeStart = activeTimeStart,
                                                        time = task.endTime!!,
                                                        minuteAngle = minuteAngle
                                                    )
                                                val taskEndAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        taskEndAngle
                                                    )
                                                val duration =
                                                    TouchGestureUtils.calculateTotalNumberOfMinutes(
                                                        task.startTime!!,
                                                        task.endTime!!
                                                    )
                                                var taskMiddleAngle =
                                                    taskStartAngle + ((duration * minuteAngle) * 0.5f)

                                                // Correct the middle angle if due to constant addition its value exceeds 360 degrees
                                                if (taskMiddleAngle > 360f) taskMiddleAngle -= 360f

                                                val taskMiddleAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        taskMiddleAngle
                                                    )

                                                // If the tasks overlap - if the dragged task is within the neighbouring task or the neighbouring task is within the dragged task
                                                if (touchedTaskNewEndAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                    || touchedTaskNewStartAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                    || taskStartAngleTranslated in touchedTaskNewStartAngleTranslated..touchedTaskNewEndAngleTranslated
                                                    || taskEndAngleTranslated in touchedTaskNewStartAngleTranslated..touchedTaskNewEndAngleTranslated
                                                ) {
                                                    val overlappedTaskIndex = index
                                                    val angleChange = currentAngle - previousAngle

//                                                    Log.i("PlannerDial", "dragDirection $dragDirection")

                                                    // If we're moving forward, we may cross the next task's start time angle
                                                    if (dragDirection == DragDirection.Forward) {
                                                        // If at least half of the dragged task overlaps with the next task
                                                        if (touchedTaskMiddleAngleTranslated > taskStartAngleTranslated
                                                            || taskMiddleAngleTranslated < touchedTaskNewEndAngleTranslated
                                                        ) {
                                                            // Start moving the overlapped task backwards as the dragged task moves forward until the two tasks stop overlapping/the overlapped task cannot be moved further
                                                            moveTaskBackward(
                                                                overlappedTaskIndex,
                                                                angleChange
                                                            )

                                                            // Set the jump-to-position to just after the next task (should the dragging end while the tasks overlap)
                                                            jumpToStartAngle =
                                                                taskEndAngle + minuteAngle
                                                            jumpToEndAngle =
                                                                jumpToStartAngle!! + (draggedTaskDuration * minuteAngle)

                                                            if (jumpToStartAngle!! > 360f) jumpToStartAngle =
                                                                jumpToStartAngle!! - 360f
                                                            if (jumpToEndAngle!! > 360f) jumpToEndAngle =
                                                                jumpToEndAngle!! - 360f
                                                        }
                                                        // If the dragged task overlaps with the next task only slightly
                                                        else {
                                                            // Set the jump-to-position to just before the next task (should the dragging end while the tasks overlap)
                                                            jumpToEndAngle =
                                                                taskStartAngle - minuteAngle
                                                            jumpToStartAngle =
                                                                jumpToEndAngle!! - (draggedTaskDuration * minuteAngle)

                                                            if (jumpToEndAngle!! < 0f) jumpToEndAngle =
                                                                jumpToEndAngle!! + 360f
                                                            if (jumpToStartAngle!! < 0f) jumpToStartAngle =
                                                                jumpToStartAngle!! + 360f
                                                        }
                                                    } else if (dragDirection == DragDirection.Backward) {
                                                        // If at least half of the dragged task overlaps with the previous task
                                                        if (touchedTaskMiddleAngleTranslated < taskEndAngleTranslated
                                                            || taskMiddleAngleTranslated > touchedTaskNewStartAngleTranslated
                                                        ) {
//                                                            Log.i("PlannerDial", "touchedTaskMiddleAngleTranslated $touchedTaskMiddleAngleTranslated")
                                                            // Start moving the overlapped task backwards as the dragged task moves forward until the two tasks stop overlapping/the overlapped task cannot be moved further
//                                                            val angleChange = -(currentAngle - previousAngle)
                                                            moveTaskForward(
                                                                overlappedTaskIndex,
                                                                angleChange
                                                            )

                                                            // Set the jump-to-position to just after the next task (should the dragging end while the tasks overlap)
                                                            jumpToEndAngle =
                                                                taskStartAngle - minuteAngle
                                                            jumpToStartAngle =
                                                                jumpToEndAngle!! - (draggedTaskDuration * minuteAngle)

                                                            if (jumpToStartAngle!! < 0f) jumpToStartAngle =
                                                                jumpToStartAngle!! + 360f
                                                            if (jumpToEndAngle!! < 0f) jumpToEndAngle =
                                                                jumpToEndAngle!! + 360f
                                                        }
                                                        // If the dragged task overlaps with the next task only slightly
                                                        else {
                                                            // Set the jump-to-position to just after the previous task (should the dragging end while the tasks overlap)
                                                            jumpToStartAngle =
                                                                taskEndAngle + minuteAngle
                                                            jumpToEndAngle =
                                                                jumpToStartAngle!! + (draggedTaskDuration * minuteAngle)

                                                            if (jumpToEndAngle!! > 360f) jumpToEndAngle =
                                                                jumpToEndAngle!! - 360f
                                                            if (jumpToStartAngle!! > 360f) jumpToStartAngle =
                                                                jumpToStartAngle!! - 360f
                                                        }
                                                    }

                                                    break
                                                }
                                                // If there is no overlap, clear the jump-to-position values
                                                else {
                                                    jumpToStartAngle = null
                                                    jumpToEndAngle = null
                                                }
                                            }
                                        }
                                    }
                                }

                                // Display the task's start time on the task clock
                                taskClockTime = TouchGestureUtils.calculateTimeFromAngle(
                                    angle = tmpStartAngle,
                                    clockStart = activeTimeStart,
                                    minuteAngle = minuteAngle
                                )
                            }
                        },
                        onDragEnd = {
                            Log.i("PlannerDial", "detectDragGestures onDragEnd")
                            if (taskMode == TaskMode.Create || taskMode == TaskMode.EditTimeRange) {
                                if (angleMode == AngleMode.Start) {
                                    val clockTaskStartTime =
                                        TouchGestureUtils.calculateTimeFromAngle(
                                            angle = tmpStartAngle,
                                            clockStart = activeTimeStart,
                                            minuteAngle = minuteAngle
                                        )

                                    setTaskStartTime(clockTaskStartTime)
                                } else if (angleMode == AngleMode.End) {
                                    val clockTaskEndTime = TouchGestureUtils.calculateTimeFromAngle(
                                        angle = tmpEndAngle,
                                        clockStart = activeTimeStart,
                                        minuteAngle = minuteAngle
                                    )

                                    setTaskEndTime(clockTaskEndTime)
                                }

                                // Reset the angle mode
                                angleMode = AngleMode.None
                                // Set new startAngle and endAngle values
                                startAngle = tmpStartAngle
                                endAngle = tmpEndAngle

                                setTaskDate(userInput.selectedDate)
                            } else if (taskMode == TaskMode.EditStartTime) {
                                Log.i("PlannerDial", "tasks $tasks")

//                                // Calculate the time represented by the angle
//                                val tmpStartAngleTranslated =
//                                    TouchGestureUtils.translateAngle270To0(tmpStartAngle)
//                                val tmpEndAngleTranslated =
//                                    TouchGestureUtils.translateAngle270To0(tmpEndAngle)

                                // FIXME: jump-to-positions overlap with other tasks
                                if (jumpToStartAngle != null && jumpToEndAngle != null) {
                                    // The indices of the tasks between which the dragged task should placed
                                    var taskBeforeIndex: Int? = null
                                    var taskAfterIndex: Int? = null

                                    var jumpToStartAngleTranslated = TouchGestureUtils.translateAngle270To0(jumpToStartAngle!!)
                                    var jumpToEndAngleTranslated = TouchGestureUtils.translateAngle270To0(jumpToEndAngle!!)

                                    // Check if the selected task would overlap another task if it was to jump to the previously determined position
                                    if (dragDirection == DragDirection.Forward) {
                                        // Find the last task the selected task overlaps with
                                        for ((index, task) in tasks.withIndex()) {
                                            if (task.id != touchedTask!!.id) {
                                                val taskStartAngle =
                                                    TouchGestureUtils.calculateAngleFromTime(
                                                        activeTimeStart = activeTimeStart,
                                                        time = task.startTime!!,
                                                        minuteAngle = minuteAngle
                                                    )
                                                val taskStartAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        taskStartAngle
                                                    )
                                                val taskEndAngle =
                                                    TouchGestureUtils.calculateAngleFromTime(
                                                        activeTimeStart = activeTimeStart,
                                                        time = task.endTime!!,
                                                        minuteAngle = minuteAngle
                                                    )
                                                val taskEndAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        taskEndAngle
                                                    )
                                                val duration =
                                                    TouchGestureUtils.calculateTotalNumberOfMinutes(
                                                        task.startTime!!,
                                                        task.endTime!!
                                                    )
                                                var taskMiddleAngle =
                                                    taskStartAngle + ((duration * minuteAngle) * 0.5f)

                                                // Correct the middle angle if due to constant addition its value exceeds 360 degrees
                                                if (taskMiddleAngle > 360f) taskMiddleAngle -= 360f

                                                val taskMiddleAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        taskMiddleAngle
                                                    )
                                                jumpToStartAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        jumpToStartAngle!!
                                                    )
                                                jumpToEndAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        jumpToEndAngle!!
                                                    )
                                                val jumpToMiddleAngleTranslated =
                                                    jumpToStartAngleTranslated + draggedTaskDuration * 0.5f

                                                // If the tasks overlap - if the dragged task would be within the neighbouring task or the neighbouring task is within the dragged task, we will want to move the dragged task so that it doesn't overlap another task
                                                if (jumpToStartAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                    || jumpToEndAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                    || taskStartAngleTranslated in jumpToStartAngleTranslated..jumpToEndAngleTranslated
                                                    || taskEndAngleTranslated in jumpToStartAngleTranslated..jumpToEndAngleTranslated
                                                ) {
                                                    taskAfterIndex = index
                                                }
                                                else {
                                                    if (taskAfterIndex != null) {
                                                        break
                                                    }
                                                }
                                            }
                                        }

                                        if (taskAfterIndex != null) {
                                            if (taskAfterIndex - 1 >= 0) {
                                                taskBeforeIndex = taskAfterIndex - 1
                                            }
                                        }
                                    }
                                    else if (dragDirection == DragDirection.Backward) {
                                        // Find the first task the selected task overlaps with
                                        for ((index, task) in tasks.withIndex()) {
                                            if (task.id != touchedTask!!.id) {
                                                val taskStartAngle =
                                                    TouchGestureUtils.calculateAngleFromTime(
                                                        activeTimeStart = activeTimeStart,
                                                        time = task.startTime!!,
                                                        minuteAngle = minuteAngle
                                                    )
                                                val taskStartAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        taskStartAngle
                                                    )
                                                val taskEndAngle =
                                                    TouchGestureUtils.calculateAngleFromTime(
                                                        activeTimeStart = activeTimeStart,
                                                        time = task.endTime!!,
                                                        minuteAngle = minuteAngle
                                                    )
                                                val taskEndAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        taskEndAngle
                                                    )
                                                val duration =
                                                    TouchGestureUtils.calculateTotalNumberOfMinutes(
                                                        task.startTime!!,
                                                        task.endTime!!
                                                    )
                                                var taskMiddleAngle =
                                                    taskStartAngle + ((duration * minuteAngle) * 0.5f)

                                                // Correct the middle angle if due to constant addition its value exceeds 360 degrees
                                                if (taskMiddleAngle > 360f) taskMiddleAngle -= 360f

                                                val taskMiddleAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        taskMiddleAngle
                                                    )
                                                jumpToStartAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        jumpToStartAngle!!
                                                    )
                                                jumpToEndAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        jumpToEndAngle!!
                                                    )
                                                val jumpToMiddleAngleTranslated =
                                                    jumpToStartAngleTranslated + draggedTaskDuration * 0.5f

                                                // If the tasks overlap - if the dragged task would be within the neighbouring task or the neighbouring task is within the dragged task, we will want to move the dragged task so that it doesn't overlap another task
                                                if (jumpToStartAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                    || jumpToEndAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                    || taskStartAngleTranslated in jumpToStartAngleTranslated..jumpToEndAngleTranslated
                                                    || taskEndAngleTranslated in jumpToStartAngleTranslated..jumpToEndAngleTranslated
                                                ) {
                                                    taskBeforeIndex = index
                                                    if (taskBeforeIndex + 1 < tasks.size) {
                                                        taskAfterIndex = taskBeforeIndex + 1
                                                    }
                                                    break
                                                }
                                            }
                                        }
                                    }

                                    // Check if there's enough space between the tasks to fit in the dragged task
                                    if (taskBeforeIndex != null && taskAfterIndex != null) {
                                        val taskBefore = tasks[taskBeforeIndex]
                                        val taskAfter = tasks[taskAfterIndex]

                                        Log.i("PlannerDial","taskBefore ${taskBefore}")
                                        Log.i("PlannerDial","taskAfter ${taskAfter}")

                                        val taskBeforeEndAngle =
                                            TouchGestureUtils.calculateAngleFromTime(
                                                activeTimeStart = activeTimeStart,
                                                time = taskBefore.endTime!!,
                                                minuteAngle = minuteAngle
                                            )
                                        val taskBeforeEndAngleTranslated =
                                            TouchGestureUtils.translateAngle270To0(
                                                taskBeforeEndAngle
                                            )
                                        val taskAfterStartAngle =
                                            TouchGestureUtils.calculateAngleFromTime(
                                                activeTimeStart = activeTimeStart,
                                                time = taskAfter.startTime!!,
                                                minuteAngle = minuteAngle
                                            )
                                        val taskAfterStartAngleTranslated =
                                            TouchGestureUtils.translateAngle270To0(
                                                taskAfterStartAngle
                                            )
                                        // Space (in minutes) between the tasks
                                        val capacity =
                                            (taskAfterStartAngleTranslated - taskBeforeEndAngleTranslated) / minuteAngle

                                        Log.i("PlannerDial", "dragDirection $dragDirection")

                                        // The 2 minutes is to separate the dragged task from the neighbouring tasks: 1 minute from each side
                                        if (capacity < draggedTaskDuration + 2) {
                                            // If there is not enough space between the tasks to fit in the dragged task, move backward the task before
                                            if (dragDirection == DragDirection.Forward) {
                                                // If the jump-to-position overlaps the next task, correct the jump-to-position
                                                if (jumpToEndAngleTranslated > taskAfterStartAngleTranslated) {
                                                    jumpToEndAngle = taskAfterStartAngle - minuteAngle
                                                    jumpToStartAngle = jumpToEndAngle!! - draggedTaskDuration * minuteAngle

                                                    if (jumpToEndAngle!! < 0f) jumpToEndAngle = jumpToEndAngle!! + 360f
                                                    if (jumpToStartAngle!! < 0f) jumpToStartAngle = jumpToStartAngle!! + 360f
                                                }

                                                jumpToStartAngleTranslated = TouchGestureUtils.translateAngle270To0(jumpToStartAngle!!)

                                                // Calculate by how much the dragged task should be moved
                                                var angleChange = taskBeforeEndAngleTranslated - jumpToStartAngleTranslated + minuteAngle

                                                if (angleChange < 0f) angleChange += 360f
                                                if (angleChange > 360f) angleChange -= 360f

//                                                            if (angleChange < 0f) angleChange += 360f
//                                                            if (angleChange > 360f) angleChange -= 360f

                                                Log.i(
                                                    "PlannerDial",
                                                    "move task ${taskBefore.title} index $taskBeforeIndex"
                                                )
                                                moveTaskBackward(taskBeforeIndex, angleChange)
                                            }
                                            // If there is not enough space between the tasks to fit in the dragged task, move backward the task after
                                            else if (dragDirection == DragDirection.Backward) {
                                                // If the jump-to-position overlaps the previous task, correct the jump-to-position
                                                if (jumpToStartAngleTranslated < taskBeforeEndAngleTranslated) {
                                                    jumpToStartAngle = taskBeforeEndAngle + minuteAngle
                                                    jumpToEndAngle = jumpToStartAngle!! + draggedTaskDuration * minuteAngle

                                                    if (jumpToStartAngle!! > 360f) jumpToStartAngle = jumpToStartAngle!! - 360f
                                                    if (jumpToEndAngle!! > 360f) jumpToEndAngle = jumpToEndAngle!! - 360f
                                                }

                                                jumpToEndAngleTranslated = TouchGestureUtils.translateAngle270To0(jumpToEndAngle!!)

                                                var angleChange = jumpToEndAngleTranslated - taskAfterStartAngleTranslated + minuteAngle

                                                if (angleChange < 0f) angleChange += 360f
                                                if (angleChange > 360f) angleChange -= 360f

                                                Log.i("PlannerDial","move task ${taskAfter.title} index $taskAfterIndex")
                                                moveTaskForward(taskAfterIndex, -angleChange)
                                            }
                                        }
                                    }
                                }

                                val taskNewStartTime = TouchGestureUtils.calculateTimeFromAngle(
                                    jumpToStartAngle ?: tmpStartAngle,
                                    minuteAngle,
                                    activeTimeStart
                                )
                                val taskNewEndTime = TouchGestureUtils.calculateTimeFromAngle(
                                    jumpToEndAngle ?: tmpEndAngle,
                                    minuteAngle,
                                    activeTimeStart
                                )

                                tasks.find { task -> task.id == touchedTask!!.id }?.apply {
                                    startTime = taskNewStartTime
                                    endTime = taskNewEndTime
                                }

                                // Update the task's start- and end times
                                for (task in tasks) {
                                    saveTask(task)
                                }

                                reset()

                                Log.i("PlannerDial", "tasks $tasks")

                            }
                        }
                    )
                }
        ) {
            if (drawClockHand && TouchGestureUtils.checkIfTimeInRange(clockTime, activeTimeStart, activeTimeEnd)) {
                drawClockHand(
                    activeTimeStart = activeTimeStart,
                    clockTime = clockTime,
                    minuteAngle = minuteAngle,
                    startRadius = outerRadius - clockHandPadding,
                    endRadius = centerRadius
                )
            }

            val minutesBetweenHoursAccumulated: Array<Int> =
                TouchGestureUtils.calculateMinutesBetweenHoursAccumulated(
                    activeTimeStart,
                    activeTimeEnd
                )
            val activeTimeHourSteps: Array<Time> = TouchGestureUtils.createClockHoursArray(
                activeTimeStart,
                activeTimeEnd
            )

            if (drawNewTaskTimeRange) {
                drawTask(
                    taskEndAngle = tmpEndAngle,
                    taskStartAngle = tmpStartAngle,
                    outerRadius = outerRadius - taskPadding,
                    sweepAngle = TouchGestureUtils.sweepAngle(
                        tmpStartAngle,
                        tmpEndAngle
                    ),
                    alpha = 0.3f,
                    borderWidth = 0f
                )
            }

            for (task in tasks) {
                // If the task is being edited, draw it with lighter shade and use the angles on the dial
                if (task.id == touchedTask?.id && (taskMode == TaskMode.EditTimeRange || taskMode == TaskMode.EditStartTime)) {
                    val clockTaskStartTime = TouchGestureUtils.calculateTimeFromAngle(
                        angle = tmpStartAngle,
                        clockStart = activeTimeStart,
                        minuteAngle = minuteAngle
                    )
                    val clockTaskEndTime = TouchGestureUtils.calculateTimeFromAngle(
                        angle = tmpEndAngle,
                        clockStart = activeTimeStart,
                        minuteAngle = minuteAngle
                    )

                    drawTask(
                        taskEndAngle = tmpEndAngle,
                        taskStartAngle = tmpStartAngle,
                        minuteAngle = minuteAngle,
                        innerRadius = centerRadius,
                        outerRadius = outerRadius - taskPadding,
                        taskDurationInMinutes = TouchGestureUtils.calculateTotalNumberOfMinutes(
                            clockTaskStartTime,
                            clockTaskEndTime
                        ),
                        taskTitle = task.title,
                        textMeasurer = textMeasurer,
                        canvasWidth = width,
                        canvasHeight = height,
                        alpha = 0.3f
                    )
                }
                // Otherwise, draw the task normally
                else {
                    // We have to offset these angles because startMinute * taskDialState.minuteAngle returns a biased angle
                    val taskStartAngle = TouchGestureUtils.calculateAngleFromTime (
                        activeTimeStart,
                        task.startTime!!,
                        minuteAngle
                    )
                    val taskEndAngle = TouchGestureUtils.calculateAngleFromTime (
                        activeTimeStart,
                        task.endTime!!,
                        minuteAngle
                    )
                    val taskDuration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                        task.startTime!!,
                        task.endTime!!
                    )

                    drawTask(
                        taskEndAngle = taskEndAngle,
                        taskStartAngle = taskStartAngle,
                        minuteAngle = minuteAngle,
                        innerRadius = centerRadius,
                        outerRadius = outerRadius - taskPadding,
                        taskDurationInMinutes = taskDuration,
                        taskTitle = task.title,
                        textMeasurer = textMeasurer,
                        canvasWidth = width,
                        canvasHeight = height
                    )
                }
            }

            drawHourStepsAndLabels(
                minutesBetweenHoursAccumulated = minutesBetweenHoursAccumulated,
                minuteAngle = minuteAngle,
                outerRadius = outerRadius,
                activeTimeHourSteps = activeTimeHourSteps,
                textMeasurer = textMeasurer,
            )

            // TODO: To be corrected
            drawMinuteSteps(
                minuteAngle,
                totalMinutes,
                minutesBetweenHoursAccumulated,
                outerRadius
            )

            drawClockCenter(
                textMeasurer = textMeasurer,
                radius = centerRadius,
                fontSize = 32.sp,
                label = clockTime
            )
        }
    }

    if (showPopupWindow) {
        PopupDialog(
            onDismissRequest = {
                showPopupWindow = false
                popupState = TaskModePopup.Info
//                // Reset the dial
//                reset()
            }
        ) {
            when (popupState) {
                TaskModePopup.Delete -> {
                    DeleteScreen(
                        onBack = {
                            reset()
                            showPopupWindow = false
                        },
                        onDelete = {
                            deleteTask()
                            reset()
                            showPopupWindow = false
                        }
                    )
                }
                TaskModePopup.Edit -> {
                    TaskEditScreen(
                        dayState = dayState,
                        lastTaskPriority = lastTaskPriority ?: 0,
                        taskUiState = taskUiState,
                        onBack = {
                            showPopupWindow = false
                            popupState = TaskModePopup.Info
                            // Reset the dial
                            reset()
                        },
                        saveTask = saveTaskFromState,
                        setTaskEndTime = setTaskEndTime,
                        setTaskDescription = setTaskDescription,
                        setTaskPriority = setTaskPriority,
                        setTaskStartTime = setTaskStartTime,
                        setTaskTitle = setTaskTitle
                    )
                }
                TaskModePopup.Info -> {
                    TaskInfoScreen(
                        displayType = TaskCardDisplayType.Popup,
                        taskUiState = taskUiState,
                        deleteTask = deleteTask,
                        onBack = {
                            showPopupWindow = false
                            // Reset the dial
                            reset()
                        },
                        onNavigateToMoveToCalendar = {
//                    val task = toDoTasks.find { task -> task.id == taskUiState.id }
                        },
                        onMoveToToDoList = onMoveToToDoList,
                        onNavigateToTaskEdit = {
                            popupState = TaskModePopup.Edit
                        }
                    )
                }
            }
        }
    }
}