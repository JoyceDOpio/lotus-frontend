package com.example.circularplanner.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.circularplanner.R
import com.example.circularplanner.ui.component.TaskCard
import com.example.circularplanner.ui.component.VoiceNoteList
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.AudioViewModel
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.ui.viewmodel.UserInput
import com.example.circularplanner.ui.viewmodel.VoiceNoteUiState

@Composable
fun TaskActivityComparisonScreen (
    modifier: Modifier = Modifier,
    activityUiState: ActivityUiState,
    taskUiState: TaskUiState,
    userInput: UserInput,
    deleteVoiceNote: (VoiceNoteUiState) -> Unit,
    onCancel: () -> Unit,
    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    val taskDetails = taskUiState
    val activityDetails = activityUiState

    val audioViewModel: AudioViewModel = viewModel(factory = AudioViewModel.Factory)

    val activityLabel = "ACTIVITY"//TODO: Read from string resource
    val taskLabel = "TASK"//TODO: Read from string resource

    Scaffold (
        bottomBar = {
            BottomAppBar (
                containerColor = Color(BOTTOM_BAR_COLOR),
                actions = {
//                    // Leading icons should typically have a high content alpha
//                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
//                        IconButton(onClick = { /* doSomething() */ }) {
//                            Icon(Icons.Filled.Menu, contentDescription = "Localized description")
//                        }
//                    }

                    // Close button
                    IconButton(onClick = {
                        // Navigate to the previous stack entry
                        onCancel()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                            contentDescription = "Open time picker",
                            modifier = Modifier.fillMaxSize(0.8F),
                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        )
                    }

                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))
                }
            )
        }
    ) { innerPadding ->
        Column (
            modifier = modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Task
            Column (
                modifier = Modifier
                    .fillMaxHeight(0.5f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Show task details if there is a task to be shown
                if (taskDetails.id != null) {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 15.dp),
                        text = taskLabel,// TODO: Read text from string resource
                        color = MaterialTheme.colorScheme.primary
                    )

                    TaskCard (
                        date = userInput.selectedDate,
                        endTime = taskDetails.endTime,
                        startTime = taskDetails.startTime,
                        title = taskDetails.title
                    ) {
                        // Description
                        Text(
                            text = taskDetails.description,
                            modifier = modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp
                        )
                    }
                }
                // Otherwise, show a statement that there is no task planned for the select time
                else {
                    Column (
                        modifier = Modifier
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NO TASK TO DISPLAY",// TODO: Read string from resource
                            color = Color.LightGray
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier
                    .padding(
                        horizontal = 15.dp,
                        vertical = 5.dp)
                    .fillMaxWidth(),
                thickness = 1.dp,
            )

            // Activity
            Column (
                modifier = Modifier
//                    .background(Color(0xff97dde8))// TODO: Add color to a theme
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (activityDetails.id != null) {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 15.dp),
                        text = activityLabel,// TODO: Read text from string resource
                        color = MaterialTheme.colorScheme.primary
                    )

                    TaskCard (
                        date = userInput.selectedDate,
                        endTime = activityDetails.endTime,
                        startTime = activityDetails.startTime,
                        title = activityDetails.title
                    ) {
                        VoiceNoteList(
                            activityUiState = activityUiState,
                            audioViewModel = audioViewModel,
                            removeVoiceNote = deleteVoiceNote,
                            updateLastPlayedPosition = updateLastPlayedPosition
                        )
                    }
                }
                // Otherwise, show a statement that there is no task planned for the select time
                else {
                    Column (
                        modifier = Modifier
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NO ACTIVITY TO DISPLAY",// TODO: Read string from resource
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}