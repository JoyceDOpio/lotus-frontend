package com.example.circularplanner.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.R
import com.example.circularplanner.ui.viewmodel.GoalUiState
import java.util.UUID

@Composable
fun GoalEditScreen(
    modifier: Modifier = Modifier,
    goalUiState: GoalUiState,
    lastPriority: Int,
    onBack: () -> Unit,
    saveGoal: () -> Unit,
    setGoalPriority: (Int) -> Unit,
    setGoalTitle: (String) -> Unit
) {
    fun getLabel(goalId: UUID?): String {
        if (goalId == null) {
            return "CREATE A GOAL"
        }

        return "EDIT GOAL"
    }

    val label: String = getLabel(goalUiState.id)
    val goalDetails = goalUiState

    if (goalUiState.priority == null) {
        setGoalPriority(lastPriority + 1)
    }

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

                    // Save button
                    IconButton(onClick = {
                        saveGoal()
                        onBack()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.save_alt_svgrepo_com),
                            contentDescription = "Save",
                            modifier = Modifier.fillMaxSize(0.6F),
                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(horizontal = 15.dp)
                .padding(innerPadding)
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label
            Text(
                modifier = Modifier
                    .padding(bottom = 10.dp),
                text = label,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // Title field
            OutlinedTextField(
                value = goalDetails.title,
                onValueChange = setGoalTitle,
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                textStyle = TextStyle(fontSize = 18.sp),
                label = { Text("Title") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = false,
                shape = RoundedCornerShape(15.dp)
            )
        }
    }
}