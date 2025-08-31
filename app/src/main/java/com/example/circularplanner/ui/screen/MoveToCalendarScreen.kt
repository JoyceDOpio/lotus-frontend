package com.example.circularplanner.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.component.Calendar
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.TaskUiState
import com.example.circularplanner.ui.viewmodel.UserInput
import com.example.circularplanner.utils.Slot
import java.time.LocalDate

@Composable
fun MoveToCalendarScreen(
    modifier: Modifier = Modifier,
    dayState: DayState,
    taskUiState: TaskUiState,
    userInput: UserInput,
    onBack: () -> Unit,
    onSetDate: (LocalDate) -> Unit
) {
    val selectedDate = userInput.selectedDate
    var slots = emptyList<Slot>()


    Column () {
        // List of tasks


        Calendar(
            userInput = userInput,
            onSetDate = onSetDate
        )
    }
}