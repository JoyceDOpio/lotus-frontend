package com.example.circularplanner.ui.component

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.screen.DeleteScreen
import com.example.circularplanner.ui.screen.TaskEditScreen
import com.example.circularplanner.ui.screen.TaskInfoScreen
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.ui.viewmodel.UserInput
import com.example.circularplanner.utils.AngleMode
import com.example.circularplanner.utils.TaskModePopup
import com.example.circularplanner.utils.TouchGestureUtils
import com.example.circularplanner.utils.TouchGestureUtils.DEG_OFFSET
import com.example.circularplanner.utils.TouchGestureUtils.DEG_TO_RAD
import com.example.circularplanner.utils.TouchGestureUtils.TOUCH_STROKE
import kotlinx.coroutines.delay
import java.time.LocalDate
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
fun TaskDial(
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
    Log.i("taskUiState", taskUiState.toString())
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
    // The angles into which the dragged task should "jump in" should the dragging end while the dragged task still hovers over another task
    var jumpInStartAngle: Float? by remember { mutableStateOf(null) }
    var jumpInEndAngle: Float? by remember { mutableStateOf(null) }

//    val nextTaskDetails = nextTaskUiState
//    val previousTaskDetails = previousTaskUiState

//    Log.i("TaskDial", "touchedTask $touchedTask")
//    Log.i("TaskDial", "previousTask $previousTask")
//    Log.i("TaskDial", "nextTask $nextTask")
//
//    Log.i("TaskDial", "taskUiState $taskUiState")
//    Log.i("TaskDial", "previousTaskUiState $previousTaskUiState")
//    Log.i("TaskDial", "nextTaskUiState $nextTaskUiState")

    fun checkIfTouchWithinTaskArea(angle: Float, taskStart: Time, taskEnd: Time): Boolean {
        val taskStartAngle = TouchGestureUtils.calculateAngleFromTime (
            activeTimeStart,
            taskStart,
            minuteAngle
        )
        val taskEndAngle = TouchGestureUtils.calculateAngleFromTime (
            activeTimeStart,
            taskEnd,
            minuteAngle
        )
        val isTouchWithinTaskArea =
            TouchGestureUtils.checkIfTouchWithinAngleRange(angle, taskStartAngle, taskEndAngle)

        return isTouchWithinTaskArea
    }

    fun checkIfTouchWithinTasks(angle: Float, tasks: List<Task>): Boolean {
        // The task stores the appropriate angle values, i.e. values corresponding to how the circle is drawn (the 0 degree starts at the right-hand side (east) of the circle). We want to 'correct' these angles as if 0 degree starts at the top of the circle (north)
        var isTouchWithinAnyTask: Boolean

        for (task in tasks) {
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
            }
        }

        Log.i("TaskDial", "checkIfTouchWithinTasks() touchedTask $touchedTask")
        Log.i("TaskDial", "checkIfTouchWithinTasks() previousTask $previousTask")
        Log.i("TaskDial", "checkIfTouchWithinTasks() nextTask $nextTask")

        Log.i("TaskDial", "checkIfTouchWithinTasks() previousTaskUiState $previousTaskUiState")
        Log.i("TaskDial", "checkIfTouchWithinTasks() nextTaskUiState $nextTaskUiState")

        if (touchedTask != null) return true
        else {
            previousTask = null
            return false
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
//        selectNextTask(null)
        previousTask = null
//        selectPreviousTask(null)
    }

    fun reset() {
        resetDialParameters()
        resetStartAndEndAngles()
        resetTask()
        taskMode = TaskMode.View
    }

    // Select the next and previous tasks based on the start- and end times
    fun selectPreviousAndNextTasks(selectedTask: Task) {
//    fun selectPreviousAndNextTasks() {
        previousTask = null
        nextTask = null

        if (touchedTask != null) {
            for (task in tasks) {
                // If the task starts earlier than the selected task
                if (task.compareTo(selectedTask) == -1) {
                    previousTask = task.copy()
                }
                // If the task starts later than the selected task
                else if (task.compareTo(selectedTask) == 1) {
                    nextTask = task.copy()
                    break
                }
            }
        }

//        selectNextTask(selectedTask)
//        selectPreviousTask(selectedTask)

        Log.i("TaskDial", "selectPreviousAndNextTasks() touchedTask $touchedTask")
        Log.i("TaskDial", "selectPreviousAndNextTasks() previousTask $previousTask")
        Log.i("TaskDial", "selectPreviousAndNextTasks() nextTask $nextTask")
        Log.i("TaskDial", "selectPreviousAndNextTasks() taskUiState $taskUiState")
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
            horizontalArrangement = Arrangement.Center
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
                .pointerInput(dayState, taskUiState, userInput) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
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
                                        checkIfTouchWithinTaskArea(
                                            angle,
                                            taskUiState.startTime!!,
                                            taskUiState.endTime!!
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
//                            Log.i("TaskDial", "detectDragGestures onDrag")
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

                                    Log.i(
                                        "TaskDial",
                                        "onDrag() currentAngleTranslated $currentAngleTranslated"
                                    )
                                    Log.i(
                                        "TaskDial",
                                        "onDrag() initialAngleTranslated $initialAngleTranslated"
                                    )
//                                    Log.i("TaskDial", "onDrag() taskUiState ${taskUiState}")
                                    Log.i("TaskDial", "onDrag() touchedTask ${touchedTask}")

//                                    Log.i("TaskDial", "onDrag() previousTaskDetails $previousTaskDetails")
                                    Log.i("TaskDial", "onDrag() previousTask $previousTask")
//                                    Log.i("TaskDial", "onDrag() nextTaskDetails ${nextTaskDetails}")
                                    Log.i("TaskDial", "onDrag() nextTask $nextTask")

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

                                var newStartAngle = tmpStartAngle + currentAngle - previousAngle
                                var newEndAngle =
                                    tmpStartAngle + (draggedTaskDuration * minuteAngle)
//                                Log.i("TaskDial", "newStartAngle ${newStartAngle}")
//                                Log.i("TaskDial", "newEndAngle ${newEndAngle}")

                                // Correct the angle if its value exceeds 360 degrees
                                if (newStartAngle > 360f) newStartAngle -= 360f
                                if (newEndAngle > 360f) newEndAngle -= 360f

                                val newStartAngleTranslated =
                                    TouchGestureUtils.translateAngle270To0(newStartAngle)
                                val newEndAngleTranslated =
                                    TouchGestureUtils.translateAngle270To0(newEndAngle)

//                                Log.i("TaskDial", "newStartAngleTranslated ${newStartAngleTranslated}")
//                                Log.i("TaskDial", "newEndAngleTranslated ${newEndAngleTranslated}")

                                // If the task's end time doesn't pass the 0/360 degree mark, move the task
                                if (newEndAngleTranslated < 360f && newStartAngleTranslated >= 0) {
                                    if (newEndAngleTranslated > newStartAngleTranslated) {
                                        tmpStartAngle = newStartAngle
                                        tmpEndAngle = newEndAngle

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
                                        var middleAngle =
                                            newStartAngle + ((draggedTaskDuration * minuteAngle) * 0.5f)
                                        // Correct the middle angle if due to constant addition its value exceeds 360 degrees
                                        if (middleAngle > 360f) middleAngle -= 360f

                                        val middleAngleTranslated =
                                            TouchGestureUtils.translateAngle270To0(middleAngle)


                                        Log.i("TaskDial", "dragDirection $dragDirection")
                                        Log.i("TaskDial","middleAngleTranslated $middleAngleTranslated")


                                        // If we're moving forward, we may cross the next task's start time angle
                                        if (dragDirection == DragDirection.Forward) {
//                                            Log.i("TaskDial", "onDrag() nextTask $nextTask")
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
                                                Log.i("TaskDial","nextTaskStartAngleTranslated $nextTaskStartAngleTranslated")

                                                if (middleAngleTranslated >= nextTaskStartAngleTranslated) {
                                                    // We're going to move the next task in the anti-clockwise direction, so we're determining first where the next task will end
                                                    val tmpNextTaskEndAngle =
                                                        TouchGestureUtils.calculateAngleFromTime(
                                                            activeTimeStart,
                                                            nextTask!!.endTime!!,
                                                            minuteAngle
                                                        )

                                                    Log.i("TaskDial","nextTask $nextTask")
                                                    Log.i( "TaskDial","nextTaskUiState $nextTaskUiState")
                                                    Log.i("TaskDial","nextTask!!.endTime!! ${nextTask!!.endTime!!}")
                                                    Log.i("TaskDial","tmpNextTaskEndAngle $tmpNextTaskEndAngle")
                                                    Log.i("TaskDial", "minuteAngle $minuteAngle")

                                                    val nextTaskDuration =
                                                        TouchGestureUtils.calculateTotalNumberOfMinutes(
                                                            nextTask!!.startTime!!,
                                                            nextTask!!.endTime!!
                                                        )
                                                    Log.i("TaskDial","nextTaskDuration $nextTaskDuration")
                                                    Log.i( "TaskDial","draggedTaskDuration $draggedTaskDuration")

                                                    var newNextTaskEndAngle =
                                                        tmpNextTaskEndAngle - ((draggedTaskDuration + 1) * minuteAngle)
                                                    var newNextTaskStartAngle =
                                                        newNextTaskEndAngle - (nextTaskDuration * minuteAngle)

                                                    Log.i("TaskDial","newNextTaskStartAngle $newNextTaskStartAngle")
                                                    Log.i("TaskDial","newNextTaskEndAngle $newNextTaskEndAngle")

                                                    // Correct the angle if its value is less than 0 degrees
                                                    if (newNextTaskEndAngle < 0f) newNextTaskEndAngle += 360f
                                                    if (newNextTaskStartAngle < 0f) newNextTaskStartAngle += 360f

                                                    Log.i("TaskDial","newNextTaskStartAngle $newNextTaskStartAngle")
                                                    Log.i("TaskDial","newNextTaskEndAngle $newNextTaskEndAngle")

                                                    val newNextTaskEndTime =
                                                        TouchGestureUtils.calculateTimeFromAngle(
                                                            angle = newNextTaskEndAngle,
                                                            minuteAngle = minuteAngle,
                                                            clockStart = activeTimeStart
                                                        )
                                                    val newNextTaskStartTime =
                                                        TouchGestureUtils.calculateTimeFromAngle(
                                                            angle = newNextTaskStartAngle,
                                                            minuteAngle = minuteAngle,
                                                            clockStart = activeTimeStart
                                                        )

                                                    // Update next task's start- and end time
                                                    tasks.find { task -> task.id == nextTask!!.id }?.apply {
                                                        startTime = newNextTaskStartTime
                                                        endTime = newNextTaskEndTime
                                                    }
//                                                    setNextTaskStartTime(newNextTaskStartTime)
//                                                    setNextTaskEndTime(newNextTaskEndTime)
//                                                    saveNextTask()

                                                    // Set new next and previous tasks
                                                    selectPreviousAndNextTasks(
                                                        touchedTask!!.copy(
                                                            startTime = TouchGestureUtils.calculateTimeFromAngle(
                                                                tmpStartAngle,
                                                                minuteAngle,
                                                                activeTimeStart
                                                            ),
                                                            endTime = TouchGestureUtils.calculateTimeFromAngle(
                                                                tmpEndAngle,
                                                                minuteAngle,
                                                    activeTimeStart
                                                            )
                                                        )
                                                    )
                                                }
                                            }
                                        } else if (dragDirection == DragDirection.Backward) {
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

                                                Log.i("TaskDial","previousTaskEndAngleTranslated $previousTaskEndAngleTranslated")

                                                if (middleAngleTranslated <= previousTaskEndAngleTranslated) {
                                                    // We're going to move the previous task in the clockwise direction, so we're determining first where the previous task will start
                                                    val tmpPreviousTaskStartAngle =
                                                        TouchGestureUtils.calculateAngleFromTime(
                                                            activeTimeStart,
                                                            previousTask!!.startTime!!,
                                                            minuteAngle
                                                        )

                                                    Log.i("TaskDial","nextTask $nextTask")
                                                    Log.i( "TaskDial","nextTaskUiState $nextTaskUiState")
                                                    Log.i("TaskDial","previousTask!!.startTime!! ${previousTask!!.startTime!!}")
                                                    Log.i("TaskDial","tmpNextTaskEndAngle $tmpPreviousTaskStartAngle")
                                                    Log.i("TaskDial", "minuteAngle $minuteAngle")

                                                    val previousTaskDuration =
                                                        TouchGestureUtils.calculateTotalNumberOfMinutes(
                                                            previousTask!!.startTime!!,
                                                            previousTask!!.endTime!!
                                                        )
                                                    Log.i("TaskDial","nextTaskDuration $previousTaskDuration")
                                                    Log.i( "TaskDial","draggedTaskDuration $draggedTaskDuration")

                                                    var newPreviousTaskStartAngle =
                                                        tmpPreviousTaskStartAngle + ((draggedTaskDuration + 1) * minuteAngle)
                                                    var newPreviousTaskEndAngle =
                                                        newPreviousTaskStartAngle + (previousTaskDuration * minuteAngle)

                                                    Log.i("TaskDial","newPreviousTaskStartAngle $newPreviousTaskStartAngle")
                                                    Log.i("TaskDial","newPreviousTaskEndAngle $newPreviousTaskEndAngle")

                                                    // Correct the angle if its value is less than 0 degrees
                                                    if (newPreviousTaskEndAngle > 360f) newPreviousTaskEndAngle -= 360f
                                                    if (newPreviousTaskStartAngle > 360f) newPreviousTaskStartAngle -= 360f

                                                    Log.i("TaskDial","newNextTaskStartAngle $newPreviousTaskStartAngle")
                                                    Log.i("TaskDial","newNextTaskEndAngle $newPreviousTaskEndAngle")

                                                    val newPreviousTaskEndTime =
                                                        TouchGestureUtils.calculateTimeFromAngle(
                                                            angle = newPreviousTaskEndAngle,
                                                            minuteAngle = minuteAngle,
                                                            clockStart = activeTimeStart
                                                        )
                                                    val newPreviousTaskStartTime =
                                                        TouchGestureUtils.calculateTimeFromAngle(
                                                            angle = newPreviousTaskStartAngle,
                                                            minuteAngle = minuteAngle,
                                                            clockStart = activeTimeStart
                                                        )

                                                    // Update next task's start- and end time
                                                    tasks.find { task -> task.id == previousTask!!.id }?.apply {
                                                        startTime = newPreviousTaskStartTime
                                                        endTime = newPreviousTaskEndTime
                                                    }
//                                                    setNextTaskStartTime(newNextTaskStartTime)
//                                                    setNextTaskEndTime(newNextTaskEndTime)
//                                                    saveNextTask()

                                                    // Set new next and previous tasks
                                                    selectPreviousAndNextTasks(
                                                        touchedTask!!.copy(
                                                            startTime = TouchGestureUtils.calculateTimeFromAngle(
                                                                tmpStartAngle,
                                                                minuteAngle,
                                                                activeTimeStart
                                                            ),
                                                            endTime = TouchGestureUtils.calculateTimeFromAngle(
                                                                tmpEndAngle,
                                                                minuteAngle,
                                                                activeTimeStart
                                                            )
                                                        )
                                                    )
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
                            }
                            else if (taskMode == TaskMode.EditStartTime) {
                                // Calculate the time represented by the angle
                                val tmpStartAngleTranslated =
                                    TouchGestureUtils.translateAngle270To0(tmpStartAngle)
                                val tmpEndAngleTranslated =
                                    TouchGestureUtils.translateAngle270To0(tmpEndAngle)

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

                                    if (tmpStartAngleTranslated < previousTaskEndAngleTranslated) {
                                        jumpInStartAngle =
                                            previousTaskEndAngle + minuteAngle
                                        jumpInEndAngle =
                                            jumpInStartAngle!! + (draggedTaskDuration * minuteAngle)

                                        if (jumpInStartAngle!! > 360f) jumpInStartAngle = jumpInStartAngle!! - 360f
                                        if (jumpInEndAngle!! > 360f) jumpInEndAngle = jumpInEndAngle!! - 360f

                                        tmpStartAngle = jumpInStartAngle!!
                                        tmpEndAngle = jumpInEndAngle!!
                                    }
                                }

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

                                    if (tmpEndAngleTranslated > nextTaskStartAngleTranslated) {
                                        jumpInEndAngle =
                                            nextTaskStartAngle - minuteAngle
                                        jumpInStartAngle =
                                            jumpInEndAngle!! - (draggedTaskDuration * minuteAngle)

                                        if (jumpInStartAngle!! < 0f) jumpInStartAngle = jumpInStartAngle!! + 360f
                                        if (jumpInEndAngle!! < 0f) jumpInEndAngle = jumpInEndAngle!! + 360f

                                        tmpStartAngle = jumpInStartAngle!!
                                        tmpEndAngle = jumpInEndAngle!!
                                    }
                                }

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

//                                val taskNewStartTime = TouchGestureUtils.calculateTimeFromAngle(
//                                    angle = tmpStartAngle,
//                                    clockStart = activeTimeStart,
//                                    minuteAngle = minuteAngle
//                                )
//                                val taskNewEndTime = TouchGestureUtils.calculateTimeFromAngle(
//                                    angle = tmpEndAngle,
//                                    clockStart = activeTimeStart,
//                                    minuteAngle = minuteAngle
//                                )

                                // Update the task's start- and end times
                                for (task in tasks) {
                                    saveTask(task)
                                }
//                                setTaskStartTime(taskNewStartTime)
//                                setTaskEndTime(taskNewEndTime)
//                                saveTaskFromState()
                                reset()
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
                    val taskDuration = TouchGestureUtils.calculateTotalNumberOfMinutes(
                        task.startTime!!,
                        task.endTime!!
                    )

                    drawTask(
                        taskEndAngle = tmpEndAngle,
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
                // Reset the dial
                reset()
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
    borderWidth: Float = 1f
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
        Offset(size.width / 2 - outerRadius, size.height / 2 - outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        alpha = alpha
    )

//    // Draw task border
//    val startBorderPath = Path()
//    var borderStartOffset = Offset(
//        x = center.x + cos(taskStartAngle * DEG_TO_RAD).toFloat(),
//        y = center.y + sin(taskStartAngle * DEG_TO_RAD).toFloat()
//    )
//    var borderEndOffset = Offset(
//        x = center.x + (outerRadius * cos(taskStartAngle * DEG_TO_RAD)).toFloat(),
//        y = center.y + (outerRadius * sin(taskStartAngle * DEG_TO_RAD)).toFloat()
//    )
//
//    startBorderPath.moveTo(
//        x = borderStartOffset.x,
//        y = borderStartOffset.y
//    )
//    startBorderPath.lineTo(
//        x = borderEndOffset.x,
//        y = borderEndOffset.y
//    )
//
//    drawPath(
//        startBorderPath,
//        color = Color(0xffffffff),
//        style = Stroke(
//            width = borderWidth
//        )
//    )
//
//    val endBorderPath = Path()
//    borderStartOffset = Offset(
//        x = center.x + cos(taskEndAngle * DEG_TO_RAD).toFloat(),
//        y = center.y + sin(taskEndAngle * DEG_TO_RAD).toFloat()
//    )
//    borderEndOffset = Offset(
//        x = center.x + (outerRadius * cos(taskEndAngle * DEG_TO_RAD)).toFloat(),
//        y = center.y + (outerRadius * sin(taskEndAngle * DEG_TO_RAD)).toFloat()
//    )
//
//    endBorderPath.moveTo(
//        x = borderStartOffset.x,
//        y = borderStartOffset.y
//    )
//    endBorderPath.lineTo(
//        x = borderEndOffset.x,
//        y = borderEndOffset.y
//    )
//
//    drawPath(
//        endBorderPath,
//        color = Color(0xffffffff),
//        style = Stroke(
//            width = borderWidth
//        )
//    )

    // Draw title
    if (
        taskTitle != null
        && taskDurationInMinutes != null
        && textMeasurer != null
        && canvasHeight != null
        && canvasWidth != null
        && minuteAngle != null
        && innerRadius != null
    ) {
        val path = Path()
        // The angle in the middle of the task area
        var middleAngle = taskStartAngle + (taskDurationInMinutes * 0.5f * minuteAngle)
        // Correct the middle angle if it exceeds the 360 degree value. Otherwise, values over 360 are not drawn properly
        if (middleAngle > 360f) {
            middleAngle -= 360f
        }
        val pathPadding: Float

        // If the task duration is long enough, we want to draw text along a curve
        // Define arc path
        if ((taskDurationInMinutes * minuteAngle).toFloat() > 45f) {
            val titleAngle: Float
            val sweepAngleDegrees: Float
            pathPadding = 3f

            if (middleAngle in 180f..360f) {
                titleAngle = (taskStartAngle).toFloat() + pathPadding
                sweepAngleDegrees = ((taskDurationInMinutes - 5 * pathPadding) * minuteAngle).toFloat()
            } else {
                titleAngle = taskStartAngle + (taskDurationInMinutes * minuteAngle).toFloat() - pathPadding
                sweepAngleDegrees = -((taskDurationInMinutes - 5 * pathPadding) * minuteAngle).toFloat()
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
            pathPadding = textMeasurer.measure("0").size.width.toFloat()

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
}


