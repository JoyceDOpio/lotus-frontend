package com.example.circularplanner.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.R
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.UserInput
import com.example.circularplanner.utils.AngleMode
import com.example.circularplanner.utils.DrawScopeUtils.drawClockCenter
import com.example.circularplanner.utils.DrawScopeUtils.drawClockHand
import com.example.circularplanner.utils.DrawScopeUtils.drawHourStepsAndLabels
import com.example.circularplanner.utils.DrawScopeUtils.drawMinuteSteps
import com.example.circularplanner.utils.DrawScopeUtils.drawTask
import com.example.circularplanner.utils.TouchGestureUtils
import com.example.circularplanner.utils.TouchGestureUtils.TOUCH_STROKE
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun MoveToCalendarDial(
    dayState: DayState,
    userInput: UserInput,
    onPressActiveTime: () -> Unit,
    minimalDuration: Int,
    tasks: List<Task>,
    taskToBeMovedToCalendar: Task
) {
    // Text
    val dateNotAvailableText = "DATE NOT AVAILABLE"// TODO: Read string from resource

    val textMeasurer = rememberTextMeasurer()
    val activeTimeStart: Time = dayState.activeTimeStart
    val activeTimeEnd: Time = dayState.activeTimeEnd
    var taskMode: TaskMode by remember { mutableStateOf(TaskMode.EditStartTime) }
    var angleMode: AngleMode by remember { mutableStateOf(AngleMode.None) }
    val activeTimeColor = MaterialTheme.colorScheme.primary

    var nextTask by remember { mutableStateOf<Task?>(null) }
    var previousTask by remember { mutableStateOf<Task?>(null) }
    var clockTime by remember { mutableStateOf(Time(LocalTime.now().hour, LocalTime.now().minute)) }
    var taskClockTime by remember { mutableStateOf(if (taskToBeMovedToCalendar.startTime != null && taskToBeMovedToCalendar.endTime != null) Time(taskToBeMovedToCalendar.startTime!!.hour, taskToBeMovedToCalendar.startTime!!.minute) else Time(LocalTime.now().hour, LocalTime.now().minute)) }

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
    var startAngle by remember { mutableFloatStateOf(0f) }// TODO: Check if startAngle and endAngle values are really needed
    var endAngle by remember { mutableFloatStateOf(0f) }
    // - values used to draw the angles - if I use the 'by remember { mutableFloatStateOf(...) }' structure for tmpStartAngle and tmpEndAngle, their values are not updated upon date change, but if I don't use 'by remember { mutableFloatStateOf(...) }' the dragging is not reflected in the task's area drawn
    // After the composable enters composition, the block inside remember is initialized and it doesn't change unless you reset it with a new key.
    var tmpStartAngle by remember(tasks) { mutableFloatStateOf(TouchGestureUtils.calculateAngleFromTime(
        activeTimeStart,
        activeTimeEnd,
        taskToBeMovedToCalendar.startTime!!,
        minuteAngle
    )) }
    var tmpEndAngle by remember(tasks) { mutableFloatStateOf(TouchGestureUtils.calculateAngleFromTime(
        activeTimeStart,
        activeTimeEnd,
        taskToBeMovedToCalendar.endTime!!,
        minuteAngle
    )) }

    // The width and height of the Canvas
    var width by remember { mutableIntStateOf(0) }
    var height by remember { mutableIntStateOf(0) }
    var angle by remember { mutableFloatStateOf(0f) }
    var touchNearTheDialEdge by remember { mutableStateOf(false) }
    var touchInsideTheDial by remember { mutableStateOf(false) }
    var center by remember { mutableStateOf(Offset.Zero) }
    // Task area padding from the end of the dial's outer radius
    val taskPadding = 40f
    val clockHandPadding = 0f

    // Basically the width of the finger touch on the screen
    val touchStroke: Float = TOUCH_STROKE

    // The radius of the dial (from the center to the end of the clock steps)
    var outerRadius by remember { mutableFloatStateOf(0f) }
    // The radius from the center to the clock steps
    var innerRadius by remember { mutableFloatStateOf(0f) }
    // The radius of the clock center (the one that displays time)
    var centerRadius by remember { mutableFloatStateOf(0f) }

    // TODO: Show a pop-up dialog informing the user that there isn't enough space to fit in the task to be moved if the movedTaskStartTime and movedTaskEndTime values are null

    // DRAGGING
    // The duration of the dragged task in minutes
    var taskToBeMovedDuration by remember { mutableIntStateOf(TouchGestureUtils.calculateTotalNumberOfMinutes(
        TouchGestureUtils.calculateTimeFromAngle(tmpStartAngle, minuteAngle, activeTimeStart),
        TouchGestureUtils.calculateTimeFromAngle(tmpEndAngle, minuteAngle, activeTimeStart)
    )) }
    var dragDirection by remember { mutableStateOf(DragDirection.None) }
    // The boundary angle corresponds to the task's start time value when the end time angle is modified, and vice versa - this is the value that mustn't be exceeded
    var boundaryAngleTranslated by remember { mutableFloatStateOf(0f) }
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
    // ZOOMING ANG PANNING
    var transformMode by remember { mutableStateOf(false) }

    fun checkIfTouchWithinTask(angle: Float, task: Task): Boolean {
        // The task stores the appropriate angle values, i.e. values corresponding to how the circle is drawn (the 0 degree starts at the right-hand side (east) of the circle). We want to 'correct' these angles as if 0 degree starts at the top of the circle (north)
        var isTouchWithinTask: Boolean
        val taskStartAngle = TouchGestureUtils.calculateAngleFromTime (
            activeTimeStart,
            activeTimeEnd,
            task.startTime!!,
            minuteAngle
        )
        val taskEndAngle = TouchGestureUtils.calculateAngleFromTime (
            activeTimeStart,
            activeTimeEnd,
            task.endTime!!,
            minuteAngle
        )
        isTouchWithinTask =
            TouchGestureUtils.checkIfTouchWithinAngleRange(angle, taskStartAngle, taskEndAngle)

        return isTouchWithinTask
    }

    fun moveTaskBackward(index: Int, angleChange: Float) {
        val taskToBeMoved = tasks[index]

        if (taskToBeMoved.id != taskToBeMovedToCalendar.id) {
            val taskToBeMovedStartAngle =
                TouchGestureUtils.calculateAngleFromTime(
                    activeTimeStart = activeTimeStart,
                    activeTimeEnd,
                    time = taskToBeMoved.startTime!!,
                    minuteAngle = minuteAngle
                )
            val taskToBeMovedEndAngle =
                TouchGestureUtils.calculateAngleFromTime(
                    activeTimeStart = activeTimeStart,
                    activeTimeEnd,
                    time = taskToBeMoved.endTime!!,
                    minuteAngle = minuteAngle
                )
            val taskToBeMovedStartAngleTranslated = TouchGestureUtils.translateAngle270To0(taskToBeMovedStartAngle)
            val taskToBeMovedNewStartAngleTranslated = taskToBeMovedStartAngleTranslated - angleChange

            // If the angle change does not cause the moved task to cross the start of the clock, continue with moving the task backwards
            if (taskToBeMovedNewStartAngleTranslated >= 0f) {
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
                    taskToBeMoved.startTime!!,
                    taskToBeMoved.endTime!!
                )

                val rangeStart = activeTimeStart
                val rangeEnd = taskTobeMovedNewEndTime
                val rangeCapacity = TouchGestureUtils.calculateTotalNumberOfMinutes(
                    start = rangeStart,
                    end = rangeEnd
                )

                // If the new start and end times are within the active time
                if (taskTobeMovedNewStartTime.compareTo(activeTimeStart) != -1
                    && taskTobeMovedNewEndTime.compareTo(activeTimeEnd) != 1) {
                    if (rangeCapacity >= duration) {
                        // Check if the task that is moved starts to overlap an adjacent task while it's moving (omit the dragged task)
                        var adjacentTaskIndex = index - 1

                        if (adjacentTaskIndex >= 0) {
                            if (tasks[adjacentTaskIndex].id == taskToBeMovedToCalendar.id) {
                                if (adjacentTaskIndex > 0) {
                                    adjacentTaskIndex--
                                }
                            }

                            if (tasks[adjacentTaskIndex].id != taskToBeMovedToCalendar.id) {
                                val adjacentTask = tasks[adjacentTaskIndex]

                                // If the tasks start to overlap, start moving backwards the adjacent task
                                if (taskTobeMovedNewStartTime.compareTo(adjacentTask.endTime!!) == -1 || taskTobeMovedNewStartTime.compareTo(adjacentTask.endTime!!) == 0) {
                                    // Re-adjust the angle change - calculate by how much the tasks are overlapping
                                    val adjacentTaskEndAngle =
                                        TouchGestureUtils.calculateAngleFromTime(
                                            activeTimeStart = activeTimeStart,
                                            activeTimeEnd,
                                            time = adjacentTask.endTime!!,
                                            minuteAngle = minuteAngle
                                        )
                                    val adjacentTaskEndAngleTranslated = TouchGestureUtils.translateAngle270To0(adjacentTaskEndAngle)
                                    val taskToBeMovedNewStartAngleTranslated = TouchGestureUtils.translateAngle270To0(taskToBeMovedNewStartAngle)
                                    val angleChangeAdjusted = adjacentTaskEndAngleTranslated - taskToBeMovedNewStartAngleTranslated

                                    moveTaskBackward(adjacentTaskIndex, angleChangeAdjusted)
                                }
                                else {
                                    tasks.find { task -> task.id == taskToBeMoved.id }?.apply {
                                        startTime = taskTobeMovedNewStartTime
                                        endTime = taskTobeMovedNewEndTime
                                    }
                                }
                            }
                            else {
                                tasks.find { task -> task.id == taskToBeMoved.id }?.apply {
                                    startTime = taskTobeMovedNewStartTime
                                    endTime = taskTobeMovedNewEndTime
                                }
                            }
                        }
                        // If there is no more tasks before the task to be moved
                        else {
                            tasks.find { task -> task.id == taskToBeMoved.id }?.apply {
                                startTime = taskTobeMovedNewStartTime
                                endTime = taskTobeMovedNewEndTime
                            }
                        }
                    }
                }
            }
        }
    }

    // The value of angleChange has to be negative
    fun moveTaskForward(index: Int, angleChange: Float) {
        val taskToBeMoved = tasks[index]

        if (taskToBeMoved.id != taskToBeMovedToCalendar.id) {
            val taskToBeMovedStartAngle =
                TouchGestureUtils.calculateAngleFromTime(
                    activeTimeStart = activeTimeStart,
                    activeTimeEnd,
                    time = taskToBeMoved.startTime!!,
                    minuteAngle = minuteAngle
                ) + 0.33f// It seems that during the TouchGestureUtils.calculateAngleFromTime() conversion around 0.33f is lost (subtracted from the angle). This makes it difficult for the task to move forward, so we're adding the 0.33f back. If I add this value back in the TouchGestureUtils.calculateAngleFromTime() method, the task will have difficulty moving backward (in the moveTaskBackward() method).
            val taskToBeMovedEndAngle =
                TouchGestureUtils.calculateAngleFromTime(
                    activeTimeStart = activeTimeStart,
                    activeTimeEnd,
                    time = taskToBeMoved.endTime!!,
                    minuteAngle = minuteAngle
                ) + 0.33f
            val taskToBeMovedEndAngleTranslated = TouchGestureUtils.translateAngle270To0(taskToBeMovedEndAngle)
            val taskToBeMovedNewEndAngleTranslated = taskToBeMovedEndAngleTranslated - angleChange

            // If the angle change does not cause the moved task to cross the start of the clock, continue with moving the task forwards
            if (taskToBeMovedNewEndAngleTranslated <= 360f) {
                var taskToBeMovedNewStartAngle = taskToBeMovedStartAngle - angleChange
                var taskToBeMovedNewEndAngle = taskToBeMovedEndAngle - angleChange

                if (taskToBeMovedNewStartAngle > 360f) taskToBeMovedNewStartAngle -= 360f
                if (taskToBeMovedNewEndAngle > 360f) taskToBeMovedNewEndAngle -= 360f
                if (taskToBeMovedNewStartAngle < 0f) taskToBeMovedNewStartAngle += 360f
                if (taskToBeMovedNewEndAngle < 0f) taskToBeMovedNewEndAngle += 360f

                val taskTobeMovedNewStartTime = TouchGestureUtils.calculateTimeFromAngle(taskToBeMovedNewStartAngle, minuteAngle, activeTimeStart)
                val taskTobeMovedNewEndTime = TouchGestureUtils.calculateTimeFromAngle(taskToBeMovedNewEndAngle, minuteAngle, activeTimeStart)

                val duration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                    taskToBeMoved.startTime!!,
                    taskToBeMoved.endTime!!
                )

                val rangeStart = taskTobeMovedNewStartTime
                val rangeEnd = activeTimeEnd
                val rangeCapacity = TouchGestureUtils.calculateTotalNumberOfMinutes(
                    start = rangeStart,
                    end = rangeEnd
                )

                // If the new start and end times are within the active time
                if (taskTobeMovedNewStartTime.compareTo(activeTimeStart) != -1
                    && taskTobeMovedNewEndTime.compareTo(activeTimeEnd) != 1) {
                    if (rangeCapacity >= duration) {
                        // Check if the task that is moved starts to overlap an adjacent task while it's moving (omit the dragged task)
                        var adjacentTaskIndex = index + 1

                        if (adjacentTaskIndex < tasks.size) {
                            if (tasks[adjacentTaskIndex].id == taskToBeMovedToCalendar.id) {
                                if (adjacentTaskIndex < tasks.size - 1) {
                                    adjacentTaskIndex++
                                }
                            }

                            if (tasks[adjacentTaskIndex].id != taskToBeMovedToCalendar.id) {
                                val adjacentTask = tasks[adjacentTaskIndex]

                                // If the tasks start to overlap, start moving forwards the adjacent task
                                if (taskTobeMovedNewEndTime.compareTo(adjacentTask.startTime!!) == 1 || taskTobeMovedNewEndTime.compareTo(adjacentTask.startTime!!) == 0) {
                                    // Re-adjust the angle change - calculate by how much the tasks are overlapping
                                    val adjacentTaskStartAngle =
                                        TouchGestureUtils.calculateAngleFromTime(
                                            activeTimeStart = activeTimeStart,
                                            activeTimeEnd,
                                            time = adjacentTask.startTime!!,
                                            minuteAngle = minuteAngle
                                        ) + 0.33f// It seems that during the TouchGestureUtils.calculateAngleFromTime() conversion around 0.33f is lost (subtracted from the angle). This makes it difficult for the task to move forward, so we're adding the 0.33f back. If I add this value back in the TouchGestureUtils.calculateAngleFromTime() method, the task will have difficulty moving backward (in the moveTaskBackward() method).
                                    val adjacentTaskStartAngleTranslated = TouchGestureUtils.translateAngle270To0(adjacentTaskStartAngle)
                                    val taskToBeMovedNewEndAngleTranslated = TouchGestureUtils.translateAngle270To0(taskToBeMovedNewEndAngle)
                                    val angleChangeAdjusted = adjacentTaskStartAngleTranslated - taskToBeMovedNewEndAngleTranslated

                                    moveTaskForward(adjacentTaskIndex, angleChangeAdjusted)
                                }
                                else {
                                    tasks.find { task -> task.id == taskToBeMoved.id }?.apply {
                                        startTime = taskTobeMovedNewStartTime
                                        endTime = taskTobeMovedNewEndTime
                                    }
                                }
                            }
                            else {
                                tasks.find { task -> task.id == taskToBeMoved.id }?.apply {
                                    startTime = taskTobeMovedNewStartTime
                                    endTime = taskTobeMovedNewEndTime
                                }
                            }
                        }
                        // If there are no more tasks after the task to be moved
                        else {
                            tasks.find { task -> task.id == taskToBeMoved.id }?.apply {
                                startTime = taskTobeMovedNewStartTime
                                endTime = taskTobeMovedNewEndTime
                            }
                        }
                    }
                }
            }
        }
    }

    fun resetDialParameters() {
        touchNearTheDialEdge = false
    }

    fun reset() {
        resetDialParameters()
        dragDirection = DragDirection.None
    }

//    fun roundToOneDecimal(x: Float) = Math.round(x * 10) / 10
    fun roundToOneDecimal(x: Float) = (x * 10).roundToInt() / 10

    // Update the clock every minute
    LaunchedEffect(true) {
        while (true) {
            delay(1000L * SECONDS_IN_MINUTE)
            clockTime = Time(LocalTime.now().hour, LocalTime.now().minute)
        }
    }

    Column (
        modifier = Modifier
            .width(380.dp)
            .height(440.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // If the user chose today or a day in the future, display the dial
        if (!userInput.selectedDate.isBefore(LocalDate.now())) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // This spacer pushes the active start- and end time button to the center
                Spacer(modifier = Modifier
                    .weight(2f))

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
                        val label = buildAnnotatedString {
                            append("%d:%02d".format(activeTimeStart.hour, activeTimeStart.minute))
                            appendLine()
                            append("%d:%02d".format(activeTimeEnd.hour, activeTimeEnd.minute))
                        }

                        Text(
                            text = label,
                            modifier = Modifier
                                .align(Alignment.Center),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
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
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Canvas (
                modifier = Modifier
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
                    .pointerInput(dayState, userInput, tasks) {
                        val viewConfig = viewConfiguration

                        awaitEachGesture {
                            var pastTouchSlop = false
                            var zoom = 1f
                            var rotation = 0f
                            var pan = Offset.Zero

                            do {
                                // Explicitly pass the event phase
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val pressed = event.changes.any { it.pressed }

                                if (event.changes.size > 1) {
                                    transformMode = true
                                }

                                // If there are more than one finger (pointer) touching the screen
                                if (transformMode && event.changes.size > 1) {
                                    // Multi-touch transform mode
                                    val zoomChange = event.calculateZoom()
                                    val rotationChange = event.calculateRotation()
                                    val panChange = event.calculatePan()

                                    if (!pastTouchSlop) {
                                        zoom *= zoomChange
                                        rotation += rotationChange
                                        pan += panChange

                                        val zoomMotion = abs(1 - zoom)
                                        val rotationMotion = abs(rotation * PI.toFloat() / 180f)
                                        val panMotion = pan.getDistance()

                                        pastTouchSlop = zoomMotion > viewConfig.touchSlop
                                                || rotationMotion > viewConfig.touchSlop
                                                || panMotion > viewConfig.touchSlop
                                    }

                                    if (pastTouchSlop) {
                                        scale *= zoomChange
                                        offset += panChange

                                        event.changes.forEach { it.consume() }
                                    }
                                }

                                if (!pressed) transformMode = false
                            } while (pressed)
                        }
                    }
                    .pointerInput(dayState, userInput, tasks) {
                        detectTapGestures(
                            onDoubleTap = { offset ->
                                // On double tap switch mode to EditTimeRange
                                val distance = TouchGestureUtils.distance(offset, center)
                                touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                    distance,
                                    centerRadius,
                                    innerRadius,
                                    touchStroke
                                )
                                angle = TouchGestureUtils.angle(center, offset)

                                if (touchInsideTheDial) {
                                    // Check if touch is within the task to be moved
                                    val touchWithinTaskToBeMoved =
                                        checkIfTouchWithinTask(
                                            angle,
                                            taskToBeMovedToCalendar
                                        )

                                    if (touchWithinTaskToBeMoved) {
                                        taskMode = TaskMode.EditTimeRange
                                    }
                                } else {
                                    // Cancel everything if the touch is outside the dial
                                    reset()
                                }
                            },
                            onLongPress = { offset ->
                                // On long press switch mode to EditStartTime
                                val distance = TouchGestureUtils.distance(offset, center)
                                touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                    distance,
                                    centerRadius,
                                    innerRadius,
                                    touchStroke
                                )
                                angle = TouchGestureUtils.angle(center, offset)

                                if (touchInsideTheDial) {
                                    // Check if touch is within the task to be moved
                                    val touchWithinTaskToBeMoved =
                                        checkIfTouchWithinTask(
                                            angle,
                                            taskToBeMovedToCalendar
                                        )

                                    if (touchWithinTaskToBeMoved) {
                                        taskMode = TaskMode.EditStartTime
                                    }
                                }
                            }
                        )
                    }
                    .pointerInput(dayState, userInput, tasks) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                // Get the starting coordinates and determine if the touch is within the dial
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
                                }
                            },
                            onDrag = { change, dragAmount ->
                                if (taskMode == TaskMode.EditTimeRange) {
                                    val offset = change.position
                                    val distance = TouchGestureUtils.distance(offset, center)
                                    touchNearTheDialEdge =
                                        TouchGestureUtils.checkIfTouchNearDialEdge(
                                            distance,
                                            innerRadius,
                                            outerRadius,
                                            touchStroke
                                        )
                                    touchInsideTheDial =
                                        TouchGestureUtils.checkIfTouchInsideDial(
                                            distance,
                                            centerRadius,
                                            innerRadius,
                                            touchStroke
                                        )

                                    if (touchNearTheDialEdge || touchInsideTheDial) {
                                        val currentAngle =
                                            TouchGestureUtils.angle(center, offset)
                                        angle = currentAngle
                                        val currentAngleTranslated =
                                            TouchGestureUtils.translateAngle270To0(currentAngle)

                                        // If we don't know which time boundary of the task to be moved we are setting
                                        if (angleMode == AngleMode.None) {
                                            // Determine which time boundary of the task to be moved we are setting: start or end time
                                            // It seems that an angle == tmpStartAngle comparison does not cut it - the app is not able to detect the moment when the two angles are equal

                                            if (currentAngleTranslated in (TouchGestureUtils.translateAngle270To0(
                                                    tmpStartAngle
                                                ) - touchStroke / 4f)..(TouchGestureUtils.translateAngle270To0(
                                                    tmpStartAngle
                                                ) + touchStroke / 4f)
                                            ) {
                                                angleMode = AngleMode.Start
                                                boundaryAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        TouchGestureUtils.calculateAngleFromTime(
                                                            activeTimeStart,
                                                            activeTimeEnd,
                                                            // We're using the end time angle as a boundary
                                                            taskToBeMovedToCalendar.endTime!!,
                                                            minuteAngle
                                                        )
                                                    )
                                            } else if (currentAngleTranslated in (TouchGestureUtils.translateAngle270To0(
                                                    tmpEndAngle
                                                ) - touchStroke / 4f)..(TouchGestureUtils.translateAngle270To0(
                                                    tmpEndAngle
                                                ) + touchStroke / 4f)
                                            ) {
                                                angleMode = AngleMode.End
                                                boundaryAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        TouchGestureUtils.calculateAngleFromTime(
                                                            activeTimeStart,
                                                            activeTimeEnd,
                                                            // We're using the start time angle as a boundary - the end time angle is not to cross the start time angle, because we are not allowing the task to disappear
                                                            taskToBeMovedToCalendar.startTime!!,
                                                            minuteAngle
                                                        )
                                                    )
                                            }
                                        }
                                        // If we do know which time boundary of the given task we are setting
                                        else {
                                            // Show the clock time the angle corresponds to
                                            taskClockTime =
                                                TouchGestureUtils.calculateTimeFromAngle(
                                                    angle = angle,
                                                    clockStart = activeTimeStart,
                                                    minuteAngle = minuteAngle
                                                )

                                            // If we are setting the start time of the given task
                                            if (angleMode == AngleMode.Start) {
                                                // If the current angle is smaller than the angle of the task's end time
                                                if (currentAngleTranslated < boundaryAngleTranslated) {
                                                    // If there is a previous task
                                                    if (previousTask != null) {
                                                        val previousTaskEndAngle =
                                                            TouchGestureUtils.calculateAngleFromTime(
                                                                activeTimeStart,
                                                                activeTimeEnd,
                                                                previousTask!!.endTime!!,
                                                                minuteAngle
                                                            )
                                                        val previousTaskEndAngleTranslated =
                                                            TouchGestureUtils.translateAngle270To0(
                                                                previousTaskEndAngle
                                                            )
                                                        // If the angle does not cross the previous task's end time
                                                        if (currentAngleTranslated > previousTaskEndAngleTranslated) {
                                                            // Make sure the task's duration is not less than 5 minutes
                                                            val newTaskDuration =
                                                                (boundaryAngleTranslated - currentAngleTranslated) / minuteAngle

                                                            if (newTaskDuration >= minimalDuration.toFloat()) {
                                                                startAngle = angle
                                                                tmpStartAngle = startAngle
                                                            }
                                                        }
                                                    }
                                                    // Else, if there is no previous task
                                                    else {
                                                        // Make sure the task's duration is not less than 5 minutes
                                                        val newTaskDuration =
                                                            (boundaryAngleTranslated - currentAngleTranslated) / minuteAngle

                                                        if (newTaskDuration >= minimalDuration.toFloat()) {
                                                            startAngle = angle
                                                            tmpStartAngle = startAngle
                                                        }
                                                    }
                                                }
                                            }
                                            // If we are setting the end time of the given task
                                            else if (angleMode == AngleMode.End) {
                                                // If the current angle is greater than the angle of the task's start time
                                                if (currentAngleTranslated > boundaryAngleTranslated) {
                                                    if (nextTask != null) {
                                                        val nextTaskStartAngle =
                                                            TouchGestureUtils.calculateAngleFromTime(
                                                                activeTimeStart,
                                                                activeTimeEnd,
                                                                nextTask!!.startTime!!,
                                                                minuteAngle
                                                            )
                                                        val nextTaskStartAngleTranslated =
                                                            TouchGestureUtils.translateAngle270To0(
                                                                nextTaskStartAngle
                                                            )
                                                        // If the angle does not cross the previous task's end time
                                                        if (currentAngleTranslated < nextTaskStartAngleTranslated) {
                                                            // Make sure the task's duration is not less than 5 minutes
                                                            val newTaskDuration =
                                                                (currentAngleTranslated - boundaryAngleTranslated) / minuteAngle

                                                            if (newTaskDuration >= minimalDuration.toFloat()) {
                                                                endAngle = angle
                                                                tmpEndAngle = endAngle
                                                            }
                                                        }
                                                    } else {
                                                        // Make sure the task's duration is not less than 5 minutes
                                                        val newTaskDuration =
                                                            (currentAngleTranslated - boundaryAngleTranslated) / minuteAngle

                                                        if (newTaskDuration >= minimalDuration.toFloat()) {
                                                            endAngle = angle
                                                            tmpEndAngle = endAngle
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }
                                }
                                // This code is executed instead of the onDrag() in .detectDragGesturesAfterLongPress() - for some reason the finger has to be lifted after a long-press and the drag gestures are then detected by the .detectDragGestures()
                                else if (taskMode == TaskMode.EditStartTime) {
                                    val currentAngle =
                                        TouchGestureUtils.angle(center, change.position)
                                    val previousAngle =
                                        TouchGestureUtils.angle(center, change.previousPosition)
                                    // We're rounding the angle values because at the end of dragging the values get mixed: e.g. during dragging backwards the previous angle is calculated as smaller than current angle
                                    val currentAngleTranslated =
                                        roundToOneDecimal(
                                            TouchGestureUtils.translateAngle270To0(
                                                currentAngle
                                            )
                                        )
                                    val previousAngleTranslated =
                                        roundToOneDecimal(
                                            TouchGestureUtils.translateAngle270To0(
                                                previousAngle
                                            )
                                        )

                                    if (currentAngleTranslated > previousAngleTranslated) {
                                        dragDirection = DragDirection.Forward
                                    } else if (currentAngleTranslated < previousAngleTranslated) {
                                        dragDirection = DragDirection.Backward
                                    }

                                    var taskToBeMovedNewStartAngle = tmpStartAngle + (currentAngle - previousAngle)
                                    var taskToBeMovedNewEndAngle = taskToBeMovedNewStartAngle + (taskToBeMovedDuration * minuteAngle)

                                    // Correct the angle if its value exceeds 360 degrees
                                    if (taskToBeMovedNewStartAngle > 360f) taskToBeMovedNewStartAngle -= 360f
                                    if (taskToBeMovedNewEndAngle > 360f) taskToBeMovedNewEndAngle -= 360f

                                    val touchedTaskNewStartAngleTranslated =
                                        TouchGestureUtils.translateAngle270To0(
                                            taskToBeMovedNewStartAngle
                                        )
                                    val touchedTaskNewEndAngleTranslated =
                                        TouchGestureUtils.translateAngle270To0(
                                            taskToBeMovedNewEndAngle
                                        )

                                    // If the task's end time doesn't pass the 0/360 degree mark, move the task
                                    if (touchedTaskNewEndAngleTranslated < 360f && touchedTaskNewStartAngleTranslated >= 0) {
                                        if (touchedTaskNewEndAngleTranslated > touchedTaskNewStartAngleTranslated) {
                                            tmpStartAngle = taskToBeMovedNewStartAngle
                                            tmpEndAngle = taskToBeMovedNewEndAngle

                                            // Update the selected task with new start- and end time - These values are not updated on time during task swapping but they are updated on time before onDragEnd()
                                            tasks.find { task -> task.id == taskToBeMovedToCalendar.id }
                                                ?.apply {
                                                    startTime =
                                                        TouchGestureUtils.calculateTimeFromAngle(
                                                            tmpStartAngle,
                                                            minuteAngle,
                                                            activeTimeStart
                                                        )
                                                    endTime =
                                                        TouchGestureUtils.calculateTimeFromAngle(
                                                            tmpEndAngle,
                                                            minuteAngle,
                                                            activeTimeStart
                                                        )
                                                }

                                            // TASK SWAPPING
                                            // Check if the middle angle of the dragged task reaches the start time of an adjacent task. If it does, the later task should get the start time of the earlier task and the earlier task should get the first start time available after the later task.
                                            var touchedTaskMiddleAngle =
                                                taskToBeMovedNewStartAngle + ((taskToBeMovedDuration * minuteAngle) * 0.5f)
                                            // Correct the middle angle if due to constant addition its value exceeds 360 degrees
                                            if (touchedTaskMiddleAngle > 360f) touchedTaskMiddleAngle -= 360f

                                            val touchedTaskMiddleAngleTranslated =
                                                TouchGestureUtils.translateAngle270To0(
                                                    touchedTaskMiddleAngle
                                                )

                                            // Find the task the task to be moved overlaps with (if any)
                                            for ((index, task) in tasks.withIndex()) {
                                                if (task.id != taskToBeMovedToCalendar.id) {
                                                    val taskStartAngle =
                                                        TouchGestureUtils.calculateAngleFromTime(
                                                            activeTimeStart = activeTimeStart,
                                                            activeTimeEnd,
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
                                                            activeTimeEnd,
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
                                                        val currentAngleTranslated =
                                                            TouchGestureUtils.translateAngle270To0(
                                                                currentAngle
                                                            )
                                                        val previousAngleTranslated =
                                                            TouchGestureUtils.translateAngle270To0(
                                                                previousAngle
                                                            )
                                                        val angleChange =
                                                            currentAngleTranslated - previousAngleTranslated

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
                                                                    jumpToStartAngle!! + (taskToBeMovedDuration * minuteAngle)

                                                                if (jumpToStartAngle!! > 360f) jumpToStartAngle =
                                                                    jumpToStartAngle!! - 360f
                                                                if (jumpToEndAngle!! > 360f) jumpToEndAngle =
                                                                    jumpToEndAngle!! - 360f

                                                                val jumpToStartAngleTranslated =
                                                                    TouchGestureUtils.translateAngle270To0(
                                                                        jumpToStartAngle!!
                                                                    )
                                                                val jumpToEndAngleTranslated =
                                                                    TouchGestureUtils.translateAngle270To0(
                                                                        jumpToEndAngle!!
                                                                    )

                                                                // If the jump-to-position crosses the start of the clock
                                                                if (jumpToEndAngleTranslated < jumpToStartAngleTranslated) {
                                                                    // Push the jump-to-position just before the end of the active time
                                                                    jumpToEndAngle = 270f
                                                                    jumpToStartAngle =
                                                                        jumpToEndAngle!! - taskToBeMovedDuration * minuteAngle

                                                                    if (jumpToStartAngle!! < 0f) jumpToStartAngle =
                                                                        jumpToStartAngle!! + 360f
                                                                    if (jumpToStartAngle!! > 360f) jumpToStartAngle =
                                                                        jumpToStartAngle!! - 360f
                                                                }
                                                            }
                                                            // If the dragged task overlaps with the next task only slightly
                                                            else {
                                                                // Set the jump-to-position to just before the next task (should the dragging end while the tasks overlap)
                                                                jumpToEndAngle =
                                                                    taskStartAngle - minuteAngle
                                                                jumpToStartAngle =
                                                                    jumpToEndAngle!! - (taskToBeMovedDuration * minuteAngle)

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
                                                                // Start moving the overlapped task backwards as the dragged task moves forward until the two tasks stop overlapping/the overlapped task cannot be moved further
                                                                moveTaskForward(
                                                                    overlappedTaskIndex,
                                                                    angleChange
                                                                )

                                                                // Set the jump-to-position to just after the next task (should the dragging end while the tasks overlap)
                                                                jumpToEndAngle =
                                                                    taskStartAngle - minuteAngle
                                                                jumpToStartAngle =
                                                                    jumpToEndAngle!! - (taskToBeMovedDuration * minuteAngle)

                                                                if (jumpToStartAngle!! < 0f) jumpToStartAngle =
                                                                    jumpToStartAngle!! + 360f
                                                                if (jumpToEndAngle!! < 0f) jumpToEndAngle =
                                                                    jumpToEndAngle!! + 360f

                                                                val jumpToStartAngleTranslated =
                                                                    TouchGestureUtils.translateAngle270To0(
                                                                        jumpToStartAngle!!
                                                                    )
                                                                val jumpToEndAngleTranslated =
                                                                    TouchGestureUtils.translateAngle270To0(
                                                                        jumpToEndAngle!!
                                                                    )

                                                                // If the jump-to-position crosses the start of the clock
                                                                if (jumpToStartAngleTranslated > jumpToEndAngleTranslated) {
                                                                    // Push the jump-to-position just before the end of the active time
                                                                    jumpToStartAngle = 0f
                                                                    jumpToEndAngle =
                                                                        jumpToStartAngle!! + taskToBeMovedDuration * minuteAngle

                                                                    if (jumpToEndAngle!! > 360f) jumpToEndAngle =
                                                                        jumpToEndAngle!! - 360f
                                                                    if (jumpToEndAngle!! < 0f) jumpToEndAngle =
                                                                        jumpToEndAngle!! + 360f
                                                                }
                                                            }
                                                            // If the dragged task overlaps with the next task only slightly
                                                            else {
                                                                // Set the jump-to-position to just after the previous task (should the dragging end while the tasks overlap)
                                                                jumpToStartAngle =
                                                                    taskEndAngle + minuteAngle
                                                                jumpToEndAngle =
                                                                    jumpToStartAngle!! + (taskToBeMovedDuration * minuteAngle)

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
                                if (taskMode == TaskMode.EditTimeRange) {
                                    if (angleMode == AngleMode.Start) {
                                        val clockTaskStartTime =
                                            TouchGestureUtils.calculateTimeFromAngle(
                                                angle = tmpStartAngle,
                                                clockStart = activeTimeStart,
                                                minuteAngle = minuteAngle
                                            )

                                        tasks.find { task -> task.id == taskToBeMovedToCalendar.id }
                                            ?.apply {
                                                startTime = clockTaskStartTime
                                            }
                                    } else if (angleMode == AngleMode.End) {
                                        val clockTaskEndTime =
                                            TouchGestureUtils.calculateTimeFromAngle(
                                                angle = tmpEndAngle,
                                                clockStart = activeTimeStart,
                                                minuteAngle = minuteAngle
                                            )

                                        tasks.find { task -> task.id == taskToBeMovedToCalendar.id }
                                            ?.apply {
                                                endTime = clockTaskEndTime
                                            }
                                    }

                                    // Reset the angle mode
                                    angleMode = AngleMode.None
                                    // Set new startAngle and endAngle values
                                    startAngle = tmpStartAngle
                                    endAngle = tmpEndAngle

                                    taskToBeMovedDuration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                                        TouchGestureUtils.calculateTimeFromAngle(tmpStartAngle, minuteAngle, activeTimeStart),
                                        TouchGestureUtils.calculateTimeFromAngle(tmpEndAngle, minuteAngle, activeTimeStart)
                                    )
                                }
                                else if (taskMode == TaskMode.EditStartTime) {
                                    // Calculate the time represented by the angle
                                    if (jumpToStartAngle != null && jumpToEndAngle != null) {
                                        // The indices of the tasks between which the dragged task should placed
                                        var taskBeforeIndex: Int? = null
                                        var taskAfterIndex: Int? = null

                                        var jumpToStartAngleTranslated: Float
                                        var jumpToEndAngleTranslated: Float

                                        // Check if the selected task would overlap another task if it was to jump to the previously determined position
                                        if (dragDirection == DragDirection.Forward) {
                                            // Find the last task the selected task overlaps with
                                            for ((index, task) in tasks.withIndex()) {
                                                if (task.id != taskToBeMovedToCalendar.id) {
                                                    val taskStartAngle =
                                                        TouchGestureUtils.calculateAngleFromTime(
                                                            activeTimeStart = activeTimeStart,
                                                            activeTimeEnd,
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
                                                            activeTimeEnd,
                                                            time = task.endTime!!,
                                                            minuteAngle = minuteAngle
                                                        )
                                                    val taskEndAngleTranslated =
                                                        TouchGestureUtils.translateAngle270To0(
                                                            taskEndAngle
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
                                                        jumpToStartAngleTranslated + taskToBeMovedDuration * minuteAngle * 0.5f

                                                    // If the tasks overlap - if the dragged task would be within the neighbouring task or the neighbouring task is within the dragged task, we will want to move the dragged task so that it doesn't overlap another task
                                                    if (jumpToStartAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                        || jumpToEndAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                        || taskStartAngleTranslated in jumpToStartAngleTranslated..jumpToEndAngleTranslated
                                                        || taskEndAngleTranslated in jumpToStartAngleTranslated..jumpToEndAngleTranslated
                                                    ) {
                                                        taskBeforeIndex == null
                                                        taskAfterIndex = null

                                                        if (jumpToMiddleAngleTranslated > taskStartAngleTranslated) {
                                                            taskBeforeIndex = index
                                                            if (taskBeforeIndex + 1 < tasks.size) {
                                                                taskAfterIndex =
                                                                    taskBeforeIndex + 1
                                                            }
                                                        } else {
                                                            taskAfterIndex = index
                                                            if (taskAfterIndex - 1 >= 0) {
                                                                taskBeforeIndex =
                                                                    taskAfterIndex - 1
                                                            }
                                                        }
                                                    } else {
                                                        if (taskAfterIndex != null || taskBeforeIndex != null) {
                                                            break
                                                        }
                                                    }
                                                }
                                            }
                                        } else if (dragDirection == DragDirection.Backward) {
                                            // Find the first task the selected task overlaps with
                                            for ((index, task) in tasks.withIndex()) {
                                                if (task.id != taskToBeMovedToCalendar.id) {
                                                    val taskStartAngle =
                                                        TouchGestureUtils.calculateAngleFromTime(
                                                            activeTimeStart = activeTimeStart,
                                                            activeTimeEnd,
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
                                                            activeTimeEnd,
                                                            time = task.endTime!!,
                                                            minuteAngle = minuteAngle
                                                        )
                                                    val taskEndAngleTranslated =
                                                        TouchGestureUtils.translateAngle270To0(
                                                            taskEndAngle
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
                                                        jumpToStartAngleTranslated + taskToBeMovedDuration * 0.5f

                                                    // If the tasks overlap - if the dragged task would be within the neighbouring task or the neighbouring task is within the dragged task, we will want to move the dragged task so that it doesn't overlap another task
                                                    if (jumpToStartAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                        || jumpToEndAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                        || taskStartAngleTranslated in jumpToStartAngleTranslated..jumpToEndAngleTranslated
                                                        || taskEndAngleTranslated in jumpToStartAngleTranslated..jumpToEndAngleTranslated
                                                    ) {
                                                        if (jumpToMiddleAngleTranslated < taskEndAngleTranslated
                                                        ) {
                                                            taskAfterIndex = index
                                                            if (taskAfterIndex - 1 >= 0) {
                                                                taskBeforeIndex =
                                                                    taskAfterIndex - 1
                                                            }
                                                        }
                                                        // If the selected task crossed the boundaries of the task, but not too far
                                                        else {
                                                            taskBeforeIndex = index
                                                            if (taskBeforeIndex + 1 < tasks.size) {
                                                                taskAfterIndex =
                                                                    taskBeforeIndex + 1
                                                            }
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

                                            val taskBeforeEndAngle =
                                                TouchGestureUtils.calculateAngleFromTime(
                                                    activeTimeStart = activeTimeStart,
                                                    activeTimeEnd,
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
                                                    activeTimeEnd,
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

                                            // The 2 minutes is to separate the dragged task from the neighbouring tasks: 1 minute from each side
                                            if (capacity < taskToBeMovedDuration + 2) {
                                                // If there is not enough space between the tasks to fit in the dragged task, move backward the task before
                                                if (dragDirection == DragDirection.Forward) {
                                                    // If the jump-to-position overlaps the next task, correct the jump-to-position
                                                    jumpToEndAngleTranslated =
                                                        TouchGestureUtils.translateAngle270To0(
                                                            jumpToEndAngle!!
                                                        )

                                                    if (jumpToEndAngleTranslated > taskAfterStartAngleTranslated) {
                                                        jumpToEndAngle =
                                                            taskAfterStartAngle - minuteAngle
                                                        jumpToStartAngle =
                                                            jumpToEndAngle!! - taskToBeMovedDuration * minuteAngle

                                                        if (jumpToEndAngle!! < 0f) jumpToEndAngle =
                                                            jumpToEndAngle!! + 360f
                                                        if (jumpToStartAngle!! < 0f) jumpToStartAngle =
                                                            jumpToStartAngle!! + 360f
                                                    }

                                                    jumpToStartAngleTranslated =
                                                        TouchGestureUtils.translateAngle270To0(
                                                            jumpToStartAngle!!
                                                        )

                                                    // Calculate by how much the dragged task should be moved
                                                    val angleChange =
                                                        taskBeforeEndAngleTranslated - jumpToStartAngleTranslated + minuteAngle

                                                    moveTaskBackward(
                                                        taskBeforeIndex,
                                                        angleChange
                                                    )
                                                }
                                                // If there is not enough space between the tasks to fit in the dragged task, move backward the task after
                                                else if (dragDirection == DragDirection.Backward) {
                                                    // If the jump-to-position overlaps the previous task, correct the jump-to-position
                                                    jumpToStartAngleTranslated =
                                                        TouchGestureUtils.translateAngle270To0(
                                                            jumpToStartAngle!!
                                                        )

                                                    if (jumpToStartAngleTranslated < taskBeforeEndAngleTranslated) {
                                                        jumpToStartAngle =
                                                            taskBeforeEndAngle + minuteAngle
                                                        jumpToEndAngle =
                                                            jumpToStartAngle!! + taskToBeMovedDuration * minuteAngle

                                                        if (jumpToStartAngle!! > 360f) jumpToStartAngle =
                                                            jumpToStartAngle!! - 360f
                                                        if (jumpToEndAngle!! > 360f) jumpToEndAngle =
                                                            jumpToEndAngle!! - 360f
                                                    }

                                                    jumpToEndAngleTranslated =
                                                        TouchGestureUtils.translateAngle270To0(
                                                            jumpToEndAngle!!
                                                        )

                                                    val angleChange =
                                                        jumpToEndAngleTranslated - taskAfterStartAngleTranslated + minuteAngle

                                                    moveTaskForward(
                                                        taskAfterIndex,
                                                        -angleChange
                                                    )
                                                }
                                            }
                                        }
                                        // If there is only a task before, the selected task is probably between the task before and the end of the active time
                                        else if (taskBeforeIndex != null) {
                                            val taskBefore = tasks[taskBeforeIndex]
                                            val taskBeforeEndAngle =
                                                TouchGestureUtils.calculateAngleFromTime(
                                                    activeTimeStart = activeTimeStart,
                                                    activeTimeEnd,
                                                    time = taskBefore.endTime!!,
                                                    minuteAngle = minuteAngle
                                                )
                                            val taskBeforeEndAngleTranslated =
                                                TouchGestureUtils.translateAngle270To0(
                                                    taskBeforeEndAngle
                                                )
                                            // Space (in minutes) between the previous task and the end of the active time
                                            val capacity =
                                                (360f - taskBeforeEndAngleTranslated) / minuteAngle

                                            if (capacity < taskToBeMovedDuration + 1) {
                                                // If there is not enough space between the previous task and the end of the active time to fit in the dragged task, move backward the task before
                                                if (dragDirection == DragDirection.Forward) {
                                                    jumpToStartAngleTranslated =
                                                        TouchGestureUtils.translateAngle270To0(
                                                            jumpToStartAngle!!
                                                        )

                                                    // Calculate by how much the dragged task should be moved
                                                    val angleChange =
                                                        taskBeforeEndAngleTranslated - jumpToStartAngleTranslated + minuteAngle

                                                    moveTaskBackward(
                                                        taskBeforeIndex,
                                                        angleChange
                                                    )
                                                }
                                            }
                                        }
                                        // If there is only a task after, the selected task is probably between the start of the active time and the task after
                                        else if (taskAfterIndex != null) {
                                            val taskAfter = tasks[taskAfterIndex]
                                            val taskAfterStartAngle =
                                                TouchGestureUtils.calculateAngleFromTime(
                                                    activeTimeStart = activeTimeStart,
                                                    activeTimeEnd,
                                                    time = taskAfter.startTime!!,
                                                    minuteAngle = minuteAngle
                                                )
                                            val taskAfterStartAngleTranslated =
                                                TouchGestureUtils.translateAngle270To0(
                                                    taskAfterStartAngle
                                                )
                                            // Space (in minutes) between the active time start and the next task
                                            val capacity =
                                                (taskAfterStartAngleTranslated - 0f) / minuteAngle

                                            if (capacity < taskToBeMovedDuration + 1) {
                                                // If there is not enough space between the active time start and the next task, move backward the task after
                                                if (dragDirection == DragDirection.Backward) {
                                                    jumpToEndAngleTranslated =
                                                        TouchGestureUtils.translateAngle270To0(
                                                            jumpToEndAngle!!
                                                        )

                                                    val angleChange =
                                                        taskAfterStartAngleTranslated - jumpToEndAngleTranslated - minuteAngle

                                                    moveTaskForward(taskAfterIndex, angleChange)
                                                }
                                            }
                                        }
                                    }

                                    val taskNewStartTime =
                                        TouchGestureUtils.calculateTimeFromAngle(
                                            jumpToStartAngle ?: tmpStartAngle,
                                            minuteAngle,
                                            activeTimeStart
                                        )
                                    val taskNewEndTime =
                                        TouchGestureUtils.calculateTimeFromAngle(
                                            jumpToEndAngle ?: tmpEndAngle,
                                            minuteAngle,
                                            activeTimeStart
                                        )

                                    tasks.find { task -> task.id == taskToBeMovedToCalendar.id }
                                        ?.apply {
                                            startTime = taskNewStartTime
                                            endTime = taskNewEndTime
                                        }

                                    reset()
                                }
                            }
                        )
                    }
            ) {
                // Draw the clock hand only on today and when the time is still within the active time range
                if (!userInput.selectedDate.isAfter(LocalDate.now()) && TouchGestureUtils.checkIfTimeInRange(clockTime, activeTimeStart, activeTimeEnd)) {
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

                for (task in tasks) {
                    // If the task is being edited, draw it with lighter shade and use the angles on the dial
                    if (task.id == taskToBeMovedToCalendar.id) {
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
                            activeTimeEnd,
                            task.startTime!!,
                            minuteAngle
                        )
                        val taskEndAngle = TouchGestureUtils.calculateAngleFromTime (
                            activeTimeStart,
                            activeTimeEnd,
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
                    label = taskClockTime
                )
            }
        }
        // If the user selected a date in the past, display a message that the chosen date is not available
        else {
            Text(
                text = dateNotAvailableText,
                color = Color.LightGray
            )
        }
    }
}