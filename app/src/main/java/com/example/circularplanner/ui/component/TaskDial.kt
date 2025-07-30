package com.example.circularplanner.ui.component

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.ui.viewmodel.UserInput
import com.example.circularplanner.utils.AngleMode
import com.example.circularplanner.utils.TaskMode
import com.example.circularplanner.utils.TouchGestureUtils
import com.example.circularplanner.utils.TouchGestureUtils.DEG_OFFSET
import com.example.circularplanner.utils.TouchGestureUtils.DEG_TO_RAD
import com.example.circularplanner.utils.TouchGestureUtils.TOUCH_STROKE
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.util.UUID
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

const val SECONDS_IN_MINUTE = 60
const val HOUR_LABEL_COLOR = 0xFF6650a4
const val MINUTE_STEP_COLOR = 0xFFcac3de
const val CLOCK_LABEL_COLOR = 0xff592687
const val CLOCK_CENTER_COLOR = 0xffffffff

@Composable
fun TaskDial(
    dayState: DayState,
    taskUiState: TaskUiState,
    userInput: UserInput,
    onNavigateToTaskEdit: () -> Unit,
    onNavigateToTaskInfo: () -> Unit,
    onPressActiveTime: () -> Unit,
    onSetTaskEndTime: (Time) -> Unit,
    onSetTaskStartTime: (Time) -> Unit,
    saveTask: () -> Unit,
    selectTask: (UUID?) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val activeTimeStart: Time = dayState.activeTimeStart
    val activeTimeEnd: Time = dayState.activeTimeEnd
    var taskMode: TaskMode by remember { mutableStateOf(TaskMode.CREATE) }
    var angleMode: AngleMode by remember { mutableStateOf(AngleMode.NONE) }
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

    var clockCenterTime by remember { mutableStateOf(Time(LocalTime.now().hour, LocalTime.now().minute)) }
    // Draw a pointer on the dial to illustrate the finger's movement along the dial edge

    val tasks = dayState.tasks

    fun checkIfTouchWithinTaskArea(angle: Float, tasks: List<Task>): Boolean {
        // The task stores the appropriate angle values, i.e. values corresponding to how the circle is drawn (the 0 degree starts at the right-hand side (east) of the circle). We want to 'correct' these angles as if 0 degree starts at the top of the circle (north)
        var isTouchWithinAnyTask = false

        for (task in tasks) {
            val taskStartAngle = TouchGestureUtils.calculateTaskAngle (
                activeTimeStart,
                task.startTime,
                minuteAngle
            )
            val taskEndAngle = TouchGestureUtils.calculateTaskAngle (
                activeTimeStart,
                task.endTime,
                minuteAngle
            )
            isTouchWithinAnyTask =
                TouchGestureUtils.checkIfTouchWithinAngleRange(angle, taskStartAngle, taskEndAngle)

            if (isTouchWithinAnyTask) {
                selectTask(task.id)
                touchedTask = task.copy()

                return isTouchWithinAnyTask
            }
        }

        return isTouchWithinAnyTask
    }

    fun resetDialParameters() {
        touchNearTheDialEdge = false
        drawNewTaskTimeRange = false
        touchWithinTaskArea = false
        selectTask(null)
    }

    fun resetClockCenter() {
        clockCenterTime = Time(LocalTime.now().hour, LocalTime.now().minute)
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
    }

    fun reset() {
        resetDialParameters()
        resetClockCenter()
        resetStartAndEndAngles()
        resetTask()
    }

    // Update the clock every minute
    LaunchedEffect(true) {
        while (true) {
            delay(1000L * SECONDS_IN_MINUTE)
            clockCenterTime = Time(LocalTime.now().hour, LocalTime.now().minute)
        }
    }

    // Clear the task UI state when the component is loaded for the first time
    LaunchedEffect(Unit) {
        selectTask(null)// TODO: This should clear the task UI state after coming back from the TaskInfoScreen, but it will not do anything when the user drags task along the dial, since then the component is not drawn for the first time (instead, it's redrawn). This could be solved if for example I cleared the task UI state based on the component's state
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
//            .defaultMinSize(
//                minWidth = 300.dp,
//                minHeight = 300.dp
//            )
//            .widthIn(min = 300.dp)
//            .heightIn(min = 300.dp)
    ) {
        Canvas (
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    width = it.size.width
                    height = it.size.height
                    center = Offset(width / 2f, height / 2f)
                    // The radius of the dial
                    outerRadius = min(width.toFloat(), height.toFloat()) / 2f
                    // The radius from the center to the clock steps
                    innerRadius = outerRadius * .8f
                    // The radius of the clock center (the one that displays time)
                    centerRadius = outerRadius * 0.25f
                }
                .pointerInput(dayState, taskUiState, userInput) {
                    // Defining time range: TASK CREATION
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            // Get the starting coordinates and determine if the touch is:
                            // 1. within the dial
                            // 2. within any existing task area
                            val distance = TouchGestureUtils.distance(offset, center)
                            angle = TouchGestureUtils.angle(center, offset)

                            // Clear the task UI state
                            selectTask(null)

                            if (taskMode == TaskMode.CREATE) {
                                touchNearTheDialEdge =
                                    TouchGestureUtils.checkIfTouchNearDialEdge(
                                        distance,
                                        innerRadius,
                                        outerRadius,
                                        touchStroke
                                    )

                                if (touchNearTheDialEdge) {
                                    startAngle = angle
                                    tmpStartAngle = startAngle
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            if (taskMode == TaskMode.CREATE) {
                                // If touch is within dial, keep track of the coordinate
                                val offset = change.position
                                val distance = TouchGestureUtils.distance(offset, center)
                                touchNearTheDialEdge =
                                    TouchGestureUtils.checkIfTouchNearDialEdge(
                                        distance,
                                        innerRadius,
                                        outerRadius,
                                        touchStroke
                                    )

                                if (touchNearTheDialEdge) {
                                    drawNewTaskTimeRange = true
                                    val currentAngle = TouchGestureUtils.angle(center, offset)
                                    angle = currentAngle
                                    endAngle = currentAngle
                                    tmpEndAngle = endAngle

                                    // Calculate the time represented by the angle to display it in the clock center
                                    val minute = TouchGestureUtils.calculateMinutes(
                                        TouchGestureUtils.angleForTimeCalculation(angle),
                                        minuteAngle
                                    )
                                    clockCenterTime =
                                        TouchGestureUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                            start = activeTimeStart,
                                            minutes = minute
                                        )
                                }
                            }
                        },
                        onDragEnd = {
                            if (taskMode == TaskMode.CREATE) {
                                if (touchNearTheDialEdge) {
                                    // The number of minutes from start active start time
                                    val taskStartTimeMinute =
                                        TouchGestureUtils.calculateMinutes(
                                            TouchGestureUtils.angleForTimeCalculation(startAngle),
                                            minuteAngle
                                        )
                                    // Clock time the number of minutes corresponds to
                                    val clockTaskStartTime =
                                        TouchGestureUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                            activeTimeStart,
                                            taskStartTimeMinute
                                        )
                                    val taskEndTimeMinute = TouchGestureUtils.calculateMinutes(
                                        TouchGestureUtils.angleForTimeCalculation(endAngle),
                                        minuteAngle
                                    )
                                    val clockTaskEndTime =
                                        TouchGestureUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                            activeTimeStart,
                                            taskEndTimeMinute
                                        )

                                    onSetTaskStartTime(clockTaskStartTime)
                                    onSetTaskEndTime(clockTaskEndTime)

                                    onNavigateToTaskEdit()
                                }
                            } else {
                                reset()
                            }
                        }
                    )
                }
                .pointerInput(dayState, taskUiState, userInput) {
                    detectTapGestures(
                        onTap = { offset ->
                            val distance = TouchGestureUtils.distance(offset, center)
                            touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
                                distance,
                                centerRadius,
                                innerRadius,
                                touchStroke
                            )
                            angle = TouchGestureUtils.angle(center, offset)

                            if (touchInsideTheDial) {
                                if (taskMode == TaskMode.CREATE) {
                                    // Check whether the touch is within the ANY task area
                                    touchWithinTaskArea =
                                        checkIfTouchWithinTaskArea(
                                            angle,
                                            tasks
                                        )

                                    if (touchWithinTaskArea) {
                                        // Show task info
                                        onNavigateToTaskInfo()
                                    }
                                } else if (taskMode == TaskMode.EDIT) {
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
                                        val startMinute = TouchGestureUtils.calculateMinutes(
                                            TouchGestureUtils.angleForTimeCalculation(
                                                tmpStartAngle
                                            ),
                                            minuteAngle
                                        )
                                        val clockTaskStartTime =
                                            TouchGestureUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                start = activeTimeStart,
                                                minutes = startMinute
                                            )
                                        // - end time
                                        val endMinute = TouchGestureUtils.calculateMinutes(
                                            TouchGestureUtils.angleForTimeCalculation(
                                                tmpEndAngle
                                            ),
                                            minuteAngle
                                        )
                                        val clockTaskEndTime =
                                            TouchGestureUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                start = activeTimeStart,
                                                minutes = endMinute
                                            )

                                        onSetTaskStartTime(clockTaskStartTime)
                                        onSetTaskEndTime(clockTaskEndTime)
                                        saveTask()

                                        // Switch the task mode to CREATE and reset
                                        taskMode = TaskMode.CREATE
                                        reset()
                                    }
                                }
                            } else {
                                // Cancel everything if touch is outside the dial
                                taskMode = TaskMode.CREATE
                                reset()
                            }
                        },
                        onDoubleTap = { offset ->
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
                                    checkIfTouchWithinTaskArea(
                                        angle,
                                        tasks
                                    )

                                if (touchWithinTaskArea) {
                                    taskMode = TaskMode.EDIT
                                    val taskStartAngle = TouchGestureUtils.calculateTaskAngle(
                                        activeTimeStart,
                                        touchedTask!!.startTime,
                                        minuteAngle
                                    )
                                    val taskEndAngle = TouchGestureUtils.calculateTaskAngle(
                                        activeTimeStart,
                                        touchedTask!!.endTime,
                                        minuteAngle
                                    )
                                    tmpStartAngle = taskStartAngle
                                    tmpEndAngle = taskEndAngle

                                    // Draw this task separately than other tasks
                                    drawNewTaskTimeRange = true
                                }
                            } else {
                                // Cancel everything if touch is outside the dial
                                reset()
                            }
                        },
                    )
                }
                .pointerInput(dayState, taskUiState, userInput) {
                    // Defining time range: TASK EDITING
                    detectDragGestures(
                        onDragStart = { offset ->
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
                            // Engage only if in task EDITING mode
                            if (taskMode == TaskMode.EDIT) {
                                // If touch is within dial, keep track of the coordinate
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

                                    // If we don't know which time boundary of the given task we are setting
                                    if (angleMode == AngleMode.NONE) {
                                        // Determine which time boundary of the given task we are setting: start or end time
                                        // It seems that an angle == tmpStartAngle comparison does not cut it - the app is not able to detect the moment when the two angles are equal
                                        if (angle in (tmpStartAngle - touchStroke / 2f)..(tmpStartAngle + touchStroke / 2f)) {
                                            angleMode = AngleMode.START
                                        } else if (angle in (tmpEndAngle - touchStroke / 2f)..(tmpEndAngle + touchStroke / 2f)) {
                                            angleMode = AngleMode.END
                                        }
                                    }
                                    // If we do know which time boundary of the given task we are setting
                                    else {
                                        // If we are setting the start time of the given task
                                        if (angleMode == AngleMode.START) {
                                            startAngle = angle
                                            tmpStartAngle = startAngle

                                            val startMinute = TouchGestureUtils.calculateMinutes(
                                                TouchGestureUtils.angleForTimeCalculation(
                                                    tmpStartAngle
                                                ),
                                                minuteAngle
                                            )
                                            val clockTaskStartTime =
                                                TouchGestureUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                    start = activeTimeStart,
                                                    minutes = startMinute
                                                )

                                            // If I update the task end time in the task UI state for saving purposes (using the onSetTaskStartTime(clockTaskStartTime) method), it interrupts the drawing. So I update the final start- and end time of the task just before saving it.
                                            // Update the task start time in the local state so that changes are drawn accordingly
                                            touchedTask!!.startTime = clockTaskStartTime
                                        }
                                        // If we are setting the end time of the given task
                                        else if (angleMode == AngleMode.END) {
                                            endAngle = angle
                                            tmpEndAngle = endAngle

                                            val endMinute = TouchGestureUtils.calculateMinutes(
                                                TouchGestureUtils.angleForTimeCalculation(
                                                    tmpEndAngle
                                                ),
                                                minuteAngle
                                            )
                                            val clockTaskEndTime =
                                                TouchGestureUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                    start = activeTimeStart,
                                                    minutes = endMinute
                                                )

                                            // If I update the task end time in the task UI state for saving purposes (using the onSetTaskEndTime(clockTaskEndTime) method), it interrupts the drawing. So I update the final start- and end time of the task just before saving it.
                                            // Update the task end time in the local state so that changes are drawn accordingly
                                            touchedTask!!.startTime = clockTaskEndTime
                                        }

                                        // Show the clock time the angle corresponds to
                                        val minute = TouchGestureUtils.calculateMinutes(
                                            TouchGestureUtils.angleForTimeCalculation(angle),
                                            minuteAngle
                                        )
                                        clockCenterTime =
                                            TouchGestureUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                start = activeTimeStart,
                                                minutes = minute
                                            )
                                    }
                                }
                            }

                        },
                        onDragEnd = {
                            if (touchNearTheDialEdge || touchInsideTheDial) {
                                // Reset the angle mode
                                angleMode = AngleMode.NONE
                                // Set new startAngle and endAngle values
                                startAngle = tmpStartAngle
                                endAngle = tmpEndAngle
                            } else {
                                reset()
                            }
                        }
                    )
                }
        ) {
            val minutesBetweenHoursAccumulated: Array<Int> =
                TouchGestureUtils.calculateMinutesBetweenHoursAccumulated(
                    activeTimeStart,
                    activeTimeEnd
                )
            val activeTimeHourSteps: Array<Time> = TouchGestureUtils.createClockHoursArray(
                activeTimeStart,
                activeTimeEnd
            )
            outerRadius = min(width, height) / 2f * 0.8f
            innerRadius = outerRadius * .8f

            if (drawNewTaskTimeRange) {

                drawNewTaskArea(
                    startAngle = tmpStartAngle,
                    size = size,
                    outerRadius = outerRadius - 40f,
                    sweepAngle = TouchGestureUtils.sweepAngle(
                        tmpStartAngle,
                        tmpEndAngle
                    )
                )
            }

            for (task in tasks) {
                // We have to offset these angles because startMinute * taskDialState.minuteAngle returns a biased angle
                val taskStartAngle = TouchGestureUtils.calculateTaskAngle (
                    activeTimeStart,
                    task.startTime,
                    minuteAngle
                )
                val taskDuration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                    task.startTime,
                    task.endTime
                )

                // Don't draw the task whose time bounds are being edited
                if (!(task.id == touchedTask?.id && taskMode == TaskMode.EDIT)) {
                    drawTask(
                        taskStartAngle = taskStartAngle,
                        minuteAngle = minuteAngle,
                        innerRadius = centerRadius,
                        outerRadius = outerRadius - 40f,
                        taskDuration = taskDuration,
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
                label = clockCenterTime
            )
        }

        // The Box has to be defined after the Canvas in order to be placed on the Canvas and be clickable
        Box (//FIXME: The Box's position is not fixed - it moves in relation to the canvas depending on the size of the component
            modifier = Modifier
                .fillMaxSize(0.16f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(activeTimeColor)
                .clickable {
                    // Show active time setup
                    onPressActiveTime()
                }
                .align(Alignment.TopCenter)
        ) {
            val hourStepLabel = buildAnnotatedString {
                append("%d:%02d".format(activeTimeStart.hour, activeTimeStart.minute))
                appendLine()
                append("%d:%02d".format(activeTimeEnd.hour, activeTimeEnd.minute))
            }

            Text(
                text = hourStepLabel,
                modifier = Modifier
                    .align(Alignment.Center),
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

fun DrawScope.drawClockCenter(
    textMeasurer: TextMeasurer,
    radius: Float,
    fontSize: TextUnit = 26.sp,
    label: Time,
) {
    // Draw clock center
    drawCircle(
        color = Color(CLOCK_CENTER_COLOR),
        center = center,
        radius = radius,
    )

    val clockHourLabel = buildAnnotatedString {
        withStyle(style = SpanStyle(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )) {
            append("%d:%02d".format(label.hour, label.minute))
        }
    }

    val hourStepLabelTextLayout = textMeasurer.measure(
        text = clockHourLabel,
        style = TextStyle()
    )

    var clockCenterLabelOffset = Offset(
        center.x - hourStepLabelTextLayout.size.width / 2f,
        center.y - hourStepLabelTextLayout.size.height / 2f
    )

    // Draw clock center label
    drawText(
        textMeasurer = textMeasurer,
        text = clockHourLabel,
        topLeft = clockCenterLabelOffset,
        style = TextStyle(
            color = Color(CLOCK_LABEL_COLOR)
        )
    )
}

fun DrawScope.drawHourStepsAndLabels(
    minutesBetweenHoursAccumulated: Array<Int>,
    minuteAngle: Float,
    outerRadius: Float,
    activeTimeHourSteps: Array<Time>,
    textMeasurer: TextMeasurer
) {
    val textStyle = TextStyle(
        textAlign = TextAlign.Center,
        color = Color(HOUR_LABEL_COLOR),
    )

    // Draw hour steps and labels
    for (i in 0..(minutesBetweenHoursAccumulated.size - 2)) {
        // Draw hour steps
        var stepAngle = minutesBetweenHoursAccumulated[i] * minuteAngle + DEG_OFFSET

        // Draw hour label
        var hourStep = activeTimeHourSteps[i]
        // Let only the first and last label display the hour and minute values - other labels will display only the hour value
        val hourStepLabel = buildAnnotatedString {
            append("%d".format(hourStep.hour))
        }
        val hourStepLabelTextLayout = textMeasurer.measure(
            text = hourStepLabel,
            style = textStyle
        )

        // Calculate the angle of the step
        stepAngle = minutesBetweenHoursAccumulated[i] * minuteAngle + DEG_OFFSET
        var stepLabelOffset = Offset(
            x = center.x + (outerRadius * cos(stepAngle * DEG_TO_RAD)).toFloat(),
            y = center.y + (outerRadius * sin(stepAngle * DEG_TO_RAD)).toFloat()
        )
        // Subtract the label width and height to position label at the center of the step
        stepLabelOffset = Offset(
            // Subtracting from the x moves the element to the left
            stepLabelOffset.x - ((hourStepLabelTextLayout.size.width) / 2f),
            // Subtracting from the y moves the element up
            stepLabelOffset.y - (hourStepLabelTextLayout.size.height / 2f)
        )

        if (i > 0) {
            drawText(
                textMeasurer = textMeasurer,
                text = hourStepLabel,
                topLeft = stepLabelOffset,
                style = textStyle
            )
        } else {
            var circleCenterOffset = Offset(
                x = center.x + (outerRadius * cos(stepAngle * DEG_TO_RAD)).toFloat(),
                y = center.y + (outerRadius * sin(stepAngle * DEG_TO_RAD)).toFloat()
            )

            // Draw only a circle to mark the start/end of active time
            drawCircle(
                color = Color(MINUTE_STEP_COLOR),
                radius = 15f,
                center = circleCenterOffset
            )
        }
    }
}

fun DrawScope.drawMinuteSteps(
    minuteAngle: Float,
    totalMinutes: Int,
    minutesBetweenHoursAccumulated: Array<Int>,
    outerRadius: Float
) {
    // Draw minute (1-, 5-, 10-, 15- or 30-minute) steps
    var minuteStep: Int
    minuteStep = if (minuteAngle > 5f) {
        1
    } else if (minuteAngle in 1f..5f) {
        5
    } else if (minuteAngle in 0.5f..1f) {
        10
    } else if (minuteAngle in 0.25f..0.5f) {
        15
    } else {
        30
    }

    for (i in 0..totalMinutes) {
        if (i % minuteStep == 0 && !(i in minutesBetweenHoursAccumulated)) {
            var stepAngle = i * minuteAngle + DEG_OFFSET

            val stepEndOffset = Offset(
                x = center.x + (outerRadius * cos(stepAngle * DEG_TO_RAD)).toFloat(),
                y = center.y + (outerRadius * sin(stepAngle * DEG_TO_RAD)).toFloat()
            )

            drawCircle(
                color = Color(MINUTE_STEP_COLOR),
                radius = 5f,
                center = stepEndOffset,
            )
        }
    }
}

fun DrawScope.drawNewTaskArea(
    startAngle: Float,
    size: Size,
    outerRadius: Float,
    sweepAngle: Float
) {
    val rainbowColors = listOf(
        Color(0xfff78f0a),
        Color(0xffc4067c),
        Color(0xff06aac4),
        Color(0xff06aac4),
        Color(0xfff78f0a),
        Color(0xfff78f0a),
    )

    val gradient = Brush.sweepGradient(
        colors = rainbowColors
    )

    drawArc(
        brush = gradient,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        Offset(size.width / 2 - outerRadius, size.height / 2 - outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        alpha = 0.3f
    )
}

fun DrawScope.drawTask(
    taskStartAngle: Float,
    minuteAngle: Float,
    innerRadius: Float,
    outerRadius: Float,
    taskDuration: Int,
    taskTitle: String,
    textMeasurer: TextMeasurer,
    canvasWidth: Int,
    canvasHeight: Int
) {
    val rainbowColors = listOf(
        Color(0xfff78f0a),
        Color(0xffc4067c),
        Color(0xff06aac4),
        Color(0xff06aac4),
        Color(0xfff78f0a),
        Color(0xfff78f0a),
    )

    val gradient = Brush.sweepGradient(
        colors = rainbowColors
    )

    // Draw task area
    drawArc(
        brush = gradient,
        startAngle = taskStartAngle,
        sweepAngle = (taskDuration * minuteAngle).toFloat(),
        useCenter = true,
        Offset(size.width / 2 - outerRadius, size.height / 2 - outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        alpha = 0.68f
    )

    // Draw title
    val path = Path()
    // The angle in the middle of the task area
    var middleAngle = taskStartAngle + (taskDuration * 0.5f * minuteAngle)
    // Correct the middle angle if it exceeds the 360 degree value. Otherwise, values over 360 are not drawn properly
    if (middleAngle > 360f) {
        middleAngle -= 360f
    }
    val pathPadding: Float

    // If the task duration is long enough, we want to draw text along a curve
    // Define arc path
    if ((taskDuration * minuteAngle).toFloat() > 45f) {
        val titleAngle: Float
        val sweepAngleDegrees: Float
        pathPadding = 3f

        if (middleAngle in 180f..360f) {
            titleAngle = (taskStartAngle).toFloat() + pathPadding
            sweepAngleDegrees = ((taskDuration - 5 * pathPadding) * minuteAngle).toFloat()
        } else {
            titleAngle = taskStartAngle + (taskDuration * minuteAngle).toFloat() - pathPadding
            sweepAngleDegrees = -((taskDuration - 5 * pathPadding) * minuteAngle).toFloat()
        }

        path.arcTo(
            rect = Rect(
                left = (canvasWidth / 2f) - (outerRadius * 0.8f),
                top = (canvasHeight / 2f) - (outerRadius * 0.8f),
                right = (canvasWidth / 2f) + (outerRadius * 0.8f),
                bottom = (canvasHeight / 2f) + (outerRadius * 0.8f)
            ),
            startAngleDegrees = titleAngle,
            sweepAngleDegrees = sweepAngleDegrees,
            forceMoveTo = false
        )
    }
    // Define radius path
    else {
        var startRadius: Float
        var endRadius: Float
        pathPadding = textMeasurer.measure("00").size.width.toFloat()

        // Draw the text from the center out...
        if (middleAngle in 270f..360f || middleAngle in 0f..90f) {
            // Add some padding to the beginning of the path
            startRadius = innerRadius + pathPadding
            // Subtract the width of hour labels so that the task title doesn't overlap with an hour label
            endRadius = outerRadius - pathPadding
        }
        // ...or from the outside in
        else {
            startRadius = outerRadius - pathPadding
            endRadius = innerRadius + pathPadding
        }
        val titleStartOffset = Offset(
            x = center.x + (startRadius * cos(middleAngle * DEG_TO_RAD)).toFloat(),
            y = center.y + (startRadius * sin(middleAngle * DEG_TO_RAD)).toFloat()
        )
        val titleEndOffset = Offset(
            x = center.x + (endRadius * cos(middleAngle * DEG_TO_RAD)).toFloat(),
            y = center.y + (endRadius * sin(middleAngle * DEG_TO_RAD)).toFloat()
        )

        path.moveTo(
            x = titleStartOffset.x,
            y = titleStartOffset.y
        )
        path.lineTo(
            x = titleEndOffset.x,
            y = titleEndOffset.y
        )
    }

    val pathMeasure = PathMeasure()
    pathMeasure.setPath(
        path = path,
        forceClosed = false
    )
    val titleMeasure = textMeasurer.measure(text = taskTitle)
    val titleWidth: Float
    if (taskTitle != "") {
        titleWidth = titleMeasure.getBoundingBox(taskTitle.lastIndex).bottomRight.x
    } else {
        titleWidth = 0f
    }

    var dialText: String
    var dialTextWidth = 0f
    val ellipsis = "\u2026"
    val ellipsisMeasure = textMeasurer.measure(text = ellipsis)

    // Truncate text if its length exceeds the path's length
    if (titleWidth >= pathMeasure.length) {
        var dialTextChars = mutableListOf<Char>()

        taskTitle.forEachIndexed { index, char ->
            if ((dialTextWidth + ellipsisMeasure.size.width) < pathMeasure.length) {
                dialTextChars.add(char)
                val charBoundingBox = titleMeasure.getBoundingBox((index))
                dialTextWidth = charBoundingBox.bottomRight.x
            }
        }

        dialText = buildAnnotatedString {
            dialTextChars.forEachIndexed { index, char ->
                if (index < dialTextChars.size - 1) {
                    append(char)
                }
            }
        }.toString()
        // Remove leading and trailing whitespaces
        dialText = dialText.trim()

        dialText = buildAnnotatedString{
            append(dialText)
            // Append ellipsis
            append(ellipsis)
        }.toString()
    } else {
        dialText = taskTitle
    }

    val measuredText = textMeasurer.measure(text = dialText)

    if (dialText != "") {
        dialTextWidth = measuredText.getBoundingBox(dialText.lastIndex).bottomRight.x
    }

    // TODO: Narrow the text's height towards the center of the circle
    // TODO: Add task duration (?)

    this.drawContext.canvas.nativeCanvas.apply {
        drawTextOnPath(
            dialText,
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


