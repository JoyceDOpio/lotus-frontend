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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.example.circularplanner.service.Stopwatch
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.VoiceNoteUiState
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
    Ready,
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
    header: String = "ACTIVITY",// TODO: Read text from string resource
    stopwatch: Stopwatch,
    title: String = "",
    dayState: DayState,
    recordedActivityUiState: ActivityUiState,
//    stopwatchService: StopwatchService,
    clearRecordedActivity: () -> Unit,
    onNavigateToActivityNoteEdit: () -> Unit,
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onStart: () -> Unit,
    onStop: () -> Unit,
    saveDay: () -> Unit,
    saveRecordedActivity: () -> Unit,
    saveVoiceNote: (VoiceNote) -> Unit,
//    setActivityNote: (String) -> Unit,
    setActualActiveTimeEnd: (Time) -> Unit,
    setActualActiveTimeStart: (Time) -> Unit,
    setRecordedActivityEndTime: (Time) -> Unit,
    setRecordedActivityId: (UUID) -> Unit,
//    setRecordedActivityNote: (String) -> Unit,
    setRecordedActivityStartTime: (Time) -> Unit,
    setRecordedActivityTitle: (String) -> Unit,
    startRecording: (String) -> Unit,
    stopRecording: () -> Unit,
//    removeVoiceNote: (VoiceNoteUiState) -> Unit,
) {
    val recordedActivityDetails = recordedActivityUiState
//    var isActivityTimerRunning  = (stopwatchService.currentState.value == StopwatchState.Started)
    val isActivityTimerRunning  = (recordedActivityUiState.id != null)

    var isRecordingVoiceNote by remember { mutableStateOf(false) }
    var timerStartVoiceNote by remember { mutableStateOf(0L) }
    var elapsedTimeVoiceNote by remember { mutableStateOf(0L) }

//    val hours by stopwatchService.hours
//    val minutes by stopwatchService.minutes
//    val seconds by stopwatchService.seconds
//    val currentState by stopwatchService.currentState
    val hours by stopwatch.hours
    val minutes by stopwatch.minutes
    val seconds by stopwatch.seconds
    val currentState by stopwatch.currentState

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val voiceNoteTimerAlpha by animateFloatAsState(
        targetValue = if (isRecordingVoiceNote) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = ""
    )

//    val voiceNoteTimerAlpha = 1f

//    var showActivityTitleField  = true
//    var showActivityTimer = true

    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    var showPermissionRationale by remember { mutableStateOf(false) }

    val activeTimeStart: Time = dayState.activeTimeStart
    val activeTimeEnd: Time = dayState.activeTimeEnd
    val initialState: ActivityState = if (isActivityTimerRunning) {
        ActivityState.Started
    } else {
        if (recordedActivityDetails.title != "") {
            ActivityState.Ready
        } else {
            ActivityState.Idle
        }
    }
    var activityState by remember { mutableStateOf(initialState) }

    var showActivityTitleField by remember { mutableStateOf(recordedActivityDetails.title != "") }
    var showActivityTimer by remember { mutableStateOf(isActivityTimerRunning) }


    var isButtonEnabled by remember { mutableStateOf(true) }
    var pauseButtonState by remember { mutableStateOf(ButtonState.Playing) }

    LaunchedEffect(Unit) {
        if (recordedActivityDetails.title == "") setRecordedActivityTitle(title)
    }

    Column(
        modifier = modifier
            .background(Color(0xffffffff))
            .padding(bottom = if (showActivityTitleField) 15.dp else 0.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
        ,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (!isActivityTimerRunning) "NEW $header" else header,// TODO: Read text from string resource
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                ,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isActivityTimerRunning) {
                    // Play/Pause button
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()

                            when (pauseButtonState) {
                                ButtonState.Playing -> {
                                    // Pause this activity and start a sub-activity
                                    onPause()
                                }
                                ButtonState.Paused -> {
                                    // Resume this activity and stop the sub-activity
                                    onResume()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = when (pauseButtonState) {
                                ButtonState.Playing -> ImageVector.vectorResource(id = R.drawable.pause_svgrepo_com)
                                ButtonState.Paused -> ImageVector.vectorResource(id = R.drawable.play_svgrepo_com)
                            },
                            contentDescription = when (pauseButtonState) {
                                ButtonState.Playing -> "Pause activity"//TODO: Read string from resource
                                ButtonState.Paused -> "Resume activity"//TODO: Read string from resource
                            },
                            modifier = Modifier.fillMaxSize(0.7F),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Main button
                IconButton(
                    onClick = {
                        focusManager.clearFocus()

                        when (activityState) {
                            ActivityState.Idle -> showActivityTitleField = !showActivityTitleField
                            ActivityState.Ready -> {
                                // Start the ForegroundService
                                onStart()

                                // Start the activity
                                val activityStartTime = Time(
                                    LocalDateTime.now().hour,
                                    LocalDateTime.now().minute
                                )

                                setRecordedActivityStartTime(activityStartTime)
                                // If the actual active time start is earlier than the planned active time start
                                if (activityStartTime.compareTo(activeTimeStart) == -1) {
                                    setActualActiveTimeStart(activityStartTime)
                                    saveDay()
                                }

                                val activityId = UUID.randomUUID()

                                setRecordedActivityId(activityId)
                                saveRecordedActivity()

                                activityState = ActivityState.Started
                                showActivityTimer = true
                            }
                            ActivityState.Started -> {
                                // Cancel (stop) the ForegroundService
                                onStop()

                                // Finish the activity
                                val activityEndTime = Time(
                                    LocalDateTime.now().hour,
                                    LocalDateTime.now().minute
                                )

                                setRecordedActivityEndTime(activityEndTime)
                                // Update the activity
                                saveRecordedActivity()
                                // Clear the recorded activity state
                                clearRecordedActivity()

                                // If the actual active time end is later than the planned active time end
                                if (activityEndTime.compareTo(activeTimeEnd) == 1) {
                                    setActualActiveTimeEnd(activityEndTime)
                                    saveDay()
                                }

                                activityState = ActivityState.Idle
                                showActivityTitleField = false
                                showActivityTimer = false
                            }
                        }
                    },
                    enabled = isButtonEnabled
                ) {
                    Icon(
                        imageVector = when (activityState) {
                            ActivityState.Idle -> {
                                if (showActivityTitleField) {
                                    ImageVector.vectorResource(id = R.drawable.minus_square_svgrepo_com)
                                } else {
                                    ImageVector.vectorResource(id = R.drawable.add_square_svgrepo_com)
                                }
                            }
                            ActivityState.Ready -> ImageVector.vectorResource(id = R.drawable.play_svgrepo_com)
                            ActivityState.Started -> ImageVector.vectorResource(id = R.drawable.stop_svgrepo_com)
                        },
                        contentDescription = when (activityState) {
                            ActivityState.Idle -> "Add activity"//TODO: Read string from resource
                            ActivityState.Ready -> "Start activity"//TODO: Read string from resource
                            ActivityState.Started -> "Stop activity"//TODO: Read string from resource
                        },
                        modifier = Modifier.fillMaxSize(0.8F),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }


        Column (
            modifier = Modifier
                .padding(
                    start = 10.dp
                )
                .fillMaxWidth()
                .leftBorder(
                    color = MaterialTheme.colorScheme.primary,
                    width = 5f
                )
            ,
        ) {
            AnimatedVisibility(
                visible = showActivityTimer
            ) {
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Recorded activity's timer
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var text = "%d:%02d".format(
                            recordedActivityDetails.startTime.hour,
                            recordedActivityDetails.startTime.minute
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
                                            val fileName = date + "_" + voiceNoteId.toString()
                                            var filePath = ""

                                            timerStartVoiceNote = timestamp

                                            // Start recording the voice note. Meanwhile, save it's id, path, timestamp and activity id to the voice note UI state
                                            val externalStorageVolumes =
                                                ContextCompat.getExternalFilesDirs(context, null)

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
                                                    activityId = recordedActivityUiState.id!!
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

            // Activity title
            AnimatedVisibility(
                visible = showActivityTitleField
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = recordedActivityDetails.title,
                        onValueChange = { value ->
                            if (isActivityTimerRunning) {
                                // If the title value is empty, disable the possibility to stop the activity
                                // Disable the stop button, if title value is empty
                                isButtonEnabled = value != ""

                                // Save every change in the title value to the activity
                                setRecordedActivityTitle(value)
                                saveRecordedActivity()
                            } else {
                                activityState =
                                    if (value != "") ActivityState.Ready else ActivityState.Idle

                                setRecordedActivityTitle(value)
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
            }

            AnimatedVisibility(
                visible = showActivityTimer
            ) {
                // Activity notes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                    ,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NOTES",
                        modifier = Modifier
                            .padding(
                                horizontal = 10.dp
                            )
                        ,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = {
                            onNavigateToActivityNoteEdit()
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.add_square_svgrepo_com),
                            contentDescription = "Add activity note",
                            modifier = Modifier.fillMaxSize(0.8F),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

////                    OutlinedTextField(
//                    TextField(
//                        value = recordedActivityDetails.note,
////                        onValueChange = onSetDayNote,
//                        onValueChange = { value ->
//                            setActivityNote(value)
//                            saveRecordedActivity()
//                        },
//                        modifier = Modifier
////                            .width(325.dp)
//                            .padding(horizontal = 10.dp)
//                            .height(190.dp)
//                            .leftBorder(
//                                color = MaterialTheme.colorScheme.primary,
//                                width = 5f
//                            )
//                            .focusRequester(focusRequester)
//                        ,
//                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
//                        textStyle = TextStyle(
//                            fontSize = 16.sp,
////                            color = Color(HOUR_LABEL_COLOR)
////                            color = Color(BOTTOM_BAR_TEXT_COLOR)
////                            color = Color(CLOCK_LABEL_COLOR)
//                        ),
//                        label = { Text("ACTIVITY NOTES") },//TODO: Read string from resource
//                        singleLine = false,
//                        shape = RoundedCornerShape(15.dp),
//                        colors = TextFieldDefaults.textFieldColors(
//                            containerColor = Color.Transparent,
//                            unfocusedPlaceholderColor = Color(0xFF928BA2),
//                            focusedPlaceholderColor = Color(0xFF928BA2),
//                            focusedIndicatorColor = Color.Transparent,
//                            unfocusedIndicatorColor = Color.Transparent
//                        )
//                    )
                }
            }
        }




//        // Voice note list
//        VoiceNoteList(
//            activityUiState = recordedActivityUiState,
//            audioViewModel = audioViewModel,
//            removeVoiceNote = removeVoiceNote,
//            updateLastPlayedPosition = updateLastPlayedPosition
//        )
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