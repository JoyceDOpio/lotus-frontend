package com.example.circularplanner.ui.component

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
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
import com.example.circularplanner.data.Goal
import kotlinx.coroutines.channels.Channel
import java.util.UUID

@Composable
fun DragItemList(
//fun <T> DragItemList(
//    items: List<Draggable>,
//    items: List<T>,
    items: List<Goal>,
    onNavigateToGoalEdit: () -> Unit,
    onSwitch: (UUID, UUID) -> Unit,
    deleteGoal: (Goal) -> Unit,
    selectGoal: (UUID?) -> Unit
//    listItem: @Composable LazyItemScope.(Modifier, Draggable) -> Unit
) {
    var draggedItem: LazyListItemInfo? = null
    var draggedItemIndex: Int? = null
    var delta by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()
    val scrollChannel = Channel<Float>()

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
                                draggedItemIndex = draggableItem.index
                                draggedItem = it
                            } }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        delta += dragAmount.y

                        val currentlyDraggedItem = draggedItem ?: return@detectDragGesturesAfterLongPress
                        val currentlyDraggedItemIndex = draggedItemIndex ?: return@detectDragGesturesAfterLongPress

                        // Swap places if the middle of the dragged item reaches the border of the another item
                        val startOffset = currentlyDraggedItem.offset + delta
                        val endOffset = currentlyDraggedItem.offset + currentlyDraggedItem.size + delta
                        val middleOffset = startOffset + (endOffset - startOffset) / 2
                        val targetItem = listState.layoutInfo.visibleItemsInfo.find { item -> middleOffset.toInt() in item.offset..item.offset + item.size && currentlyDraggedItemIndex != item.index && item.contentType is Draggable }

                        if (targetItem != null) {
                            val draggedItemId = (currentlyDraggedItem.contentType as Draggable).id
                            val targetItemId = (targetItem.contentType as Draggable).id

                            onSwitch(draggedItemId, targetItemId)

                            val targetIndex = (targetItem.contentType as Draggable).index
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
                            val canScrollDown = currentlyDraggedItemIndex != items.size - 1 && endOffsetToBottom > 0
                            val canScrollUp = currentlyDraggedItemIndex != 0 && startOffsetToTop < 0
                            if (scroll != 0f && (canScrollUp || canScrollDown)) {
                                scrollChannel.trySend(scroll)
                            }
                        }
                    },
                    onDragEnd = {
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
            items = items,
//            contentType = { index, item -> Draggable(index = index, id = item.id) }
            contentType = { index, item -> Draggable(index = index, id = item.id) }
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

//            listItem(modifier, item)
            GoalListItem(
                modifier,
                item,
                onNavigateToGoalEdit = onNavigateToGoalEdit,
                deleteGoal = deleteGoal,
                selectGoal = selectGoal
            )
        }
    }
}

open class Draggable(val index: Int, val id: UUID)
