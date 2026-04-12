package com.eternalfairy.timeaware.ui.component

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.eternalfairy.timeaware.ui.data.GoalUiState
import com.eternalfairy.timeaware.ui.screen.DeleteScreen
import com.eternalfairy.timeaware.ui.screen.DeleteType
import com.eternalfairy.timeaware.ui.screen.GoalEditScreen
import com.eternalfairy.timeaware.ui.screen.GoalInfoScreen
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.utils.GoalModePopup
import kotlinx.coroutines.channels.Channel
import java.util.UUID

@Composable
fun DragItemListGoal(//TODO: Merge with DragItemListTask
//fun <T> DragItemList(
    componentHeight: Dp = 680.dp,
    componentWidth: Dp = 400.dp,
    goalUiState: GoalUiState,
//    items: List<Draggable>,
//    items: List<T>,
    items: List<GoalUiState>,
    lastGoalPriority: Int?,
    deleteGoal: (UUID) -> Unit,
    saveGoal: (GoalUiState) -> Unit,
    saveGoalFromState: () -> Unit,
    selectGoal: (UUID?) -> Unit,
    setGoalPriority: (Int) -> Unit,
    setGoalTitle: (String) -> Unit
//    listItem: @Composable LazyItemScope.(Modifier, Draggable) -> Unit
) {
    var draggedItem: LazyListItemInfo? by remember { mutableStateOf(null) }
    var draggedItemIndex: Int? by remember { mutableStateOf(null) }
    var delta by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()
    val scrollChannel = Channel<Float>()

    val itemsCopy = items.toMutableList()

    var showPopupWindow by remember { mutableStateOf(false) }
    var goalState by remember { mutableStateOf(GoalModePopup.Info) }


    fun onSwap(fromIndex: Int, toIndex: Int) {
        itemsCopy.apply { add(toIndex, removeAt(fromIndex)) }
    }

    LaunchedEffect(listState) {
        while (true) {
            val value = scrollChannel.receive()
            listState.scrollBy(value)
        }
    }

    LazyColumn(
        modifier = Modifier
            .height(componentHeight)
            .width(componentWidth)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
            .background(COMPONENT_BACKGROUND_COLOR)
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
                            saveGoal(item.copy(priority = index + 1))
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
            contentType = { index, item -> Draggable(index = index, id = item.id!!) }
        ) { index, item ->
            val modifier = if (draggedItemIndex == index) {
                Modifier
                    .zIndex(1f)
                    .graphicsLayer(
                        translationY = delta
                    )
            } else {
                Modifier
            }

            ListItemGoal(
                modifier = modifier
                    .animateItem(
                        placementSpec = tween(
                            durationMillis = 600
                        )
                    )
                ,
                goal = item,
                onNavigateToGoalInfo = { showPopupWindow = true },
                selectGoal = selectGoal
            )
        }
    }

    if (showPopupWindow) {
        PopupDialog(
            onDismissRequest = {
                goalState = GoalModePopup.Info
                showPopupWindow = false
            }
        ) {
            when (goalState) {
                GoalModePopup.Edit -> {
                    GoalEditScreen(
                        goalUiState = goalUiState,
                        lastPriority = lastGoalPriority ?: 0,
                        onBack = {
                            goalState = GoalModePopup.Info
                        },
                        saveGoal = saveGoalFromState,
                        setGoalPriority = setGoalPriority,
                        setGoalTitle = setGoalTitle
                    )
                }
                GoalModePopup.Info -> {
                    GoalInfoScreen (
                        goalUiState = goalUiState,
                        onDeleteGoal = {
                            goalState = GoalModePopup.Delete
                        },
                        onBack = {
                            showPopupWindow = false
                        },
                        onNavigateToGoalEdit = {
                            goalState = GoalModePopup.Edit
                        }
                    )
                }

                GoalModePopup.Delete -> {
                    DeleteScreen(
                        onBack = {
                            goalState = GoalModePopup.Info
                            showPopupWindow = false
                        },
                        onDelete = {
                            deleteGoal(goalUiState.id!!)
                            showPopupWindow = false
                        },
                        deleteType= DeleteType.Goal
                    )
                }
            }
        }
    }
}

open class Draggable(val index: Int, val id: UUID)
