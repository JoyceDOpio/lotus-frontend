package com.example.circularplanner.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.R
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.screen.BOTTOM_BAR_COLOR
import com.example.circularplanner.ui.screen.BOTTOM_BAR_TEXT_COLOR
import com.example.circularplanner.ui.viewmodel.DayUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTimeSetUp (
    modifier: Modifier = Modifier,
    dayUiState: DayUiState,// TODO: There is a loophole here - it is possible I should read the first value from DayState
    onBack: () -> Unit,
    onClickSaveActiveTime: () -> Unit,
    setActiveTimeStart: (Time) -> Unit,
    setActiveTimeEnd: (Time) -> Unit
){
    var showTimePicker by remember { mutableStateOf(false) }
    var showStartActiveTimePicker by remember { mutableStateOf(false) }
    var activeTimeStart = dayUiState.activeTimeStart
    var activeTimeEnd = dayUiState.activeTimeEnd
    var isActiveTimeValid by remember { mutableStateOf(false) }
    val startActiveTimePickerState = rememberTimePickerState(
        activeTimeStart.hour,
        activeTimeStart.minute
    )
    val endActiveTimePickerState = rememberTimePickerState(
        activeTimeEnd.hour,
        activeTimeEnd.minute
    )
//    val cancelButtonText = "Cancel"
//    val okButtonText = "Save"

    fun validateActiveTime(): Boolean {
        val start = activeTimeStart
        val end = activeTimeEnd

        if (start.hour == 0 && start.minute == 0) {
            if (end.hour >= start.hour || end.minute >= start.minute) {
                return true
            }
        } else {
            if (end.hour == start.hour) {
                if (end.minute > start.minute) {
                    return true
                }
            } else if (end.hour > start.hour) {
                return true
            }
        }

        return false
    }

    fun onCancelCloseTimePicker() {
        showTimePicker = false
        showStartActiveTimePicker = false
    }

    fun onSaveCloseTimePicker() {
        if (showStartActiveTimePicker) {
            setActiveTimeStart(Time(startActiveTimePickerState.hour, startActiveTimePickerState.minute))
        } else {
            setActiveTimeEnd(Time(endActiveTimePickerState.hour, endActiveTimePickerState.minute))
        }
        isActiveTimeValid = validateActiveTime()

        showTimePicker = false
        showStartActiveTimePicker = false
    }

    fun openTimePicker() {
        showTimePicker = true
    }

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
                        onClickSaveActiveTime()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.save_alt_svgrepo_com),
                            contentDescription = "Save task",
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
            ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                modifier = Modifier
                    .padding(bottom = 10.dp),
                text = "ACTIVE TIME",// TODO: Read string from resource
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Row (
                modifier = Modifier.padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active time start
                Box (
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            onClick = {
                                showStartActiveTimePicker = true
                                openTimePicker()
                            }
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .padding(10.dp),
                        text = "%d:%02d".format(
                            activeTimeStart.hour,
                            activeTimeStart.minute
                        ),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    " - ",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Active time end
                Box (
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            onClick = {
                                openTimePicker()
                            }
                        )
                ) {
                    Text(
                        modifier = Modifier
                            .padding(10.dp),
                        text = "%d:%02d".format(
                            activeTimeEnd.hour,
                            activeTimeEnd.minute
                        ),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

//            Row(
//                modifier = modifier
//                    .fillMaxWidth(),
////                .background(Color(0xff97dde8)),// TODO: Add the color to a theme
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//
//            ) {
//                Button(
//                    onClick = onBack,
//                    shape = RoundedCornerShape(8.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
//                ) {
//                    Text(
//                        text = cancelButtonText,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//
//                Button(
//                    //TODO: Display notification about invalid input
//                    onClick = {
//                        onClickSaveActiveTime()
//                        onBack()
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth(0.9f),
//                    enabled = isActiveTimeValid,
//                    shape = RoundedCornerShape(8.dp)
//                ) {
//                    Text(
//                        text = okButtonText,
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }
        }
    }


    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = {
                onCancelCloseTimePicker()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaveCloseTimePicker()
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onCancelCloseTimePicker()
                    }
                ) { Text("Cancel") }
            }
        )
        {
            if (showStartActiveTimePicker) {
                TimePicker(state = startActiveTimePickerState)
            } else {
                TimePicker(state = endActiveTimePickerState)
            }
        }
    }
}