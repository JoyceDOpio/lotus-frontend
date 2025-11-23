package com.example.circularplanner.ui.component

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.circularplanner.R
import com.example.circularplanner.data.Time
import com.example.circularplanner.data.VoiceNote
import com.example.circularplanner.service.Constants.PAUSE
import com.example.circularplanner.service.Constants.RESUME
import com.example.circularplanner.service.Constants.START
import com.example.circularplanner.service.Constants.STOP
import com.example.circularplanner.service.ServiceHelper
import com.example.circularplanner.service.StopwatchService
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.DayState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

enum class ActivityState {
    Idle,
    Started
}

enum class ButtonState {
    Playing,
    Paused
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ActivityRecorder(
    context: Context,
    modifier: Modifier = Modifier,
    dayState: DayState,
    recordedMainActivityUiState: ActivityUiState,
    recordedSubActivityUiState: ActivityUiState,
    stopwatchService: StopwatchService,
    clearRecordedMainActivity: () -> Unit,
    clearRecordedSubActivity: () -> Unit,
    onNavigateToActivityNoteEdit: () -> Unit,
    saveDay: () -> Unit,
    saveRecordedMainActivity: () -> Unit,
    saveRecordedSubActivity: () -> Unit,
    saveVoiceNote: (VoiceNote) -> Unit,
    setActualActiveTimeEnd: (Time) -> Unit,
    setActualActiveTimeStart: (Time) -> Unit,
    setRecordedMainActivityEndTime: (Time) -> Unit,
    setRecordedSubActivityEndTime: (Time) -> Unit,
    setRecordedMainActivityId: (UUID) -> Unit,
    setRecordedSubActivityId: (UUID) -> Unit,
    setRecordedSubActivityMainActivityId: (UUID) -> Unit,
    setRecordedMainActivityStartTime: (Time) -> Unit,
    setRecordedSubActivityStartTime: (Time) -> Unit,
    setRecordedMainActivityTitle: (String) -> Unit,
    setRecordedSubActivityTitle: (String) -> Unit,
    startRecording: (String) -> Unit,
    stopRecording: () -> Unit
) {
    val mainActivityHeader = "ACTIVITY"// TODO: Read text from string resource
    val subActivityHeader = "SUB-ACTIVITY"// TODO: Read text from string resource
    val recordedMainActivityDetails = recordedMainActivityUiState
    val recordedSubActivityDetails = recordedSubActivityUiState
    val isMainActivityTimerRunning  = (recordedMainActivityUiState.id != null)
    val isSubActivityTimerRunning  = (recordedSubActivityUiState.id != null)

    var isRecordingVoiceNote by remember { mutableStateOf(false) }
    var timerStartVoiceNote by remember { mutableLongStateOf(0L) }
    var elapsedTimeVoiceNote by remember { mutableLongStateOf(0L) }

    val hours by stopwatchService.hours
    val minutes by stopwatchService.minutes
    val seconds by stopwatchService.seconds
    val subActivityHours by stopwatchService.subHours
    val subActivityMinutes by stopwatchService.subMinutes
    val subActivitySeconds by stopwatchService.subSeconds

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val voiceNoteTimerAlpha by animateFloatAsState(
        targetValue = if (isRecordingVoiceNote) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = ""
    )

    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )
    var showPermissionRationale by remember { mutableStateOf(false) }

    val activeTimeStart: Time = dayState.activeTimeStart
    val activeTimeEnd: Time = dayState.activeTimeEnd
    val initialState: ActivityState = if (isMainActivityTimerRunning) {
        ActivityState.Started
    } else {
        ActivityState.Idle
    }
    var activityState by remember { mutableStateOf(initialState) }

    var showMainActivityTimer by remember { mutableStateOf(isMainActivityTimerRunning) }
    var showSubActivity by remember { mutableStateOf(isSubActivityTimerRunning) }
    val showMainActivityVoiceRecorder = !showSubActivity

    // Disable the start/stop button, if title value is empty
    // When I use isMainActivityButtonEnabled by remember { mutableStateOf(recordedMainActivityUiState.title != "") }, the value of isMainActivityButtonEnabled is not updated when the recordedMainActivityUiState.title changes
    val isMainActivityButtonEnabled  = recordedMainActivityUiState.title != ""
    var isSubActivityButtonEnabled = recordedSubActivityUiState.title != ""
    var pauseButtonState = if (isSubActivityTimerRunning) ButtonState.Paused else ButtonState.Playing

    fun startSubActivity(mainActivityId: UUID) {
        // Create the sub-activity
        val activityStartTime = Time(
            LocalDateTime.now().hour,
            LocalDateTime.now().minute
        )

        setRecordedSubActivityStartTime(activityStartTime)

        val activityId = UUID.randomUUID()

        setRecordedSubActivityId(activityId)
        setRecordedSubActivityMainActivityId(mainActivityId)
        saveRecordedSubActivity()

        showSubActivity = true
    }

    fun stopSubActivity() {
        // Finish the activity
        val activityEndTime = Time(
            LocalDateTime.now().hour,
            LocalDateTime.now().minute
        )

        setRecordedSubActivityEndTime(activityEndTime)
        // Update the activity
        saveRecordedSubActivity()
        // Clear the recorded activity state
        clearRecordedSubActivity()

        // If the actual active time end is later than the planned active time end
        if (activityEndTime.compareTo(activeTimeEnd) == 1) {
            setActualActiveTimeEnd(activityEndTime)
            saveDay()
        }

        showSubActivity = false
    }

    fun pauseMainActivity() {
        startSubActivity(recordedMainActivityDetails.id!!)
        // Pause the main activity timer and start the sub-activity timer
        ServiceHelper.triggerForegroundService(
            context = context,
            action = PAUSE
        )
        pauseButtonState = ButtonState.Paused
    }

    fun resumeMainActivity() {
        stopSubActivity()
        // Stop the sub-activity's timer in the Service and resume the main activity timer
        ServiceHelper.triggerForegroundService(
            context = context,
            action = RESUME
        )
        pauseButtonState = ButtonState.Playing
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
        ,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main activity
        Column () {
            Row(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(13.dp, 13.dp, 0.dp, 0.dp))
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                ,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mainActivityHeader,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                    ,
                    color = Color(0xffffffff),
                    fontWeight = FontWeight.Normal
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMainActivityTimerRunning) {
                        // Play/Pause button
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()

                                when (pauseButtonState) {
                                    ButtonState.Playing -> {
                                        // Pause this activity and start a sub-activity
                                        pauseMainActivity()
                                    }
                                    ButtonState.Paused -> {
                                        // Resume this activity and stop the sub-activity
                                        resumeMainActivity()
                                    }
                                }
                            },
                            enabled = isSubActivityButtonEnabled
                        ) {
                            Icon(
                                imageVector = when (pauseButtonState) {
                                    ButtonState.Playing -> ImageVector.vectorResource(id = R.drawable.pause_svgrepo_com)
                                    ButtonState.Paused -> ImageVector.vectorResource(id = R.drawable.play_svgrepo_com)
                                },
                                contentDescription = when (pauseButtonState) {
                                    ButtonState.Playing -> "Pause activity"
                                    ButtonState.Paused -> "Resume activity"
                                },
                                modifier = Modifier.fillMaxSize(0.65F),
                                tint = if (pauseButtonState == ButtonState.Playing) Color(0xffffffff) else (if (isSubActivityButtonEnabled) Color(0xffffffff) else Color(0xff9A89C7))
                            )
                        }
                    }

                    // Main button (the stop button)
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()

                            when (activityState) {
                                ActivityState.Idle -> {
                                    // Start the main activity
                                    val activityStartTime = Time(
                                        LocalDateTime.now().hour,
                                        LocalDateTime.now().minute
                                    )

                                    setRecordedMainActivityStartTime(activityStartTime)
                                    // If the actual active time start is earlier than the planned active time start
                                    if (activityStartTime.compareTo(activeTimeStart) == -1) {
                                        setActualActiveTimeStart(activityStartTime)
                                        saveDay()
                                    }

                                    val activityId = UUID.randomUUID()

                                    setRecordedMainActivityId(activityId)
                                    saveRecordedMainActivity()

                                    activityState = ActivityState.Started
                                    showMainActivityTimer = true

                                    // Start the ForegroundService
                                    ServiceHelper.triggerForegroundService(
                                        context = context,
                                        action = START
                                    )
                                }
                                ActivityState.Started -> {
                                    // Finish the sub-activity
                                    if (isSubActivityTimerRunning) stopSubActivity()

                                    // Finish the activity
                                    val activityEndTime = Time(
                                        LocalDateTime.now().hour,
                                        LocalDateTime.now().minute
                                    )

                                    setRecordedMainActivityEndTime(activityEndTime)
                                    // Update the activity
                                    saveRecordedMainActivity()
                                    // Clear the recorded activity state
                                    clearRecordedMainActivity()

                                    // If the actual active time end is later than the planned active time end
                                    if (activityEndTime.compareTo(activeTimeEnd) == 1) {
                                        setActualActiveTimeEnd(activityEndTime)
                                        saveDay()
                                    }

                                    activityState = ActivityState.Idle
                                    showMainActivityTimer = false

                                    // Stop the ForegroundService
                                    ServiceHelper.triggerForegroundService(
                                        context = context,
                                        action = STOP
                                    )
                                }
                            }
                        },
                        enabled = isMainActivityButtonEnabled
                    ) {
                        Icon(
                            imageVector = when (activityState) {
                                ActivityState.Idle -> ImageVector.vectorResource(id = R.drawable.play_svgrepo_com)
                                ActivityState.Started -> ImageVector.vectorResource(id = R.drawable.stop_svgrepo_com)
                            },
                            contentDescription = when (activityState) {
                                ActivityState.Idle -> "Start activity"//TODO: Read string from resource
                                ActivityState.Started -> "Stop activity"//TODO: Read string from resource
                            },
                            modifier = Modifier.fillMaxSize(
                                when (activityState) {
                                    ActivityState.Started -> 0.7F
                                    ActivityState.Idle -> 0.8f
                                }
                            ),
                            tint = if (isMainActivityButtonEnabled) Color(0xffffffff) else Color(0xff9A89C7)
                        )
                    }
                }
            }

            Column (
                modifier = Modifier
                    .padding(
                        start = 1.dp
                    )
                    .fillMaxWidth()
                    .leftBorder(
                        color = MaterialTheme.colorScheme.primary,
                        width = 5f
                    )
                ,
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = recordedMainActivityDetails.title,
                        onValueChange = { value ->
                            if (isMainActivityTimerRunning) {
                                // Save every change in the title value to the activity
                                setRecordedMainActivityTitle(value)
                                saveRecordedMainActivity()
                            } else {
                                setRecordedMainActivityTitle(value)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                        ,
                        placeholder = { Text("Title") },//TODO: Read string from resource
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        singleLine = true,
                        shape = RoundedCornerShape(6.dp),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            unfocusedPlaceholderColor = Color(0xFF928BA2),
                            focusedPlaceholderColor = Color(0xFF928BA2),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                AnimatedVisibility(
                    visible = showMainActivityTimer
                ) {
                    Column () {
                        // Recorded activity's timer
                        Row(
                            modifier = Modifier
                                .padding(
                                    start = 10.dp,
                                    end = 6.dp
                                )
                                .height(35.dp)
                                .fillMaxWidth()
                            ,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            var text = "%d:%02d".format(
                                recordedMainActivityDetails.startTime.hour,
                                recordedMainActivityDetails.startTime.minute
                            )

                            Text(
                                text = text,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(Modifier.width(15.dp))

                            text = "%02d:%02d:%02d".format(hours, minutes, seconds)

                            Text(
                                text = text,
                                fontWeight = FontWeight.Normal,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                            ,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Main activity notes
                            IconButton(
                                onClick = {
                                    onNavigateToActivityNoteEdit()
                                }
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.notes_svgrepo_com),
                                    contentDescription = "Edit main activity notes",
                                    modifier = Modifier.fillMaxSize(0.7F),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Show main activity voice recorder only when the main activity is running
                            AnimatedVisibility(
                                visible = showMainActivityVoiceRecorder
                            ) {
                                Row (
                                    modifier = Modifier
                                    ,
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    // The voice note timer
                                    Row(
                                        modifier = Modifier
                                            .alpha(voiceNoteTimerAlpha)
                                        ,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AppearingDisappearingIcon(
                                            modifier = Modifier
                                                .width(35.dp)
                                            ,
                                            imageVectorResource = R.drawable.recording_02_svgrepo_com,
                                            contentDescription = "Voice recorder",
                                            tint = MaterialTheme.colorScheme.primary
                                        )

                                        val text =
                                            (elapsedTimeVoiceNote).milliseconds.toComponents { hours, minutes, seconds, nanoseconds ->
                                                "%02d:%02d".format(minutes, seconds)
                                            }

                                        Spacer(Modifier.width(5.dp))

                                        Text(
                                            text = text,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Spacer(Modifier.width(20.dp))

                                    val scope = rememberCoroutineScope()

                                    // The voice recording button
                                    Box(// FIXME: This button doesn't work well
                                        modifier = Modifier
                                            .width(30.dp)
                                            .pointerInput(
                                                // If I put the voiceNoteUiState into the pointerInput(), the first down gesture is not consumed. But the voice note UI state is still not updated
                                                Unit
                                            ) {
                                                awaitEachGesture {
                                                    val initialPress =
                                                        awaitFirstDown(requireUnconsumed = true).also { it.consume() }

                                                    // Check if permission for audio recording is granted
                                                    if (permissionState.status.isGranted) {
                                                        // Create file name
                                                        val date = LocalDate.now()
                                                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                                        val timestamp = System.currentTimeMillis()
                                                        val voiceNoteId = UUID.randomUUID()
                                                        val fileName =
                                                            date + "_" + voiceNoteId.toString()
                                                        var filePath = ""

                                                        timerStartVoiceNote = timestamp

                                                        // Start recording the voice note. Meanwhile, save it's id, path, timestamp and activity id to the voice note UI state
                                                        val externalStorageVolumes =
                                                            ContextCompat.getExternalFilesDirs(
                                                                context,
                                                                null
                                                            )

                                                        if (externalStorageVolumes.size > 0) {
                                                            val directory = externalStorageVolumes[0]
                                                            filePath =
                                                                directory.absolutePath + "/$fileName" + ".mp3"// FIXME: Z jakiegoś powodu nie mogę stworzyć foldera

                                                            startRecording(filePath)
                                                            isRecordingVoiceNote = !isRecordingVoiceNote
                                                        }

                                                        val onPressCoroutineJob = scope.launch {
                                                            while (initialPress.pressed) {
                                                                delay(1000)
                                                                elapsedTimeVoiceNote =
                                                                    System.currentTimeMillis() - timerStartVoiceNote
                                                            }
                                                        }
                                                        val up = waitForUpOrCancellation()

                                                        if (up != null) {
                                                            onPressCoroutineJob.cancel()
                                                            // Once the finger is lifted, stop recording, create a VoiceNote object and add it to the voice note list of the activity UI state
                                                            stopRecording()
                                                            // Set variable to false
                                                            isRecordingVoiceNote = !isRecordingVoiceNote

                                                            val duration =
                                                                System.currentTimeMillis() - timestamp
                                                            val voiceNote = VoiceNote(
                                                                uri = filePath,
                                                                timestamp = timestamp,
                                                                id = voiceNoteId,
                                                                duration = duration,
                                                                activityId = recordedMainActivityUiState.id!!
                                                            )

                                                            saveVoiceNote(voiceNote)
                                                            // Reset the voice note timer
                                                            scope.launch {
                                                                // Delay resetting so that the user doesn't see it
                                                                delay(1000)
                                                                elapsedTimeVoiceNote = 0L
                                                            }
                                                        }
                                                    } else {
                                                        if (permissionState.status.shouldShowRationale) {
                                                            showPermissionRationale = true
                                                        } else {
                                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        Log.i("fileList", context.fileList().joinToString())

                                        Icon(
                                            imageVector = ImageVector.vectorResource(id = R.drawable.user_speak_svgrepo_com),
                                            contentDescription = "Voice recorder",
                                            modifier = Modifier.fillMaxSize(),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sub-activity
        AnimatedVisibility(
            visible = showSubActivity
        ) {
            Column (
                modifier = Modifier
                    .padding(top = 15.dp)
            ) {
                // Sub-activity header
                Row(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(13.dp, 13.dp, 0.dp, 0.dp))
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                    ,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subActivityHeader,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                        ,
                        color = Color(0xffffffff),
                        fontWeight = FontWeight.Light,
                    )

                    // Main button (the stop button)
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            resumeMainActivity()
                        },
                        enabled = isSubActivityButtonEnabled
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.stop_svgrepo_com),
                            contentDescription = "Stop sub-activity", //TODO: Read string from resource
                            modifier = Modifier.fillMaxSize(0.7F),
                            tint = if (isSubActivityButtonEnabled) Color(0xffffffff) else Color(0xff9A89C7)
                        )
                    }
                }

                Column (
                    modifier = Modifier
                        .padding(
                            start = 1.dp
                        )
                        .fillMaxWidth()
                        .leftBorder(
                            color = MaterialTheme.colorScheme.primary,
                            width = 5f
                        )
                    ,
                ) {
                    // Activity title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = recordedSubActivityDetails.title,
                            onValueChange = { value ->
                                // If the title value is empty, disable the possibility to stop the activity
                                // Disable the stop button, if title value is empty
                                isSubActivityButtonEnabled = value != ""

                                // Save every change in the title value to the activity
                                setRecordedSubActivityTitle(value)
                                saveRecordedSubActivity()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            placeholder = { Text("Title") },//TODO: Read string from resource
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp),
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                unfocusedPlaceholderColor = Color(0xFF928BA2),
                                focusedPlaceholderColor = Color(0xFF928BA2),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    // Recorded activity's timer
                    Row(
                        modifier = Modifier
                            .padding(
                                start = 10.dp,
                                end = 6.dp
                            )
                            .height(35.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        var text = "%d:%02d".format(
                            recordedSubActivityDetails.startTime.hour,
                            recordedSubActivityDetails.startTime.minute
                        )

                        Text(
                            text = text,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.width(15.dp))

                        text = "%02d:%02d:%02d".format(subActivityHours, subActivityMinutes, subActivitySeconds)

                        Text(
                            text = text,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                        ,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Sub-activity notes
                        IconButton(
                            onClick = {
                                onNavigateToActivityNoteEdit()
                            }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.notes_svgrepo_com),
                                contentDescription = "Edit sub-activity notes",
                                modifier = Modifier.fillMaxSize(0.7F),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            // The voice note timer
                            Row(
                                modifier = Modifier
                                    .alpha(voiceNoteTimerAlpha),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AppearingDisappearingIcon(
                                    modifier = Modifier
                                        .width(35.dp),
                                    imageVectorResource = R.drawable.recording_02_svgrepo_com,
                                    contentDescription = "Voice recorder",
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                val text =
                                    (elapsedTimeVoiceNote).milliseconds.toComponents { hours, minutes, seconds, nanoseconds ->
                                        "%02d:%02d".format(minutes, seconds)
                                    }

                                Spacer(Modifier.width(5.dp))

                                Text(
                                    text = text,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(Modifier.width(20.dp))

                            val scope = rememberCoroutineScope()

                            // The voice recording button
                            Box(// FIXME: This button doesn't work well
                                modifier = Modifier
                                    .width(30.dp)
                                    .pointerInput(
                                        // If I put the voiceNoteUiState into the pointerInput(), the first down gesture is not consumed. But the voice note UI state is still not updated
                                        Unit
                                    ) {
                                        awaitEachGesture {
                                            val initialPress =
                                                awaitFirstDown(requireUnconsumed = true).also { it.consume() }

                                            // Check if permission for audio recording is granted
                                            if (permissionState.status.isGranted) {
                                                // Create file name
                                                val date = LocalDate.now()
                                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                                val timestamp = System.currentTimeMillis()
                                                val voiceNoteId = UUID.randomUUID()
                                                val fileName =
                                                    date + "_" + voiceNoteId.toString()
                                                var filePath = ""

                                                timerStartVoiceNote = timestamp

                                                // Start recording the voice note. Meanwhile, save it's id, path, timestamp and activity id to the voice note UI state
                                                val externalStorageVolumes =
                                                    ContextCompat.getExternalFilesDirs(
                                                        context,
                                                        null
                                                    )

                                                if (externalStorageVolumes.size > 0) {
                                                    val directory = externalStorageVolumes[0]
                                                    filePath =
                                                        directory.absolutePath + "/$fileName" + ".mp3"// FIXME: I can't create a folder

                                                    startRecording(filePath)
                                                    isRecordingVoiceNote = !isRecordingVoiceNote
                                                }

                                                val onPressCoroutineJob = scope.launch {
                                                    while (initialPress.pressed) {
                                                        delay(1000)
                                                        elapsedTimeVoiceNote =
                                                            System.currentTimeMillis() - timerStartVoiceNote
                                                    }
                                                }
                                                val up = waitForUpOrCancellation()

                                                if (up != null) {
                                                    onPressCoroutineJob.cancel()
                                                    // Once the finger is lifted, stop recording, create a VoiceNote object and add it to the voice note list of the activity UI state
                                                    stopRecording()
                                                    // Set variable to false
                                                    isRecordingVoiceNote = !isRecordingVoiceNote

                                                    val duration =
                                                        System.currentTimeMillis() - timestamp
                                                    val voiceNote = VoiceNote(
                                                        uri = filePath,
                                                        timestamp = timestamp,
                                                        id = voiceNoteId,
                                                        duration = duration,
                                                        activityId = recordedSubActivityUiState.id!!
                                                    )

                                                    saveVoiceNote(voiceNote)
                                                    // Reset the voice note timer
                                                    scope.launch {
                                                        // Delay resetting so that the user doesn't see it
                                                        delay(1000)
                                                        elapsedTimeVoiceNote = 0L
                                                    }
                                                }
                                            } else {
                                                if (permissionState.status.shouldShowRationale) {
                                                    showPermissionRationale = true
                                                } else {
                                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                }
                                            }
                                        }
                                    }
                            ) {
                                Log.i("fileList", context.fileList().joinToString())

                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.user_speak_svgrepo_com),
                                    contentDescription = "Voice recorder",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPermissionRationale) {
        PermissionRationaleDialog(
            permissionTextProvider = AudioRecordPermissionTextProvider(),
            onDismiss = { showPermissionRationale = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRationaleDialog(
    permissionTextProvider: PermissionTextProvider,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("OK")
            }
        },
//        dismissButton = {
//            Button(
//                onClick = onDismiss
//            ) {
//                Text("OK")
//            }
//        },
        title = {
            Text(
                text = "Permission required"// TODO: Read text from string resource
            )
        },
        text = {
            Text(
                text = permissionTextProvider.getDescription()
            )
        },
        modifier = modifier
    )
}

interface PermissionTextProvider {
    fun getDescription(): String
}

class AudioRecordPermissionTextProvider: PermissionTextProvider {
    override fun getDescription(): String {
        return "This app needs access to your microphone so that you can record voice notes."// TODO: Read text from string resource
    }
}

fun Modifier.leftBorder(
    color: Color,
    width: Float,
    startY: Float = 0f,
    endY: Float = 0f
) = this.drawWithContent {
    drawContent()
    drawLine(
        color = color,
        start = Offset(0f, 0f + startY),
        end = Offset(0f, size.height - endY),
        strokeWidth = width,
    )
}