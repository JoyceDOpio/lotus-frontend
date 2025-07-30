package com.example.circularplanner.ui.screen

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.component.ActivityGraph
import com.example.circularplanner.ui.component.ActivityRecorder
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.VoiceNoteUiState
import java.util.UUID

enum class Type {
    ACTIVITY,
    TASK
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun ActivityScreen(
    innerPadding: PaddingValues,
    context: Context,
    dayState: DayState,
    recordedActivityState: ActivityUiState,
    addVoiceNoteToActivity: (VoiceNoteUiState) -> Unit,
    clearRecordedActivity: () -> Unit,
    onNavigateToTaskActivityComparison: () -> Unit,
    setActivityId: (UUID) -> Unit,
    setActivityTitle: (String) -> Unit,
    saveActivity: () -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
    setActivityStartTime: (Time) -> Unit,
    setActivityEndTime: (Time) -> Unit,
    setIsTimerRunning: (Boolean) -> Unit,
    startRecording: (String) -> Unit,
    stopRecording: () -> Unit,
    removeVoiceNote: (VoiceNoteUiState) -> Unit,
    updateLastPlayedPosition: (Long, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ActivityGraph(
            dayState = dayState,
            onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
            selectActivity = selectActivity,
            selectTask = selectTask
        )

        ActivityRecorder(
            context = context,
            recordedActivityState = recordedActivityState,
            addVoiceNoteToActivity = addVoiceNoteToActivity,
            clearRecordedActivity = clearRecordedActivity,
            setActivityId = setActivityId,
            setActivityTitle = setActivityTitle,
            saveActivity = saveActivity,
            setActivityStartTime = setActivityStartTime,
            setActivityEndTime = setActivityEndTime,
            setIsTimerRunning = setIsTimerRunning,
            startRecording = startRecording,
            stopRecording = stopRecording,
            removeVoiceNote = removeVoiceNote,
            updateLastPlayedPosition = updateLastPlayedPosition
        )
    }
}