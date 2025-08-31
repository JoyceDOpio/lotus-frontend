package com.example.circularplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.example.circularplanner.R
import com.example.circularplanner.ui.viewmodel.VoiceNoteUiState
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import kotlin.math.floor

@Composable
fun VoiceNoteListItem (
    isPlaying: Boolean,
    exoPlayer: ExoPlayer,
    voiceNote: VoiceNoteUiState,
    onPlayClick: () -> Unit
) {
    var progress by remember { mutableStateOf(voiceNote.lastPlayedPosition) }
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun millisecondsToDuration(position: Long): String {
        val totalSeconds = floor(position / 1E3).toInt()
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds - (minutes * 60)

        return "%d:%02d".format(minutes, remainingSeconds)
    }

    fun millisecondsToTime(milliseconds: Long): String {
        val hour = milliseconds / (1000 * 60 * 60) % 24
        val minute = milliseconds / (1000 * 60)

        return "%d:%02d".format(hour, minute)
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            progress = exoPlayer.currentPosition
            delay(5L)
        }
    }

    OutlinedCard(
        modifier = Modifier
            .padding(vertical = 3.dp)
            .sizeIn(maxHeight = 50.dp)
            .fillMaxWidth(),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column () {
            Row (
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/pause button
                IconButton(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(15.dp)),
                    onClick = {
                        onPlayClick()
                    },
                    enabled = true
                ) {
                    when (isPlaying) {
                        true -> {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.pause_svgrepo_com),
                                contentDescription = "Pause",
                                modifier = Modifier.fillMaxSize(0.5f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        false -> {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.play_svgrepo_com),
                                contentDescription = "Play",
                                modifier = Modifier
                                    .fillMaxSize(0.6f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Position
                Text(
                    text = millisecondsToDuration(progress),
                    modifier = Modifier.padding(horizontal = 5.dp)
                )

                // Slider
                Slider(
                    value = progress.toFloat(),
                    onValueChange = { value ->
                        exoPlayer.seekTo(value.toLong())
                        progress = exoPlayer.currentPosition
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.75f),
                    valueRange = 0f..voiceNote.duration.toFloat(),// FIXME: Voice note duration is inaccurate (it's bigger than exoPlayer.contentDuration)
//                    valueRange = 0f..exoPlayer.contentDuration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.LightGray,
                    )
                )

                // Duration
                Text(
                    text = millisecondsToDuration(voiceNote.duration),
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                )
            }
        }
    }
}