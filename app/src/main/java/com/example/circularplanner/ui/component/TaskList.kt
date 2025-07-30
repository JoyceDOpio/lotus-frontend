package com.example.circularplanner.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.circularplanner.data.Task
import com.example.circularplanner.ui.viewmodel.DayState
import java.util.UUID

@Composable
fun TaskList(
    dayState: DayState,
    onNavigateToTaskInfo: () -> Unit,
    selectTask: (UUID?) -> Unit,
    removeTask: (Task) -> Unit
) {
    // Sorts tasks according to their start time
    val TaskSortingComparator = Comparator <Task> { first, second ->
        if (first.startTime.hour < second.startTime.hour) {
            -1
        } else if (first.startTime.hour > second.startTime.hour) {
            1
        } else {
            if (first.startTime.minute < second.startTime.minute) {
                -1
            } else if (first.startTime.minute > second.startTime.minute) {
                1
            } else {
                0
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(
            items = dayState.tasks.sortedWith(TaskSortingComparator),
            key = { it.id }
        ) { task ->
            TaskListItem(
                task = task,
                onNavigateToTaskInfo = onNavigateToTaskInfo,
                selectTask = selectTask
            )
        }
    }
}