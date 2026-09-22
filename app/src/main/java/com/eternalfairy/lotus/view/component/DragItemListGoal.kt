package com.eternalfairy.lotus.view.component

import android.util.Log
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
import com.eternalfairy.lotus.data.model.Goal
import com.eternalfairy.lotus.view.data.GoalUiState
import com.eternalfairy.lotus.view.screen.DeleteScreen
import com.eternalfairy.lotus.view.screen.DeleteType
import com.eternalfairy.lotus.view.screen.GoalEditScreen
import com.eternalfairy.lotus.view.screen.GoalInfoScreen
import com.eternalfairy.lotus.view.screen.planner.PlannerUiEvent
import com.eternalfairy.lotus.view.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.lotus.view.utils.GoalModePopup
import com.eternalfairy.lotus.view.viewmodel.PlannerViewModel
import kotlinx.coroutines.channels.Channel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun DragItemListGoal(//TODO: Merge with DragItemListTask
//fun <T> DragItemList(
    componentHeight: Dp = 680.dp,
    componentWidth: Dp = 400.dp,
    viewModel: PlannerViewModel,
    items: List<GoalUiState> = listOf(
        GoalUiState(
            id = Uuid.Companion.parse("495502d1-4f23-4c31-8edb-4ec1107f35be"),
            title = "goal 1",
            priority = 1
        ),
        GoalUiState(
            id = Uuid.Companion.parse("5f6c53ca-b5d7-4aec-89af-4f725bc8c50f"),
            title = "goal 2",
            priority = 2
        ),
        GoalUiState(
            id = Uuid.Companion.parse("98e36da8-521f-4e14-a370-4740aa4498ff"),
            title = "goal 3",
            priority = 3
        )
    )
) {
    val state = viewModel.state
    val goalUiState = state.selectedGoal
    val items = state.goals
    val lastGoalPriority = state.lastGoalPriority

    var draggedItem: LazyListItemInfo? by remember { mutableStateOf(null) }
    var draggedItemIndex: Int? by remember { mutableStateOf(null) }
    var delta by remember { mutableFloatStateOf(0f) }
    val listState = rememberLazyListState()
//    val scrollChannel = Channel<Float>()
    val scrollChannel = remember { Channel<Float>() }

//    val itemsCopy = items.toMutableList()
//    val itemsCopy = remember {
//        mutableListOf(
//            GoalUiState(
//                id = Uuid.Companion.parse("495502d1-4f23-4c31-8edb-4ec1107f35be"),
//                title = "predefined goal",
//                priority = 1
//            ),
//            GoalUiState(
//                id = Uuid.Companion.parse("5f6c53ca-b5d7-4aec-89af-4f725bc8c50f"),
//                title = "predefined goal 2",
//                priority = 2
//            ),
//            GoalUiState(
//                id = Uuid.Companion.parse("5f6c53ca-b5d7-4aec-89af-4f725bc8c50f"),
//                title = "predefined goal 3",
//                priority = 3
//            )
//        )
//    }
    val itemsCopy = remember { items.toMutableList() }

    Log.i("DragItemListGoal", "items")
    items.forEach { Log.i("DragItemListGoal", "$it") }
    Log.i("DragItemListGoal", "itemsCopy")
    itemsCopy.forEach { Log.i("DragItemListGoal", "$it") }

    var showPopupWindow by remember { mutableStateOf(false) }
    var goalState by remember { mutableStateOf(GoalModePopup.Info) }

    fun onSwap(fromIndex: Int, toIndex: Int) {
        itemsCopy.apply { add(toIndex, removeAt(fromIndex)) }
//        items.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    LaunchedEffect(listState) {
        while (true) {
            val value = scrollChannel.receive()
            listState.scrollBy(value)
        }
    }

//    LaunchedEffect(items) {
//        itemsCopy = items.toMutableList()
//    }

    LazyColumn(
        modifier = Modifier
            .height(componentHeight)
            .width(componentWidth)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
            .background(COMPONENT_BACKGROUND_COLOR)
//            .pointerInput(key1 = listState) {
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
                            onSwap(currentlyDraggedItemIndex, targetIndex)
                            draggedItem = targetItem
                            draggedItemIndex = targetIndex
                            delta += currentlyDraggedItem.offset - targetItem.offset
//                            delta = 0f
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
                                currentlyDraggedItemIndex != itemsCopy.size - 1 && endOffsetToBottom > 0
                            val canScrollUp = currentlyDraggedItemIndex != 0 && startOffsetToTop < 0
                            if (scroll != 0f && (canScrollUp || canScrollDown)) {
                                scrollChannel.trySend(scroll)
                            }
                        }
                    },
                    onDragEnd = {
                        itemsCopy.forEachIndexed { index, item ->
//                            saveGoal(item.copy(priority = index + 1))
                            viewModel.selectGoal(item.id)
                            viewModel.onEvent(PlannerUiEvent.GoalPriorityChanged(index + 1))
                            viewModel.onEvent(PlannerUiEvent.SaveGoal)
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
//            items = items,
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
//                selectGoal = selectGoal
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
//                        goalUiState = goalUiState,
//                        lastPriority = lastGoalPriority ?: 0,
                        viewModel = viewModel,
                        onBack = {
                            goalState = GoalModePopup.Info
                        },
//                        saveGoal = saveGoalFromState,
//                        setGoalPriority = setGoalPriority,
//                        setGoalTitle = setGoalTitle
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
//                            deleteGoal(goalUiState.id!!)
//                            viewModel.onEvent(PlannerUiEvent.GoalToBeDeletedIdChanged(goalUiState.id!!))
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
