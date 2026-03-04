package com.eternalfairy.timeaware.utils

import android.graphics.Paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.eternalfairy.timeaware.data.Time
import com.eternalfairy.timeaware.ui.component.CLOCK_CENTER_COLOR
import com.eternalfairy.timeaware.ui.component.CLOCK_LABEL_COLOR
import com.eternalfairy.timeaware.ui.component.HOUR_LABEL_COLOR
import com.eternalfairy.timeaware.ui.component.MINUTE_STEP_COLOR
import com.eternalfairy.timeaware.utils.TouchGestureUtils.DEG_OFFSET
import com.eternalfairy.timeaware.utils.TouchGestureUtils.DEG_TO_RAD
import kotlin.math.cos
import kotlin.math.sin

object DrawScopeUtils {
    fun truncateTextToPath(textToBeTruncated: String, textMeasurer: TextMeasurer, pathMeasure: PathMeasure): String {
        if (textToBeTruncated == "") return textToBeTruncated

        val textToBeTruncatedMeasure = textMeasurer.measure(textToBeTruncated)
        val textToBeTruncatedWidth = textToBeTruncatedMeasure.getBoundingBox(textToBeTruncated.lastIndex).bottomRight.x

        if (textToBeTruncatedWidth <= pathMeasure.length) return textToBeTruncated

        val ellipsis = "\u2026"
        val ellipsisMeasure = textMeasurer.measure(text = ellipsis)
        // The text chars that are going to be included in the displayed text
        val textChars = mutableListOf<Char>()
        var textWidth = 0f

        textToBeTruncated.forEachIndexed { index, char ->
            if ((textWidth + ellipsisMeasure.size.width) < pathMeasure.length) {
                textChars.add(char)
                val charBoundingBox = textToBeTruncatedMeasure.getBoundingBox((index))
                textWidth = charBoundingBox.bottomRight.x
            }
        }

        // If the text to be displayed would consist of 1 or 2 characters, just return an empty string
        if (textChars.size <= 2) {
            return ""
        }

        var text = buildAnnotatedString {
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

        val clockCenterLabelOffset = Offset(
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

    fun DrawScope.drawClockHand(
        activeTimeStart: Time,
        startRadius: Float,
        clockTime: Time,
        minuteAngle: Float,
        endRadius: Float
    ) {
        val minutesFromActiveTimeStart = TouchGestureUtils.calculateTotalNumberOfMinutes(activeTimeStart, clockTime)
        var angle = minuteAngle * minutesFromActiveTimeStart

        if (angle in 270f..360f) angle -= 90f else angle += 270f

        val startOffset = Offset(
            x = center.x + (startRadius * cos(angle * DEG_TO_RAD)).toFloat(),
            y = center.y + (startRadius * sin(angle * DEG_TO_RAD)).toFloat()
        )
        val endOffset = Offset(
            x = center.x + (endRadius * cos(angle * DEG_TO_RAD)).toFloat(),
            y = center.y + (endRadius * sin(angle * DEG_TO_RAD)).toFloat()
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
        drawPath(
            path,
            color = Color(CLOCK_LABEL_COLOR),
            alpha = 0.7f,
            style = Stroke(
                width = 8f
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
            val hourStep = activeTimeHourSteps[i]
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
                // If it is refers to the second or second last hour label
                if (i == 1 || i == (minutesBetweenHoursAccumulated.size - 2)) {
                    // If the interval between the two consecutive hour labels is less than 30 minutes, don't draw it the second/second last label
                    if ((i == 1 && minutesBetweenHoursAccumulated[i] - minutesBetweenHoursAccumulated[0] >= 30)
                        || (i == (minutesBetweenHoursAccumulated.size - 2) && (minutesBetweenHoursAccumulated[minutesBetweenHoursAccumulated.size - 1] - minutesBetweenHoursAccumulated[i]) >= 30)) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = hourStepLabel,
                            topLeft = stepLabelOffset,
                            style = textStyle
                        )
                    }
                }
                else {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = hourStepLabel,
                        topLeft = stepLabelOffset,
                        style = textStyle
                    )
                }
            } else {
                val circleCenterOffset = Offset(
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

    fun DrawScope.drawTask(
        taskEndAngle: Float,
        taskStartAngle: Float,
        minuteAngle: Float? = null,
        innerRadius: Float? = null,
        outerRadius: Float,
        sweepAngle: Float? = null,
        taskDurationInMinutes: Int? = null,
        taskTitle: String? = null,
        textMeasurer: TextMeasurer? = null,
        canvasWidth: Int? = null,
        canvasHeight: Int? = null,
        alpha: Float = 0.68f,
        borderWidth: Float = 2f
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
            sweepAngle = if (taskDurationInMinutes != null && minuteAngle != null) (taskDurationInMinutes * minuteAngle) else sweepAngle!!,
            useCenter = true,
            topLeft = Offset(size.width / 2 - outerRadius, size.height / 2 - outerRadius),
            size = Size(outerRadius * 2, outerRadius * 2),
            alpha = alpha
        )

        // Draw task border
        val startBorderPath = Path()
        var borderStartOffset = Offset(
            x = center.x + cos(taskStartAngle * DEG_TO_RAD).toFloat(),
            y = center.y + sin(taskStartAngle * DEG_TO_RAD).toFloat()
        )
        var borderEndOffset = Offset(
            x = center.x + (outerRadius * cos(taskStartAngle * DEG_TO_RAD)).toFloat(),
            y = center.y + (outerRadius * sin(taskStartAngle * DEG_TO_RAD)).toFloat()
        )

        startBorderPath.moveTo(
            x = borderStartOffset.x, y = borderStartOffset.y
        )
        startBorderPath.lineTo(
            x = borderEndOffset.x, y = borderEndOffset.y
        )

        drawPath(
            startBorderPath, color = Color(0xffffffff), style = Stroke(
                width = borderWidth
            )
        )

        val endBorderPath = Path()
        borderStartOffset = Offset(
            x = center.x + cos(taskEndAngle * DEG_TO_RAD).toFloat(),
            y = center.y + sin(taskEndAngle * DEG_TO_RAD).toFloat()
        )
        borderEndOffset = Offset(
            x = center.x + (outerRadius * cos(taskEndAngle * DEG_TO_RAD)).toFloat(),
            y = center.y + (outerRadius * sin(taskEndAngle * DEG_TO_RAD)).toFloat()
        )

        endBorderPath.moveTo(
            x = borderStartOffset.x, y = borderStartOffset.y
        )
        endBorderPath.lineTo(
            x = borderEndOffset.x, y = borderEndOffset.y
        )

        drawPath(
            endBorderPath, color = Color(0xffffffff), style = Stroke(
                width = borderWidth
            )
        )

        // Draw title
        if (taskTitle != null && taskDurationInMinutes != null && textMeasurer != null && canvasHeight != null && canvasWidth != null && minuteAngle != null && innerRadius != null) {
            val path = Path()
            // The angle in the middle of the task area
            var middleAngle = taskStartAngle + (taskDurationInMinutes * 0.5f * minuteAngle)
            // Correct the middle angle if it exceeds the 360 degree value. Otherwise, values over 360 are not drawn properly
            if (middleAngle > 360f) {
                middleAngle -= 360f
            }
            val pathPadding: Float
            val titleMeasure = textMeasurer.measure(text = taskTitle)
            val titleWidth = if (taskTitle != "") {
                titleMeasure.getBoundingBox(taskTitle.lastIndex).bottomRight.x
            } else {
                0f
            }
            val titleHeight = if (taskTitle != "") {
                titleMeasure.getBoundingBox(taskTitle.lastIndex).bottomRight.y
            } else {
                0f
            }
            var drawTitle = true

            // If the task duration is long enough, we want to draw text along a curve
            // Define arc path
            if ((taskDurationInMinutes * minuteAngle).toFloat() > 45f) {
                val titleAngle: Float
                val sweepAngleDegrees: Float
                pathPadding = 3f

                if (middleAngle in 180f..360f) {
                    titleAngle = (taskStartAngle).toFloat() + pathPadding
                    sweepAngleDegrees =
                        ((taskDurationInMinutes - 5 * pathPadding) * minuteAngle).toFloat()
                } else {
                    titleAngle =
                        taskStartAngle + (taskDurationInMinutes * minuteAngle).toFloat() - pathPadding
                    sweepAngleDegrees =
                        -((taskDurationInMinutes - 5 * pathPadding) * minuteAngle).toFloat()
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
                pathPadding = textMeasurer.measure("0").size.width.toFloat()
                // Draw the text from the outside in...
                var startRadius = outerRadius - pathPadding
                var endRadius = innerRadius + pathPadding

                // ...or from the center out
                if (middleAngle in 270f..360f || middleAngle in 0f..90f) {
                    // Add some padding to the beginning of the path
                    startRadius = innerRadius + pathPadding
                    // Subtract the width of hour labels so that the task title doesn't overlap with an hour label
                    endRadius = outerRadius - pathPadding
                }

                // Determine whether the arc length at the start and the end of the task's area is wide enough to fit in text
                val taskEndAngleAngleTranslated =
                    TouchGestureUtils.translateAngle270To0(taskEndAngle)
                val taskStartAngleTranslated =
                    TouchGestureUtils.translateAngle270To0(taskStartAngle)
                val startArc =
                    (startRadius * (taskEndAngleAngleTranslated - taskStartAngleTranslated) * Math.PI / 180) + pathPadding
                val endArc =
                    (endRadius * (taskEndAngleAngleTranslated - taskStartAngleTranslated) * Math.PI / 180) + pathPadding

                if (startArc <= titleHeight || endArc <= titleHeight) drawTitle = false

                val titleStartOffset = Offset(
                    x = center.x + (startRadius * cos(middleAngle * DEG_TO_RAD)).toFloat(),
                    y = center.y + (startRadius * sin(middleAngle * DEG_TO_RAD)).toFloat()
                )
                val titleEndOffset = Offset(
                    x = center.x + (endRadius * cos(middleAngle * DEG_TO_RAD)).toFloat(),
                    y = center.y + (endRadius * sin(middleAngle * DEG_TO_RAD)).toFloat()
                )

                path.moveTo(
                    x = titleStartOffset.x, y = titleStartOffset.y
                )
                path.lineTo(
                    x = titleEndOffset.x, y = titleEndOffset.y
                )
            }

            if (drawTitle) {
                val pathMeasure = PathMeasure()
                pathMeasure.setPath(
                    path = path, forceClosed = false
                )

                var text: String
                var textWidth = 0f
                val ellipsis = "\u2026"
                val ellipsisMeasure = textMeasurer.measure(text = ellipsis)

                // Truncate text if its length exceeds the path's length
                if (titleWidth >= pathMeasure.length) {
//                    text = truncateTextToPath(taskTitle, textMeasurer, pathMeasure)
                    val textChars = mutableListOf<Char>()

                    taskTitle.forEachIndexed { index, char ->
                        if ((textWidth + ellipsisMeasure.size.width) < pathMeasure.length) {
                            textChars.add(char)
                            val charBoundingBox = titleMeasure.getBoundingBox((index))
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
                }
                else {
                    text = taskTitle
                }

                val measuredText = textMeasurer.measure(text = text)

//                if (text != "") {
//                    textWidth = measuredText.getBoundingBox(text.lastIndex).bottomRight.x
//                }

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
                        })
                    }
                }
            }
        }
//    }
}