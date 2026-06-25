package com.eternalfairy.lotus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.min
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import com.eternalfairy.lotus.view.viewmodel.room.DayState
import com.eternalfairy.lotus.view.viewmodel.room.UserInput
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.square
import java.time.LocalDate
import kotlin.math.sqrt

@Composable
fun TestTaskDial(
    dayState: DayState,
    userInput: UserInput,
    onSetDate: (LocalDate) -> Unit
) {
    // The width and height of the Canvas
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    var touchInsideTheDial by remember { mutableStateOf(false) }
    var center by remember { mutableStateOf(Offset.Zero) }

    // Basically the width of the finger touch on the screen
    val touchStroke: Float = 50f

    // The radius of the dial (from the center to the end of the clock steps)
    var outerRadius by remember { mutableStateOf(0f) }

    Log.i("DDselectedDate", userInput.selectedDate.toString())
    Log.i("DDdate", dayState.date.toString())
    Log.i("DDtasks", dayState.tasks.joinToString())

    fun distance(first: Offset, second: Offset): Float {
        return sqrt((first.x - second.x).square() + (first.y - second.y).square())
    }

    fun checkIfTouchInsideDial(distance: Float, radius: Float, touchStroke: Float): Boolean {
        if (distance <= radius + touchStroke * 2f) {
            return true
        } else {
            return false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Canvas (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .onGloballyPositioned {
                    width = it.size.width
                    height = it.size.height
                    center = Offset(width / 2f, height / 2f)
                    // The radius of the dial
                    outerRadius = min(width.toFloat(), height.toFloat()) / 2f
                }
                .pointerInput(dayState, userInput) {
                    detectTapGestures(
                        onTap = { offset ->
                            val distance = distance(offset, center)
                            touchInsideTheDial = checkIfTouchInsideDial(
                                distance,
                                outerRadius,
                                touchStroke
                            )

                            if (touchInsideTheDial) {
                                Log.i("TaskDial", "onTap")
                                Log.i("DDselectedDate", userInput.selectedDate.toString())
                                Log.i("DDdate", dayState.date.toString())
                                Log.i("DDtasks", dayState.tasks.joinToString())
                            }
                        }
                    )
                }
        ) {

            outerRadius = min(width, height) / 2f * 0.8f

            // Draw dial
            drawCircle(
                color = Color.Cyan,
                center = center,
                radius = outerRadius,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.1f),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { onSetDate(LocalDate.parse("2025-06-26")) }) {
                Text("26th June")
            }
            Button(onClick = { onSetDate(LocalDate.parse("2025-06-27")) }) {
                Text("27th June")
            }
        }

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.1f),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = userInput.selectedDate.toString())
        }
    }
}