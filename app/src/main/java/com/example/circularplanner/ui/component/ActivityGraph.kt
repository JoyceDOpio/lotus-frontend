package com.example.circularplanner.ui.component

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.screen.Type
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.utils.TouchGestureUtils
import com.example.circularplanner.utils.TouchGestureUtils.TOUCH_STROKE
import java.util.UUID
import kotlin.Int
import kotlin.math.ceil
import kotlin.math.sqrt

const val ACTIVITY_MINUTE_STEP_COLOR = 0xffb4acbd

@Composable
fun ActivityGraph (
    dayState: DayState,
    height: Dp = 320.dp,// Minimum height is 300.dp - at 280.dp there is a problem with index out of bounds
    onNavigateToTaskActivityComparison: () -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit
) {
    val tasks = dayState.tasks
    val activities = dayState.activities

    var canvasWidth by remember { mutableStateOf(0.dp) }
    var canvasHeight by remember { mutableStateOf(0f) }
    var canvasHeightDp by remember { mutableStateOf(0.dp) }

    val activeTimeStart: Time = dayState.activeTimeStart
    val activeTimeEnd: Time = dayState.activeTimeEnd

    val textMeasurer = rememberTextMeasurer()
    val localDensity = LocalDensity.current

    val axisHorizontalPadding = 70.dp
    val minuteWidth = (1.5).dp
    val totalMinutes = TouchGestureUtils.calculateTotalNumberOfMinutes(
        activeTimeStart,
        activeTimeEnd
    )
    canvasWidth = axisHorizontalPadding * 2 + minuteWidth * totalMinutes
    val canvasScrollState = rememberScrollState()
    var graphBoxSize by remember { mutableStateOf(IntSize.Zero) }

    // The width of the finger touch on the screen
    val touchStroke: Float = TOUCH_STROKE
    var touchWithinAxis by remember { mutableStateOf(false) }

    fun calculateClockTimeFromAxis(minuteWidth: Float, touchOffsetX: Float, activeTimeStart: Time): Time {
        var hour = activeTimeStart.hour
        val totalMinutes = (touchOffsetX / minuteWidth) + activeTimeStart.minute
        var hoursToAdd = (totalMinutes / 60).toInt()
        var minutes = (totalMinutes % 60).toInt()

        hour += hoursToAdd

        return Time(hour, minutes)
    }

    fun checkIfTimeInTimeRange(time: Time, startTime: Time, endTime: Time): Boolean {
        if (time.hour < startTime.hour || time.hour > endTime.hour) return false
        else {
            if (time.hour == startTime.hour) {
                if (time.minute >= startTime.minute) return true
                else return false
            } else if (time.hour == endTime.hour) {
                if (time.minute <= endTime.minute) return true
                return false
            } else return true
        }
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
            val isTouchedTimeInTaskRange = checkIfTimeInTimeRange(
                time = time,
                startTime = task.startTime,
                endTime = task.endTime
            )
            if (isTouchedTimeInTaskRange) {
                selectTask(task.id)
            }
        }

        // Select activity
        for (activity in activities) {
            val isTouchedTimeInActivityRange = checkIfTimeInTimeRange(
                time = time,
                startTime = activity.startTime,
                endTime = activity.endTime
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

    // Graph
    Box (
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .onSizeChanged {
                graphBoxSize = it
            }
    ) {
        Box (
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .horizontalScroll(canvasScrollState)
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

                                touchWithinAxis = checkIfTouchWithinAxis(
                                    touchOffsetX = offset,
                                    axisStart = Offset(
                                        x = with(localDensity) { axisHorizontalPadding.toPx() },
                                        y = canvasHeight * 0.5f
                                    ),
                                    axisEnd = Offset(
                                        x = (with(localDensity) { canvasWidth.toPx() } - with(localDensity) { axisHorizontalPadding.toPx() }),
                                        y = canvasHeight * 0.5f
                                    ),
                                    touchStroke = touchStroke
                                )

                                if (touchWithinAxis) {
                                    // Determine what time the touch offset corresponds to
                                    val time = calculateClockTimeFromAxis(
                                        minuteWidth = with(localDensity) { minuteWidth.toPx() },
                                        touchOffsetX = offset.x - with(localDensity) { axisHorizontalPadding.toPx() },
                                        activeTimeStart = activeTimeStart
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
                // Draw tasks
                for (task in tasks) {
                    // We have to offset these angles because startMinute * taskDialState.minuteAngle returns a biased angle
                    val taskMinutesFromActiveTimeStart = TouchGestureUtils.calculateTotalNumberOfMinutes (
                        activeTimeStart,
                        task.startTime
                    )
                    val taskDuration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                        task.startTime,
                        task.endTime
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
                        yOffset = 0.25f,
                        type = Type.TASK
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
                        activity.endTime
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
                        type = Type.ACTIVITY
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

                // Draw the time axis starting from the active time start
                drawAxisAndGrid(
                    axisHorizontalPadding = with(localDensity) { axisHorizontalPadding.toPx() },
                    canvasHeight = canvasHeight,
                    minuteWidth = with(localDensity) { minuteWidth.toPx() },
                    minutesBetweenHoursAccumulated = minutesBetweenHoursAccumulated,
                    activeTimeHourSteps = activeTimeHourSteps,
                    textMeasurer = textMeasurer,
                    totalMinutes = totalMinutes
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
    }
}

fun DrawScope.drawAxisAndGrid(
    axisHorizontalPadding: Float,
    canvasHeight: Float,
    minuteWidth: Float,
    minutesBetweenHoursAccumulated: Array<Int>,
    activeTimeHourSteps: Array<Time>,
    textMeasurer: TextMeasurer,
    totalMinutes: Int
) {
    val textStyle = TextStyle(
        textAlign = TextAlign.Center,
        color = Color(HOUR_LABEL_COLOR)
    )

    // Draw grid - hour steps
    for (i in 0..(minutesBetweenHoursAccumulated.size - 1)) {
        var hourOffset = Offset(
            x = (axisHorizontalPadding + (minutesBetweenHoursAccumulated[i] * minuteWidth)),
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

        drawText(
            textMeasurer = textMeasurer,
            text = hourStepLabel,
            topLeft = hourOffset,
            style = textStyle
        )

        drawCircle(
            color = Color(HOUR_LABEL_COLOR),
            radius = 10f,
            center = Offset(
                x = (axisHorizontalPadding + (minutesBetweenHoursAccumulated[i] * minuteWidth)),
                y = canvasHeight * 0.4f
            )
        )

        drawCircle(
            color = Color(HOUR_LABEL_COLOR),
            radius = 10f,
            center = Offset(
                x = (axisHorizontalPadding + (minutesBetweenHoursAccumulated[i] * minuteWidth)),
                y = canvasHeight * 0.6f
            )
        )
    }

    // Draw minutes of the task axis
    drawMinuteSteps(
        axisHorizontalPadding = axisHorizontalPadding,
        canvasHeight = canvasHeight,
        minuteWidth = minuteWidth,
        totalMinutes = totalMinutes,
        minutesBetweenHoursAccumulated = minutesBetweenHoursAccumulated,
        yOffset = 0.4f
    )
    // Draw minutes of the activity axis
    drawMinuteSteps(
        axisHorizontalPadding = axisHorizontalPadding,
        canvasHeight = canvasHeight,
        minuteWidth = minuteWidth,
        totalMinutes = totalMinutes,
        minutesBetweenHoursAccumulated = minutesBetweenHoursAccumulated,
        yOffset = 0.6f
    )
}

fun DrawScope.drawMinuteSteps(
    axisHorizontalPadding: Float,
    canvasHeight: Float,
    minuteWidth: Float,
    totalMinutes: Int,
    minutesBetweenHoursAccumulated: Array<Int>,
    yOffset: Float
)
{
    // Draw minute 10-minute steps
    val minuteStep = 10

    for (i in 0..totalMinutes) {
        if (i % minuteStep == 0 && !(i in minutesBetweenHoursAccumulated)) {
            drawCircle(
                color = Color(ACTIVITY_MINUTE_STEP_COLOR),
                radius = 5f,
                center = Offset(
                    x = (axisHorizontalPadding + (i * minuteWidth)),
                    y = canvasHeight * yOffset
                )
            )
        }
    }
}

fun DrawScope.drawTask(
    type: Type,
    axisHorizontalPadding: Float,
    taskMinutesFromActiveTimeStart: Int,
    minuteWidth: Float,
    taskDuration: Int,
    taskStartTime: String,
    taskEndTime: String,
    taskTitle: String,
    textMeasurer: TextMeasurer,
    canvasHeight: Float,
    yOffset: Float
) {
    // Draw task area
    val taskWidth = minuteWidth * taskDuration
    val taskHeight = canvasHeight * 0.15f
    val taskOffset = Offset(
        x = (axisHorizontalPadding + (minuteWidth * taskMinutesFromActiveTimeStart)),
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
    val fullText = taskStartTime + " - " + taskEndTime + "    " + taskTitle
    val timeText = taskStartTime + " - " + taskEndTime

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
        diagonalText = taskTitle
    }
    else if ((startTimeWidth + pathPadding * 2) <= taskWidth) {
        horizontalText = taskStartTime
        diagonalText = taskTitle
    }
    else if (textHeight <= taskWidth) {
        horizontalText = ""
        diagonalText = taskTitle
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
        if (type == Type.TASK) {
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
                if (type == Type.TASK) {
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