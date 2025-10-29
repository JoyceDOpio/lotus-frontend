package com.example.circularplanner.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.VoiceNoteUiState
import com.example.circularplanner.utils.TouchGestureUtils
import java.time.LocalDateTime

@Composable
fun SubActivityList (
    mainActivityUiState: ActivityUiState,
    removeVoiceNote: (VoiceNoteUiState) -> Unit,
    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    Log.i("SubActivityList", "mainActivityUiState $mainActivityUiState")
    Column (
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
            ,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text (
                text = "SUB-ACTIVITIES",// TODO: Read from resource
//            fontWeight = FontWeight.Thin,
                color = MaterialTheme.colorScheme.primary,

                )

            var subActivitiesTotal = 0
            for (subActivity in mainActivityUiState.subActivitiesUiState) {
                val endTimeValue = subActivity.endTime ?: Time(LocalDateTime.now().hour, LocalDateTime.now().minute)
                subActivitiesTotal += TouchGestureUtils.calculateTotalNumberOfMinutes(subActivity.startTime, endTimeValue)
            }

            Text (
//                modifier = Modifier
//                    .padding(horizontal = 10.dp)
//                ,
                text = TouchGestureUtils.formatTime(subActivitiesTotal),
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        LazyColumn(
            modifier = Modifier
                .padding(vertical = 5.dp)
                .fillMaxWidth()
        ) {
            itemsIndexed (
                items = mainActivityUiState.subActivitiesUiState,
                key = { _, subActivity: ActivityUiState -> subActivity.id!! }
            ) { index: Int, subActivity: ActivityUiState ->
                var minutesBetween: Int

                if (index == 0) {
                    minutesBetween = TouchGestureUtils.calculateTotalNumberOfMinutes(mainActivityUiState.startTime, mainActivityUiState.subActivitiesUiState[index ].startTime)
                }
                else {
                    minutesBetween = TouchGestureUtils.calculateTotalNumberOfMinutes(mainActivityUiState.subActivitiesUiState[index - 1].endTime!!, mainActivityUiState.subActivitiesUiState[index].startTime)
                }

                Row (
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                    ,
                ) {
                    Text (
                        modifier = Modifier
                            .leftBorder(
                                color = MaterialTheme.colorScheme.primary,
                                width = 5f
                            )
                            .padding(horizontal = 10.dp)
                        ,
                        text = TouchGestureUtils.formatTime(minutesBetween),
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                SubActivityListItem(
                    ordinalNumber = index + 1,
                    subActivity = subActivity,
                    deleteVoiceNote = removeVoiceNote,
                    updateLastPlayedPosition = updateLastPlayedPosition
                )
            }
        }
    }
}