package com.eternalfairy.lotus.view.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.lotus.R
import com.eternalfairy.lotus.view.component.CardDisplayType
import com.eternalfairy.lotus.view.component.DropDownItem
import com.eternalfairy.lotus.view.component.InfoCard
import com.eternalfairy.lotus.view.component.TaskDropdownMenu
import com.eternalfairy.lotus.view.screen.planner.PlannerUiEvent
import com.eternalfairy.lotus.view.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.lotus.view.theme.HEADER_TEXT_COLOR
import com.eternalfairy.lotus.viewmodel.PlannerViewModel

@Composable
fun TaskInfoScreen(
    modifier: Modifier = Modifier,
    displayType: CardDisplayType,
    viewModel: PlannerViewModel,
//    dayUiState: DayUiState,
//    taskUiState: TaskUiState,
    onDeleteTask: () -> Unit,
    onBack: () -> Unit,
    onMoveToCalendar: () -> Unit,
//    onMoveToToDoList: () -> Unit,
    onNavigateToTaskEdit: () -> Unit,
//    onPinTask: (Boolean) -> Unit
) {
    val state = viewModel.state
    val lastTaskPriority = state.lastTaskPriority
    val taskUiState = state.editedTask

    Scaffold (
        bottomBar = {
            BottomAppBar (
                containerColor = COMPONENT_BACKGROUND_COLOR,
                actions = {
                    // Close button
                    IconButton(onClick = {
                        // Navigate to previous stack entry
                        onBack()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                            contentDescription = "Cancel",
                            modifier = Modifier.fillMaxSize(0.8F),
                            tint = HEADER_TEXT_COLOR
                        )
                    }

                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))

                    val dropdownItems = listOf<DropDownItem>(
                        DropDownItem(
                            text = "Edit",
                            iconId = R.drawable.edit_24dp_5f6368_fill0_wght400_grad0_opsz24,
                            onClick = {
                                onNavigateToTaskEdit()
                            }
                        ),
                        DropDownItem(
                            text = "Delete",
                            iconId = R.drawable.delete_24dp_5f6368_fill0_wght400_grad0_opsz24,
                            onClick = {
                                onDeleteTask()
                            }
                        ),
                        DropDownItem(
                            text = if (taskUiState.date == null) "Move to calendar" else "Move to \"To Do\"",
                            iconId = if (taskUiState.date == null) R.drawable.calendar_month_24dp_5f6368_fill0_wght400_grad0_opsz24 else R.drawable.list_svgrepo_com,
                            onClick = {
                                if (taskUiState.date == null) {
                                    onMoveToCalendar()
                                } else {
//                                    onMoveToToDoList()
                                    viewModel.onEvent(PlannerUiEvent.TaskDateChanged(null))
                                    viewModel.onEvent(PlannerUiEvent.TaskStartTimeChanged(null))
                                    viewModel.onEvent(PlannerUiEvent.TaskEndTimeChanged(null))
                                    viewModel.onEvent(PlannerUiEvent.TaskPriorityChanged(lastTaskPriority + 1))
                                    viewModel.onEvent(PlannerUiEvent.SaveTask)
                                }
                                onBack()
                            }
                        ),
                        DropDownItem(
                            text = if (!taskUiState.pinned) "Pin task" else "Unpin task",
                            iconId = R.drawable.pin_list_svgrepo_com,
//                            iconId = R.drawable.pin_svgrepo_com,
                            onClick = {
                                Log.i("TaskInfoScreen", "taskUiState $taskUiState")
                                Log.i("TaskInfoScreen", "!taskUiState.pinned ${!taskUiState.pinned}")
//                                onPinTask(!taskUiState.pinned)
                                viewModel.onEvent(PlannerUiEvent.TaskPinnedChanged(!taskUiState.pinned))
                                Log.i("TaskInfoScreen", "taskUiState $taskUiState")
                                onBack()
                            },
                            size = 32.dp
                        )
                    )
                    TaskDropdownMenu(
                        dropdownItems = dropdownItems,
                        modifier = Modifier.fillMaxSize(0.7F)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .background(COMPONENT_BACKGROUND_COLOR)
                .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
        ) {
            InfoCard (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                viewModel = viewModel,
//                date = taskUiState.date,
//                dayUiState = dayUiState,
                displayType = displayType,
//                endTime = taskUiState.endTime,
//                startTime = taskUiState.startTime,
//                pinned = taskUiState.pinned,
//                title = taskUiState.title
            ) {
                Row (
                    modifier = modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Description
                    Text(
                        text = taskUiState.description ?: "",
                        modifier = modifier.verticalScroll(rememberScrollState()),
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}