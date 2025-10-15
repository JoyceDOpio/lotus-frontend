package com.example.circularplanner.ui.component

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.circularplanner.R
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.utils.TouchGestureUtils
import com.example.circularplanner.utils.TouchGestureUtils.TOUCH_STROKE
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.util.UUID
import kotlin.Int
import kotlin.math.ceil
import kotlin.math.sqrt

const val ACTIVITY_MINUTE_STEP_COLOR = 0xffb4acbd

enum class ActivityGraphDisplayType {
    Activity,
    Task
}

@Composable
fun ActivityGraph (
    dayState: DayState,
    drawClockHand: Boolean = false,
//    height: Dp = 320.dp,// Minimum height is 300.dp - at 280.dp there is a problem with index out of bounds
    onNavigateToTaskActivityComparison: () -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit
) {
    val tasks = dayState.tasks
    val activities = dayState.activities

    var canvasWidth by remember { mutableStateOf(0.dp) }
    var canvasHeight by remember { mutableStateOf(0f) }
    var canvasHeightDp by remember { mutableStateOf(0.dp) }

    // The planned start and end time of the day
    val activeTimeStart: Time = dayState.activeTimeStart
    val activeTimeEnd: Time = dayState.activeTimeEnd
    // The actual start and end time of the day
    var actualActiveTimeStart = dayState.actualActiveTimeStart
    var actualActiveTimeEnd = dayState.actualActiveTimeEnd
    // In case the activity starts before the active time start we need to draw the task axis a little further
    var xOffsetInMinutesTask = TouchGestureUtils.calculateTotalNumberOfMinutes(
        actualActiveTimeStart ?: activeTimeStart, activeTimeStart
    )

    val textMeasurer = rememberTextMeasurer()
    val localDensity = LocalDensity.current

    val axisHorizontalPadding = 70.dp
    val minuteWidth = (1.5).dp
    // The total minutes of actual active time throughout the day
    val totalMinutes = TouchGestureUtils.calculateTotalNumberOfMinutes(
        actualActiveTimeStart ?: activeTimeStart,
        actualActiveTimeEnd ?: activeTimeEnd
    )
    canvasWidth = axisHorizontalPadding * 2 + minuteWidth * totalMinutes
    val canvasScrollState = rememberScrollState()
    var graphBoxSize by remember { mutableStateOf(IntSize.Zero) }

    // The width of the finger touch on the screen
    val touchStroke: Float = TOUCH_STROKE
    var touchWithinAxis by remember { mutableStateOf(false) }

    var clockTime by remember { mutableStateOf(Time(LocalTime.now().hour, LocalTime.now().minute)) }

    var magnifierSourceCenter by remember {
        mutableStateOf(Offset.Unspecified)
    }
    var magnifierCenterOffset by remember {
        mutableStateOf(Offset(0f, -160f))
    }
    val magnifierSize = DpSize(80.dp, 80.dp)
    var showMagnifier by remember { mutableStateOf(false) }
    val magnifierModifier = Modifier
        .magnifier(
            sourceCenter = { magnifierSourceCenter },
            magnifierCenter = { magnifierSourceCenter + magnifierCenterOffset },
            zoom = 8f,
            size = magnifierSize,
            cornerRadius = 50.dp,
        )
        .pointerInput(Unit) {
            detectDragGestures { change, _ ->
                if (change.position.y in 0f..canvasHeight) magnifierSourceCenter = change.position
            }
        }

    fun calculateClockTimeFromAxis(minuteWidth: Float, touchOffsetX: Float, activeTimeStart: Time): Time {
        var hour = activeTimeStart.hour
        val totalMinutes = (touchOffsetX / minuteWidth) + activeTimeStart.minute
        var hoursToAdd = (totalMinutes / 60).toInt()
        var minutes = (totalMinutes % 60).toInt()

        hour += hoursToAdd

        return Time(hour, minutes)
    }

    fun checkIfTouchWithinAxis(touchOffsetX: Offset, axisStart: Offset, axisEnd: Offset, touchStroke: Float): Boolean {
        if (touchOffsetX.y in (axisStart.y - touchStroke * 0.5f)..(axisStart.y + touchStroke * 0.5f)) {
            if (touchOffsetX.x in (axisStart.x - touchStroke * 0.5f)..(axisEnd.x + touchStroke * 0.5f)) {
                return true
            }
        }
        return false
    }

    fun selectTaskAndActivity(time: Time) {
        // Select task
        for (task in tasks) {
            val isTouchedTimeInTaskRange = TouchGestureUtils.checkIfTimeInRange(
                time = time,
                rangeStart = task.startTime!!,
                rangeEnd = task.endTime!!
            )
            if (isTouchedTimeInTaskRange) {
                selectTask(task.id)
            }
        }

        // Select activity
        for (activity in activities) {
            val isTouchedTimeInActivityRange = TouchGestureUtils.checkIfTimeInRange(
                time = time,
                rangeStart = activity.startTime,
                rangeEnd = activity.endTime ?: Time(LocalTime.now().hour, LocalTime.now().minute)//TODO: Test me
            )

            if (isTouchedTimeInActivityRange) {
                selectActivity(activity.id)
            }
        }
    }

    // Clear the activity UI state when the component is loaded
    LaunchedEffect(Unit) {
        selectActivity(null)
    }

    // Update the clock every minute
    LaunchedEffect(true) {
        while (true) {
            delay(1000L * SECONDS_IN_MINUTE)
            clockTime = Time(LocalTime.now().hour, LocalTime.now().minute)
        }
    }

//    LaunchedEffect(dayState.date) {//FIXME: Scroll to beginning only when the selected date changes
//        // Scroll to the beginning of the graph when the date changes
//        canvasScrollState.animateScrollTo(
//            value = 0,
//            animationSpec = tween(
//                durationMillis = 1000
//            )
//        )
//    }

    // Graph
    Box (
        modifier = Modifier
//            .height(height)
            .fillMaxWidth()
            .onSizeChanged {
                graphBoxSize = it
            }
            .background(Color(0xffffffff))
            .onGloballyPositioned { layoutCoordinates ->
                val topLeft = layoutCoordinates.boundsInRoot().topLeft
                magnifierSourceCenter = layoutCoordinates.boundsInRoot().center
            }
    ) {
        Box (
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .horizontalScroll(canvasScrollState)
                .onGloballyPositioned { layoutCoordinates ->
                    magnifierSourceCenter = layoutCoordinates.boundsInRoot().topCenter
                }
                .conditional(showMagnifier, magnifierModifier)
            ,
        ) {
            Canvas (
                modifier = Modifier
                    .width(canvasWidth)
                    .fillMaxHeight()
                    .onGloballyPositioned {
                        canvasHeight = it.size.height.toFloat()
                        canvasHeightDp = it.size.height.dp
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                // Clear the task- and activity UI states
                                selectTask(null)
                                selectActivity(null)

                                var time: Time? = null
                                touchWithinAxis = checkIfTouchWithinAxis(
                                    touchOffsetX = offset,
                                    axisStart = Offset(
                                        x = with(localDensity) { axisHorizontalPadding.toPx() },
                                        y = canvasHeight * 0.5f
                                    ),
                                    axisEnd = Offset(
                                        x = (with(localDensity) { canvasWidth.toPx() } - with(
                                            localDensity
                                        ) { axisHorizontalPadding.toPx() }),
                                        y = canvasHeight * 0.5f
                                    ),
                                    touchStroke = touchStroke
                                )

                                // If touch within the magnifier
                                if (offset.x in (magnifierSourceCenter.x + magnifierCenterOffset.x - (magnifierSize.width * 0.5f).toPx())..(magnifierSourceCenter.x + magnifierCenterOffset.x + (magnifierSize.width * 0.5f).toPx()) && offset.y in (magnifierSourceCenter.y + magnifierCenterOffset.y - (magnifierSize.height * 0.5f).toPx())..(magnifierSourceCenter.y + magnifierCenterOffset.y + (magnifierSize.height * 0.5f).toPx())) {
                                    time = calculateClockTimeFromAxis(
                                        minuteWidth = with(localDensity) { minuteWidth.toPx() },
                                        touchOffsetX = magnifierSourceCenter.x - with(
                                            localDensity
                                        ) { axisHorizontalPadding.toPx() },
                                        activeTimeStart = activeTimeStart
                                    )
                                }
                                else if (touchWithinAxis) {
                                    // Determine what time the touch offset corresponds to
                                    time = calculateClockTimeFromAxis(
                                        minuteWidth = with(localDensity) { minuteWidth.toPx() },
                                        touchOffsetX = offset.x - with(localDensity) { axisHorizontalPadding.toPx() },
                                        activeTimeStart = activeTimeStart
                                    )
                                }

                                if (time != null) {
                                    // Select task and activity
                                    selectTaskAndActivity(time)

                                    // Navigate to the task and activity comparison screen
                                    onNavigateToTaskActivityComparison()
                                }
                            }
                        )
                    }
            ) {
                // Draw tasks
                for (task in tasks) {
                    // We have to offset these angles because startMinute * taskDialState.minuteAngle returns a biased angle
                    val taskMinutesFromActiveTimeStart = TouchGestureUtils.calculateTotalNumberOfMinutes (
                        activeTimeStart,
                        task.startTime!!
                    )
                    val taskDuration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                        task.startTime!!,
                        task.endTime!!
                    )

                    drawTask(
                        axisHorizontalPadding = with(localDensity) { axisHorizontalPadding.toPx() },
                        taskMinutesFromActiveTimeStart = taskMinutesFromActiveTimeStart,
                        minuteWidth = with(localDensity) { minuteWidth.toPx() },
                        taskDuration = taskDuration,
                        taskEndTime = task.endTime.toString(),
                        taskStartTime = task.startTime.toString(),
                        taskTitle = task.title,
                        textMeasurer = textMeasurer,
                        canvasHeight = canvasHeight,
                        xOffsetInMinutes = xOffsetInMinutesTask,
                        yOffset = 0.25f,
                        displayType = ActivityGraphDisplayType.Task
                    )
                }

                // Draw activities
                for (activity in activities) {
                    // We have to offset these angles because startMinute * taskDialState.minuteAngle returns a biased angle
                    val activityMinutesFromActiveTimeStart = TouchGestureUtils.calculateTotalNumberOfMinutes (
                        activeTimeStart,
                        activity.startTime
                    )
                    val activityDuration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                        activity.startTime,
                        activity.endTime ?: Time(LocalTime.now().hour, LocalTime.now().minute)
                    )

                    drawTask(
                        axisHorizontalPadding = with(localDensity) { axisHorizontalPadding.toPx() },
                        taskMinutesFromActiveTimeStart = activityMinutesFromActiveTimeStart,
                        minuteWidth = with(localDensity) { minuteWidth.toPx() },
                        taskDuration = activityDuration,
                        taskEndTime = activity.endTime.toString(),
                        taskStartTime = activity.startTime.toString(),
                        taskTitle = activity.title,
                        textMeasurer = textMeasurer,
                        canvasHeight = canvasHeight,
                        yOffset = 0.6f,
                        displayType = ActivityGraphDisplayType.Activity
                    )
                }

                val minutesBetweenHoursAccumulatedTask =
                    TouchGestureUtils.calculateMinutesBetweenHoursAccumulated(
                        activeTimeStart,
                        activeTimeEnd
                    )
                // We are going to draw a separate axis for activities which may be longer than the task axis, if an activity was recorded before the planned active time start or after the planned active time end
                val minutesBetweenHoursAccumulatedActivity = TouchGestureUtils.calculateMinutesBetweenHoursAccumulated(
                    actualActiveTimeStart ?: activeTimeStart,
                    actualActiveTimeEnd ?: activeTimeEnd
                )

                val activeTimeHourSteps = TouchGestureUtils.createClockHoursArray(
                    actualActiveTimeStart ?: activeTimeStart,
                    actualActiveTimeEnd ?: activeTimeEnd
                )

                // Total number of minutes between the planned start and end time of the day
                val totalMinutesTask = TouchGestureUtils.calculateTotalNumberOfMinutes(
                    activeTimeStart,
                    activeTimeEnd
                )

                // Draw the time axis starting from the active time start
                drawGraph(
                    activeTimeStart = activeTimeStart,
                    axisHorizontalPadding = with(localDensity) { axisHorizontalPadding.toPx() },
                    canvasHeight = canvasHeight,
                    clockTime = clockTime,
                    drawClockHand = drawClockHand && TouchGestureUtils.checkIfTimeInRange(clockTime, activeTimeStart, activeTimeEnd),
                    minuteWidth = with(localDensity) { minuteWidth.toPx() },
                    minutesBetweenHoursAccumulatedTask = minutesBetweenHoursAccumulatedTask,
                    minutesBetweenHoursAccumulatedActivity = minutesBetweenHoursAccumulatedActivity,
                    activeTimeHourSteps = activeTimeHourSteps,
                    textMeasurer = textMeasurer,
                    totalMinutesTask = totalMinutesTask,
                    totalMinutesActivity = totalMinutes,
                    xOffsetInMinutesTask = xOffsetInMinutesTask
                )
            }
        }

        // Header
        Box (
            modifier = Modifier
                // If I place the offset modifier at the end of the modifiers' chain, it is ignored
                // TODO: The offset should dependent on canvas height
                .align(Alignment.TopStart)
                .clip(shape = RoundedCornerShape(0.dp, 5.dp, 5.dp, 0.dp))
                // TODO: This width should have a specific value in px or something
                .fillMaxWidth(0.1f)
                // TODO: The height could cover the upper and lower axis completely
                .fillMaxHeight()
        ) {
            var width by remember { mutableStateOf(0) }
            var height by remember { mutableStateOf(0) }
            var center by remember { mutableStateOf(Offset.Zero) }

            Canvas (
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .onGloballyPositioned {
                        width = it.size.width
                        height = it.size.height
                        center = Offset(width / 2f, height / 2f)
                    }
            ) {
                // Tasks
                var text = "PLANNED"//TODO: Read from string resource
                var measuredText = textMeasurer.measure(text = text)
                var path = Path()
                path.moveTo(
                    x = width * 0.5f,
                    y = height * 0.5f
                )
                path.lineTo(
                    x = width * 0.5f,
                    y = 0f
                )

                // Draw background
                drawRect(
                    color = Color(CLOCK_LABEL_COLOR),
                    topLeft = Offset(
                        x = 0f,
                        y = 0f
                    ),
                    size = Size(
                        width = width.toFloat(),
                        height = height.toFloat()
                    )
                )
                // Draw text
                this.drawContext.canvas.nativeCanvas.apply {
                    drawTextOnPath(
                        text,
                        path.asAndroidPath(),
                        0f,
                        measuredText.size.height * 0.9f * 0.29f,// So that the path runs through the middle of the text's height
                        Paint().apply {
                            this.color = android.graphics.Color.WHITE
                            this.textSize = measuredText.size.height * 0.9f
                            this.textAlign = Paint.Align.CENTER
                        }
                    )
                }

                // Activities
//                    color = Color(0xff00c0de)// TODO: Add to the theme
                text = "ACTUAL"//TODO: Read from string resource
                measuredText = textMeasurer.measure(text = text)
                path = Path()
                path.moveTo(
                    x = width * 0.5f,
                    y = height.toFloat()
                )
                path.lineTo(
                    x = width * 0.5f,
                    y = height * 0.5f
                )

                // Draw text
                this.drawContext.canvas.nativeCanvas.apply {
                    drawTextOnPath(
                        text,
                        path.asAndroidPath(),
                        0f,
                        measuredText.size.height * 0.9f * 0.29f,// So that the path runs through the middle of the text's height
                        Paint().apply {
                            this.color = android.graphics.Color.WHITE
                            this.textSize = measuredText.size.height * 0.9f
                            this.textAlign = Paint.Align.CENTER
                        }
                    )
                }
            }

        }

        Box (
            modifier = Modifier
                // If I place the offset modifier at the end of the modifiers' chain, it is ignored
                // TODO: The offset should dependent on canvas height
//                    .align(Alignment.TopEnd)
//                    .clip(shape = RoundedCornerShape(0.dp, 5.dp, 5.dp, 0.dp))
                // TODO: This width should have a specific value in px or something
                .fillMaxWidth()
                // TODO: The height could cover the upper and lower axis completely
                .fillMaxHeight()
//                .background(Color(0xffd4567))
            ,
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = {
                    showMagnifier = !showMagnifier
                },
                modifier = Modifier
                    .padding(5.dp)
                    .clip(CircleShape)
                    .background(Color(CLOCK_LABEL_COLOR))
            ) {
                Icon(
                    imageVector = if (showMagnifier) ImageVector.vectorResource(id = R.drawable.cross_small_svgrepo_com) else ImageVector.vectorResource(id = R.drawable.loupe_search_svgrepo_com),
                    contentDescription = "Magnifier",
                    modifier = Modifier.fillMaxSize(0.8F),
                    tint = Color(0xffffffff)
                )
            }
        }

//        Box (
//            modifier = Modifier
//                .alpha(if (showMagnifier) 1f else 0f)
//                .fillMaxWidth()
//                .fillMaxHeight()
////                .background(Color(0xffd4567))
//            ,
//            contentAlignment = Alignment.TopStart
//        ) {
//            Box (
//                modifier = Modifier
//                    .size(50.dp)
//                    .offset(0.dp, 0.dp)
//                    .graphicsLayer(
//                        translationX = (magnifierSourceCenter + magnifierCenterOffset).x,
//                        translationY = (magnifierSourceCenter + magnifierCenterOffset).y
//                    )
//                    .clip(CircleShape)
//                    .zIndex(10000f)
////                    .background(Color(0xffd5566e))
//                ,
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = ImageVector.vectorResource(id = R.drawable.arrow_down_svgrepo_com),
//                    contentDescription = "Arrow",
//                    modifier = Modifier
//                        .fillMaxSize(0.8F)
//                    ,
//                    tint = Color(CLOCK_LABEL_COLOR)
//                )
//            }
//        }
    }
}

fun DrawScope.drawClockHand(
    activeTimeStart: Time,
    axisHorizontalPadding: Float,
    canvasHeight: Float,
    clockTime: Time,
    minuteWidth: Float
) {
    val minutesFromActiveTimeStart = TouchGestureUtils.calculateTotalNumberOfMinutes(activeTimeStart, clockTime)
    var startOffset = Offset(
        x = axisHorizontalPadding + minutesFromActiveTimeStart * minuteWidth,
        y = canvasHeight * 0.4f
    )
    var endOffset = Offset(
        x = axisHorizontalPadding + minutesFromActiveTimeStart * minuteWidth,
        y = 0f
    )
    val path = Path()

    path.moveTo(
        x = startOffset.x,
        y = startOffset.y
    )
    path.lineTo(
        x = endOffset.x,
        y = endOffset.y
    )
    path.moveTo(
        x = startOffset.x,
        y = canvasHeight * 0.6f
    )
    path.lineTo(
        x = endOffset.x,
        y = canvasHeight
    )
    drawPath(
        path,
        color = Color(CLOCK_LABEL_COLOR),
        alpha = 0.35f,
        style = Stroke(
            width = 8f
        )
    )
}

fun DrawScope.drawGraph(
    activeTimeStart: Time,
    axisHorizontalPadding: Float,
    canvasHeight: Float,
    clockTime: Time,
    drawClockHand: Boolean,
    minuteWidth: Float,
    minutesBetweenHoursAccumulatedTask: Array<Int>,
    minutesBetweenHoursAccumulatedActivity: Array<Int>,
    activeTimeHourSteps: Array<Time>,
    textMeasurer: TextMeasurer,
    totalMinutesTask: Int,
    totalMinutesActivity: Int,
    xOffsetInMinutesTask: Int
) {
    if (drawClockHand) {
        drawClockHand(
            activeTimeStart = activeTimeStart,
            axisHorizontalPadding = axisHorizontalPadding,
            canvasHeight = canvasHeight,
            clockTime = clockTime,
            minuteWidth = minuteWidth,
        )
    }

    drawHourLabels(
        axisHorizontalPadding = axisHorizontalPadding,
        canvasHeight = canvasHeight,
        minuteWidth = minuteWidth,
        minutesBetweenHoursAccumulated = minutesBetweenHoursAccumulatedActivity,
        activeTimeHourSteps = activeTimeHourSteps,
        textMeasurer = textMeasurer
    )
    // Draw hour steps on the task axis
    drawHourSteps(
        axisHorizontalPadding = axisHorizontalPadding,
        canvasHeight = canvasHeight,
        minuteWidth = minuteWidth,
        minutesBetweenHoursAccumulated = minutesBetweenHoursAccumulatedTask,
        xOffsetInMinutes = xOffsetInMinutesTask,
        yOffset = 0.4f
    )
    // Draw minute steps on the task axis
    drawMinuteSteps(// FIXME: Correct draw minutes
        axisHorizontalPadding = axisHorizontalPadding,
        canvasHeight = canvasHeight,
        minuteWidth = minuteWidth,
        totalMinutes = totalMinutesTask,
        minutesBetweenHoursAccumulated = minutesBetweenHoursAccumulatedTask,
        xOffsetInMinutes = xOffsetInMinutesTask,
        yOffset = 0.4f
    )
    // Draw hour steps on the activity axis
    drawHourSteps(
        axisHorizontalPadding = axisHorizontalPadding,
        canvasHeight = canvasHeight,
        minuteWidth = minuteWidth,
        minutesBetweenHoursAccumulated = minutesBetweenHoursAccumulatedActivity,
        yOffset = 0.6f
    )
    // Draw minutes on the activity axis
    drawMinuteSteps(
        axisHorizontalPadding = axisHorizontalPadding,
        canvasHeight = canvasHeight,
        minuteWidth = minuteWidth,
        totalMinutes = totalMinutesActivity,
        minutesBetweenHoursAccumulated = minutesBetweenHoursAccumulatedActivity,
        yOffset = 0.6f
    )
}

fun DrawScope.drawHourLabels(
    axisHorizontalPadding: Float,
    canvasHeight: Float,
    minuteWidth: Float,
    minutesBetweenHoursAccumulated: Array<Int>,
    activeTimeHourSteps: Array<Time>,
    textMeasurer: TextMeasurer
) {
    val textStyle = TextStyle(
        textAlign = TextAlign.Center,
        color = Color(HOUR_LABEL_COLOR)
    )

    // Draw grid - hour steps
    for (i in 0..(minutesBetweenHoursAccumulated.size - 1)) {
        var hourOffset = Offset(
            x = (axisHorizontalPadding + (minutesBetweenHoursAccumulated[i]) * minuteWidth),
            y = canvasHeight * 0.5f
        )

        // Draw hour label
        var hourStep = activeTimeHourSteps[i]
        // Let only the first and last label display the hour and minute values - other labels will display only the hour value
        val hourStepLabel = buildAnnotatedString {
            if (i == 0 || i == minutesBetweenHoursAccumulated.size - 1) {
                append("%d:%02d".format(hourStep.hour, hourStep.minute))
            } else {
                append("%d".format(hourStep.hour))
            }
        }
        val hourStepLabelTextLayout = textMeasurer.measure(
            text = hourStepLabel,
            style = textStyle
        )

        // Subtract the label width and height to position label at the center of the step and beneath the axis
        hourOffset = Offset(
            // Subtracting from the x moves the element to the left
            hourOffset.x - ((hourStepLabelTextLayout.size.width) / 2f),
            // Subtracting from the y moves the element up
            hourOffset.y - hourStepLabelTextLayout.size.height * 0.5f
        )

        // If i refers to the second or second last hour label
        if (i == 1 || i == (minutesBetweenHoursAccumulated.size - 2)) {
            // If the interval between the two consecutive hour labels is less than 30 minutes, don't draw it the second/second last label
            if (i == 1 && minutesBetweenHoursAccumulated[i] - minutesBetweenHoursAccumulated[0] >= 30) {
                // Draw the hour labels between the two axes
                drawText(
                    textMeasurer = textMeasurer,
                    text = hourStepLabel,
                    topLeft = hourOffset,
                    style = textStyle
                )
            }
            if (i == (minutesBetweenHoursAccumulated.size - 2) && (minutesBetweenHoursAccumulated[minutesBetweenHoursAccumulated.size - 1] - minutesBetweenHoursAccumulated[i]) >= 30) {
                // Draw the hour labels between the two axes
                drawText(
                    textMeasurer = textMeasurer,
                    text = hourStepLabel,
                    topLeft = hourOffset,
                    style = textStyle
                )
            }
        } else {
            // Draw the hour labels between the two axes
            drawText(
                textMeasurer = textMeasurer,
                text = hourStepLabel,
                topLeft = hourOffset,
                style = textStyle
            )
        }
    }
}

fun DrawScope.drawHourSteps(
    axisHorizontalPadding: Float,
    canvasHeight: Float,
    minuteWidth: Float,
    minutesBetweenHoursAccumulated: Array<Int>,
    xOffsetInMinutes: Int = 0,
    yOffset: Float
) {
    for (i in 0..(minutesBetweenHoursAccumulated.size - 1)) {
        drawCircle(
            color = Color(HOUR_LABEL_COLOR),
            radius = 10f,
            center = Offset(
                x = (axisHorizontalPadding + (minutesBetweenHoursAccumulated[i] + xOffsetInMinutes) * minuteWidth),
                y = canvasHeight * yOffset
            )
        )
    }
}

fun DrawScope.drawMinuteSteps(
    axisHorizontalPadding: Float,
    canvasHeight: Float,
    minuteWidth: Float,
    totalMinutes: Int,
    minutesBetweenHoursAccumulated: Array<Int>,
    xOffsetInMinutes: Int = 0,
    yOffset: Float
) {
    // Draw minute 10-minute steps
    val minuteStep = 10

    for (i in 0..totalMinutes) {
        if (i % minuteStep == 0 && !(i in minutesBetweenHoursAccumulated)) {
            drawCircle(
                color = Color(ACTIVITY_MINUTE_STEP_COLOR),
                radius = 5f,
                center = Offset(
                    x = (axisHorizontalPadding + ((i + xOffsetInMinutes) * minuteWidth)),
                    y = canvasHeight * yOffset
                )
            )
        }
    }
}

fun DrawScope.drawTask(// FIXME: Area is drawn outside the axis, if activity is started before planned active start time
    displayType: ActivityGraphDisplayType,
    axisHorizontalPadding: Float,
    taskMinutesFromActiveTimeStart: Int,
    minuteWidth: Float,
    taskDuration: Int,
    taskStartTime: String,
    taskEndTime: String,
    taskTitle: String,
    textMeasurer: TextMeasurer,
    canvasHeight: Float,
    xOffsetInMinutes: Int = 0,
    yOffset: Float
) {
    // Draw task area
    val taskWidth = minuteWidth * taskDuration
    val taskHeight = canvasHeight * 0.15f
    val taskOffset = Offset(
        x = (axisHorizontalPadding + (minuteWidth * (taskMinutesFromActiveTimeStart + xOffsetInMinutes))),
        y = canvasHeight * yOffset
    )

    val rainbowColors = listOf(
        Color(0xfff78f0a),
        Color(0xfff78f0a),
        Color(0xffc4067c),
        Color(0xffc4067c),
        Color(0xff06aac4),
        Color(0xff06aac4),
    )

    val gradient = Brush.linearGradient(
        colors = rainbowColors,
    )

    drawRoundRect(
        brush = gradient,
        topLeft = taskOffset,
        size = Size(
            width = taskWidth.toFloat(),
            height = taskHeight
        ),
        cornerRadius = CornerRadius(10f, 10f),
        alpha = 0.68f
    )

    // Draw task labels:
    val taskTitleTrimmed = taskTitle.trim()
    val fullText = taskStartTime + (if (taskEndTime != "null") " - $taskEndTime" else "") + "    " + taskTitleTrimmed
    val timeText = taskStartTime + (if (taskEndTime != "null") " - $taskEndTime" else "")

    val fullTextMeasure = textMeasurer.measure(fullText)
    val timeTextMeasure = textMeasurer.measure(timeText)
    val startTimeTextMeasure = textMeasurer.measure(taskStartTime)

    val fullTextWidth = fullTextMeasure.getBoundingBox(fullText.lastIndex).bottomRight.x
    val timeWidth = timeTextMeasure.getBoundingBox(timeText.lastIndex).bottomRight.x
    val startTimeWidth = startTimeTextMeasure.getBoundingBox(taskStartTime.lastIndex).bottomRight.x

    val pathPadding = 15f
    val textHeight = textMeasurer.measure("0").size.height * 1.15f // The 1.15f is for some extra padding
    var textStartOffset: Offset
    var textEndOffset: Offset

    val horizontalText: String
    val diagonalText: String

    // If the full text (time and title) are within the task's width, draw the text within the task area
    if ((fullTextWidth + pathPadding * 2) <= taskWidth) {
        horizontalText = fullText
        diagonalText = ""
    }
    // If the time text would take no more than half of the task area width
    else if ((timeWidth + pathPadding) <= taskWidth * 0.5f) {
        horizontalText = fullText
        diagonalText = ""
    }
    // If not even half of the full text fits within the task area, we're going to split the text into time and title sections.
    else if ((timeWidth + pathPadding * 2) <= taskWidth) {
        horizontalText = timeText
        diagonalText = taskTitleTrimmed
    }
    else if ((startTimeWidth + pathPadding * 2) <= taskWidth) {
        horizontalText = taskStartTime
        diagonalText = taskTitleTrimmed
    }
    else if (textHeight <= taskWidth) {
        horizontalText = ""
        diagonalText = taskTitleTrimmed
    }
    // If the task area is not wide enough to diagonally draw the title above it
    else {
        horizontalText = ""
        diagonalText = ""
    }

    var text: String
    var textWidth = 0f
    var textColor = android.graphics.Color.rgb(255, 255, 255)
    var textMeasure: TextLayoutResult

    // - draw horizontally (within the task area)
    if (horizontalText != "") {
        textStartOffset = Offset(
            x = taskOffset.x + pathPadding,
            y = taskOffset.y + taskHeight * 0.5f
        )
        textEndOffset = Offset(
            x = taskOffset.x + taskWidth - pathPadding,
            y = taskOffset.y + taskHeight * 0.5f
        )

        val horizontalPath = Path()
        horizontalPath.moveTo(
            x = textStartOffset.x,
            y = textStartOffset.y
        )
        horizontalPath.lineTo(
            x = textEndOffset.x,
            y = textEndOffset.y
        )

        val pathMeasure = PathMeasure()
        pathMeasure.setPath(
            path = horizontalPath,
            forceClosed = false
        )

        val horizontalTextMeasure = textMeasurer.measure(horizontalText)
        val horizontalTextWidth = horizontalTextMeasure.getBoundingBox(horizontalText.lastIndex).bottomRight.x

        // Truncate text if its length exceeds the path's length
        if (horizontalTextWidth >= pathMeasure.length) {
            text = truncateTextToPath(
                textToBeTruncated = horizontalText,
                textMeasurer = textMeasurer,
                pathMeasure = pathMeasure
            )
        } else {
            text = horizontalText
        }

        textMeasure = textMeasurer.measure(text = text)
        textWidth = textMeasure.getBoundingBox(text.lastIndex).bottomRight.x

        // Draw the task title only if it will fit within the task area
        if (taskWidth >= textMeasure.size.height) {
            this.drawContext.canvas.nativeCanvas.apply {
                drawTextOnPath(
                    text,
                    horizontalPath.asAndroidPath(),
                    0f,
                    textMeasure.size.height * 0.9f * 0.29f,// So that the path runs through the middle of the text's height
                    Paint().apply {
                        this.color = textColor
                        this.textSize = textMeasure.size.height * 0.9f
                        this.textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }

    // - draw diagonally (above the task area)
    if (diagonalText != "") {
        val diagonalTextMeasure = textMeasurer.measure(diagonalText)
        val diagonalTextWidth = diagonalTextMeasure.getBoundingBox(diagonalText.lastIndex).bottomRight.x
        // The number of times the text's height will fit within the task area width
        val timesTextWithinTaskWidth = taskWidth / textWidth

        // If it's a task, we're drawing the label upwards
        if (displayType == ActivityGraphDisplayType.Task) {
            textStartOffset = Offset(
                x = taskOffset.x + taskWidth * 0.5f - textHeight * 0.25f,
                y = taskOffset.y - pathPadding
            )
            textEndOffset = Offset(
                x = textStartOffset.x + ((canvasHeight * 0.15f) / sqrt(3.00)).toFloat(),
                y = 0f + pathPadding
            )
        }
        // If it's an activity, we're drawing the label downwards
        else {
            textStartOffset = Offset(
                x = taskOffset.x + taskWidth * 0.5f - textHeight * 0.25f,
                y = taskOffset.y + taskHeight + pathPadding
            )
            textEndOffset = Offset(
                x = textStartOffset.x + ((canvasHeight * 0.15f) / sqrt(3.00)).toFloat(),
                y = canvasHeight - pathPadding
            )
        }

        val diagonalPath = Path()
        diagonalPath.moveTo(
            x = textStartOffset.x,
            y = textStartOffset.y
        )
        diagonalPath.lineTo(
            x = textEndOffset.x,
            y = textEndOffset.y
        )
        val pathMeasure = PathMeasure()
        pathMeasure.setPath(
            path = diagonalPath,
            forceClosed = false
        )

        // Text to display on each line
        var lineTexts: List<String> = emptyList()

        // If the text doesn't fit within the path and there's space for more than just one line of text above the task area, we might want to divide the text into smaller chunks and draw these on parallel paths along the task area
        if (diagonalTextWidth >= pathMeasure.length && timesTextWithinTaskWidth > 1) {
            // The number of lines available to fit in the text
            val numberOfTextLines = ceil(taskWidth / textHeight).toInt()
            val textChunks = diagonalText.split("\\s".toRegex())
            var text = ""

            for (textChunk in textChunks) {
                // If the text is empty, add the text chunk even if it exceeds the path width
                val textWidth = textMeasurer.measure(text + " " + textChunk).size.width
                if (textWidth <= pathMeasure.length || text == "") {
                    text += " " + textChunk
                } else {
                    lineTexts = lineTexts + text.trim()
                    text = textChunk
                }
            }
            // Add the last text line
            lineTexts = lineTexts + text.trim()

            if (lineTexts.size > numberOfTextLines) lineTexts = lineTexts.subList(0, numberOfTextLines)

            // Truncate any texts that are too long
            lineTexts = lineTexts.map { lineText ->
                truncateTextToPath(lineText, textMeasurer, pathMeasure)
            }
        }
        // Otherwise, truncate text if its length exceeds the path's length
        else {
            lineTexts = lineTexts + truncateTextToPath(
                textToBeTruncated = diagonalText,
                textMeasurer = textMeasurer,
                pathMeasure = pathMeasure
            )
        }

        // Change color
        textColor = android.graphics.Color.rgb(102, 80, 164)
        // Horizontal offset to align the text to the left of the path (otherwise, the text is centered)
        var horizontalOffset: Float

        // If there's only one line of text to draw
        if (lineTexts.size == 1) {
            val text = lineTexts[0]
            textMeasure = textMeasurer.measure(text = text)
            textWidth = textMeasure.getBoundingBox(text.lastIndex).bottomRight.x
            horizontalOffset = (textMeasure.size.width - pathMeasure.length) * 0.5f

            this.drawContext.canvas.nativeCanvas.apply {
                drawTextOnPath(
                    text,
                    diagonalPath.asAndroidPath(),
                    horizontalOffset,
                    textMeasure.size.height * 0.9f * 0.29f,// So that the path runs through the middle of the text's height
                    Paint().apply {
                        this.color = textColor
                        this.textSize = textMeasure.size.height * 0.9f
                        this.textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
        // If there are multiple lines of text to draw
        else {
            // The width needed for all of the text lines along the task area. The subtracting of the textHeight * 0.25f centers the text line
            val textLinesWidth = textHeight * lineTexts.size - textHeight * 0.75f

            for (i in 0..lineTexts.size - 1) {
                // Redefine the path for each text line
                // Draw the task labels upwards and to the right
                if (displayType == ActivityGraphDisplayType.Task) {
                    textStartOffset = Offset(
                        // Subtracting half of the total textLinesWidth sets the offset to the first of the text lines, while (textHeight * i) moves the offset one textHeight at a time along the task area
                        x = taskOffset.x + ((taskWidth - textLinesWidth) * 0.5f) + (textHeight * i),
                        y = taskOffset.y - pathPadding
                    )
                    textEndOffset = Offset(
                        x = textStartOffset.x + ((canvasHeight * 0.15f) / sqrt(3.00)).toFloat(),
                        y = 0f + pathPadding
                    )
                }
                // Draw the activity labels downwards and to the left
                else {
                    textStartOffset = Offset(
                        x = taskOffset.x + taskWidth - ((taskWidth - textLinesWidth) * 0.5f) - (textHeight * i),
                        y = taskOffset.y + taskHeight + pathPadding
                    )
                    textEndOffset = Offset(
                        x = textStartOffset.x + ((canvasHeight * 0.15f) / sqrt(3.00)).toFloat(),
                        y = canvasHeight - pathPadding
                    )
                }
                // I need to create a new path for each text. If I reuse the previously defined path, the texts will be drawn in the same place
                val diagonalPath = Path()
                diagonalPath.moveTo(
                    x = textStartOffset.x,
                    y = textStartOffset.y
                )
                diagonalPath.lineTo(
                    x = textEndOffset.x,
                    y = textEndOffset.y
                )
                pathMeasure.setPath(
                    path = diagonalPath,
                    forceClosed = false
                )

                val text = lineTexts[i]
                Log.i("text", text)
                textMeasure = textMeasurer.measure(text = text)
                textWidth = textMeasure.getBoundingBox(text.lastIndex).bottomRight.x
                horizontalOffset = (textMeasure.size.width - pathMeasure.length) * 0.5f

                this.drawContext.canvas.nativeCanvas.apply {
                    drawTextOnPath(
                        text,
                        diagonalPath.asAndroidPath(),
                        horizontalOffset,
                        textMeasure.size.height * 0.9f * 0.29f,// So that the path runs through the middle of the text's height
                        Paint().apply {
                            this.color = textColor
                            this.textSize = textMeasure.size.height * 0.9f
                            this.textAlign = Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

// Truncate the text to fit the given path's length
fun truncateTextToPath(textToBeTruncated: String, textMeasurer: TextMeasurer, pathMeasure: PathMeasure): String {
    if (textToBeTruncated == "") return textToBeTruncated

    val textToBeTruncatedMeasure = textMeasurer.measure(textToBeTruncated)
    val textToBeTruncatedWidth = textToBeTruncatedMeasure.getBoundingBox(textToBeTruncated.lastIndex).bottomRight.x

    if (textToBeTruncatedWidth <= pathMeasure.length) return textToBeTruncated

    val ellipsis = "\u2026"
    val ellipsisMeasure = textMeasurer.measure(text = ellipsis)
    var textChars = mutableListOf<Char>()
    var text: String
    var textWidth = 0f

    textToBeTruncated.forEachIndexed { index, char ->
        if ((textWidth + ellipsisMeasure.size.width) < pathMeasure.length) {
            textChars.add(char)
            val charBoundingBox = textToBeTruncatedMeasure.getBoundingBox((index))
            textWidth = charBoundingBox.bottomRight.x
        }
    }

    text = buildAnnotatedString {
        textChars.forEachIndexed { index, char ->
            if (index < textChars.size - 1) {
                append(char)
            }
        }
    }.toString()
    // Remove leading and trailing whitespaces
    text = text.trim()

    text = buildAnnotatedString{
        append(text)
        // Append ellipsis
        append(ellipsis)
    }.toString()

    return text
}