package com.example.circularplanner.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.R
import com.example.circularplanner.ui.component.TaskCardDisplayType
import com.example.circularplanner.ui.component.DropDownItem
import com.example.circularplanner.ui.component.TaskCard
import com.example.circularplanner.ui.component.TaskDropdownMenu
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.TaskUiState

@Composable
fun TaskInfoScreen(
    modifier: Modifier = Modifier,
    displayType: TaskCardDisplayType,
    dayState: DayState,
    taskUiState: TaskUiState,
    onDeleteTask: () -> Unit,
    onBack: () -> Unit,
    onMoveToCalendar: () -> Unit,
    onMoveToToDoList: () -> Unit,
    onNavigateToTaskEdit: () -> Unit
) {
    val taskDetails = taskUiState

    Scaffold (
        bottomBar = {
            BottomAppBar (
                containerColor = Color(BOTTOM_BAR_COLOR),
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
                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
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
                            text = if (taskDetails.date == null) "Move to calendar" else "Move to TO-DO list",
                            iconId = if (taskDetails.date == null) R.drawable.calendar_month_24dp_5f6368_fill0_wght400_grad0_opsz24 else R.drawable.list_svgrepo_com,
                            onClick = {
                                if (taskDetails.date == null) onMoveToCalendar() else onMoveToToDoList()
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
        ) {
            TaskCard (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                date = taskDetails.date,
                dayState = dayState,
                displayType = displayType,
                endTime = taskDetails.endTime,
                startTime = taskDetails.startTime,
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