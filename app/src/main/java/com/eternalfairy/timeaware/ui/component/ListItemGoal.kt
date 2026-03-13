package com.eternalfairy.timeaware.ui.component

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
import com.eternalfairy.timeaware.data.Goal
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR
import java.util.UUID

@Composable
fun ListItemGoal(
    modifier: Modifier = Modifier,
    goal: Goal,
    onNavigateToGoalInfo: () -> Unit,
    selectGoal: (UUID?) -> Unit
){
    Row(
        modifier = modifier
            .padding(
                horizontal = 10.dp,
                vertical = 10.dp
            )
            .clickable {
                selectGoal(goal.id)
                onNavigateToGoalInfo()
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
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(
                    vertical = 10.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row (
                modifier = Modifier
                    .padding(
                        start = 15.dp
                    )
                ,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column () {
                    Text(
                        text = goal.priority.toString(),
                        fontWeight = FontWeight.Bold,
                        color = HEADER_TEXT_COLOR
                    )
                }

                Spacer(Modifier.width(15.dp))

                Column (
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                ) {
                    Text(
                        text = goal.title,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}