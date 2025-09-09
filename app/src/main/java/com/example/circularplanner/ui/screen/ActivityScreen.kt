package com.example.circularplanner.ui.screen

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.circularplanner.R
import com.example.circularplanner.data.Time
import com.example.circularplanner.data.VoiceNote
import com.example.circularplanner.service.Constants.ACTION_SERVICE_CANCEL
import com.example.circularplanner.service.Constants.ACTION_SERVICE_START
import com.example.circularplanner.service.Constants.ACTION_SERVICE_STOP
import com.example.circularplanner.service.ServiceHelper
import com.example.circularplanner.service.StopwatchService
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
    recordedActivityUiState: ActivityUiState,
    stopwatchService: StopwatchService,
    clearRecordedActivity: () -> Unit,
    onNavigateToActivityNoteEdit: () -> Unit,
    onNavigateToDayNoteEdit: () -> Unit,
    onNavigateToTaskActivityComparison: () -> Unit,
    saveDay: () -> Unit,
    saveVoiceNote: (VoiceNote) -> Unit,
    saveRecordedActivity: () -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
    setActivityNote: (String) -> Unit,
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
//            .background(Color(0xffFEE7FA))
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.SpaceBetween
        verticalArrangement = Arrangement.Top
    ) {
        ActivityGraph(
            dayState = dayState,
            drawClockHand = true,
            onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
            selectActivity = selectActivity,
            selectTask = selectTask
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // Day notes
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = 5.dp
                    )
                    .padding(
                        top = 10.dp,
                        bottom = 5.dp
                    )
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(15.dp)
                    )
                ,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAY NOTES",// TODO: Read text from string resource
                    modifier = Modifier
                        .padding(
                            horizontal = 10.dp
                        )
                    ,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = {
                        onNavigateToDayNoteEdit()
                    }
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.add_square_svgrepo_com),
                        contentDescription = "Add",
                        modifier = Modifier.fillMaxSize(0.8F),
//                    tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Main activity
            ActivityRecorder(
                context = context,
                modifier = Modifier
                    .padding(
                        horizontal = 5.dp,
                        vertical = 5.dp
                    )
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(15.dp)
                    )
//                .clip(RoundedCornerShape(15.dp))
                ,
                dayState = dayState,
                recordedActivityUiState = recordedActivityUiState,
                stopwatch = stopwatchService.mainActivityStopwatch,
//            stopwatchService = stopwatchService,
                clearRecordedActivity = clearRecordedActivity,
                onNavigateToActivityNoteEdit = onNavigateToActivityNoteEdit,
                onPause = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = ACTION_SERVICE_STOP
                    )
                },
                onResume = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = ACTION_SERVICE_START
                    )
                },
                onStart = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = ACTION_SERVICE_START
                    )
                },
                onStop = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = ACTION_SERVICE_CANCEL
                    )
                },
                saveRecordedActivity = saveRecordedActivity,
                saveDay = saveDay,
                saveVoiceNote = saveVoiceNote,
                setActivityNote = setActivityNote,
                setActualActiveTimeEnd = setActualActiveTimeEnd,
                setActualActiveTimeStart = setActualActiveTimeStart,
                setRecordedActivityId = setRecordedActivityId,
                setRecordedActivityNote = setRecordedActivityNote,
                setRecordedActivityTitle = setRecordedActivityTitle,
                setRecordedActivityStartTime = setRecordedActivityStartTime,
                setRecordedActivityEndTime = setRecordedActivityEndTime,
                startRecording = startRecording,
                stopRecording = stopRecording,
                removeVoiceNote = removeVoiceNote
            )


            // Sub-activity
            ActivityRecorder(
                context = context,
                header = "SUB-ACTIVITY",// TODO: Read text from string resource
                modifier = Modifier
                    .padding(
                        horizontal = 5.dp,
                        vertical = 5.dp
                    )
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(15.dp)
                    )
                ,
                title = "Break",// TODO: Read text from string resource
                dayState = dayState,
                recordedActivityUiState = recordedActivityUiState,
                stopwatch = stopwatchService.subActivityStopwatch,
                clearRecordedActivity = clearRecordedActivity,
                onNavigateToActivityNoteEdit = onNavigateToActivityNoteEdit,
                onStart = {},
                onStop = {},
                saveRecordedActivity = saveRecordedActivity,
                saveDay = saveDay,
                saveVoiceNote = saveVoiceNote,
                setActivityNote = setActivityNote,
                setActualActiveTimeEnd = setActualActiveTimeEnd,
                setActualActiveTimeStart = setActualActiveTimeStart,
                setRecordedActivityId = setRecordedActivityId,
                setRecordedActivityNote = setRecordedActivityNote,
                setRecordedActivityTitle = setRecordedActivityTitle,
                setRecordedActivityStartTime = setRecordedActivityStartTime,
                setRecordedActivityEndTime = setRecordedActivityEndTime,
                startRecording = startRecording,
                stopRecording = stopRecording,
                removeVoiceNote = removeVoiceNote
            )
        }
    }
}