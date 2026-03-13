package com.eternalfairy.timeaware.ui.component

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.timeaware.R
import com.eternalfairy.timeaware.data.Time
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.viewmodel.DayState
import com.eternalfairy.timeaware.utils.DrawScopeUtils.drawClockCenter
import com.eternalfairy.timeaware.utils.DrawScopeUtils.drawClockHand
import com.eternalfairy.timeaware.utils.DrawScopeUtils.drawHourStepsAndLabels
import com.eternalfairy.timeaware.utils.DrawScopeUtils.drawMinuteSteps
import com.eternalfairy.timeaware.utils.DrawScopeUtils.drawTask
import com.eternalfairy.timeaware.utils.TouchGestureUtils
import com.eternalfairy.timeaware.utils.TouchGestureUtils.TOUCH_STROKE
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.util.UUID
import kotlin.math.min

@Composable
fun ComparisonDial(
    componentHeight: Dp = 450.dp,
    componentWidth: Dp = 400.dp,
    dayState: DayState,
    drawClockHand: Boolean = false,
    onNavigateToTaskActivityComparison: () -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    // The actual start and end time of the day are for the cases when the activities start or end before or after the planned active time, respectively
    val activeTimeStart: Time = dayState.actualActiveTimeStart ?: dayState.activeTimeStart
    val activeTimeEnd: Time = dayState.actualActiveTimeEnd?.let {TouchGestureUtils.addMinutesToTime(1, dayState.actualActiveTimeEnd)} ?: dayState.activeTimeEnd //FIXME: I need to add this one minute at the end of the actual active time end for the activity to draw correctly - otherwise, the activity's title isn't drawn

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
    val minuteAngle: Float = 360 / totalMinutes.toFloat()

    // The width and height of the Canvas
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    var angle by remember { mutableStateOf(0f) }
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

    var clockTime by remember { mutableStateOf(Time(LocalTime.now().hour, LocalTime.now().minute)) }

    val tasks = dayState.tasks
    val activities = dayState.activities

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val scaleDownButtonAlpha by animateFloatAsState(
        targetValue = if (scale > 1f || offset != Offset.Zero) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = ""
    )

    fun selectTaskAndActivity(time: Time) {
        // Select task
        for (task in tasks) {
            val isTouchedTimeInTaskRange = TouchGestureUtils.checkIfTimeInRange(
                time = time,
                rangeStart = task.startTime!!,
                rangeEnd = task.endTime!!
            )
            TouchGestureUtils.checkIfTouchWithinTaskArea(
                angle,
                clockStart = activeTimeStart,
                clockEnd = activeTimeEnd,
                minuteAngle = minuteAngle,
                taskStart = task.startTime!!,
                taskEnd = task.endTime!!
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

    Column(
        modifier = Modifier
            .height(componentHeight)
            .width(componentWidth)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
            .background(COMPONENT_BACKGROUND_COLOR)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box (
            modifier = Modifier
            ,
            contentAlignment = Alignment.TopEnd
        ) {
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
                    .pointerInput(dayState) {
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
                    .pointerInput(dayState) {
                        detectTapGestures(
                            onTap = { offset ->
                                // Clear the task- and activity UI states
                                selectTask(null)
                                selectActivity(null)

                                Log.i("TaskDial", "detectTapGestures onTap")
                                val distance = TouchGestureUtils.distance(offset, center)
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
                                angle = TouchGestureUtils.angle(center, offset)

                                if (touchInsideTheDial || touchNearTheDialEdge) {
                                    // Determine what time the touch offset corresponds to
                                    val time = TouchGestureUtils.calculateTimeFromAngle(
                                        angle = angle,
                                        minuteAngle = minuteAngle,
                                        clockStart = activeTimeStart
                                    )
                                    // Select task and activity
                                    selectTaskAndActivity(time)

                                    // Navigate to the task and activity comparison screen
                                    onNavigateToTaskActivityComparison()
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

                for (activity in activities) {
                    // We have to offset these angles because startMinute * activityDialState.minuteAngle returns a biased angle
                    val activityStartAngle = TouchGestureUtils.calculateAngleFromTime (
                        activeTimeStart,
                        activeTimeEnd,
                        activity.startTime,
                        minuteAngle
                    )
                    val activityEndAngle = TouchGestureUtils.calculateAngleFromTime (
                        activeTimeStart,
                        activeTimeEnd,
                        activity.endTime ?: Time(LocalTime.now().hour, LocalTime.now().minute),//TODO: Test me
                        minuteAngle
                    )
                    val activityDuration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                        activity.startTime,
                        activity.endTime ?: Time(LocalTime.now().hour, LocalTime.now().minute)//TODO: Test me
                    )

                    drawTask(
                        taskEndAngle = activityEndAngle,
                        taskStartAngle = activityStartAngle,
                        minuteAngle = minuteAngle,
                        innerRadius = outerRadius * 0.5f + taskPadding * 1.35f,
                        outerRadius = outerRadius - taskPadding,
                        taskDurationInMinutes = activityDuration,
                        taskTitle = activity.title,
                        textMeasurer = textMeasurer,
                        canvasWidth = width,
                        canvasHeight = height
                    )
                }

                // A background arc to visually separate the activities from tasks - the arcs are drawn one on top of the other in the order: activity, background, task
                val whiteArcOuterRadius = outerRadius * 0.5f + taskPadding * 1.35f
                drawArc(
                    color = COMPONENT_BACKGROUND_COLOR,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = true,
                    topLeft = Offset(size.width / 2 - whiteArcOuterRadius, size.height / 2 - whiteArcOuterRadius),
                    size = Size(whiteArcOuterRadius * 2, whiteArcOuterRadius * 2),
                    alpha = 1f
                )

                for (task in tasks) {
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
                        outerRadius = outerRadius * 0.5f + taskPadding * 1.15f,
                        taskDurationInMinutes = taskDuration,
                        taskTitle = task.title,
                        textMeasurer = textMeasurer,
                        canvasWidth = width,
                        canvasHeight = height
                    )
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

            IconButton(
                onClick = {
                    scale = 1f
                    offset = Offset.Zero
                },
                modifier = Modifier
                    .alpha(scaleDownButtonAlpha)
                    .padding(5.dp)
                    .clip(CircleShape)
                    .background(COMPONENT_BACKGROUND_COLOR)
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


}