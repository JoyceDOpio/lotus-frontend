package com.example.circularplanner.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.circularplanner.data.Task
import java.util.UUID

@Composable
fun TaskList(
    tasks: List<Task>,
    onNavigateToTaskInfo: () -> Unit,
    selectTask: (UUID?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(
                vertical = 5.dp,
                horizontal = 5.dp
            )
            .fillMaxSize()
    ) {
        items(
            items = tasks,
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