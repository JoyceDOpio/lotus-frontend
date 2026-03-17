package com.eternalfairy.timeaware.ui.component

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.timeaware.R
import com.eternalfairy.timeaware.data.room.Task
import com.eternalfairy.timeaware.data.Time
import com.eternalfairy.timeaware.ui.theme.COMMENT_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.viewmodel.room.DayState
import com.eternalfairy.timeaware.ui.viewmodel.room.UserInput
import com.eternalfairy.timeaware.utils.AngleMode
import com.eternalfairy.timeaware.utils.DrawScopeUtils.drawClockCenter
import com.eternalfairy.timeaware.utils.DrawScopeUtils.drawClockHand
import com.eternalfairy.timeaware.utils.DrawScopeUtils.drawHourStepsAndLabels
import com.eternalfairy.timeaware.utils.DrawScopeUtils.drawMinuteSteps
import com.eternalfairy.timeaware.utils.DrawScopeUtils.drawTask
import com.eternalfairy.timeaware.utils.TouchGestureUtils
import com.eternalfairy.timeaware.utils.TouchGestureUtils.TOUCH_STROKE
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun MoveToCalendarDial(
    componentHeight: Dp = 450.dp,
    componentWidth: Dp = 400.dp,
    paddingStart: Dp = 10.dp,
    paddingTop: Dp = 5.dp,
    paddingEnd: Dp = 10.dp,
    paddingBottom: Dp = 5.dp,
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
    // After the composable enters composition, the block inside remember is initialized, and it doesn't change unless you reset it with a new key.
    var tmpStartAngle by remember(tasks) { mutableFloatStateOf((TouchGestureUtils.calculateAngleFromTime(
        activeTimeStart,
        activeTimeEnd,
        taskToBeMovedToCalendar.startTime!!,
        minuteAngle
    ))) }
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
    var draggedTaskDuration by remember { mutableIntStateOf(TouchGestureUtils.calculateTotalNumberOfMinutes(
        TouchGestureUtils.calculateTimeFromAngle(tmpStartAngle, minuteAngle, activeTimeStart),
        TouchGestureUtils.calculateTimeFromAngle(tmpEndAngle, minuteAngle, activeTimeStart)
    )) }
    var dragDirection by remember { mutableStateOf(DragDirection.None) }
    // The boundary angle corresponds to the task's start time value when the end time angle is modified, and vice versa - this is the value that mustn't be exceeded
    var boundaryAngleTranslated by remember { mutableFloatStateOf(0f) }
    // The angles which the dragged task should "jump to" should the dragging end while the dragged task still overlaps another task
    var jumpToStartAngle: Float? by remember { mutableStateOf(null) }
    var jumpToEndAngle: Float? by remember { mutableStateOf(null) }
    var overlappedTask by remember { mutableStateOf<Task?>(null) }

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

    // Decide whether the indices of the previous and next tasks should be changed
    fun calculatePreviousAndNextTaskIndices(previousTaskIndex: Int?, nextTaskIndex: Int?, overlappedTaskIndex: Int?) {
        previousTask = null
        nextTask = null
        overlappedTask = null

        if (previousTaskIndex != null) previousTask = tasks[previousTaskIndex]
        if (nextTaskIndex != null) nextTask = tasks[nextTaskIndex]
        if (overlappedTaskIndex != null) overlappedTask = tasks[overlappedTaskIndex]

        val slotStart = previousTask?.endTime ?: activeTimeStart
        val slotEnd = nextTask?.startTime ?: activeTimeEnd

        val slotStartAngle = TouchGestureUtils.calculateAngleFromTime(
            activeTimeStart = activeTimeStart,
            activeTimeEnd = activeTimeEnd,
            time = slotStart,
            minuteAngle = minuteAngle
        )
        val slotStartAngleTranslated =
            TouchGestureUtils.translateAngle270To0(
                slotStartAngle
            )
        val slotEndAngle = TouchGestureUtils.calculateAngleFromTime(
            activeTimeStart = activeTimeStart,
            activeTimeEnd = activeTimeEnd,
            time = slotEnd,
            minuteAngle = minuteAngle
        )
        var slotEndAngleTranslated =
            TouchGestureUtils.translateAngle270To0(
                slotEndAngle
            )
        if (slotEndAngleTranslated == 0f) slotEndAngleTranslated = 360f

        // Space (in minutes) between the tasks
        val capacity = (slotEndAngleTranslated - slotStartAngleTranslated) / minuteAngle

        // If there is not enough space between the tasks to fit in the dragged task, move the dragged task. The 2 minutes is to separate the task to be moved from the neighbouring tasks: 1 minute from each side
        if (capacity < (draggedTaskDuration + 2).toFloat()) {
            var newPreviousTaskIndex: Int? = null
            var newNextTaskIndex: Int? = null
            var newOverlappedTaskIndex: Int? = null

            // Move the index range of the previous and next tasks backwards (e.g. previous task index 1 to previous task index 0)
            if (dragDirection == DragDirection.Forward) {
                previousTaskIndex?.minus(1)?.let { newIndex ->
                    // Do not assign a new previous index if that index belongs to the task that we are moving
                    newPreviousTaskIndex = if (newIndex >= 0 && tasks[newIndex].id != taskToBeMovedToCalendar.id) newIndex else null
                }
                nextTaskIndex?.minus(1)?.let { newIndex ->
                    newNextTaskIndex = if (newIndex >= 0 && tasks[newIndex].id != taskToBeMovedToCalendar.id) newIndex else nextTaskIndex
                } ?: run {
                    newNextTaskIndex = previousTaskIndex
                }
                // If we find that there is not enough space between the current previous and next tasks and we move the indices backwards, there will definitely be at least a new next task index
                newOverlappedTaskIndex = newNextTaskIndex!!
            }
            else if (dragDirection == DragDirection.Backward) {
                previousTaskIndex?.plus(1)?.let { newIndex ->
                    newPreviousTaskIndex = if (newIndex < tasks.size && tasks[newIndex].id != taskToBeMovedToCalendar.id) newIndex else previousTaskIndex
                } ?: run {
                    newPreviousTaskIndex = nextTaskIndex
                }
                nextTaskIndex?.plus(1)?.let { newIndex ->
                    newNextTaskIndex = if (newIndex < tasks.size && tasks[newIndex].id != taskToBeMovedToCalendar.id) newIndex else null
                }
                // If we find that there is not enough space between the current previous and next tasks and we move the indices forwards, there will definitely be at least a new previous task index
                newOverlappedTaskIndex = newPreviousTaskIndex!!
            }

            calculatePreviousAndNextTaskIndices(newPreviousTaskIndex, newNextTaskIndex, newOverlappedTaskIndex)
        }
    }

    // Calculates the jump-to position between the previous and next tasks
    fun calculateJumpTo(previousTaskIndex: Int?, nextTaskIndex: Int?, overlappedTaskIndex: Int?) {
        calculatePreviousAndNextTaskIndices(previousTaskIndex, nextTaskIndex, overlappedTaskIndex)

        val slotStart = previousTask?.endTime ?: activeTimeStart
        val slotEnd = nextTask?.startTime ?: activeTimeEnd

        val slotStartAngle = TouchGestureUtils.calculateAngleFromTime(
            activeTimeStart = activeTimeStart,
            activeTimeEnd = activeTimeEnd,
            time = slotStart,
            minuteAngle = minuteAngle
        )
        val slotEndAngle = TouchGestureUtils.calculateAngleFromTime(
            activeTimeStart = activeTimeStart,
            activeTimeEnd = activeTimeEnd,
            time = slotEnd,
            minuteAngle = minuteAngle
        )

        // Place the task to be moved near the overlapped task
        if (overlappedTask == nextTask) {
            jumpToEndAngle = slotEndAngle - minuteAngle
            jumpToStartAngle = jumpToEndAngle!! - draggedTaskDuration * minuteAngle

            if (jumpToEndAngle!! < 0f) jumpToEndAngle = jumpToEndAngle!! + 360f
            if (jumpToStartAngle!! < 0f) jumpToStartAngle = jumpToStartAngle!! + 360f
        }
        else if (overlappedTask == previousTask) {
            jumpToStartAngle = slotStartAngle + minuteAngle
            jumpToEndAngle = jumpToStartAngle!! + draggedTaskDuration * minuteAngle

            if (jumpToStartAngle!! > 360f) jumpToStartAngle = jumpToStartAngle!! - 360f
            if (jumpToEndAngle!! > 360f) jumpToEndAngle = jumpToEndAngle!! - 360f
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

    Column(
        modifier = Modifier
            .height(componentHeight)
            .width(componentWidth)
            .padding(
                start = paddingStart,
                top = paddingTop,
                end = paddingEnd,
                bottom = paddingBottom
            )
            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
            .background(COMPONENT_BACKGROUND_COLOR)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column (
            modifier = Modifier
            ,
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

                    // Active time setup button
                    Column (
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box (
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(HEADER_TEXT_COLOR)
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
                                color = COMPONENT_BACKGROUND_COLOR,
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
                                tint = HEADER_TEXT_COLOR
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

                                            pastTouchSlop =
                                                zoomMotion > viewConfig.touchSlop || rotationMotion > viewConfig.touchSlop || panMotion > viewConfig.touchSlop
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
                            detectTapGestures(onDoubleTap = { offset ->
                                // On double tap switch mode to EditTimeRange
                                val distance = TouchGestureUtils.distance(offset, center)
                                touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                    distance, centerRadius, innerRadius, touchStroke
                                )
                                angle = TouchGestureUtils.angle(center, offset)

                                if (touchInsideTheDial) {
                                    // Check if touch is within the task to be moved
                                    val touchWithinTaskToBeMoved = checkIfTouchWithinTask(
                                        angle, taskToBeMovedToCalendar
                                    )

                                    if (touchWithinTaskToBeMoved) {
                                        taskMode = TaskMode.EditTimeRange
                                    }
                                } else {
                                    // Cancel everything if the touch is outside the dial
                                    reset()
                                }
                            }, onLongPress = { offset ->
                                // On long press switch mode to EditStartTime
                                val distance = TouchGestureUtils.distance(offset, center)
                                touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                    distance, centerRadius, innerRadius, touchStroke
                                )
                                angle = TouchGestureUtils.angle(center, offset)

                                if (touchInsideTheDial) {
                                    // Check if touch is within the task to be moved
                                    val touchWithinTaskToBeMoved = checkIfTouchWithinTask(
                                        angle, taskToBeMovedToCalendar
                                    )

                                    if (touchWithinTaskToBeMoved) {
                                        taskMode = TaskMode.EditStartTime
                                    }
                                }
                            })
                        }
                        .pointerInput(dayState, userInput, tasks) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    // Get the starting coordinates and determine if the touch is within the dial
                                    val distance = TouchGestureUtils.distance(offset, center)
                                    touchNearTheDialEdge = TouchGestureUtils.checkIfTouchNearDialEdge(
                                        distance, innerRadius, outerRadius, touchStroke
                                    )
                                    touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                        distance, centerRadius, innerRadius, touchStroke
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
                                        touchNearTheDialEdge = TouchGestureUtils.checkIfTouchNearDialEdge(
                                            distance, innerRadius, outerRadius, touchStroke
                                        )
                                        touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                            distance, centerRadius, innerRadius, touchStroke
                                        )

                                        if (touchNearTheDialEdge || touchInsideTheDial) {
                                            val currentAngle = TouchGestureUtils.angle(center, offset)
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
                                                taskClockTime = TouchGestureUtils.calculateTimeFromAngle(
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
                                        val currentAngle = TouchGestureUtils.angle(center, change.position)
                                        val previousAngle =
                                            TouchGestureUtils.angle(center, change.previousPosition)
                                        // We're rounding the angle values because at the end of dragging the values get mixed: e.g. during dragging backwards the previous angle is calculated as smaller than current angle
                                        val currentAngleTranslated = roundToOneDecimal(
                                            TouchGestureUtils.translateAngle270To0(
                                                currentAngle
                                            )
                                        )
                                        val previousAngleTranslated = roundToOneDecimal(
                                            TouchGestureUtils.translateAngle270To0(
                                                previousAngle
                                            )
                                        )

                                        if (currentAngleTranslated > previousAngleTranslated) {
                                            dragDirection = DragDirection.Forward
                                        } else if (currentAngleTranslated < previousAngleTranslated) {
                                            dragDirection = DragDirection.Backward
                                        }

                                        var taskToBeMovedNewStartAngle =
                                            tmpStartAngle + (currentAngle - previousAngle)
                                        var taskToBeMovedNewEndAngle =
                                            taskToBeMovedNewStartAngle + (draggedTaskDuration * minuteAngle)

                                        // Correct the angle if its value exceeds 360 degrees or becomes negative
                                        if (taskToBeMovedNewStartAngle > 360f) taskToBeMovedNewStartAngle -= 360f
                                        if (taskToBeMovedNewEndAngle > 360f) taskToBeMovedNewEndAngle -= 360f
                                        if (taskToBeMovedNewStartAngle < 0f) taskToBeMovedNewStartAngle += 360f
                                        if (taskToBeMovedNewEndAngle < 0f) taskToBeMovedNewEndAngle += 360f

                                        val taskToBeMovedNewStartAngleTranslated =
                                            TouchGestureUtils.translateAngle270To0(
                                                taskToBeMovedNewStartAngle
                                            )
                                        val taskToBeMovedNewEndAngleTranslated =
                                            TouchGestureUtils.translateAngle270To0(
                                                taskToBeMovedNewEndAngle
                                            )

                                        // If the task's end time and start time don't pass the 0/360 degree mark, move the task
                                        if (taskToBeMovedNewEndAngleTranslated < 360f && taskToBeMovedNewStartAngleTranslated >= 0) {
                                            // If the task's end time is greater then its start time (which is correct)
                                            if (taskToBeMovedNewEndAngleTranslated > taskToBeMovedNewStartAngleTranslated) {
                                                tmpStartAngle = taskToBeMovedNewStartAngle
                                                tmpEndAngle = taskToBeMovedNewEndAngle
                                                jumpToStartAngle = tmpStartAngle
                                                jumpToEndAngle = tmpEndAngle

                                                // Update the selected task with new start- and end time - These values are not updated on time during task swapping but they are updated on time before onDragEnd()
                                                tasks.find { task -> task.id == taskToBeMovedToCalendar.id }
                                                    ?.apply {
                                                        startTime =
                                                            TouchGestureUtils.calculateTimeFromAngle(
                                                                tmpStartAngle, minuteAngle, activeTimeStart
                                                            )
                                                        endTime = TouchGestureUtils.calculateTimeFromAngle(
                                                            tmpEndAngle, minuteAngle, activeTimeStart
                                                        )
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
                                            val clockTaskEndTime = TouchGestureUtils.calculateTimeFromAngle(
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

                                        draggedTaskDuration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                                            TouchGestureUtils.calculateTimeFromAngle(
                                                tmpStartAngle, minuteAngle, activeTimeStart
                                            ), TouchGestureUtils.calculateTimeFromAngle(
                                                tmpEndAngle, minuteAngle, activeTimeStart
                                            )
                                        )
                                    }
                                    else if (taskMode == TaskMode.EditStartTime) {
                                        // The indices of the tasks between which the dragged task should be placed
                                        var taskBeforeIndex: Int? = null
                                        var taskAfterIndex: Int? = null
                                        var overlappedTaskIndex: Int? = null

                                        var tmpMiddleAngle = tmpStartAngle + ((draggedTaskDuration * minuteAngle) * 0.5f)
                                        // Correct the middle angle if due to constant addition its value exceeds 360 degrees
                                        if (tmpMiddleAngle > 360f) tmpMiddleAngle -= 360f

                                        val tmpMiddleAngleTranslated =
                                            TouchGestureUtils.translateAngle270To0(
                                                tmpMiddleAngle
                                            )

                                        val tmpStartAngleTranslated =
                                            TouchGestureUtils.translateAngle270To0(
                                                tmpStartAngle
                                            )
                                        val tmpEndAngleTranslated = TouchGestureUtils.translateAngle270To0(
                                            tmpEndAngle
                                        )

                                        // Check if the task's new start and end time values overlap with another task.
                                        for ((index, task) in tasks.withIndex()) {
                                            // Omit the task that we are moving to the calendar
                                            if (task.id != taskToBeMovedToCalendar.id) {
                                                val taskStartAngle = TouchGestureUtils.calculateAngleFromTime(
                                                    activeTimeStart,
                                                    activeTimeEnd,
                                                    task.startTime!!,
                                                    minuteAngle
                                                )
                                                val taskStartAngleTranslated = TouchGestureUtils.translateAngle270To0(taskStartAngle)
                                                val taskEndAngleTranslated =
                                                    TouchGestureUtils.translateAngle270To0(
                                                        TouchGestureUtils.calculateAngleFromTime(
                                                            activeTimeStart,
                                                            activeTimeEnd,
                                                            task.endTime!!,
                                                            minuteAngle
                                                        )
                                                    )
                                                val taskDuration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                                                    task.startTime!!,
                                                    task.endTime!!
                                                )
                                                var taskMiddleAngle = taskStartAngle + ((taskDuration * minuteAngle) * 0.5f)
                                                // Correct the middle angle if due to constant addition its value exceeds 360 degrees
                                                if (taskMiddleAngle > 360f) taskMiddleAngle -= 360f
                                                val taskMiddleAngleTranslated = TouchGestureUtils.translateAngle270To0(taskMiddleAngle)

                                                // Check if the task is overlapped by the task to be moved
                                                if (tmpStartAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                    || tmpEndAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                    || taskStartAngleTranslated in tmpStartAngleTranslated..tmpEndAngleTranslated
                                                    || taskEndAngleTranslated in tmpStartAngleTranslated..tmpEndAngleTranslated) {
                                                    // Save the index of the overlapped task
                                                    overlappedTaskIndex = index

                                                    // Find the previous and next tasks: the previous task is the LAST task whose start angle is smaller than the middle angle of the task to be moved; the next task is the FIRST task whose end angle is greater than the middle angle of the task to be moved.
                                                    if (tmpMiddleAngleTranslated > taskMiddleAngleTranslated) {
                                                        taskBeforeIndex = index

                                                        // If possible, assign the next index as the index of the next task
                                                        if (taskBeforeIndex + 1 < tasks.size && tasks[taskBeforeIndex + 1].id != taskToBeMovedToCalendar.id) {
                                                            taskAfterIndex = taskBeforeIndex + 1
                                                        }
                                                        else {
                                                            taskAfterIndex = null
                                                        }

                                                        break
                                                    }
                                                    else if (tmpMiddleAngleTranslated < taskMiddleAngleTranslated) {
                                                        taskAfterIndex = index

                                                        // If possible, assign the previous index as the index of the previous task
                                                        if (taskAfterIndex - 1 >= 0 && tasks[taskAfterIndex - 1].id != taskToBeMovedToCalendar.id) {
                                                            taskBeforeIndex = taskAfterIndex - 1
                                                        }
                                                        else {
                                                            taskBeforeIndex = null
                                                        }

                                                        break
                                                    }
                                                }
                                            }
                                        }

                                        // If there is a taskBeforeIndex or taskAfterIndex, it means that the current position of the task to be moved is overlapping with another task
                                        if (taskBeforeIndex != null || taskAfterIndex != null) {
                                            // Therefore, we're going to calculate the new position into which the task to be moved will have to jump into
                                            calculateJumpTo(taskBeforeIndex, taskAfterIndex, overlappedTaskIndex)
                                        }

                                        jumpToStartAngle?.let { value ->
                                            tmpStartAngle = jumpToStartAngle!!
                                        }
                                        jumpToEndAngle?.let { value ->
                                            tmpEndAngle = jumpToEndAngle!!
                                        }

                                        val taskNewStartTime = TouchGestureUtils.calculateTimeFromAngle(
                                            tmpStartAngle,
                                            minuteAngle,
                                            activeTimeStart
                                        )
                                        val taskNewEndTime = TouchGestureUtils.calculateTimeFromAngle(
                                            tmpEndAngle,
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
                    color = COMMENT_TEXT_COLOR
                )
            }
        }
    }
}