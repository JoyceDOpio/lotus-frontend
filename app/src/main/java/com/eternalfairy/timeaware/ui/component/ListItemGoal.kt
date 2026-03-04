package com.eternalfairy.timeaware.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eternalfairy.timeaware.R
import com.eternalfairy.timeaware.data.Goal
import java.util.UUID

@Composable
fun ListItemGoal(
    modifier: Modifier = Modifier,
    goal: Goal,
    onNavigateToGoalEdit: () -> Unit,
    onDeleteGoal: (Goal) -> Unit,
    selectGoal: (UUID?) -> Unit
){
    OutlinedCard(
        modifier = modifier
            .padding(
                horizontal = 5.dp,
                vertical = 3.dp
            )
            .sizeIn(maxHeight = 150.dp),
        border = CardDefaults.outlinedCardBorder(),
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
                        color = MaterialTheme.colorScheme.primary
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

            Column (
                modifier = Modifier
                    .clip(CircleShape)
                    .width(40.dp)
                    .aspectRatio(1f)
            ) {
                val dropdownItems = listOf<DropDownItem>(
                    DropDownItem(
                        text = "Edit",
                        iconId = R.drawable.edit_24dp_5f6368_fill0_wght400_grad0_opsz24,
                        onClick = {
                            selectGoal(goal.id)
                            onNavigateToGoalEdit()
                        }
                    ),
                    DropDownItem(
                        text = "Delete",
                        iconId = R.drawable.delete_24dp_5f6368_fill0_wght400_grad0_opsz24,
                        onClick = {
                            onDeleteGoal(goal)
                        }
                    ),
                )

                TaskDropdownMenu(
                    dropdownItems = dropdownItems
                )
            }
        }
    }
}