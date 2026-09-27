package com.eternalfairy.lotus.presentation.component

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
import androidx.compose.runtime.mutableFloatStateOf
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
import com.eternalfairy.lotus.presentation.screen.DeleteScreen
import com.eternalfairy.lotus.presentation.screen.DeleteType
import com.eternalfairy.lotus.presentation.screen.GoalEditScreen
import com.eternalfairy.lotus.presentation.screen.GoalInfoScreen
import com.eternalfairy.lotus.presentation.screen.planner.PlannerUiEvent
import com.eternalfairy.lotus.presentation.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.lotus.presentation.utils.GoalModePopup
import com.eternalfairy.lotus.presentation.viewmodel.PlannerViewModel
import kotlinx.coroutines.channels.Channel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun DragItemListGoal(//TODO: Merge with DragItemListTask
//fun <T> DragItemList(
    componentHeight: Dp = 680.dp,
    componentWidth: Dp = 400.dp,
    viewModel: PlannerViewModel
) {
    val state = viewModel.state
    val goalUiState = state.selectedGoal
    val items = state.goals
    val lastGoalPriority = state.lastGoalPriority

    var draggedItem: LazyListItemInfo? by remember { mutableStateOf(null) }
    var draggedItemIndex: Int? by remember { mutableStateOf(null) }
    var delta by remember { mutableFloatStateOf(0f) }
    val listState = rememberLazyListState()
    val scrollChannel = remember { Channel<Float>() }

    var showPopupWindow by remember { mutableStateOf(false) }
    var goalState by remember { mutableStateOf(GoalModePopup.Info) }

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
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        listState.layoutInfo.visibleItemsInfo
                            .firstOrNull() { item -> offset.y.toInt() in item.offset..(item.offset + item.size) }
                            ?.also {
                                (it.contentType as? Draggable)?.let { draggableItem ->
                                    draggedItem = it
                                    draggedItemIndex = draggableItem.index
                                }
                            }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        delta += dragAmount.y

                        val currentlyDraggedItemIndex =
                            draggedItemIndex ?: return@detectDragGesturesAfterLongPress
                        val currentlyDraggedItem =
                            draggedItem ?: return@detectDragGesturesAfterLongPress

                        // Swap places if the middle of the dragged item reaches the border of the another item
                        val topOffset = currentlyDraggedItem.offset + delta
                        val bottomOffset =
                            currentlyDraggedItem.offset + currentlyDraggedItem.size + delta
                        val middleOffset = topOffset + (bottomOffset - topOffset) / 2
                        val targetItem =
                            listState.layoutInfo.visibleItemsInfo.find { item -> middleOffset.toInt() in item.offset..item.offset + item.size && currentlyDraggedItemIndex != item.index && item.contentType is Draggable }

                        if (targetItem != null) {
                            val targetIndex = (targetItem.contentType as Draggable).index

                            viewModel.onEvent(PlannerUiEvent.SwapGoals(currentlyDraggedItemIndex, targetIndex))

                            draggedItem = targetItem
                            draggedItemIndex = targetIndex
                            delta += currentlyDraggedItem.offset - targetItem.offset
                        } else {
                            val startOffsetToTop =
                                topOffset - listState.layoutInfo.viewportStartOffset
                            val endOffsetToBottom =
                                topOffset - listState.layoutInfo.viewportEndOffset
                            val scroll =
                                when {
                                    startOffsetToTop < 0 -> startOffsetToTop.coerceAtMost(0f)
                                    endOffsetToBottom > 0 -> endOffsetToBottom.coerceAtLeast(0f)
                                    else -> 0f
                                }
                            val canScrollDown =
                                currentlyDraggedItemIndex != items.size - 1 && endOffsetToBottom > 0
                            val canScrollUp = currentlyDraggedItemIndex != 0 && startOffsetToTop < 0
                            if (scroll != 0f && (canScrollUp || canScrollDown)) {
                                scrollChannel.trySend(scroll)
                            }
                        }
                    },
                    onDragEnd = {
                        viewModel.onEvent(PlannerUiEvent.UpdateGoals)

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
                selectGoal = { viewModel.onEvent(PlannerUiEvent.SelectedGoalIdChanged(item.id)) }
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
                        viewModel = viewModel,
                        onBack = {
                            goalState = GoalModePopup.Info
                        }
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
                            viewModel.onEvent(PlannerUiEvent.DeleteGoal)

                            showPopupWindow = false
                        },
                        deleteType= DeleteType.Goal
                    )
                }
            }
        }
    }
}

open class Draggable @OptIn(ExperimentalUuidApi::class) constructor(val index: Int, val id: Uuid)
