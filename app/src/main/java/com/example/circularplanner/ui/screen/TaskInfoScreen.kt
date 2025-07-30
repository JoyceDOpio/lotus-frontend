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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.R
import com.example.circularplanner.ui.component.TaskCard
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.ui.viewmodel.UserInput

@Composable
fun TaskInfoScreen(
    modifier: Modifier = Modifier,
    taskUiState: TaskUiState,
    userInput: UserInput,
    onCancel: () -> Unit,
    onNavigateToTaskEdit: () -> Unit
) {
    val taskDetails = taskUiState

    Scaffold (
        bottomBar = {
            BottomAppBar (
//                contentColor = MaterialTheme.colorScheme.primaryContainer
                actions = {
//                    // Leading icons should typically have a high content alpha
//                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
//                        IconButton(onClick = { /* doSomething() */ }) {
//                            Icon(Icons.Filled.Menu, contentDescription = "Localized description")
//                        }
//                    }

                    // Close button
                    IconButton(onClick = {
                        // Navigate to previous stack entry
                        onCancel()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.cancel_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Open time picker",
                            modifier = Modifier.fillMaxSize(0.8F)
                        )
                    }

                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))

                    // Edit button
                    IconButton(onClick = {
                        onNavigateToTaskEdit()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.edit_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Open time picker",
                            modifier = Modifier.fillMaxSize(0.8F)
                        )
                    }
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
                date = userInput.selectedDate,
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