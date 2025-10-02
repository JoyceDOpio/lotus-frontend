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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.circularplanner.ui.component.PopupDialog
import com.example.circularplanner.ui.navigation.RecordedActivity
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.DayUiState
import com.example.circularplanner.ui.viewmodel.VoiceNoteUiState
import com.example.circularplanner.utils.NoteModePopup
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun ActivityScreen(
    innerPadding: PaddingValues,
    context: Context,
    activityUiState: ActivityUiState,
    dayState: DayState,
    dayUiState: DayUiState,
    mainRecordedActivityUiState: ActivityUiState,
    subRecordedActivityUiState: ActivityUiState,
    stopwatchService: StopwatchService,
    clearMainRecordedActivity: () -> Unit,
    clearSubRecordedActivity: () -> Unit,
    onNavigateToTaskActivityComparison: () -> Unit,
    saveActivity: () -> Unit,
    saveDay: () -> Unit,
    saveVoiceNote: (VoiceNote) -> Unit,
//    saveRecordedActivity: (UUID) -> Unit,
    saveMainRecordedActivity: () -> Unit,
    saveSubRecordedActivity: () -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
//    setMainActivityNote: (String) -> Unit,
//    setSubActivityNote: (String) -> Unit,
    setActivityNote: (String) -> Unit,
    setActualActiveTimeEnd: (Time) -> Unit,
    setActualActiveTimeStart: (Time) -> Unit,
    setDayNote: (String) -> Unit,
    setMainRecordedActivityEndTime: (Time) -> Unit,
    setSubRecordedActivityEndTime: (Time) -> Unit,
    setMainRecordedActivityId: (UUID) -> Unit,
    setSubRecordedActivityId: (UUID) -> Unit,
//    setMainRecordedActivityNote: (String) -> Unit,
//    setSubRecordedActivityNote: (String) -> Unit,
    setMainRecordedActivityStartTime: (Time) -> Unit,
    setSubRecordedActivityStartTime: (Time) -> Unit,
    setMainRecordedActivityTitle: (String) -> Unit,
    setSubRecordedActivityTitle: (String) -> Unit,
    setRecordedActivityState: (RecordedActivity) -> Unit,
    startRecording: (String) -> Unit,
    stopRecording: () -> Unit,
//    removeVoiceNote: (VoiceNoteUiState) -> Unit,
) {
    val isMainActivityTimerRunning  = (mainRecordedActivityUiState.id != null)
    val isSubActivityTimerRunning  = (subRecordedActivityUiState.id != null)

    var showPopupWindow by remember { mutableStateOf(false) }
    var popupState by remember { mutableStateOf(NoteModePopup.Day) }

    Column(
        modifier = Modifier
//            .background(Color(0xffFEE7FA))
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.SpaceBetween
//        verticalArrangement = Arrangement.Top
        verticalArrangement = Arrangement.Center
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
//                .verticalScroll(rememberScrollState())
//                .fillMaxSize()
                .fillMaxWidth()
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
                        popupState = NoteModePopup.Day
                        showPopupWindow = true
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
                    ),
//                .clip(RoundedCornerShape(15.dp))
                dayState = dayState,
                recordedActivityUiState = mainRecordedActivityUiState,
                stopwatch = stopwatchService.mainActivityStopwatch,
//            stopwatchService = stopwatchService,
                clearRecordedActivity = clearMainRecordedActivity,
                onNavigateToActivityNoteEdit = {
                    setRecordedActivityState(RecordedActivity.Main)
                    popupState = NoteModePopup.Activity
                    showPopupWindow = true
               },
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
                saveRecordedActivity = saveMainRecordedActivity,
                saveDay = saveDay,
                saveVoiceNote = saveVoiceNote,
//                setActivityNote = setMainActivityNote,
                setActualActiveTimeEnd = setActualActiveTimeEnd,
                setActualActiveTimeStart = setActualActiveTimeStart,
                setRecordedActivityId = setMainRecordedActivityId,
//                setRecordedActivityNote = setMainRecordedActivityNote,
                setRecordedActivityTitle = setMainRecordedActivityTitle,
                setRecordedActivityStartTime = setMainRecordedActivityStartTime,
                setRecordedActivityEndTime = setMainRecordedActivityEndTime,
                startRecording = startRecording,
                stopRecording = stopRecording,
//                removeVoiceNote = removeVoiceNote
            )

            if (isSubActivityTimerRunning) {
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
                    recordedActivityUiState = subRecordedActivityUiState,
                    stopwatch = stopwatchService.subActivityStopwatch,
                    clearRecordedActivity = clearSubRecordedActivity,
                    onNavigateToActivityNoteEdit = {
                        setRecordedActivityState(RecordedActivity.Sub)
                        popupState = NoteModePopup.Activity
                        showPopupWindow = true
                    },
                    onStart = {},
                    onStop = {},
                    saveRecordedActivity = saveSubRecordedActivity,
                    saveDay = saveDay,
                    saveVoiceNote = saveVoiceNote,
//                    setActivityNote = setSubActivityNote,
                    setActualActiveTimeEnd = setActualActiveTimeEnd,
                    setActualActiveTimeStart = setActualActiveTimeStart,
                    setRecordedActivityId = setSubRecordedActivityId,
//                    setRecordedActivityNote = setSubRecordedActivityNote,
                    setRecordedActivityTitle = setSubRecordedActivityTitle,
                    setRecordedActivityStartTime = setSubRecordedActivityStartTime,
                    setRecordedActivityEndTime = setSubRecordedActivityEndTime,
                    startRecording = startRecording,
                    stopRecording = stopRecording,
//                    removeVoiceNote = removeVoiceNote
                )
            }
        }
    }

    if (showPopupWindow) {
        PopupDialog(
            onDismissRequest = {
                showPopupWindow = false
            }
        ) {
            when (popupState) {
                NoteModePopup.Activity -> {
                    ActivityNoteEditScreen(
                        activityUiState = activityUiState,
                        onBack = {
                            showPopupWindow = false
                        },
                        saveActivity = saveActivity,
                        setActivityNote = setActivityNote
                    )
                }
                NoteModePopup.Day -> {
                    DayNoteEditScreen(
                        dayUiState = dayUiState,
                        onBack = {
                            showPopupWindow = false
                        },
                        saveDay = saveDay,
                        setDayNote = setDayNote
                    )
                }
            }
        }
    }
}