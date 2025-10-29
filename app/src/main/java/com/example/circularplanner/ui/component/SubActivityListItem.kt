package com.example.circularplanner.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.circularplanner.R
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.AudioViewModel
import com.example.circularplanner.ui.viewmodel.VoiceNoteUiState
import com.example.circularplanner.utils.TouchGestureUtils
import java.time.LocalDateTime

@Composable
fun SubActivityListItem (
    ordinalNumber: Int,
    subActivity: ActivityUiState,
    deleteVoiceNote: (VoiceNoteUiState) -> Unit,
    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    val audioViewModel: AudioViewModel = viewModel(factory = AudioViewModel.Factory)

    var showContent by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = ""
    )
    // A value to use in case an activity has not been finished yet
    var endTimeValue = subActivity.endTime

    if (endTimeValue == null) {
        endTimeValue = Time(LocalDateTime.now().hour, LocalDateTime.now().minute)
    }

//    OutlinedCard(
//    Card(
    Row(
        modifier = Modifier
//            .padding(vertical = 10.dp)
            .sizeIn(maxHeight = 50.dp)
            .fillMaxWidth()
//            .background(Color(0xffffffff))
        ,
//        border = CardDefaults.outlinedCardBorder()
    ) {
        Column (
            modifier = Modifier
//                .padding(horizontal = 10.dp)
            ,
            verticalArrangement = Arrangement.Center,
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
                        .fillMaxWidth(0.5f)
                    ,
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text (
                        text = ordinalNumber.toString(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Row () {
                        Text (text = "${subActivity.startTime}")

                        Spacer(modifier = Modifier.width(10.dp))

                        // Time duration
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
                            fontWeight = FontWeight.Thin,
//                                fontSize = 18.sp
                        )
                    }
                }

//                Spacer(modifier = Modifier.width(20.dp))

                Row (
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                    ,
                    horizontalArrangement = Arrangement.End,
                ){
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
                }
            }


            Column (
                modifier = Modifier
                    .alpha(contentAlpha)
            ) {
                val totalMinutes = TouchGestureUtils.calculateTotalNumberOfMinutes(subActivity.startTime, endTimeValue)

                Text (text = TouchGestureUtils.formatTime(totalMinutes))

                // Notes
                Text(
                    text = subActivity.note,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp
                )

                VoiceNoteList(
                    activityUiState = subActivity,
                    audioViewModel = audioViewModel,
                    removeVoiceNote = deleteVoiceNote,
                    updateLastPlayedPosition = updateLastPlayedPosition
                )
            }
        }
    }
}