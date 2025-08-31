package com.example.circularplanner.ui.component

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.AudioViewModel
import com.example.circularplanner.ui.viewmodel.VoiceNoteUiState

@Composable
fun VoiceNoteList (
    audioViewModel: AudioViewModel,
    activityUiState: ActivityUiState,
    removeVoiceNote: (VoiceNoteUiState) -> Unit,
    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    var voiceNotesUiState = activityUiState.voiceNotesUiState
    val context = LocalContext.current
    val exoPlayer = remember(context) { ExoPlayer.Builder(context).build() }
    val playingItemIndex = audioViewModel.currentlyPlayingIndex.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(playingItemIndex.value) {
        if (playingItemIndex.value == null) {
            exoPlayer.pause()
        } else {
            val audio = voiceNotesUiState[playingItemIndex.value!!]
            exoPlayer.setMediaItem(MediaItem.fromUri(audio.uri), audio.lastPlayedPosition)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object: Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                if (playbackState == Player.STATE_ENDED) {
                    // Move the position to the beginning of the audio
                    exoPlayer.seekTo(0L)
                    updateLastPlayedPosition(0L, playingItemIndex.value!!)
                    // Stop the player - otherwise, it keeps playing over and over again after moving the position to 0L
                    exoPlayer.stop()
                    audioViewModel.setCurrentlyPlayingIndex(null)
                }
            }
        }
        exoPlayer.addListener(listener)

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (playingItemIndex.value == null) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_START, Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_RESUME, Lifecycle.Event.ON_DESTROY, Lifecycle.Event.ON_ANY -> null
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> exoPlayer.pause()
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            exoPlayer.release()
        }
    }

    LazyColumn(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        itemsIndexed (
            items = voiceNotesUiState,
            key = { _, voiceNoteUiState: VoiceNoteUiState -> voiceNoteUiState.id!! }
        ) { index: Int, voiceNoteUiState: VoiceNoteUiState ->
            VoiceNoteListItem(
                isPlaying = index == playingItemIndex.value,
                exoPlayer = exoPlayer,
                voiceNote = voiceNoteUiState,
                onPlayClick = { audioViewModel.onPlayClick(
                    exoPlayer.currentPosition, index, updateLastPlayedPosition
                ) }
            )
        }
    }
}