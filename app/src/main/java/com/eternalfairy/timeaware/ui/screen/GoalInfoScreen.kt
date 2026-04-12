package com.eternalfairy.timeaware.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.eternalfairy.timeaware.R
import com.eternalfairy.timeaware.ui.component.DropDownItem
import com.eternalfairy.timeaware.ui.component.GoalCard
import com.eternalfairy.timeaware.ui.component.TaskDropdownMenu
import com.eternalfairy.timeaware.ui.data.GoalUiState
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR

@Composable
fun GoalInfoScreen(
    goalUiState: GoalUiState,
    onDeleteGoal: () -> Unit,
    onBack: () -> Unit,
    onNavigateToGoalEdit: () -> Unit
) {
    val goalDetails = goalUiState

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
                                onNavigateToGoalEdit()
                            }
                        ),
                        DropDownItem(
                            text = "Delete",
                            iconId = R.drawable.delete_24dp_5f6368_fill0_wght400_grad0_opsz24,
                            onClick = {
                                onDeleteGoal()
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
            GoalCard (
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                title = goalDetails.title
            )
        }
    }
}