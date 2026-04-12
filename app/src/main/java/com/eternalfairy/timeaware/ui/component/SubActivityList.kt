package com.eternalfairy.timeaware.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eternalfairy.timeaware.db.Time
import com.eternalfairy.timeaware.ui.data.ActivityUiState
import com.eternalfairy.timeaware.ui.data.VoiceNoteUiState
import com.eternalfairy.timeaware.ui.theme.Teal12
import com.eternalfairy.timeaware.utils.TouchGestureUtils
import java.time.LocalDateTime
import java.util.UUID

@Composable
fun SubActivityList (
    mainActivityUiState: ActivityUiState,
    onDeleteItem: (UUID) -> Unit,
    onEditItem: (ActivityUiState) -> Unit,
    removeVoiceNote: (VoiceNoteUiState) -> Unit,
    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    // Text
    val headerText = "SUB-ACTIVITIES"// TODO: Read from resource

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
                text = headerText,
                color = Teal12
            )

            var subActivitiesTotal = 0
            for (subActivity in mainActivityUiState.subActivitiesUiState) {
                val endTimeValue = subActivity.endTime ?: Time(LocalDateTime.now().hour, LocalDateTime.now().minute)
                subActivitiesTotal += TouchGestureUtils.calculateTotalNumberOfMinutes(subActivity.startTime, endTimeValue)
            }

            Text (
                text = TouchGestureUtils.formatTime(subActivitiesTotal),
                fontWeight = FontWeight.Light,
                color = Teal12
            )
        }

        Column(
            modifier = Modifier
                .padding(vertical = 5.dp)
                .fillMaxWidth()
        ) {
            for ((index, subActivity) in mainActivityUiState.subActivitiesUiState.withIndex()) {
                // Minutes between the current sub-activity and previous sub-activity/start of the main activity
                val minutesBetween: Int = if (index == 0) {
                    TouchGestureUtils.calculateTotalNumberOfMinutes(mainActivityUiState.startTime, mainActivityUiState.subActivitiesUiState[index ].startTime)
                } else {
                    TouchGestureUtils.calculateTotalNumberOfMinutes(mainActivityUiState.subActivitiesUiState[index - 1].endTime!!, mainActivityUiState.subActivitiesUiState[index].startTime)
                }

                Row (
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                    ,
                ) {
                    Text (
                        modifier = Modifier
                            .leftBorder(
                                color = Teal12,
                                width = 5f
                            )
                            .padding(
                                horizontal = 17.dp,
                                vertical = 5.dp
                            )
                        ,
                        text = TouchGestureUtils.formatTime(minutesBetween),
                        fontWeight = FontWeight.Light,
                        color = Teal12,
                    )
                }

                SubActivityListItem(
                    ordinalNumber = index + 1,
                    subActivity = subActivity,
                    onDelete = onDeleteItem,
                    onEdit = onEditItem,
                    onDeleteVoiceNote = removeVoiceNote,
                    updateLastPlayedPosition = updateLastPlayedPosition
                )
            }
        }
    }
}