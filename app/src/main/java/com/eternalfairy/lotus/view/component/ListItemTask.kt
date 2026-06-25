package com.eternalfairy.lotus.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.lotus.view.data.TaskUiState
import com.eternalfairy.lotus.view.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.lotus.view.theme.HEADER_TEXT_COLOR
import java.util.UUID

@Composable
fun ListItemTask(
    modifier: Modifier = Modifier,
    task: TaskUiState,
    onNavigateToTaskInfo: () -> Unit,
    selectTask: (UUID?) -> Unit
){
    Row(
        modifier = modifier
            .padding(
                horizontal = 10.dp,
                vertical = 10.dp
            )
            .clickable {
                selectTask(task.id)
                onNavigateToTaskInfo()
            }
            .sizeIn(maxHeight = 150.dp)
            .background(COMPONENT_BACKGROUND_COLOR)
            .leftBorder(
                color = HEADER_TEXT_COLOR,
                width = 5f
            )
    ) {
        Row (
            modifier = Modifier
                .padding(
                    horizontal = 5.dp,
                    vertical = 3.dp
                )
                .clickable {
                    selectTask(task.id)
                    onNavigateToTaskInfo()
                }
                .sizeIn(maxHeight = 150.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(
                    vertical = 10.dp
                )
                .padding(
                    start = 15.dp
                )

            ,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column (
            ) {
                Text(
                    text = task.priority.toString(),
                    fontWeight = FontWeight.Bold,
                    color = HEADER_TEXT_COLOR
                )
            }

            Spacer(Modifier.width(15.dp))

            Column (
                modifier = Modifier.weight(2f)
            ) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = task.description ?: "",
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}