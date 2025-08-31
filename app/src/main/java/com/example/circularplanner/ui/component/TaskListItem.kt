package com.example.circularplanner.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.data.Task
import java.util.UUID

@Composable
fun TaskListItem(
    modifier: Modifier = Modifier,
    task: Task,
    onNavigateToTaskInfo: () -> Unit,
    selectTask: (UUID?) -> Unit
){
    OutlinedCard(
        modifier = modifier
            .padding(
                horizontal = 5.dp,
                vertical = 3.dp
            )
            .clickable {
                selectTask(task.id)
                onNavigateToTaskInfo()
            }
            .sizeIn(maxHeight = 150.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Column (
                modifier = Modifier.weight(2f)
            ) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = task.description,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}