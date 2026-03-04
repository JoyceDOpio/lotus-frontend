package com.eternalfairy.timeaware.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eternalfairy.timeaware.R
import com.eternalfairy.timeaware.data.Time
import com.eternalfairy.timeaware.ui.viewmodel.ActivityUiState
import com.eternalfairy.timeaware.ui.viewmodel.AudioViewModel
import com.eternalfairy.timeaware.ui.viewmodel.VoiceNoteUiState
import com.eternalfairy.timeaware.utils.TouchGestureUtils
import java.time.LocalDateTime
import java.util.UUID

@Composable
fun SubActivityListItem (
    ordinalNumber: Int,
    subActivity: ActivityUiState,
    onDelete: (UUID) -> Unit,
    onDeleteVoiceNote: (VoiceNoteUiState) -> Unit,
    onEdit: (ActivityUiState) -> Unit,
    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    val audioViewModel: AudioViewModel = viewModel(factory = AudioViewModel.Factory)

    var showContent by remember { mutableStateOf(false) }
    // A value to use in case an activity has not been finished yet
    var endTimeValue = subActivity.endTime

    if (endTimeValue == null) {
        endTimeValue = Time(LocalDateTime.now().hour, LocalDateTime.now().minute)
    }

    // Texts
    val notesHeader = "NOTES"// TODO: Read string from source string
    val editText = "Edit"//TODO: Read from string resource
    val deleteText = "Delete"//TODO: Read from string resource

    Column(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth()
    ) {
            Row (
                modifier = Modifier
                    .fillMaxSize()
                ,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                    ,
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Number of sub-activity
                    Text (
                        text = ordinalNumber.toString(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Row (

                    ) {
                        // Sub-activity start time
                        Text (text = "${subActivity.startTime}")

                        Spacer(modifier = Modifier.width(10.dp))

                        // Sub-activity duration
                        val totalMinutes = TouchGestureUtils.calculateTotalNumberOfMinutes(
                            subActivity.startTime, endTimeValue)
                        val hours = totalMinutes / TouchGestureUtils.MINUTES_IN_HOUR
                        val minutes = totalMinutes % TouchGestureUtils.MINUTES_IN_HOUR
                        val timeDurationText = if (hours == 0) {
                            "%01d min".format(minutes)
                        } else if (minutes == 0) {
                            if (hours > 1) {
                                "%01d hours".format(hours)
                            } else {
                                "%01d hour".format(hours)
                            }
                        } else {
                            if (hours > 1) {
                                "%01d hours %01d min".format(hours, minutes)
                            } else {
                                "%01d hour %01d min".format(hours, minutes)
                            }
                        }

                        Text(
                            text = timeDurationText,
                            fontWeight = FontWeight.Thin
                        )
                    }
                }

                Row (
                    modifier = Modifier
                        .fillMaxWidth(1f)
                    ,
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    // Sub-activity title
                    Text (
                        text = subActivity.title,
                        modifier = Modifier
                            .basicMarquee()
                    )

                    if (subActivity.note != "" || !subActivity.voiceNotesUiState.isEmpty()) {
                        Spacer(modifier = Modifier.width(10.dp))

                        IconButton(
                            onClick = {
//                        focusManager.clearFocus()
                                showContent = !showContent
                            }
                        ) {
                            Icon(
                                imageVector = if (!showContent) ImageVector.vectorResource(id = R.drawable.arrow_down_expand)
                                else ImageVector.vectorResource(id = R.drawable.arrow_up_hide),
                                contentDescription = if (!showContent) "Show content"//TODO: Read string from resource
                                else "Hide content",//TODO: Read string from resource
                                modifier = Modifier.fillMaxSize(0.7F),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    val dropdownItems = listOf(
                        DropDownItem(
                            text = editText,
                            iconId = R.drawable.edit_24dp_5f6368_fill0_wght400_grad0_opsz24,
                            onClick = { onEdit(subActivity) }
                        ),
                        DropDownItem(
                            text = deleteText,
                            iconId = R.drawable.delete_24dp_5f6368_fill0_wght400_grad0_opsz24,
                            onClick = { onDelete(subActivity.id!!) }
                        )
                    )
                    TaskDropdownMenu(
                        dropdownItems = dropdownItems,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(0.7F)
                    )
                }
            }


            AnimatedVisibility(
                visible = showContent
            ) {
                Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 23.dp)
                ) {
                    // Notes
                    Text (
                        text = notesHeader,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = subActivity.note,
                        modifier = Modifier
                            .fillMaxSize()
                        ,
                        fontWeight = FontWeight.Normal
                    )

                    VoiceNoteList(
                        activityUiState = subActivity,
                        audioViewModel = audioViewModel,
                        onDeleteItem = onDeleteVoiceNote,
                        updateLastPlayedPosition = updateLastPlayedPosition
                    )
                }
            }
    }
}