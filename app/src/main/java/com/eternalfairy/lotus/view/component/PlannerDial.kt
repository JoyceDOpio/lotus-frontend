package com.eternalfairy.lotus.view.component

import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.lotus.R
import com.eternalfairy.lotus.view.data.DayUiState
import com.eternalfairy.lotus.view.data.TaskUiState
import com.eternalfairy.lotus.view.data.UserInput
import com.eternalfairy.lotus.view.screen.DeleteScreen
import com.eternalfairy.lotus.view.screen.TaskEditScreen
import com.eternalfairy.lotus.view.screen.TaskInfoScreen
import com.eternalfairy.lotus.view.screen.planner.PlannerUiEvent
import com.eternalfairy.lotus.view.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.lotus.view.theme.HEADER_TEXT_COLOR
import com.eternalfairy.lotus.view.utils.AngleMode
import com.eternalfairy.lotus.view.utils.DrawScopeUtils.drawClockCenter
import com.eternalfairy.lotus.view.utils.DrawScopeUtils.drawClockHand
import com.eternalfairy.lotus.view.utils.DrawScopeUtils.drawHourStepsAndLabels
import com.eternalfairy.lotus.view.utils.DrawScopeUtils.drawMinuteSteps
import com.eternalfairy.lotus.view.utils.DrawScopeUtils.drawTask
import com.eternalfairy.lotus.view.utils.TaskModePopup
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.TOUCH_STROKE
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.angle
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.calculateAngleFromTime
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.calculateMinutesBetweenHoursAccumulated
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.calculateTimeFromAngle
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.calculateTotalNumberOfMinutes
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.checkIfTimeInRange
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.checkIfTouchInsideDial
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.checkIfTouchNearDialEdge
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.checkIfTouchWithinAngleRange
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.checkIfTouchWithinTaskArea
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.createClockHoursArray
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.distance
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.sweepAngle
import com.eternalfairy.lotus.view.utils.TouchGestureUtils.translateAngle270To0
import com.eternalfairy.lotus.viewmodel.PlannerViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.uuid.ExperimentalUuidApi

const val SECONDS_IN_MINUTE = 60

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

@OptIn(ExperimentalUuidApi::class)
@Composable
fun PlannerDial(
    componentHeight: Dp = 450.dp,
    componentWidth: Dp = 400.dp,
    paddingStart: Dp = 10.dp,
    paddingTop: Dp = 5.dp,
    paddingEnd: Dp = 10.dp,
    paddingBottom: Dp = 5.dp,
    drawClockHand: Boolean = false,
    viewModel: PlannerViewModel,
//    dayUiState: DayUiState,
//    lastTaskPriority: Int?,
//    taskUiState: TaskUiState,
//    userInput: UserInput,
//    deleteTask: () -> Unit,
//    onMoveToToDoList: () -> Unit,
    onMoveToCalendar: () -> Unit,
    onPressActiveTime: () -> Unit,
//    onPinTask: (Boolean) -> Unit,
//    setTaskDescription: (String) -> Unit,
//    setTaskEndTime: (Time) -> Unit,
//    setTaskPriority: (Int) -> Unit,
//    setTaskStartTime: (Time) -> Unit,
//    setTaskTitle: (String) -> Unit,
//    saveTask: (TaskUiState) -> Unit,
//    saveTaskFromState: () -> Unit,
//    selectTask: (UUID?) -> Unit,
////    setTaskDate: (OffsetDateTime?) -> Unit
//    setTaskDate: (LocalDate?) -> Unit
) {
    val state = viewModel.state

    val day = state.selectedDay
    val editedTask = state.editedTask
    val selectedDate = state.selectedDate
//    val selectedTask = state.selectedTask

    val textMeasurer = rememberTextMeasurer()
    val activeTimeStart: LocalTime = state.selectedDay.activeTimeStart
    val activeTimeEnd: LocalTime = state.selectedDay.activeTimeEnd
    var taskMode: TaskMode by remember { mutableStateOf(TaskMode.View) }
    var angleMode: AngleMode by remember { mutableStateOf(AngleMode.None) }

    val totalMinutes: Int = calculateTotalNumberOfMinutes(
        LocalTime(
            activeTimeStart.hour,
            activeTimeStart.minute
        ),
        LocalTime(
            activeTimeEnd.hour,
            activeTimeEnd.minute
        )
    )
    // The angle that corresponds to 1 minute
    val minuteAngle: Float = 360 / totalMinutes.toFloat()

    // Note the start angle of where the finger touched the screen to get the supposed new start time value of a task:
    // - values used to trace the finger movement
    var startAngle by remember { mutableStateOf<Float?>(null) }
    // - values used to draw the angles
    var tmpStartAngle by remember { mutableStateOf<Float?>(null) }
    var tmpEndAngle by remember { mutableStateOf<Float?>(null) }

    // The width and height of the Canvas
    var width by remember { mutableIntStateOf(0) }
    var height by remember { mutableIntStateOf(0) }
    var angle by remember { mutableFloatStateOf(0f) }
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
    var outerRadius by remember { mutableFloatStateOf(0f) }
    // The radius from the center to the clock steps
    var innerRadius by remember { mutableFloatStateOf(0f) }
    // The radius of the clock center (the one that displays time)
    var centerRadius by remember { mutableFloatStateOf(0f) }

    // Variables to identify whether the dial was touched within an existing task area
    var touchWithinTaskArea by remember { mutableStateOf(true) }
    // If I save the taskUiState under the touchedTask it seems it is not updated in time after touching it. The dial tries to draw it before its value is updated.
    var touchedTask by remember { mutableStateOf<TaskUiState?>(null) }
    var nextTask by remember { mutableStateOf<TaskUiState?>(null) }
    var previousTask by remember { mutableStateOf<TaskUiState?>(null) }

    var clockTime by remember { mutableStateOf(
        LocalTime.parse(java.time.LocalTime.now().toString())
    ) }
    var taskClockTime by remember { mutableStateOf(
        LocalTime.parse(java.time.LocalTime.now().toString())
    ) }

    val tasks = state.tasks.toMutableList()

    var showPopupWindow by remember { mutableStateOf(false) }
    var popupState by remember { mutableStateOf(TaskModePopup.Info) }

    // DRAGGING
    // The duration of the dragged task in minutes
    var draggedTaskDuration by remember { mutableIntStateOf(0) }
    var dragDirection by remember { mutableStateOf(DragDirection.None) }
    // The boundary angle corresponds to the task's start time value when the end time angle is modified, and vice versa. The idea is that when a task's end time angle is being modified, its value should not get too close to the start time angle. Otherwise, the task will seize to exist
    var boundaryAngleTranslated by remember { mutableStateOf<Float?>(null) }
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
    var transformMode by remember { mutableStateOf(false) }
//    Log.i("PlannerDial", "tasks $tasks")
//    Log.i("PlannerDial", "isTransforming $isTransforming")

    fun checkIfTouchWithinTasks(angle: Float, tasks: List<TaskUiState>): Boolean {
        // The task stores the appropriate angle values, i.e. values corresponding to how the circle is drawn (the 0 degree starts at the right-hand side (east) of the circle). We want to 'correct' these angles as if 0 degree starts at the top of the circle (north)
        var isTouchWithinAnyTask: Boolean

        for (task in tasks) {
            val taskStartAngle = calculateAngleFromTime (
                activeTimeStart = activeTimeStart,
//                activeTimeEnd,
                time = task.startTime!!,
                minuteAngle = minuteAngle
            )
            val taskEndAngle = calculateAngleFromTime (
                activeTimeStart = activeTimeStart,
//                activeTimeEnd,
                time = task.endTime!!,
                minuteAngle = minuteAngle
            )
            isTouchWithinAnyTask = checkIfTouchWithinAngleRange(angle, taskStartAngle, taskEndAngle)

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
//                selectTask(task.id)
                viewModel.onEvent(PlannerUiEvent.SelectedTaskIdChanged(task.id))

                touchedTask = task.copy()
            }
        }

        if (touchedTask != null) return true
        else {
            previousTask = null
            return false
        }
    }
    
    fun moveTasks(previousTaskIndex: Int, nextTaskIndex: Int, index: Int) {
        
    }

    // Moves the task with given index by the specified angle change - this method is used to move tasks other than the one that is dragged by the user
    fun moveTaskBackwardCascade(index: Int, angleChange: Float) {
        val taskToBeMoved = tasks[index]

        if (taskToBeMoved.id != touchedTask!!.id) {
            val taskToBeMovedStartAngle =
                calculateAngleFromTime(
                    activeTimeStart = activeTimeStart,
//                    activeTimeEnd,
                    time = taskToBeMoved.startTime!!,
                    minuteAngle = minuteAngle
                )
            val taskToBeMovedEndAngle =
                calculateAngleFromTime(
                    activeTimeStart = activeTimeStart,
//                    activeTimeEnd,
                    time = taskToBeMoved.endTime!!,
                    minuteAngle = minuteAngle
                )
            val taskToBeMovedStartAngleTranslated = translateAngle270To0(taskToBeMovedStartAngle)
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

                val taskTobeMovedNewStartTime = calculateTimeFromAngle(taskToBeMovedNewStartAngle, minuteAngle, activeTimeStart)
                val taskTobeMovedNewEndTime = calculateTimeFromAngle(taskToBeMovedNewEndAngle, minuteAngle, activeTimeStart)
                val duration = calculateTotalNumberOfMinutes(
                    taskToBeMoved.startTime!!,
                    taskToBeMoved.endTime!!
                )

                // Now, we're calculating how much space we have between the clock start and the new end of the task
                val rangeStart = activeTimeStart
                val rangeEnd = taskTobeMovedNewEndTime
                val rangeCapacity = calculateTotalNumberOfMinutes(
                    start = rangeStart,
                    end = rangeEnd
                )

                // If the new start and end times are within the active time (literally if the new start time is not smaller than the active start time and the new end time is not greater than the active end time)
                if (taskTobeMovedNewStartTime.compareTo(activeTimeStart) != -1
                    && taskTobeMovedNewEndTime.compareTo(activeTimeEnd) != 1) {
                    // If the range capacity is big enough to fit in the task
                    if (rangeCapacity >= duration) {
                        // Check if the task that is moved starts to overlap an adjacent task while it's moving (omit the dragged task)
                        // Calculate the index of the adjacent task (assign the previous index)
                        var adjacentTaskIndex = index - 1

                        // If there are tasks before the task to be moved
                        if (adjacentTaskIndex >= 0) {
                            // Check if the adjacent task index does not point to the task to be moved. If so, decrease the adjacent index further
                            if (tasks[adjacentTaskIndex].id == touchedTask!!.id) {
                                if (adjacentTaskIndex > 0) {
                                    adjacentTaskIndex--
                                }
                            }

                            // If the adjacent task is not the task to be moved
                            if (tasks[adjacentTaskIndex].id != touchedTask!!.id) {
                                val adjacentTask = tasks[adjacentTaskIndex]

                                // If the tasks start to overlap, start moving backwards the adjacent task
                                if (taskTobeMovedNewStartTime.compareTo(adjacentTask.endTime!!) == -1
                                    || taskTobeMovedNewStartTime.compareTo(adjacentTask.endTime!!) == 0) {
                                    // Re-adjust the angle change - calculate by how much the tasks are overlapping
                                    val adjacentTaskEndAngle =
                                        calculateAngleFromTime(
                                            activeTimeStart = activeTimeStart,
//                                            activeTimeEnd,
                                            time = adjacentTask.endTime!!,
                                            minuteAngle = minuteAngle
                                        )
                                    val adjacentTaskEndAngleTranslated = translateAngle270To0(adjacentTaskEndAngle)
                                    val taskToBeMovedNewStartAngleTranslated = translateAngle270To0(taskToBeMovedNewStartAngle)
                                    var angleChangeAdjusted = adjacentTaskEndAngleTranslated - taskToBeMovedNewStartAngleTranslated

                                    // If the adjacent task is pinned, we will want to swap the index of the task to be moved with the index of the pinned task
                                    if (adjacentTask.pinned) {
                                        // To do so, we will have to:
                                        // - remove the element at the index of the task to be moved and add it at the index of the pinned task
                                        tasks[index] = tasks[adjacentTaskIndex].also {
                                            tasks[adjacentTaskIndex] = tasks[index]
                                        }

                                        // - re-calculate the angle change by which the task to be moved should be moved - that will correspond to placing the task to be moved before the pinned task
                                        val pinnedTaskDuration = calculateTotalNumberOfMinutes(
                                            adjacentTask.startTime!!,
                                            adjacentTask.endTime!!
                                        ) * minuteAngle
                                        angleChangeAdjusted = angleChange + pinnedTaskDuration + minuteAngle - angleChangeAdjusted

                                        // - call the moveTaskBackwardCascade() method again on the new index of the task to be moved with the new angle change
                                        moveTaskBackwardCascade(adjacentTaskIndex, angleChangeAdjusted)

                                        // - return so that the moving of the task at the index is cancelled because now the index points to the pinned task
                                        return
                                    }

                                    moveTaskBackwardCascade(adjacentTaskIndex, angleChangeAdjusted)
                                }
                                else {
                                    tasks.find { task -> task.id == taskToBeMoved.id }?.apply {
                                        startTime = taskTobeMovedNewStartTime
                                        endTime = taskTobeMovedNewEndTime
                                    }
                                }
                            }
                            // If the adjacent task is still the task to be moved, just update the task to moved with new start- and end times
                            else {
                                tasks.find { task -> task.id == taskToBeMoved.id }?.apply {
                                    startTime = taskTobeMovedNewStartTime
                                    endTime = taskTobeMovedNewEndTime
                                }
                            }
                        }
                        // If there are no more tasks before the task to be moved
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

    // Moves the task with given index by the specified angle change. The value of angleChange must be negative
    fun moveTaskForwardCascade(index: Int, angleChange: Float) {
        val taskToBeMoved = tasks[index]

        if (taskToBeMoved.id != touchedTask!!.id) {
            val taskToBeMovedStartAngle =
                calculateAngleFromTime(
                    activeTimeStart = activeTimeStart,
//                    activeTimeEnd,
                    time = taskToBeMoved.startTime!!,
                    minuteAngle = minuteAngle
                ) + 0.33f// It seems that during the TouchGestureUtils.calculateAngleFromTime() conversion around 0.33f is lost (subtracted from the angle). This makes it difficult for the task to move forward, so we're adding the 0.33f back. If I add this value back in the TouchGestureUtils.calculateAngleFromTime() method, the task will have difficulty moving backward (in the moveTaskBackward() method).
            val taskToBeMovedEndAngle =
                calculateAngleFromTime(
                    activeTimeStart = activeTimeStart,
//                    activeTimeEnd,
                    time = taskToBeMoved.endTime!!,
                    minuteAngle = minuteAngle
                ) + 0.33f
            val taskToBeMovedEndAngleTranslated = translateAngle270To0(taskToBeMovedEndAngle)
            val taskToBeMovedNewEndAngleTranslated = taskToBeMovedEndAngleTranslated - angleChange

            // If the angle change does not cause the moved task to cross the start of the clock, continue with moving the task forwards
            if (taskToBeMovedNewEndAngleTranslated <= 360f) {
                var taskToBeMovedNewStartAngle = taskToBeMovedStartAngle - angleChange
                var taskToBeMovedNewEndAngle = taskToBeMovedEndAngle - angleChange

                if (taskToBeMovedNewStartAngle > 360f) taskToBeMovedNewStartAngle -= 360f
                if (taskToBeMovedNewEndAngle > 360f) taskToBeMovedNewEndAngle -= 360f
                if (taskToBeMovedNewStartAngle < 0f) taskToBeMovedNewStartAngle += 360f
                if (taskToBeMovedNewEndAngle < 0f) taskToBeMovedNewEndAngle += 360f

                val taskTobeMovedNewStartTime = calculateTimeFromAngle(taskToBeMovedNewStartAngle, minuteAngle, activeTimeStart)
                val taskTobeMovedNewEndTime = calculateTimeFromAngle(taskToBeMovedNewEndAngle, minuteAngle, activeTimeStart)

                val duration = calculateTotalNumberOfMinutes(
                    taskToBeMoved.startTime!!,
                    taskToBeMoved.endTime!!
                )

                val rangeStart = taskTobeMovedNewStartTime
                val rangeEnd = activeTimeEnd
                val rangeCapacity = calculateTotalNumberOfMinutes(
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
                                    val adjacentTaskStartAngle = calculateAngleFromTime(
                                            activeTimeStart = activeTimeStart,
//                                            activeTimeEnd,
                                            time = adjacentTask.startTime!!,
                                            minuteAngle = minuteAngle
                                        ) + 0.33f// It seems that during the TouchGestureUtils.calculateAngleFromTime() conversion around 0.33f is lost (subtracted from the angle). This makes it difficult for the task to move forward, so we're adding the 0.33f back. If I add this value back in the TouchGestureUtils.calculateAngleFromTime() method, the task will have difficulty moving backward (in the moveTaskBackward() method).
                                    val adjacentTaskStartAngleTranslated = translateAngle270To0(adjacentTaskStartAngle)
                                    val taskToBeMovedNewEndAngleTranslated = translateAngle270To0(taskToBeMovedNewEndAngle)
                                    val angleChangeAdjusted = adjacentTaskStartAngleTranslated - taskToBeMovedNewEndAngleTranslated

                                    moveTaskForwardCascade(adjacentTaskIndex, angleChangeAdjusted)
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
        drawNewTaskTimeRange = false
        touchWithinTaskArea = false
//        selectTask(null)
        viewModel.onEvent(PlannerUiEvent.SelectedTaskIdChanged(null))
    }

    // Reset the start and end angles of where the finger touched the dial
    fun resetStartAndEndAngles() {
        startAngle = null
        tmpStartAngle = null
        tmpEndAngle = null
        boundaryAngleTranslated = null
    }

    fun resetTask() {
        touchedTask = null
//        selectTask(null)
        viewModel.onEvent(PlannerUiEvent.SelectedTaskIdChanged(null))
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

//    fun roundToOneDecimal(x: Float) = Math.round(x * 10) / 10
    fun roundToOneDecimal(x: Float) = (x * 10).roundToInt() / 10

    // Update the clock every minute
    LaunchedEffect(true) {
        while (true) {
            delay(1000L * SECONDS_IN_MINUTE)
            clockTime = LocalTime.parse(java.time.LocalTime.now().toString())
        }
    }

    // Clear the task UI state when the component is loaded for the first time
    LaunchedEffect(Unit) {
//        selectTask(null)// TODO: This should clear the task UI state after coming back from the TaskInfoScreen, but it will not do anything when the user drags task along the dial, since then the component is not drawn for the first time (instead, it's redrawn). This could be solved if for example I cleared the task UI state based on the component's state
        viewModel.onEvent(PlannerUiEvent.SelectedTaskIdChanged(null))
    }

    LaunchedEffect(selectedDate) {
        // Resets the zoom among others
        reset()
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
                .padding(top = 10.dp)
                .fillMaxSize()
            ,
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // This column pushes the active start- and end time button to the center
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
                            .background(HEADER_TEXT_COLOR)
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
                        tint = HEADER_TEXT_COLOR
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
                    .pointerInput(day, editedTask, selectedDate) {
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

                                if (transformMode && event.changes.size > 1) {
                                    // Multitouch transform mode
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
//                                    onTransform(zoomChange, rotationChange, panChange)
                                        scale *= zoomChange
                                        offset += panChange

                                        event.changes.forEach { it.consume() }
                                    }
                                } else if (!transformMode && event.changes.size == 1) {
                                    // Single-finger drag
                                    val change = event.changes.first()
                                    val dragAmount = change.positionChange()

//                                if (dragAmount != Offset.Zero) {
//                                    onDrag(dragAmount)
//                                    change.consume()
//                                }
                                }

                                if (!pressed) transformMode = false
                            } while (pressed)
                        }
                    }
                    .pointerInput(day, editedTask, selectedDate) {
                        detectTapGestures(
                            onTap = { offset ->
                                val distance = distance(offset, center)
                                touchInsideTheDial = checkIfTouchInsideDial(
                                    distance,
                                    centerRadius,
                                    innerRadius,
                                    touchStroke
                                )
                                angle = angle(center, offset)

                                if (touchInsideTheDial) {
                                    if (taskMode == TaskMode.View) {
                                        // Check whether the touch is within the ANY task area
                                        touchWithinTaskArea = checkIfTouchWithinTasks(
                                            angle,
                                            tasks
                                        )

                                        if (touchWithinTaskArea) {
                                            // Show task info
                                            showPopupWindow = true
                                        }
                                    } else if (taskMode == TaskMode.EditTimeRange || taskMode == TaskMode.EditStartTime) {
                                        // Check whether the touch is within the GIVEN task area
                                        touchWithinTaskArea = checkIfTouchWithinAngleRange(
                                            angle,
                                            // The values should be filled in either in onDoubleTap or in detectDragGesturesAfterLongPress
                                            tmpStartAngle!!,
                                            tmpEndAngle!!
                                        )

                                        if (touchWithinTaskArea) {
                                            // Start angle carries now the supposedly new value for start time of the given task, the end angle the value for the end time, respectively. Update the task:
                                            // - start time
                                            val clockTaskStartTime = calculateTimeFromAngle(
                                                // The value should be filled in either in onDoubleTap or in detectDragGesturesAfterLongPress
                                                angle = tmpStartAngle!!,
                                                clockStart = activeTimeStart,
                                                minuteAngle = minuteAngle
                                            )
                                            // - end time
                                            val clockTaskEndTime = calculateTimeFromAngle(
                                                // The value should be filled in either in onDoubleTap or in detectDragGesturesAfterLongPress
                                                angle = tmpEndAngle!!,
                                                clockStart = activeTimeStart,
                                                minuteAngle = minuteAngle
                                            )

//                                            setTaskStartTime(clockTaskStartTime)
//                                            setTaskEndTime(clockTaskEndTime)
                                            viewModel.onEvent(PlannerUiEvent.TaskStartTimeChanged(clockTaskStartTime))
                                            viewModel.onEvent(PlannerUiEvent.TaskEndTimeChanged(clockTaskEndTime))


                                            // Set the task's date
//                                            setTaskDate(userInput.selectedDate)
//                                            saveTaskFromState()
                                            viewModel.onEvent(PlannerUiEvent.TaskDateChanged(selectedDate))
                                            viewModel.onEvent(PlannerUiEvent.SaveTask)

                                            // Reset the dial
                                            reset()
                                        }
                                    } else if (taskMode == TaskMode.Create) {
//                                        Log.i("PlannerDial", "selectedTask $selectedTask")
                                        Log.i("PlannerDial", "editedTask $editedTask")

                                        // Check whether the touch is within the new task area
                                        touchWithinTaskArea = checkIfTouchWithinTaskArea(
                                            angle = angle,
                                            clockStart = activeTimeStart,
//                                            clockEnd = activeTimeEnd,
                                            minuteAngle = minuteAngle,
                                            taskStart = editedTask.startTime!!,
                                            taskEnd = editedTask.endTime!!
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
                                val distance = distance(offset, center)
                                touchInsideTheDial = checkIfTouchInsideDial(
                                    distance,
                                    centerRadius,
                                    innerRadius,
                                    touchStroke
                                )
                                angle = angle(center, offset)

                                if (touchInsideTheDial) {
                                    // Check if touch is within ANY task area
                                    touchWithinTaskArea = checkIfTouchWithinTasks(
                                        angle,
                                        tasks
                                    )

                                    if (touchWithinTaskArea) {
                                        taskMode = TaskMode.EditTimeRange
                                        val taskStartAngle = calculateAngleFromTime(
                                            activeTimeStart = activeTimeStart,
//                                            activeTimeEnd,
                                            time = touchedTask!!.startTime!!,
                                            minuteAngle = minuteAngle
                                        )
                                        val taskEndAngle = calculateAngleFromTime(
                                            activeTimeStart = activeTimeStart,
//                                            activeTimeEnd,
                                            time = touchedTask!!.endTime!!,
                                            minuteAngle = minuteAngle
                                        )
                                        tmpStartAngle = taskStartAngle
                                        tmpEndAngle = taskEndAngle

                                        // Display the task's start time on the task clock
                                        taskClockTime = calculateTimeFromAngle(
                                            angle = tmpStartAngle!!,
                                            clockStart = activeTimeStart,
                                            minuteAngle = minuteAngle
                                        )
                                    }
                                } else {
                                    // Cancel everything if the touch is outside the dial
                                    reset()
                                }
                            },
//                        onLongPress = { offset ->
//                            // Get the starting coordinates and determine if the touch is:
//                            // 1. within the dial
//                            // 2. within any existing task area
//                            val distance = TouchGestureUtils.distance(offset, center)
//                            angle = TouchGestureUtils.angle(center, offset)
//
//                            // Clear the task UI state
//                            selectTask(null)
//
//                            // If the touch is within a task area, we will want to drag that task along the dial. Therefore, the mode will be changed to EDIT_TIME_START.
//                            touchInsideTheDial = TouchGestureUtils.checkIfTouchInsideDial(
//                                distance,
//                                centerRadius,
//                                innerRadius,
//                                touchStroke
//                            )
//
//                            // Check if touch is near the dial edge
//                            touchNearTheDialEdge =
//                                TouchGestureUtils.checkIfTouchNearDialEdge(
//                                    distance,
//                                    innerRadius,
//                                    outerRadius,
//                                    touchStroke
//                                )
//
//                            if (touchInsideTheDial || touchNearTheDialEdge) {
//                                touchWithinTaskArea =
//                                    checkIfTouchWithinTasks(
//                                        angle,
//                                        tasks
//                                    )
//
//                                // If the long press was within a task, switch to the edit-the-task's-start-time mode
//                                if (touchWithinTaskArea) {
//                                    taskMode = TaskMode.EditStartTime
//
//                                    tmpStartAngle = TouchGestureUtils.calculateAngleFromTime(
//                                        activeTimeStart,
//                                        touchedTask!!.startTime!!,
//                                        minuteAngle
//                                    )
//
//                                    tmpEndAngle = TouchGestureUtils.calculateAngleFromTime(
//                                        activeTimeStart,
//                                        touchedTask!!.endTime!!,
//                                        minuteAngle
//                                    )
//
//                                    draggedTaskDuration =
//                                        TouchGestureUtils.calculateTotalNumberOfMinutes(
//                                            touchedTask!!.startTime!!,
//                                            touchedTask!!.endTime!!
//                                        )
//
//                                    // Display the task's start time on the task clock
//                                    taskClockTime = TouchGestureUtils.calculateTimeFromAngle(
//                                        angle = tmpStartAngle,
//                                        clockStart = activeTimeStart,
//                                        minuteAngle = minuteAngle
//                                    )
//                                } else {
//                                    taskMode = TaskMode.Create
//
//                                    // Calculate the time represented by the angle to display it in the clock center
//                                    taskClockTime = TouchGestureUtils.calculateTimeFromAngle(
//                                        angle = angle,
//                                        clockStart = activeTimeStart,
//                                        minuteAngle = minuteAngle
//                                    )
//
//                                    startAngle = angle
//                                    endAngle = angle
//                                    tmpStartAngle = angle
//                                    tmpEndAngle = angle
//
//                                    val currentAngleTranslated =
//                                        TouchGestureUtils.translateAngle270To0(angle)
//
//                                    initialAngleTranslated = currentAngleTranslated
//
//                                    // Find the next and previous tasks, if any
//                                    for (task in tasks) {
//                                        val taskEndAngle =
//                                            TouchGestureUtils.calculateAngleFromTime(
//                                                activeTimeStart,
//                                                task.endTime!!,
//                                                minuteAngle
//                                            )
//                                        val taskEndAngleTranslated =
//                                            TouchGestureUtils.translateAngle270To0(taskEndAngle)
//
//                                        if (taskEndAngleTranslated < currentAngleTranslated) {
//                                            previousTask = task.copy()
//                                        } else {
//                                            if (nextTask == null) {
//                                                nextTask = task.copy()
//                                            }
//                                        }
//                                    }
//                                }
//
//                                isTransforming = false
//                            }
//                        }
                        )
                    }
                    .pointerInput(day, editedTask, selectedDate) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                Log.i("PlannerDial", "detectDragGesturesAfterLongPress() onDragEnd")
                                // Get the starting coordinates and determine if the touch is:
                                // 1. within the dial
                                // 2. within any existing task area
                                val distance = distance(offset, center)
                                angle = angle(center, offset)
                                startAngle = angle

                                // Clear the task UI state
//                                selectTask(null)
                                viewModel.onEvent(PlannerUiEvent.SelectedTaskIdChanged(null))

                                // If the touch is within a task area, we will want to drag that task along the dial. Therefore, the mode will be changed to EDIT_TIME_START.
                                touchInsideTheDial = checkIfTouchInsideDial(
                                    distance,
                                    centerRadius,
                                    innerRadius,
                                    touchStroke
                                )

                                // Check if touch is near the dial edge
                                touchNearTheDialEdge = checkIfTouchNearDialEdge(
                                    distance,
                                    innerRadius,
                                    outerRadius,
                                    touchStroke
                                )

                                if (touchInsideTheDial || touchNearTheDialEdge) {
                                    touchWithinTaskArea = checkIfTouchWithinTasks(
                                        angle,
                                        tasks
                                    )

                                    // If the long press was within a task, switch to the edit-the-task's-start-time mode - i.e. allow the task to me dragged
                                    if (touchWithinTaskArea) {
                                        taskMode = TaskMode.EditStartTime

                                        tmpStartAngle = calculateAngleFromTime(
                                            activeTimeStart = activeTimeStart,
//                                            activeTimeEnd,
                                            time = touchedTask!!.startTime!!,
                                            minuteAngle = minuteAngle
                                        )

                                        tmpEndAngle = calculateAngleFromTime(
                                            activeTimeStart = activeTimeStart,
//                                            activeTimeEnd,
                                            time = touchedTask!!.endTime!!,
                                            minuteAngle = minuteAngle
                                        )

                                        draggedTaskDuration = calculateTotalNumberOfMinutes(
                                            touchedTask!!.startTime!!,
                                            touchedTask!!.endTime!!
                                        )

                                        // Display the task's start time on the task clock
                                        taskClockTime = calculateTimeFromAngle(
                                            angle = tmpStartAngle!!,
                                            clockStart = activeTimeStart,
                                            minuteAngle = minuteAngle
                                        )
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                Log.i("PlannerDial", "detectDragGesturesAfterLongPress() onDrag")
                                // If touch is within dial, keep track of the coordinates
                                val offset = change.position
                                val distance = distance(offset, center)

                                touchNearTheDialEdge = checkIfTouchNearDialEdge(
                                    distance,
                                    innerRadius,
                                    outerRadius,
                                    touchStroke
                                )
                                touchInsideTheDial = checkIfTouchInsideDial(
                                    distance,
                                    centerRadius,
                                    innerRadius,
                                    touchStroke
                                )

                                if (taskMode == TaskMode.EditStartTime) {
                                    if (touchInsideTheDial || touchNearTheDialEdge) {
                                        // Determine the direction of the drag
                                        val currentAngle = angle(center, offset)
                                        angle = currentAngle
                                        val currentAngleTranslated =
                                            translateAngle270To0(currentAngle)
                                        val startAngleTranslated =
                                            translateAngle270To0(startAngle!!)

                                        angleMode =
                                            if (currentAngleTranslated < startAngleTranslated) {
                                                AngleMode.Start
                                            } else {
                                                AngleMode.End
                                            }

                                        // This code is not executed. After long-pressing on a task area the drag is not detected by the .detectDragGesturesAfterLongPress() - for some reason the finger has to be lifted and then the drag gestures are detected by the .detectDragGestures() instead
                                        if (taskMode == TaskMode.EditStartTime) {
                                            val currentAngle = angle(center, change.position)
                                            val previousAngle =
                                                angle(center, change.previousPosition)
                                            // We're rounding the angle values because at the end of dragging the values get mixed: e.g. during dragging backwards the previous angle is calculated as smaller than current angle
                                            val currentAngleTranslated = roundToOneDecimal(
                                                translateAngle270To0(
                                                    currentAngle
                                                )
                                            )
                                            val previousAngleTranslated = roundToOneDecimal(
                                                translateAngle270To0(
                                                    previousAngle
                                                )
                                            )

                                            if (currentAngleTranslated > previousAngleTranslated) {
                                                dragDirection = DragDirection.Forward
                                            } else if (currentAngleTranslated < previousAngleTranslated) {
                                                dragDirection = DragDirection.Backward
                                            }

                                            // The tmpStartAngle value should be filled in detectDragGesturesAfterLongPress
                                            var touchedTaskNewStartAngle =
                                                tmpStartAngle!! + currentAngle - previousAngle
                                            var touchedTaskNewEndAngle =
                                                tmpStartAngle!! + (draggedTaskDuration * minuteAngle)

                                            // Correct the angle if its value exceeds 360 degrees
                                            if (touchedTaskNewStartAngle > 360f) touchedTaskNewStartAngle -= 360f
                                            if (touchedTaskNewEndAngle > 360f) touchedTaskNewEndAngle -= 360f

                                            val touchedTaskNewStartAngleTranslated =
                                                translateAngle270To0(
                                                    touchedTaskNewStartAngle
                                                )
                                            val touchedTaskNewEndAngleTranslated =
                                                translateAngle270To0(
                                                    touchedTaskNewEndAngle
                                                )

                                            // If the task's end time doesn't pass the 0/360 degree mark, move the task
                                            if (touchedTaskNewEndAngleTranslated < 360f && touchedTaskNewStartAngleTranslated >= 0) {
                                                if (touchedTaskNewEndAngleTranslated > touchedTaskNewStartAngleTranslated) {
                                                    tmpStartAngle = touchedTaskNewStartAngle
                                                    tmpEndAngle = touchedTaskNewEndAngle

                                                    // Update the selected task with new start- and end time - These values are not updated on time during task swapping, but they are updated on time before onDragEnd()
                                                    tasks.find { task -> task.id == touchedTask!!.id }
                                                        ?.apply {
                                                            startTime = calculateTimeFromAngle(
                                                                tmpStartAngle!!,
                                                                minuteAngle,
                                                                activeTimeStart
                                                            )
                                                            endTime = calculateTimeFromAngle(
                                                                tmpEndAngle!!,
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
                                                        translateAngle270To0(
                                                            touchedTaskMiddleAngle
                                                        )

                                                    // Find the task the touched task overlaps (if any)
                                                    for ((index, task) in tasks.withIndex()) {
                                                        if (task.id != touchedTask!!.id) {
                                                            val taskStartAngle =
                                                                calculateAngleFromTime(
                                                                    activeTimeStart = activeTimeStart,
//                                                                    activeTimeEnd,
                                                                    time = task.startTime!!,
                                                                    minuteAngle = minuteAngle
                                                                )
                                                            val taskStartAngleTranslated =
                                                                translateAngle270To0(
                                                                    taskStartAngle
                                                                )
                                                            val taskEndAngle =
                                                                calculateAngleFromTime(
                                                                    activeTimeStart = activeTimeStart,
//                                                                    activeTimeEnd,
                                                                    time = task.endTime!!,
                                                                    minuteAngle = minuteAngle
                                                                )
                                                            val taskEndAngleTranslated =
                                                                translateAngle270To0(
                                                                    taskEndAngle
                                                                )
                                                            val duration =
                                                                calculateTotalNumberOfMinutes(
                                                                    start = task.startTime!!,
                                                                    end = task.endTime!!
                                                                )
                                                            var taskMiddleAngle =
                                                                taskStartAngle + ((duration * minuteAngle) * 0.5f)

                                                            // Correct the middle angle if due to constant addition its value exceeds 360 degrees
                                                            if (taskMiddleAngle > 360f) taskMiddleAngle -= 360f

                                                            val taskMiddleAngleTranslated =
                                                                translateAngle270To0(
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
                                                                    translateAngle270To0(
                                                                        currentAngle
                                                                    )
                                                                val previousAngleTranslated =
                                                                    translateAngle270To0(
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
                                                                        // If the task is not pinned...
                                                                        if (!task.pinned) {
                                                                            // ...start moving the overlapped task backwards as the dragged task moves forward until the two tasks stop overlapping/the overlapped task cannot be moved further
                                                                            moveTaskBackwardCascade(
                                                                                overlappedTaskIndex,
                                                                                angleChange
                                                                            )
                                                                        }

                                                                        // Set the jump-to-position to just after the next task (should the dragging end while the tasks overlap)
                                                                        jumpToStartAngle =
                                                                            taskEndAngle + minuteAngle
                                                                        jumpToEndAngle =
                                                                            jumpToStartAngle!! + (draggedTaskDuration * minuteAngle)

                                                                        if (jumpToStartAngle!! > 360f) jumpToStartAngle =
                                                                            jumpToStartAngle!! - 360f
                                                                        if (jumpToEndAngle!! > 360f) jumpToEndAngle =
                                                                            jumpToEndAngle!! - 360f

                                                                        val jumpToStartAngleTranslated =
                                                                            translateAngle270To0(
                                                                                jumpToStartAngle!!
                                                                            )
                                                                        val jumpToEndAngleTranslated =
                                                                            translateAngle270To0(
                                                                                jumpToEndAngle!!
                                                                            )

                                                                        // If the jump-to-position crosses the start of the clock
                                                                        if (jumpToEndAngleTranslated < jumpToStartAngleTranslated) {
                                                                            // Push the jump-to-position just before the end of the active time
                                                                            jumpToEndAngle = 270f
                                                                            jumpToStartAngle =
                                                                                jumpToEndAngle!! - draggedTaskDuration * minuteAngle

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
                                                                        // If the task is not pinned...
                                                                        if (!task.pinned) {
                                                                            // ...start moving the overlapped task backwards as the dragged task moves forward until the two tasks stop overlapping/the overlapped task cannot be moved further
                                                                            moveTaskForwardCascade(
                                                                                overlappedTaskIndex,
                                                                                angleChange
                                                                            )
                                                                        }

                                                                        // Set the jump-to-position to just after the next task (should the dragging end while the tasks overlap)
                                                                        jumpToEndAngle =
                                                                            taskStartAngle - minuteAngle
                                                                        jumpToStartAngle =
                                                                            jumpToEndAngle!! - (draggedTaskDuration * minuteAngle)

                                                                        if (jumpToStartAngle!! < 0f) jumpToStartAngle =
                                                                            jumpToStartAngle!! + 360f
                                                                        if (jumpToEndAngle!! < 0f) jumpToEndAngle =
                                                                            jumpToEndAngle!! + 360f

                                                                        val jumpToStartAngleTranslated =
                                                                            translateAngle270To0(
                                                                                jumpToStartAngle!!
                                                                            )
                                                                        val jumpToEndAngleTranslated =
                                                                            translateAngle270To0(
                                                                                jumpToEndAngle!!
                                                                            )

                                                                        // If the jump-to-position crosses the start of the clock
                                                                        if (jumpToStartAngleTranslated > jumpToEndAngleTranslated) {
                                                                            // Push the jump-to-position just before the end of the active time
                                                                            jumpToStartAngle = 0f
                                                                            jumpToEndAngle =
                                                                                jumpToStartAngle!! + draggedTaskDuration * minuteAngle

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
                                            tmpStartAngle?.let {
                                                taskClockTime = calculateTimeFromAngle(
                                                    angle = tmpStartAngle!!,
                                                    clockStart = activeTimeStart,
                                                    minuteAngle = minuteAngle
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            onDragEnd = {
                                Log.i("PlannerDial", "detectDragGesturesAfterLongPress() onDragEnd")
                                // This code is not executed. After long-pressing on a task area the drag is not detected by the .detectDragGesturesAfterLongPress() - for some reason the finger has to be lifted and then the drag gestures are detected by the .detectDragGestures() instead
                                if (taskMode == TaskMode.EditStartTime) {
                                    // Calculate the time represented by the angle
                                    val taskNewStartTime = calculateTimeFromAngle(
                                        angle = tmpStartAngle!!,
                                        clockStart = activeTimeStart,
                                        minuteAngle = minuteAngle
                                    )
                                    val taskNewEndTime = calculateTimeFromAngle(
                                        angle = tmpEndAngle!!,
                                        clockStart = activeTimeStart,
                                        minuteAngle = minuteAngle
                                    )

                                    // Update the task's start- and end times
//                                    setTaskStartTime(taskNewStartTime)
//                                    setTaskEndTime(taskNewEndTime)
//                                    saveTaskFromState()
                                    viewModel.onEvent(PlannerUiEvent.TaskStartTimeChanged(taskNewStartTime))
                                    viewModel.onEvent(PlannerUiEvent.TaskEndTimeChanged(taskNewEndTime))
                                    viewModel.onEvent(PlannerUiEvent.SaveTask)

                                    reset()
                                }
                            }
                        )
                    }
                    .pointerInput(day, editedTask, selectedDate) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                Log.i("PlannerDial", "1499 detectDragGestures() onDragStart")
                                // Get the starting coordinates and determine if the touch is:
                                // 1. within the dial
                                // 2. within any existing task area
                                val distance = distance(offset, center)
                                touchNearTheDialEdge = checkIfTouchNearDialEdge(
                                    distance,
                                    innerRadius,
                                    outerRadius,
                                    touchStroke
                                )
                                touchInsideTheDial = checkIfTouchInsideDial(
                                    distance,
                                    centerRadius,
                                    innerRadius,
                                    touchStroke
                                )

                                if (touchNearTheDialEdge || touchInsideTheDial) {
                                    val currentAngle = angle(center, offset)
                                    angle = currentAngle
                                    startAngle = angle

                                    if (taskMode == TaskMode.View) {
                                        // Calculate the time represented by the angle to display it in the clock center
                                        taskClockTime = calculateTimeFromAngle(
                                            angle = angle,
                                            clockStart = activeTimeStart,
                                            minuteAngle = minuteAngle
                                        )
                                        Log.i("PlannerDial", "1542 angle $angle")
                                        Log.i("PlannerDial", "1543 startAngle $startAngle")
                                        val currentAngleTranslated = translateAngle270To0(angle)

//                                        boundaryAngleTranslated = currentAngleTranslated

                                        // Find the next and previous tasks, if any
                                        for (task in tasks) {
                                            val taskEndAngle = calculateAngleFromTime(
                                                activeTimeStart = activeTimeStart,
//                                                activeTimeEnd,
                                                time = task.endTime!!,
                                                minuteAngle = minuteAngle
                                            )
                                            val taskEndAngleTranslated =
                                                translateAngle270To0(taskEndAngle)

                                            if (taskEndAngleTranslated < currentAngleTranslated) {
                                                previousTask = task.copy()
                                            } else {
                                                if (nextTask == null) {
                                                    nextTask = task.copy()
                                                }
                                            }
                                        }
                                        Log.i("PlannerDial", "previousTask $previousTask")
                                        Log.i("PlannerDial", "nextTask $nextTask")
                                    }
                                }
                                // -------------------------- ADDED CODE
                            },
                            onDrag = { change, dragAmount ->
                                Log.i("PlannerDial", "detectDragGestures() onDrag")
                                if (taskMode == TaskMode.Create
                                    || taskMode == TaskMode.EditTimeRange
                                    || taskMode == TaskMode.View
                                ) {
                                    Log.i("PlannerDial", "1578 taskMode $taskMode")
                                    val offset = change.position
                                    val distance = distance(offset, center)
                                    touchNearTheDialEdge = checkIfTouchNearDialEdge(
                                        distance,
                                        innerRadius,
                                        outerRadius,
                                        touchStroke
                                    )
                                    touchInsideTheDial = checkIfTouchInsideDial(
                                        distance,
                                        centerRadius,
                                        innerRadius,
                                        touchStroke
                                    )

                                    if (touchNearTheDialEdge || touchInsideTheDial) {
                                        val currentAngle = angle(center, offset)
                                        angle = currentAngle
                                        val currentAngleTranslated =
                                            translateAngle270To0(currentAngle)

                                        // If we're still in the view mode, meaning we're not creating or editing anything
                                        if (taskMode == TaskMode.View) {
                                            drawNewTaskTimeRange = true

//                                            Log.i("PlannerDial", "1605 taskUiState $taskUiState")

                                            // The startAngle value should be filled in detectDragGestures.onDragStart()
                                            val startAngleTranslated = translateAngle270To0(startAngle!!)
                                            Log.i("PlannerDial", "1613 angle $angle")
                                            Log.i("PlannerDial", "1614 startAngle $startAngle")
                                            Log.i("PlannerDial", "1615 startAngleTranslated $startAngleTranslated")
                                            val currentAngleTranslated = translateAngle270To0(currentAngle)
                                            Log.i("PlannerDial", "1617 currentAngleTranslated $currentAngleTranslated")

                                            angleMode =
                                                if (currentAngleTranslated < startAngleTranslated) {
                                                    tmpEndAngle = startAngle
                                                    tmpStartAngle = angle
                                                    AngleMode.Start
                                                } else if (currentAngleTranslated > startAngleTranslated) {
                                                    tmpStartAngle = startAngle
                                                    tmpEndAngle = angle
                                                    AngleMode.End
                                                } else {
                                                    AngleMode.None
                                                }
                                            Log.i("PlannerDial", "1630 angleMode $angleMode")
                                            if (angleMode != AngleMode.None) {
                                                taskMode = TaskMode.Create
                                                boundaryAngleTranslated = startAngleTranslated
                                            }
                                        }
                                        // ----------------------------- ADDED CODE
                                        else {
                                            Log.i("PlannerDial", "1684 angleMode $angleMode")
                                            Log.i("PlannerDial", "1685 angle $angle")
                                            Log.i("PlannerDial", "1686 startAngle $startAngle")
                                            val startAngleTranslated =
                                                translateAngle270To0(startAngle!!)
                                            Log.i(
                                                "PlannerDial",
                                                "1688 startAngleTranslated $startAngleTranslated"
                                            )
                                            Log.i(
                                                "PlannerDial",
                                                "1689 currentAngleTranslated $currentAngleTranslated"
                                            )
                                            // If we don't know which time boundary of the given task we are setting
                                            if (angleMode == AngleMode.None) {
                                                // Determine which time boundary of the given task we are setting: start or end time
                                                // It seems that an angle == tmpStartAngle comparison does not cut it - the app is not able to detect the moment when the two angles are equal
                                                // The tmpStartAngle value should be filled in onDoubleTap or in detectDragGesturesAfterLongPress
                                                if (currentAngleTranslated in (translateAngle270To0(
                                                        tmpStartAngle!!
                                                    ) - touchStroke / 4f)..(translateAngle270To0(
                                                        tmpStartAngle!!
                                                    ) + touchStroke / 4f)
                                                ) {
                                                    angleMode = AngleMode.Start
                                                    boundaryAngleTranslated = translateAngle270To0(
                                                        calculateAngleFromTime(
                                                            activeTimeStart = activeTimeStart,
//                                                            activeTimeEnd,
                                                            // We're using the end time angle as a boundary
                                                            time = editedTask.endTime!!,
                                                            minuteAngle = minuteAngle
                                                        )
                                                    )
                                                }
                                                // The tmpEndAngle value should be filled in onDoubleTap or in detectDragGesturesAfterLongPress
                                                else if (currentAngleTranslated in (translateAngle270To0(tmpEndAngle!!) - touchStroke / 4f)..(translateAngle270To0(tmpEndAngle!!) + touchStroke / 4f)
                                                ) {
                                                    angleMode = AngleMode.End
                                                    boundaryAngleTranslated = translateAngle270To0(
                                                        calculateAngleFromTime(
                                                            activeTimeStart = activeTimeStart,
//                                                            activeTimeEnd,
                                                            // We're using the start time angle as a boundary
                                                            time = editedTask.startTime!!,
                                                            minuteAngle = minuteAngle
                                                        )
                                                    )
                                                }
                                            }
                                            // If we do know which time boundary of the given task we are setting
                                            else {
                                                // Show the clock time the angle corresponds to
                                                taskClockTime = calculateTimeFromAngle(
                                                    angle = angle,
                                                    clockStart = activeTimeStart,
                                                    minuteAngle = minuteAngle
                                                )

                                                // If we are setting the start time of the given task
                                                if (angleMode == AngleMode.Start) {
                                                    // If the current angle is smaller than the angle of the task's end time
                                                    // The boundaryAngleTranslated value should be provided in the onDrag() method
                                                    if (currentAngleTranslated < boundaryAngleTranslated!!) {
                                                        // If there is a previous task
                                                        if (previousTask != null) {
                                                            val previousTaskEndAngle =
                                                                calculateAngleFromTime(
                                                                    activeTimeStart = activeTimeStart,
//                                                                    activeTimeEnd,
                                                                    time = previousTask!!.endTime!!,
                                                                    minuteAngle = minuteAngle
                                                                )
                                                            val previousTaskEndAngleTranslated =
                                                                translateAngle270To0(
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
                                                    // The boundaryAngleTranslated value should be provided in the onDrag() method
                                                    if (currentAngleTranslated > boundaryAngleTranslated!!) {
                                                        if (nextTask != null) {
                                                            val nextTaskStartAngle =
                                                                calculateAngleFromTime(
                                                                    activeTimeStart = activeTimeStart,
//                                                                    activeTimeEnd,
                                                                    time = nextTask!!.startTime!!,
                                                                    minuteAngle = minuteAngle
                                                                )
                                                            val nextTaskStartAngleTranslated =
                                                                translateAngle270To0(
                                                                    nextTaskStartAngle
                                                                )
                                                            // If the angle does not cross the previous task's end time
                                                            if (currentAngleTranslated < nextTaskStartAngleTranslated) {
                                                                tmpEndAngle = angle
                                                            }
                                                        } else {
                                                            tmpEndAngle = angle
                                                        }
                                                    }
                                                }

                                                if (taskMode != TaskMode.View) {
                                                    // Ask whether the task should be deleted
                                                    // The boundaryAngleTranslated value should be provided in the onDrag() method
                                                    if (currentAngleTranslated in boundaryAngleTranslated!! - minuteAngle..boundaryAngleTranslated!! + minuteAngle) {
                                                        popupState = TaskModePopup.Delete
                                                        showPopupWindow = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                // This code is executed instead of the onDrag() in .detectDragGesturesAfterLongPress() - for some reason the finger has to be lifted after a long-press and the drag gestures are then detected by the .detectDragGestures()
                                else if (taskMode == TaskMode.EditStartTime) {
                                    val currentAngle = angle(center, change.position)
                                    val previousAngle = angle(center, change.previousPosition)
                                    // We're rounding the angle values because at the end of dragging the values get mixed: e.g. during dragging backwards the previous angle is calculated as smaller than current angle
                                    val currentAngleTranslated = roundToOneDecimal(
                                        translateAngle270To0(
                                            currentAngle
                                        )
                                    )
                                    val previousAngleTranslated = roundToOneDecimal(
                                        translateAngle270To0(
                                            previousAngle
                                        )
                                    )

                                    if (currentAngleTranslated > previousAngleTranslated) {
                                        dragDirection = DragDirection.Forward
                                    } else if (currentAngleTranslated < previousAngleTranslated) {
                                        dragDirection = DragDirection.Backward
                                    }

                                    // The tmpStartAngle value should be filled in detectDragGesturesAfterLongPress
                                    var touchedTaskNewStartAngle =
                                        tmpStartAngle!! + currentAngle - previousAngle
                                    var touchedTaskNewEndAngle =
                                        tmpStartAngle!! + (draggedTaskDuration * minuteAngle)

                                    // Correct the angle if its value exceeds 360 degrees
                                    if (touchedTaskNewStartAngle > 360f) touchedTaskNewStartAngle -= 360f
                                    if (touchedTaskNewEndAngle > 360f) touchedTaskNewEndAngle -= 360f

                                    val touchedTaskNewStartAngleTranslated = translateAngle270To0(
                                        touchedTaskNewStartAngle
                                    )
                                    val touchedTaskNewEndAngleTranslated = translateAngle270To0(
                                        touchedTaskNewEndAngle
                                    )

                                    // If the task's end time doesn't pass the 0/360 degree mark, move the task
                                    if (touchedTaskNewEndAngleTranslated < 360f && touchedTaskNewStartAngleTranslated >= 0) {
                                        if (touchedTaskNewEndAngleTranslated > touchedTaskNewStartAngleTranslated) {
                                            tmpStartAngle = touchedTaskNewStartAngle
                                            tmpEndAngle = touchedTaskNewEndAngle

                                            // Update the selected task with new start- and end time - These values are not updated on time during task swapping, but they are updated on time before onDragEnd()
                                            tasks.find { task -> task.id == touchedTask!!.id }
                                                ?.apply {
                                                    startTime = calculateTimeFromAngle(
                                                        angle = tmpStartAngle!!,
                                                        minuteAngle = minuteAngle,
                                                        clockStart = activeTimeStart
                                                    )
                                                    endTime = calculateTimeFromAngle(
                                                        angle = tmpEndAngle!!,
                                                        minuteAngle = minuteAngle,
                                                        clockStart = activeTimeStart
                                                    )
                                                }

                                            // TASK SWAPPING
                                            // Check if the middle angle of the dragged task reaches the start time of an adjacent task. If it does, the later task should get the start time of the earlier task and the earlier task should get the first start time available after the later task.
                                            var touchedTaskMiddleAngle =
                                                touchedTaskNewStartAngle + ((draggedTaskDuration * minuteAngle) * 0.5f)
                                            // Correct the middle angle if due to constant addition its value exceeds 360 degrees
                                            if (touchedTaskMiddleAngle > 360f) touchedTaskMiddleAngle -= 360f

                                            val touchedTaskMiddleAngleTranslated =
                                                translateAngle270To0(
                                                    touchedTaskMiddleAngle
                                                )

                                            // Find the task the touched task overlaps (if any)
                                            for ((index, task) in tasks.withIndex()) {
                                                if (task.id != touchedTask!!.id) {
                                                    val taskStartAngle = calculateAngleFromTime(
                                                        activeTimeStart = activeTimeStart,
//                                                        activeTimeEnd,
                                                        time = task.startTime!!,
                                                        minuteAngle = minuteAngle
                                                    )
                                                    val taskStartAngleTranslated =
                                                        translateAngle270To0(
                                                            taskStartAngle
                                                        )
                                                    val taskEndAngle = calculateAngleFromTime(
                                                        activeTimeStart = activeTimeStart,
//                                                        activeTimeEnd,
                                                        time = task.endTime!!,
                                                        minuteAngle = minuteAngle
                                                    )
                                                    val taskEndAngleTranslated =
                                                        translateAngle270To0(
                                                            taskEndAngle
                                                        )
                                                    val duration = calculateTotalNumberOfMinutes(
                                                        start = task.startTime!!,
                                                        end = task.endTime!!
                                                    )
                                                    var taskMiddleAngle =
                                                        taskStartAngle + ((duration * minuteAngle) * 0.5f)

                                                    // Correct the middle angle if due to constant addition its value exceeds 360 degrees
                                                    if (taskMiddleAngle > 360f) taskMiddleAngle -= 360f

                                                    val taskMiddleAngleTranslated =
                                                        translateAngle270To0(
                                                            taskMiddleAngle
                                                        )

                                                    // If the tasks overlap - if the dragged task is within the neighboring task or the neighboring task is within the dragged task
                                                    if (touchedTaskNewEndAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                        || touchedTaskNewStartAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                        || taskStartAngleTranslated in touchedTaskNewStartAngleTranslated..touchedTaskNewEndAngleTranslated
                                                        || taskEndAngleTranslated in touchedTaskNewStartAngleTranslated..touchedTaskNewEndAngleTranslated
                                                    ) {
                                                        val overlappedTaskIndex = index
                                                        val currentAngleTranslated =
                                                            translateAngle270To0(
                                                                currentAngle
                                                            )
                                                        val previousAngleTranslated =
                                                            translateAngle270To0(
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
                                                                moveTaskBackwardCascade(
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

                                                                val jumpToStartAngleTranslated =
                                                                    translateAngle270To0(
                                                                        jumpToStartAngle!!
                                                                    )
                                                                val jumpToEndAngleTranslated =
                                                                    translateAngle270To0(
                                                                        jumpToEndAngle!!
                                                                    )

                                                                // If the jump-to-position crosses the start of the clock
                                                                if (jumpToEndAngleTranslated < jumpToStartAngleTranslated) {
                                                                    // Push the jump-to-position just before the end of the active time
                                                                    jumpToEndAngle = 270f
                                                                    jumpToStartAngle =
                                                                        jumpToEndAngle!! - draggedTaskDuration * minuteAngle

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
                                                                // Start moving the overlapped task backwards as the dragged task moves forward until the two tasks stop overlapping/the overlapped task cannot be moved further
                                                                moveTaskForwardCascade(
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

                                                                val jumpToStartAngleTranslated =
                                                                    translateAngle270To0(
                                                                        jumpToStartAngle!!
                                                                    )
                                                                val jumpToEndAngleTranslated =
                                                                    translateAngle270To0(
                                                                        jumpToEndAngle!!
                                                                    )

                                                                // If the jump-to-position crosses the start of the clock
                                                                if (jumpToStartAngleTranslated > jumpToEndAngleTranslated) {
                                                                    // Push the jump-to-position just before the end of the active time
                                                                    jumpToStartAngle = 0f
                                                                    jumpToEndAngle =
                                                                        jumpToStartAngle!! + draggedTaskDuration * minuteAngle

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

                                    tmpStartAngle?.let {
                                        // Display the task's start time on the task clock
                                        taskClockTime = calculateTimeFromAngle(
                                            angle = tmpStartAngle!!,
                                            clockStart = activeTimeStart,
                                            minuteAngle = minuteAngle
                                        )
                                    }
                                }
                            },
                            onDragEnd = {
                                Log.i("PlannerDial", "detectDragGestures() onDragStart")
                                if (taskMode == TaskMode.Create || taskMode == TaskMode.EditTimeRange) {
                                    val clockTaskStartTime = calculateTimeFromAngle(
                                        angle = tmpStartAngle!!,
                                        clockStart = activeTimeStart,
                                        minuteAngle = minuteAngle
                                    )

//                                    setTaskStartTime(clockTaskStartTime)
                                    viewModel.onEvent(PlannerUiEvent.TaskStartTimeChanged(clockTaskStartTime))


                                    val clockTaskEndTime = calculateTimeFromAngle(
                                        angle = tmpEndAngle!!,
                                        clockStart = activeTimeStart,
                                        minuteAngle = minuteAngle
                                    )

//                                    setTaskEndTime(clockTaskEndTime)
                                    viewModel.onEvent(PlannerUiEvent.TaskEndTimeChanged(clockTaskEndTime))


                                    // Reset the angle mode
                                    angleMode = AngleMode.None
                                    // Set new startAngle and endAngle values
                                    startAngle = tmpStartAngle

//                                    setTaskDate(userInput.selectedDate)
                                    viewModel.onEvent(PlannerUiEvent.TaskDateChanged(selectedDate))
                                }
                                else if (taskMode == TaskMode.EditStartTime) {
                                    // Calculate the time represented by the angle
                                    if (jumpToStartAngle != null && jumpToEndAngle != null) {
                                        // The indices of the tasks between which the dragged task should be placed
                                        var taskBeforeIndex: Int? = null
                                        var taskAfterIndex: Int? = null

                                        var jumpToStartAngleTranslated: Float
                                        var jumpToEndAngleTranslated: Float

                                        // Check if the selected task would overlap another task if it was to jump to the previously determined position
                                        if (dragDirection == DragDirection.Forward) {
                                            // Find the last task the selected task overlaps with
                                            for ((index, task) in tasks.withIndex()) {
                                                if (task.id != touchedTask!!.id) {
                                                    val taskStartAngle = calculateAngleFromTime(
                                                        activeTimeStart = activeTimeStart,
//                                                        activeTimeEnd,
                                                        time = task.startTime!!,
                                                        minuteAngle = minuteAngle
                                                    )
                                                    val taskStartAngleTranslated =
                                                        translateAngle270To0(
                                                            taskStartAngle
                                                        )
                                                    val taskEndAngle = calculateAngleFromTime(
                                                        activeTimeStart = activeTimeStart,
//                                                        activeTimeEnd,
                                                        time = task.endTime!!,
                                                        minuteAngle = minuteAngle
                                                    )
                                                    val taskEndAngleTranslated =
                                                        translateAngle270To0(
                                                            taskEndAngle
                                                        )

                                                    jumpToStartAngleTranslated =
                                                        translateAngle270To0(
                                                            jumpToStartAngle!!
                                                        )
                                                    jumpToEndAngleTranslated = translateAngle270To0(
                                                        jumpToEndAngle!!
                                                    )
                                                    val jumpToMiddleAngleTranslated =
                                                        jumpToStartAngleTranslated + draggedTaskDuration * minuteAngle * 0.5f

                                                    // If the tasks overlap - if the dragged task would be within the neighboring task or the neighboring task is within the dragged task, we will want to move the dragged task so that it doesn't overlap another task
                                                    if (jumpToStartAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                        || jumpToEndAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                        || taskStartAngleTranslated in jumpToStartAngleTranslated..jumpToEndAngleTranslated
                                                        || taskEndAngleTranslated in jumpToStartAngleTranslated..jumpToEndAngleTranslated
                                                    ) {
                                                        // We need to nullify both the before and after tasks' indices because we might end up updating only one of them. We don't want any of them to hold a value from the previous looping iteration.
                                                        taskBeforeIndex = null
                                                        taskAfterIndex = null

                                                        if (jumpToMiddleAngleTranslated > taskStartAngleTranslated) {
                                                            taskBeforeIndex = index
                                                            if (taskBeforeIndex + 1 < tasks.size) {
                                                                taskAfterIndex = taskBeforeIndex + 1
                                                            }
                                                        } else {
                                                            taskAfterIndex = index
                                                            if (taskAfterIndex - 1 >= 0) {
                                                                taskBeforeIndex = taskAfterIndex - 1
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
                                                if (task.id != touchedTask!!.id) {
                                                    val taskStartAngle = calculateAngleFromTime(
                                                        activeTimeStart = activeTimeStart,
//                                                        activeTimeEnd,
                                                        time = task.startTime!!,
                                                        minuteAngle = minuteAngle
                                                    )
                                                    val taskStartAngleTranslated =
                                                        translateAngle270To0(
                                                            taskStartAngle
                                                        )
                                                    val taskEndAngle = calculateAngleFromTime(
                                                        activeTimeStart = activeTimeStart,
//                                                        activeTimeEnd,
                                                        time = task.endTime!!,
                                                        minuteAngle = minuteAngle
                                                    )
                                                    val taskEndAngleTranslated =
                                                        translateAngle270To0(
                                                            taskEndAngle
                                                        )

                                                    jumpToStartAngleTranslated =
                                                        translateAngle270To0(
                                                            jumpToStartAngle!!
                                                        )
                                                    jumpToEndAngleTranslated = translateAngle270To0(
                                                        jumpToEndAngle!!
                                                    )
                                                    val jumpToMiddleAngleTranslated =
                                                        jumpToStartAngleTranslated + draggedTaskDuration * 0.5f

                                                    // If the tasks overlap - if the dragged task would be within the neighboring task or the neighboring task is within the dragged task, we will want to move the dragged task so that it doesn't overlap another task
                                                    if (jumpToStartAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                        || jumpToEndAngleTranslated in taskStartAngleTranslated..taskEndAngleTranslated
                                                        || taskStartAngleTranslated in jumpToStartAngleTranslated..jumpToEndAngleTranslated
                                                        || taskEndAngleTranslated in jumpToStartAngleTranslated..jumpToEndAngleTranslated
                                                    ) {
                                                        if (jumpToMiddleAngleTranslated < taskEndAngleTranslated
                                                        ) {
                                                            taskAfterIndex = index
                                                            if (taskAfterIndex - 1 >= 0) {
                                                                taskBeforeIndex = taskAfterIndex - 1
                                                            }
                                                        }
                                                        // If the selected task crossed the boundaries of the task, but not too far
                                                        else {
                                                            taskBeforeIndex = index
                                                            if (taskBeforeIndex + 1 < tasks.size) {
                                                                taskAfterIndex = taskBeforeIndex + 1
                                                            }
                                                        }

                                                        // We're breaking the loop right after finding the first match
                                                        break
                                                    }
                                                }
                                            }
                                        }

                                        // Check if there's enough space between the tasks to fit in the dragged task
                                        if (taskBeforeIndex != null && taskAfterIndex != null) {
                                            val taskBefore = tasks[taskBeforeIndex]
                                            val taskAfter = tasks[taskAfterIndex]

                                            val taskBeforeEndAngle = calculateAngleFromTime(
                                                activeTimeStart = activeTimeStart,
//                                                activeTimeEnd = activeTimeEnd,
                                                time = taskBefore.endTime!!,
                                                minuteAngle = minuteAngle
                                            )
                                            val taskBeforeEndAngleTranslated = translateAngle270To0(
                                                taskBeforeEndAngle
                                            )
                                            val taskAfterStartAngle = calculateAngleFromTime(
                                                activeTimeStart = activeTimeStart,
//                                                activeTimeEnd = activeTimeEnd,
                                                time = taskAfter.startTime!!,
                                                minuteAngle = minuteAngle
                                            )
                                            val taskAfterStartAngleTranslated =
                                                translateAngle270To0(
                                                    taskAfterStartAngle
                                                )
                                            // Space (in minutes) between the tasks
                                            val capacity =
                                                (taskAfterStartAngleTranslated - taskBeforeEndAngleTranslated) / minuteAngle

                                            // The 2 minutes is to separate the dragged task from the neighboring tasks: 1 minute from each side
                                            if (capacity < draggedTaskDuration + 2) {
                                                // If there is not enough space between the tasks to fit in the dragged task, move backward the task before
                                                if (dragDirection == DragDirection.Forward) {
                                                    // If the jump-to-position overlaps the next task, correct the jump-to-position
                                                    jumpToEndAngleTranslated = translateAngle270To0(
                                                        jumpToEndAngle!!
                                                    )

                                                    if (jumpToEndAngleTranslated > taskAfterStartAngleTranslated) {
                                                        jumpToEndAngle =
                                                            taskAfterStartAngle - minuteAngle
                                                        jumpToStartAngle =
                                                            jumpToEndAngle!! - draggedTaskDuration * minuteAngle

                                                        if (jumpToEndAngle!! < 0f) jumpToEndAngle =
                                                            jumpToEndAngle!! + 360f
                                                        if (jumpToStartAngle!! < 0f) jumpToStartAngle =
                                                            jumpToStartAngle!! + 360f
                                                    }

                                                    jumpToStartAngleTranslated =
                                                        translateAngle270To0(
                                                            jumpToStartAngle!!
                                                        )

                                                    // Calculate by how much the dragged task should be moved
                                                    val angleChange =
                                                        taskBeforeEndAngleTranslated - jumpToStartAngleTranslated + minuteAngle

                                                    moveTaskBackwardCascade(
                                                        taskBeforeIndex,
                                                        angleChange
                                                    )
                                                }
                                                // If there is not enough space between the tasks to fit in the dragged task, move backward the task after
                                                else if (dragDirection == DragDirection.Backward) {
                                                    // If the jump-to-position overlaps the previous task, correct the jump-to-position
                                                    jumpToStartAngleTranslated =
                                                        translateAngle270To0(
                                                            jumpToStartAngle!!
                                                        )

                                                    if (jumpToStartAngleTranslated < taskBeforeEndAngleTranslated) {
                                                        jumpToStartAngle =
                                                            taskBeforeEndAngle + minuteAngle
                                                        jumpToEndAngle =
                                                            jumpToStartAngle!! + draggedTaskDuration * minuteAngle

                                                        if (jumpToStartAngle!! > 360f) jumpToStartAngle =
                                                            jumpToStartAngle!! - 360f
                                                        if (jumpToEndAngle!! > 360f) jumpToEndAngle =
                                                            jumpToEndAngle!! - 360f
                                                    }

                                                    jumpToEndAngleTranslated = translateAngle270To0(
                                                        jumpToEndAngle!!
                                                    )

                                                    val angleChange =
                                                        jumpToEndAngleTranslated - taskAfterStartAngleTranslated + minuteAngle

                                                    moveTaskForwardCascade(
                                                        taskAfterIndex,
                                                        -angleChange
                                                    )
                                                }
                                            }
                                        }
                                        // If there is only a task before, the selected task is probably between the task before and the end of the active time
                                        else if (taskBeforeIndex != null) {
                                            val taskBefore = tasks[taskBeforeIndex]
                                            val taskBeforeEndAngle = calculateAngleFromTime(
                                                activeTimeStart = activeTimeStart,
//                                                activeTimeEnd,
                                                time = taskBefore.endTime!!,
                                                minuteAngle = minuteAngle
                                            )
                                            val taskBeforeEndAngleTranslated = translateAngle270To0(
                                                taskBeforeEndAngle
                                            )
                                            // Space (in minutes) between the previous task and the end of the active time
                                            val capacity =
                                                (360f - taskBeforeEndAngleTranslated) / minuteAngle

                                            if (capacity < draggedTaskDuration + 1) {
                                                // If there is not enough space between the previous task and the end of the active time to fit in the dragged task, move backward the task before
                                                if (dragDirection == DragDirection.Forward) {
                                                    jumpToStartAngleTranslated =
                                                        translateAngle270To0(
                                                            jumpToStartAngle!!
                                                        )

                                                    // Calculate by how much the dragged task should be moved
                                                    val angleChange =
                                                        taskBeforeEndAngleTranslated - jumpToStartAngleTranslated + minuteAngle

                                                    moveTaskBackwardCascade(
                                                        taskBeforeIndex,
                                                        angleChange
                                                    )
                                                }
                                            }
                                        }
                                        // If there is only a task after, the selected task is probably between the start of the active time and the task after
                                        else if (taskAfterIndex != null) {
                                            val taskAfter = tasks[taskAfterIndex]
                                            val taskAfterStartAngle = calculateAngleFromTime(
                                                activeTimeStart = activeTimeStart,
//                                                activeTimeEnd,
                                                time = taskAfter.startTime!!,
                                                minuteAngle = minuteAngle
                                            )
                                            val taskAfterStartAngleTranslated =
                                                translateAngle270To0(
                                                    taskAfterStartAngle
                                                )
                                            // Space (in minutes) between the active time start and the next task
                                            val capacity =
                                                (taskAfterStartAngleTranslated - 0f) / minuteAngle

                                            if (capacity < draggedTaskDuration + 1) {
                                                // If there is not enough space between the active time start and the next task, move backward the task after
                                                if (dragDirection == DragDirection.Backward) {
                                                    jumpToEndAngleTranslated = translateAngle270To0(
                                                        jumpToEndAngle!!
                                                    )

                                                    val angleChange =
                                                        taskAfterStartAngleTranslated - jumpToEndAngleTranslated - minuteAngle

                                                    moveTaskForwardCascade(
                                                        taskAfterIndex,
                                                        angleChange
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // The tmpStartAngle value should be filled in detectDragGesturesAfterLongPress
                                    val taskNewStartTime = calculateTimeFromAngle(
                                        jumpToStartAngle ?: tmpStartAngle!!,
                                        minuteAngle,
                                        activeTimeStart
                                    )
                                    // The tmpEndAngle value should be filled in detectDragGesturesAfterLongPress
                                    val taskNewEndTime = calculateTimeFromAngle(
                                        jumpToEndAngle ?: tmpEndAngle!!,
                                        minuteAngle,
                                        activeTimeStart
                                    )

                                    tasks.find { task -> task.id == touchedTask!!.id }?.apply {
                                        startTime = taskNewStartTime
                                        endTime = taskNewEndTime
                                    }

                                    // Update the task's start- and end times in the database
                                    for (task in tasks) {
//                                        saveTask(task)
                                        viewModel.onEvent(PlannerUiEvent.SaveTask)
                                    }

                                    reset()
                                }
                            }
                        )
                    }
            ) {
                if (drawClockHand && checkIfTimeInRange(clockTime, activeTimeStart, activeTimeEnd)) {
                    drawClockHand(
                        activeTimeStart = activeTimeStart,
                        clockTime = clockTime,
                        minuteAngle = minuteAngle,
                        startRadius = outerRadius - clockHandPadding,
                        endRadius = centerRadius
                    )
                }

                val minutesBetweenHoursAccumulated: Array<Int> =
                    calculateMinutesBetweenHoursAccumulated(
                        activeTimeStart,
                        activeTimeEnd
                    )
                val activeTimeHourSteps: Array<LocalTime> = createClockHoursArray(
                    activeTimeStart,
                    activeTimeEnd
                )

                if (drawNewTaskTimeRange) {
                    tmpStartAngle?.let {
                        tmpEndAngle?.let {
                            drawTask(
                                taskEndAngle = tmpEndAngle!!,
                                taskStartAngle = tmpStartAngle!!,
                                outerRadius = outerRadius - taskPadding,
                                sweepAngle = sweepAngle(
                                    tmpStartAngle!!,
                                    tmpEndAngle!!
                                ),
                                alpha = 0.3f,
                                borderWidth = 0f
                            )
                        }
                    }
                }

                for (task in tasks) {
                    // If the task is being edited, draw it with lighter shade and use the angles on the dial
                    if (task.id == touchedTask?.id && (taskMode == TaskMode.EditTimeRange || taskMode == TaskMode.EditStartTime)) {
                       tmpStartAngle?.let {
                           tmpEndAngle?.let {
                               val clockTaskStartTime = calculateTimeFromAngle(
                                   angle = tmpStartAngle!!,
                                   clockStart = activeTimeStart,
                                   minuteAngle = minuteAngle
                               )
                               val clockTaskEndTime = calculateTimeFromAngle(
                                   angle = tmpEndAngle!!,
                                   clockStart = activeTimeStart,
                                   minuteAngle = minuteAngle
                               )

                               drawTask(
                                   taskEndAngle = tmpEndAngle!!,
                                   taskStartAngle = tmpStartAngle!!,
                                   minuteAngle = minuteAngle,
                                   innerRadius = centerRadius,
                                   outerRadius = outerRadius - taskPadding,
                                   taskDurationInMinutes = calculateTotalNumberOfMinutes(
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
                       }
                    }
                    // Otherwise, draw the task normally
                    else {
                        // We have to offset these angles because startMinute * taskDialState.minuteAngle returns a biased angle
                        val taskStartAngle = calculateAngleFromTime (
                            activeTimeStart = activeTimeStart,
//                            activeTimeEnd,
                            time = task.startTime!!,
                            minuteAngle = minuteAngle
                        )
                        val taskEndAngle = calculateAngleFromTime (
                            activeTimeStart = activeTimeStart,
//                            activeTimeEnd,
                            time = task.endTime!!,
                            minuteAngle = minuteAngle
                        )
                        val taskDuration = calculateTotalNumberOfMinutes(
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
    }

    if (showPopupWindow) {
        PopupDialog(
            onDismissRequest = {
                showPopupWindow = false
                popupState = TaskModePopup.Info
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
//                            deleteTask()
                            viewModel.onEvent(PlannerUiEvent.DeleteTask)
                            reset()
                            showPopupWindow = false
                        }
                    )
                }
                TaskModePopup.Edit -> {
                    TaskEditScreen(
//                        dayUiState = dayUiState,
//                        lastTaskPriority = lastTaskPriority ?: 0,
//                        taskUiState = taskUiState,
                        viewModel = viewModel,
                        onBack = {
                            showPopupWindow = false
                            popupState = TaskModePopup.Info
                            // Reset the dial
                            reset()
                        },
//                        saveTask = saveTaskFromState,
//                        setTaskEndTime = setTaskEndTime,
//                        setTaskDescription = setTaskDescription,
//                        setTaskPriority = setTaskPriority,
//                        setTaskStartTime = setTaskStartTime,
//                        setTaskTitle = setTaskTitle
                    )
                }
                TaskModePopup.Info -> {
                    TaskInfoScreen(
                        displayType = CardDisplayType.Popup,
//                        dayUiState = dayUiState,
//                        taskUiState = taskUiState,
                        viewModel = viewModel,
                        onDeleteTask = {
                            popupState = TaskModePopup.Delete
                        },
                        onBack = {
                            popupState = TaskModePopup.Info
                            showPopupWindow = false
                            // Reset the dial
                            reset()
                        },
                        onMoveToCalendar = {//FIXME: What is this doing here?
//                    val task = toDoTasks.find { task -> task.id == taskUiState.id }
                        },
//                        onMoveToToDoList = onMoveToToDoList,
                        onNavigateToTaskEdit = {
                            popupState = TaskModePopup.Edit
                        },
//                        onPinTask = onPinTask
                    )
                }
            }
        }
    }
}