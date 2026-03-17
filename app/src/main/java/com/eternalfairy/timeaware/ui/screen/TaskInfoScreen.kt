package com.eternalfairy.timeaware.ui.screen

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
import com.eternalfairy.timeaware.R
import com.eternalfairy.timeaware.ui.component.CardDisplayType
import com.eternalfairy.timeaware.ui.component.DropDownItem
import com.eternalfairy.timeaware.ui.component.TaskCard
import com.eternalfairy.timeaware.ui.component.TaskDropdownMenu
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.viewmodel.room.DayState
import com.eternalfairy.timeaware.ui.viewmodel.room.TaskUiState

@Composable
fun TaskInfoScreen(
    modifier: Modifier = Modifier,
    displayType: CardDisplayType,
    dayState: DayState,
    taskUiState: TaskUiState,
    onDeleteTask: () -> Unit,
    onBack: () -> Unit,
    onMoveToCalendar: () -> Unit,
    onMoveToToDoList: () -> Unit,
    onNavigateToTaskEdit: () -> Unit,
    onPinTask: (Boolean) -> Unit
) {
    val taskDetails = taskUiState

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
                            text = if (taskDetails.date == null) "Move to calendar" else "Move to \"To Do\"",
                            iconId = if (taskDetails.date == null) R.drawable.calendar_month_24dp_5f6368_fill0_wght400_grad0_opsz24 else R.drawable.list_svgrepo_com,
                            onClick = {
                                if (taskDetails.date == null) onMoveToCalendar() else onMoveToToDoList()
                                onBack()
                            }
                        ),
                        DropDownItem(
                            text = if (!taskDetails.pinned) "Pin task" else "Unpin task",
//                            iconId = R.drawable.pin_list_svgrepo_com,
//                            iconId = R.drawable.pin_svgrepo_com,
                            iconId = R.drawable.list_svgrepo_com,
                            onClick = {
                                onPinTask(!taskDetails.pinned)
                                onBack()
                            }
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
            TaskCard (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                date = taskDetails.date,
                dayState = dayState,
                displayType = displayType,
                endTime = taskDetails.endTime,
                startTime = taskDetails.startTime,
                pinned = taskDetails.pinned,
                title = taskDetails.title
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
                        text = taskDetails.description,
                        modifier = modifier.verticalScroll(rememberScrollState()),
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}