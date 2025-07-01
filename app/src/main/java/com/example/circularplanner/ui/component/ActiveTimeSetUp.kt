package com.example.circularplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.circularplanner.R
import com.example.circularplanner.data.Time
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
    var activeTimeStart by remember { mutableStateOf(dayUiState.activeTimeStart) }
    var activeTimeEnd by remember { mutableStateOf(dayUiState.activeTimeEnd) }
    var isActiveTimeValid by remember { mutableStateOf(false) }
    val startActiveTimePickerState = rememberTimePickerState(
        activeTimeStart.hour,
        activeTimeStart.minute
    )
    val endActiveTimePickerState = rememberTimePickerState(
        activeTimeEnd.hour,
        activeTimeEnd.minute
    )
    val cancelButtonText = "Cancel"
    val okButtonText = "Set Active Time"

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

    Column(
        modifier = modifier
            .fillMaxWidth(0.7f)
            .fillMaxHeight()
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Spacer(Modifier.weight(1f, true))

        Column(
            modifier = modifier
                .weight(3f)
                .padding(vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceAround,
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                )
                {
//                    IconButton(onClick = {
//                        //TODO: open info popup
//                    }) {
//                        Icon(
//                            imageVector = ImageVector.vectorResource(id = R.drawable.info_27dp_5f6368_fill0_wght400_grad0_opsz24),
//                            contentDescription = stringResource(id = R.string.info_content_desc),
//                            //                        modifier = Modifier.fillMaxSize(0.25F)
//                        )
//                    }

                    ActiveTimePicker(
                        startActiveTimePickerState,
                        ActiveTime.START
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        showTimePicker = true
                        showStartActiveTimePicker = true
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.schedule_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Open active time start setter",
                            modifier = Modifier.fillMaxSize(0.8f)
                        )
                    }
                }

            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,

                    ) {
//                    IconButton(onClick = {
//                        //TODO: open info popup
//                    }) {
//                        Icon(
//                            imageVector = ImageVector.vectorResource(id = R.drawable.info_27dp_5f6368_fill0_wght400_grad0_opsz24),
//                            contentDescription = stringResource(id = R.string.info_content_desc),
//                            //                        modifier = Modifier.fillMaxSize(0.25F)
//                        )
//                    }

                    ActiveTimePicker(
                        endActiveTimePickerState,
                        ActiveTime.END
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        showTimePicker = true
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.schedule_24dp_5f6368_fill0_wght400_grad0_opsz24),
                            contentDescription = "Open active time end setter",
                            modifier = Modifier.fillMaxSize(0.8F)
                        )
                    }
                }
            }
        }

        Row(
            modifier = modifier
                .weight(1f)
                .fillMaxWidth()
                .paddingFromBaseline(top = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = cancelButtonText)
            }

            Spacer(Modifier.weight(0.2f, true))

            Button(
                //TODO: Display notification about invalid input
                onClick = {
                    onClickSaveActiveTime()
                    onBack()
                },
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight(),
                enabled = isActiveTimeValid,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = okButtonText,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.weight(2f, true))
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