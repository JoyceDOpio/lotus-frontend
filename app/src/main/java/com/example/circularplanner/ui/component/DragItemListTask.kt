package com.example.circularplanner.ui.component

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.screen.TaskEditScreen
import com.example.circularplanner.ui.screen.TaskInfoScreen
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.utils.TaskModePopup
import kotlinx.coroutines.channels.Channel
import java.util.UUID

@Composable
fun DragItemListTask(//TODO: Merge with DragItemListGoal
    dayState: DayState,
    items: List<Task>,
    lastTaskPriority: Int?,
    taskUiState: TaskUiState,
    deleteTask: () -> Unit,
    onMoveToToDoList: () -> Unit,
    saveTask: (Task) -> Unit,
    saveTaskFromState: () -> Unit,
    selectTask: (UUID?) -> Unit,
    setTaskDescription: (String) -> Unit,
    setTaskEndTime: (Time) -> Unit,
    setTaskPriority: (Int) -> Unit,
    setTaskStartTime: (Time) -> Unit,
    setTaskTitle: (String) -> Unit
) {
    var draggedItem: LazyListItemInfo? by remember { mutableStateOf(null) }
    var draggedItemIndex: Int? by remember { mutableStateOf(null) }
    var delta by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()
    val scrollChannel = Channel<Float>()

    val itemsCopy = items.toMutableList()

    fun onSwap(fromIndex: Int, toIndex: Int) {
        itemsCopy.apply { add(toIndex, removeAt(fromIndex)) }
    }

    var showPopupWindow by remember { mutableStateOf(false) }
    var taskState by remember { mutableStateOf(TaskModePopup.Info) }

    LaunchedEffect(listState) {
        while (true) {
            val value = scrollChannel.receive()
            listState.scrollBy(value)
        }
    }

    LazyColumn(
        modifier = Modifier
            .pointerInput(key1 = listState) {
                detectDragGesturesAfterLongPress (
                    onDragStart = { offset ->
                        listState.layoutInfo.visibleItemsInfo.firstOrNull() { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
                            ?.also { (it.contentType as? Draggable)?.let { draggableItem ->
                                draggedItem = it
                                draggedItemIndex = draggableItem.index
                            } }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        delta += dragAmount.y

                        val currentlyDraggedItemIndex = draggedItemIndex ?: return@detectDragGesturesAfterLongPress
                        val currentlyDraggedItem = draggedItem ?: return@detectDragGesturesAfterLongPress

                        // Swap places if the middle of the dragged item reaches the border of the another item
                        val startOffset = currentlyDraggedItem.offset + delta
                        val endOffset = currentlyDraggedItem.offset + currentlyDraggedItem.size + delta
                        val middleOffset = startOffset + (endOffset - startOffset) / 2
                        val targetItem = listState.layoutInfo.visibleItemsInfo.find { item -> middleOffset.toInt() in item.offset..item.offset + item.size && currentlyDraggedItemIndex != item.index && item.contentType is Draggable }

                        if (targetItem != null) {
                            val targetIndex = (targetItem.contentType as Draggable).index

                            onSwap(currentlyDraggedItemIndex, targetIndex)

                            draggedItem = targetItem
                            draggedItemIndex = targetIndex
                            delta += currentlyDraggedItem.offset - targetItem.offset
                        } else {
                            val startOffsetToTop = startOffset - listState.layoutInfo.viewportStartOffset
                            val endOffsetToBottom = startOffset - listState.layoutInfo.viewportEndOffset
                            val scroll =
                                when {
                                    startOffsetToTop < 0 -> startOffsetToTop.coerceAtMost(0f)
                                    endOffsetToBottom > 0 -> endOffsetToBottom.coerceAtLeast(0f)
                                    else -> 0f
                                }
                            val canScrollDown = currentlyDraggedItemIndex != itemsCopy.size - 1 && endOffsetToBottom > 0
                            val canScrollUp = currentlyDraggedItemIndex != 0 && startOffsetToTop < 0
                            if (scroll != 0f && (canScrollUp || canScrollDown)) {
                                scrollChannel.trySend(scroll)
                            }
                        }
                    },
                    onDragEnd = {
                        itemsCopy.forEachIndexed { index, item ->
                            saveTask(item.copy(priority = index + 1))
                        }

                        draggedItemIndex = null
                        draggedItem = null
                        delta = 0f
                    },
                    onDragCancel = {
                        draggedItemIndex = null
                        draggedItem = null
                        delta = 0f
                    }
                )
            },
        state = listState
    ) {
        itemsIndexed(
            items = itemsCopy,
//            contentType = { index, item -> Draggable(index = index, id = item.id) }
            contentType = { index, item -> Draggable(index = index, id = item.id) }
        ) { index, item ->
            Log.i("draggedItemIndex", draggedItemIndex.toString())
            Log.i("index", index.toString())
            val modifier = if (draggedItemIndex == index) {
                Modifier
                    .zIndex(1f)
                    .graphicsLayer(
                        translationY = delta
                    )
            } else {
                Modifier
            }

//            listItem(modifier, item)
            ListItemTask(
                modifier,
                item,
//                onNavigateToTaskInfo = onNavigateToTaskInfo,
                onNavigateToTaskInfo = { showPopupWindow = true },
                selectTask = selectTask
            )
        }
    }

    if (showPopupWindow) {
        PopupDialog(
            onDismissRequest = {
                showPopupWindow = false
            }
        ) {
            when (taskState) {
                TaskModePopup.Edit -> {
                    TaskEditScreen(
                        dayState = dayState,
                        lastTaskPriority = lastTaskPriority ?: 0,
                        taskUiState = taskUiState,
                        onBack = {
                            taskState = TaskModePopup.Info
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
                        taskUiState = taskUiState,
                        deleteTask = deleteTask,
                        onBack = {
                            showPopupWindow = false
                        },
                        onNavigateToMoveToCalendar = {
//                    val task = toDoTasks.find { task -> task.id == taskUiState.id }
                        },
                        onMoveToToDoList = onMoveToToDoList,
                        onNavigateToTaskEdit = {
                            taskState = TaskModePopup.Edit
                        },
                    )
                }
            }
        }
    }
}