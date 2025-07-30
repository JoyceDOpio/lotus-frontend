package com.example.circularplanner.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class AudioViewModel (
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return AudioViewModel(
                    savedStateHandle
                ) as T
            }
        }
    }

    val currentlyPlayingIndex = MutableStateFlow<Int?>(null)

    fun onPlayClick(playbackPosition: Long, itemIndex: Int, updateVoiceNotes: (Long, Int) -> Unit) {
        when (currentlyPlayingIndex.value) {
            // If the index of the currently playing item is null, it means nothing is actually played and we want to start playing the audio file of the given item index
            null -> currentlyPlayingIndex.value = itemIndex
            // If the index of the currently playing item is the same as the item index, we want to stop it
            itemIndex -> {
                currentlyPlayingIndex.value = null
                updateVoiceNotes(playbackPosition, itemIndex)
            }
            else -> {
                currentlyPlayingIndex.value = itemIndex
                updateVoiceNotes(playbackPosition, itemIndex)
            }
        }
    }

    fun setCurrentlyPlayingIndex(index: Int?) {
        currentlyPlayingIndex.update {
            index
        }
    }
}