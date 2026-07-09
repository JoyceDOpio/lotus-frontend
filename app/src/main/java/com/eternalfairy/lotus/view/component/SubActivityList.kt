package com.eternalfairy.lotus.view.component

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
import com.eternalfairy.lotus.view.data.ActivityUiState
import com.eternalfairy.lotus.view.screen.planner.PlannerUiEvent
import com.eternalfairy.lotus.view.theme.Teal12
import com.eternalfairy.lotus.view.utils.TouchGestureUtils
import com.eternalfairy.lotus.viewmodel.PlannerViewModel
import kotlinx.datetime.LocalTime
import java.time.LocalDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun SubActivityList (
    viewModel: PlannerViewModel,
//    mainActivityUiState: ActivityUiState,
    onDeleteItem: (Uuid) -> Unit,
    onEditItem: (ActivityUiState) -> Unit,
//    removeVoiceNote: (VoiceNoteUiState) -> Unit,
//    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    val state = viewModel.state
    val mainActivity = state.selectedActivity
    val subActivities = state.subActivities

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
            for (subActivity in subActivities) {
                val endTimeValue = subActivity.endTime ?: LocalTime(
                    LocalDateTime.now().hour,
                    LocalDateTime.now().minute
                )
                subActivitiesTotal += TouchGestureUtils.calculateTotalNumberOfMinutes(subActivity.startTime!!, endTimeValue)
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
            for ((index, subActivity) in subActivities.withIndex()) {
                // Minutes between the current sub-activity and previous sub-activity/start of the main activity
                val minutesBetween: Int = if (index == 0) {
                    TouchGestureUtils.calculateTotalNumberOfMinutes(mainActivity.startTime!!, subActivities[index].startTime!!)
                } else {
                    TouchGestureUtils.calculateTotalNumberOfMinutes(subActivities[index - 1].endTime!!, subActivities[index].startTime!!)
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
//                    onDeleteVoiceNote = removeVoiceNote,
                    onDeleteVoiceNote = { viewModel.onEvent(PlannerUiEvent.DeleteVoiceNote) },
//                    updateLastPlayedPosition = updateLastPlayedPosition
                    updateLastPlayedPosition = { position, itemIndex -> viewModel.updateLastPlayedPosition(position, itemIndex) }
                )
            }
        }
    }
}