package com.example.circularplanner.ui.component

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.example.circularplanner.service.Constants.ACTION_SERVICE_START
import com.example.circularplanner.service.Constants.ACTION_SERVICE_STOP
import com.example.circularplanner.service.ServiceHelper
import com.example.circularplanner.service.StopwatchService
import com.example.circularplanner.service.StopwatchState
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.DayUiState
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActivityRecorder(
    context: Context,
    dayState: DayState,
    recordedActivityUiState: ActivityUiState,
    stopwatchService: StopwatchService,
    clearRecordedActivity: () -> Unit,
    saveDay: () -> Unit,
    saveRecordedActivity: () -> Unit,
    saveVoiceNote: (VoiceNote) -> Unit,
    setActualActiveTimeEnd: (Time) -> Unit,
    setActualActiveTimeStart: (Time) -> Unit,
    setRecordedActivityEndTime: (Time) -> Unit,
    setRecordedActivityId: (UUID) -> Unit,
    setRecordedActivityNote: (String) -> Unit,
    setRecordedActivityStartTime: (Time) -> Unit,
    setRecordedActivityTitle: (String) -> Unit,
    startRecording: (String) -> Unit,
    stopRecording: () -> Unit,
    removeVoiceNote: (VoiceNoteUiState) -> Unit,
) {
    val recordedActivityDetails = recordedActivityUiState
    var isActivityTimerRunning  = (stopwatchService.currentState.value == StopwatchState.Started)
    var isRecordingVoiceNote by remember { mutableStateOf(false) }
    var timerStartVoiceNote by remember { mutableStateOf(0L) }
    var elapsedTimeVoiceNote by remember { mutableStateOf(0L) }

    val hours by stopwatchService.hours
    val minutes by stopwatchService.minutes
    val seconds by stopwatchService.seconds
    val currentState by stopwatchService.currentState

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val activityTimerAlpha by animateFloatAsState(
        targetValue = if (isActivityTimerRunning) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = ""
    )
    val voiceNoteTimerAlpha by animateFloatAsState(
        targetValue = if (isRecordingVoiceNote) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = ""
    )

//    val activityTimerAlpha = 1f
//    val voiceNoteTimerAlpha = 1f

    val permissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    var showPermissionRationale by remember { mutableStateOf(false) }

    val activeTimeStart: Time = dayState.activeTimeStart
    val activeTimeEnd: Time = dayState.activeTimeEnd

    Column(
        modifier = Modifier
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "NEW ACTIVITY",// TODO: Read text from string resource
            modifier = Modifier
                .padding(bottom = 10.dp),
            color = MaterialTheme.colorScheme.primary
        )

        // Activity title
        Row (
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = recordedActivityDetails.title,
                onValueChange = setRecordedActivityTitle,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .width(307.dp)
                    .height(55.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("Title") },//TODO: Read string from resource
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true,
                shape = RoundedCornerShape(15.dp)
            )

//            val iconButtonColor: Color by animateColorAsState(if (isActivityTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer)
            val iconButtonColor: Color = MaterialTheme.colorScheme.primary

            // Play/Stop button
            IconButton(
                modifier = Modifier
                    .width(55.dp)
                    // Aspect ratio = 1 makes the element as wide as it is tall
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        color = if (recordedActivityDetails.title != "") iconButtonColor else Color(0xffddd9e3)
                    ),
                onClick = {
                    ServiceHelper.triggerForegroundService(
                        context = context,
                        action = if (currentState == StopwatchState.Started) ACTION_SERVICE_STOP else ACTION_SERVICE_START
                    )
                    // Start the activity
                    if (!isActivityTimerRunning) {
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
                    }
                    // Finish the activity
                    else {
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
                    }

                    focusManager.clearFocus()
                },
                enabled = (recordedActivityDetails.title != "")
            ) {
                if (!isActivityTimerRunning) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.play_svgrepo_com),
                        contentDescription = "Play",
                        modifier = Modifier
                            .fillMaxSize(0.8f),
//                        tint = if (recordedActivityDetails.title != "") MaterialTheme.colorScheme.primary else Color(0xffbcb9c1)
                        tint = if (recordedActivityDetails.title != "") Color.White else Color(0xffbcb9c1)
//                        tint = if (recordedActivityDetails.title != "") Color(BOTTOM_BAR_TEXT_COLOR) else Color(0xffbcb9c1)
                    )
                }
                else {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.stop_svgrepo_com),
                        contentDescription = "Stop",
                        modifier = Modifier.fillMaxSize(0.6f),
                        tint = Color.White
//                        tint = Color(BOTTOM_BAR_TEXT_COLOR)
                    )
                }
            }
        }

        // Recorded activity's start time
        Column (
            modifier = Modifier
                .alpha(activityTimerAlpha)
                .padding(top = 5.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Recorded activity's timer
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.4f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var text = "%d:%02d".format(recordedActivityDetails.startTime.hour, recordedActivityDetails.startTime.minute)

                    Text(
                        text = text,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    text = "%02d:%02d:%02d".format(hours, minutes, seconds)

                    Text(
                        text = text,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(Modifier.weight(1f, true))

                // The voice note timer
                Row (
                    modifier = Modifier
                        .weight(2f)
                        .alpha(voiceNoteTimerAlpha),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AppearingDisappearingIcon(
                        modifier = Modifier
                            .weight(0.5f),
                        imageVectorResource = R.drawable.mic_svgrepo_com,
                        contentDescription = "Voice recorder",
                        tint = MaterialTheme.colorScheme.primary
//                        tint = Color(BOTTOM_BAR_TEXT_COLOR)
                    )

                    val text = (elapsedTimeVoiceNote).milliseconds.toComponents { hours, minutes, seconds, nanoseconds ->
                        "%02d:%02d".format(minutes, seconds)
                    }

                    Text(
                        text = text,
                        modifier = Modifier
                            .weight(1f),
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    )
                }

                val scope = rememberCoroutineScope()

                // The voice recording button
                Box (
                    modifier = Modifier
//                        .width(35.dp)
                        .width(55.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(15.dp))
//                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                        .background(color = MaterialTheme.colorScheme.primary)
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

                                        val duration = System.currentTimeMillis() - timestamp
                                        val voiceNote = VoiceNote(
                                            uri = filePath,
                                            timestamp = timestamp,
                                            id = voiceNoteId,
                                            duration = duration,
                                            activityId = recordedActivityUiState.id!!
                                        )

                                        saveVoiceNote(voiceNote)
                                        // Reset the voice note timer
                                        elapsedTimeVoiceNote = 0L
                                    }
                                } else {
                                    if (permissionState.status.shouldShowRationale) {
                                        showPermissionRationale = true
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Log.i("fileList", context.fileList().joinToString())

                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.mic_svgrepo_com),
                        contentDescription = "Voice recorder",
                        modifier = Modifier.fillMaxSize(0.7F),
//                        tint = MaterialTheme.colorScheme.primary
//                        tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        tint = Color.White
                    )
                }
            }

//            Row (
//                modifier = Modifier
//                    .padding(
//                        vertical = 5.dp
//                    ),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Activity note
//                OutlinedTextField(
//                    value = recordedActivityDetails.note,
//                    onValueChange = setRecordedActivityNote,
//                    modifier = Modifier
//                        .width(325.dp)
//                        .height(190.dp)
//                        .focusRequester(focusRequester),
//                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
//                    textStyle = TextStyle(
//                        fontSize = 16.sp,
//                        color = Color(HOUR_LABEL_COLOR)
//                    ),
//                    label = { Text("Activity Notes") },
//                    singleLine = false,
//                    shape = RoundedCornerShape(15.dp)
//                )
//
//                Spacer(Modifier.width(5.dp))
//
//                Column (
//                    horizontalAlignment = Alignment.End,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    // Save button
//                    IconButton(
//                        onClick = {
//                            saveRecordedActivity()
//                            focusManager.clearFocus()
//                        }
//                    ) {
//                        Icon(
//                            imageVector = ImageVector.vectorResource(id = R.drawable.save_alt_svgrepo_com),
//                            contentDescription = "Save activity note",
//                            modifier = Modifier
//                                .fillMaxSize(0.8f)
//                            ,
//                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
//                        )
//                    }
//                }
//            }
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