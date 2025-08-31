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
import com.example.circularplanner.data.VoiceNote
import com.example.circularplanner.service.StopwatchService
import com.example.circularplanner.ui.component.ActivityGraph
import com.example.circularplanner.ui.component.ActivityRecorder
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.DayUiState
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
    recordedActivityUiState: ActivityUiState,
    stopwatchService: StopwatchService,
    clearRecordedActivity: () -> Unit,
    onNavigateToTaskActivityComparison: () -> Unit,
    saveDay: () -> Unit,
    saveVoiceNote: (VoiceNote) -> Unit,
    saveRecordedActivity: () -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
    setActualActiveTimeEnd: (Time) -> Unit,
    setActualActiveTimeStart: (Time) -> Unit,
    setRecordedActivityId: (UUID) -> Unit,
    setRecordedActivityNote: (String) -> Unit,
    setRecordedActivityTitle: (String) -> Unit,
    setRecordedActivityEndTime: (Time) -> Unit,
    setRecordedActivityStartTime: (Time) -> Unit,
    startRecording: (String) -> Unit,
    stopRecording: () -> Unit,
    removeVoiceNote: (VoiceNoteUiState) -> Unit,
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
            drawClockHand = true,
            onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
            selectActivity = selectActivity,
            selectTask = selectTask
        )

        ActivityRecorder(
            context = context,
            dayState = dayState,
            recordedActivityUiState = recordedActivityUiState,
            stopwatchService = stopwatchService,
            clearRecordedActivity = clearRecordedActivity,
            saveRecordedActivity = saveRecordedActivity,
            saveDay = saveDay,
            saveVoiceNote = saveVoiceNote,
            setActualActiveTimeEnd = setActualActiveTimeEnd,
            setActualActiveTimeStart = setActualActiveTimeStart,
            setRecordedActivityId = setRecordedActivityId,
            setRecordedActivityNote = setRecordedActivityNote,
            setRecordedActivityTitle = setRecordedActivityTitle,
            setRecordedActivityStartTime = setRecordedActivityStartTime,
            setRecordedActivityEndTime = setRecordedActivityEndTime,
            startRecording = startRecording,
            stopRecording = stopRecording,
            removeVoiceNote = removeVoiceNote,
        )
    }
}