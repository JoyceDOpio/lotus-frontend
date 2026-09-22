package com.eternalfairy.lotus.view.screen.planner

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eternalfairy.lotus.view.component.ActiveTimeSetUp
import com.eternalfairy.lotus.view.component.ActivityRecorder
import com.eternalfairy.lotus.view.component.MultiWindowSizeLayout
import com.eternalfairy.lotus.view.component.PopupDialog
import com.eternalfairy.lotus.view.screen.ActivityEditMode
import com.eternalfairy.lotus.view.screen.ActivityEditScreen
import com.eternalfairy.lotus.view.screen.DeleteScreen
import com.eternalfairy.lotus.view.screen.DeleteType
import com.eternalfairy.lotus.view.screen.EditedActivityType
import com.eternalfairy.lotus.view.screen.GoalEditScreen
import com.eternalfairy.lotus.view.screen.TaskEditScreen
import com.eternalfairy.lotus.view.service.StopwatchService
import com.eternalfairy.lotus.view.theme.Teal74
import com.eternalfairy.lotus.view.theme.White
import com.eternalfairy.lotus.view.viewmodel.AudioViewModel
import com.eternalfairy.lotus.view.viewmodel.PlannerViewModel
import com.eternalfairy.lotus.view.viewmodel.StopwatchViewModel
import com.eternalfairy.lotus.view.viewmodel.utils.AudioRecorder
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@Serializable
enum class SmallScreenState {
    // Comparison of task and activity
    Comparison,
    // Day overview: tasks
    DayTask,
    // Day overview: activities
    DayActivity,
    Goal,
    MoveToCalendar,
    ToDo
}

const val SCREEN_STATE = "SCREEN_STATE"

enum class SmallScreenButtonState {
    // Day overview
    Day,
    Goal,
    ToDo
}

enum class BigScreenMainPanelState {
    // Comparison of task and activity
    Comparison,
    DayActivity,
    DayTask,
    MoveToCalendar
}

enum class BigScreenSidePanelState {
    Goal,
    ToDo
}

enum class PopupState {
    ActiveTimeSetup,
    ActivityRecorder,
    DeleteActivity,
    DeleteTask,
    DeleteVoiceNote,
    EditActivity,
    EditActivityNotes,
    EditGoal,
    EditTask
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun PlannerContainer(
    audioRecorder: AudioRecorder,
//    screenState: SmallScreenState?,
    stopwatchViewModel: StopwatchViewModel,
    plannerViewModel: PlannerViewModel,
    context: Context,
    stopwatchService: StopwatchService,
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state = plannerViewModel.state

    val isSubActivityTimerRunning  = (state.recordedActivitySub.id != null)
    var showPopupWindow by remember { mutableStateOf(false) }
    var popupState by remember { mutableStateOf(PopupState.EditTask) }
    val audioViewModel: AudioViewModel = viewModel(factory = AudioViewModel.Factory)

    // Banner ad
//    val adView = remember { AdView(context) }
    // Set the unique ID for this specific ad unit.
//    val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
//    adView.adUnitId = BANNER_AD_UNIT_ID
// Set a large anchored adaptive banner ad size with a given width.
//    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(LocalContext.current, 360)
//    adView.setAdSize(adSize)

//    var isLoading by remember { mutableStateOf((userInfo == null)) }
//    Log.i("PlannerContainer", "userInfo $userInfo")
//    Log.i("PlannerContainer", "isLoading $isLoading")

    MultiWindowSizeLayout(
        default = {},
        portraitPhone = {
            SmallScreenPortrait(
                audioViewModel = audioViewModel,
                plannerViewModel = plannerViewModel,
                context = context,
//                screenStateValue = screenState,
                onDeleteActivity = { id ->
                    // Pass the id of the main activity to mark it for deletion
                    plannerViewModel.onEvent(PlannerUiEvent.ActivityToBeDeletedIdChanged(id))

                    popupState = PopupState.DeleteActivity
                    showPopupWindow = true

                },
                onEditActivity = { id ->
                    // Pass the id of the main activity to mark it for editing
                    plannerViewModel.onEvent(PlannerUiEvent.ActivityToBeEditedIdChanged(id))

                    popupState = PopupState.EditActivity
                    showPopupWindow = true
                },
                onDeleteTask = {
                    popupState = PopupState.DeleteTask
                    showPopupWindow = true
                },
                onEditTask = {
                    popupState = PopupState.EditTask
                    showPopupWindow = true
                },
                onDeleteVoiceNote = { id ->
                    plannerViewModel.onEvent(PlannerUiEvent.VoiceNoteToBeDeletedIdChanged(id))

                    popupState = PopupState.DeleteVoiceNote
                    showPopupWindow = true
                },
                onPressActiveTime = {
                    popupState = PopupState.ActiveTimeSetup
                    showPopupWindow = true
                },
                onShowPopupWindow = { state ->
                    popupState = state
                    showPopupWindow = true
                }
            )
        },
        landscapePhone = {
            SmallScreenLandscape(
                audioViewModel = audioViewModel,
                plannerViewModel = plannerViewModel,
                context = context,
                onDeleteActivity = { id ->
                    // Pass the id of the main activity to mark it for deletion
                    //                    selectActivityToBeDeleted(id)
                    plannerViewModel.onEvent(PlannerUiEvent.ActivityToBeDeletedIdChanged(id))

                    popupState = PopupState.DeleteActivity
                    showPopupWindow = true

                },
                onEditActivity = { id ->
                    // Pass the id of the main activity to mark it for editing
                    plannerViewModel.onEvent(PlannerUiEvent.ActivityToBeEditedIdChanged(id))

                    popupState = PopupState.EditActivity
                    showPopupWindow = true
                },
                onDeleteTask = {
                    popupState = PopupState.DeleteTask
                    showPopupWindow = true
                },
                onEditTask = {
                    popupState = PopupState.EditTask
                    showPopupWindow = true
                },
                onDeleteVoiceNote = { id ->
                    plannerViewModel.onEvent(PlannerUiEvent.VoiceNoteToBeDeletedIdChanged(id))

                    popupState = PopupState.DeleteVoiceNote
                    showPopupWindow = true
                },
                onPressActiveTime = {
                    popupState = PopupState.ActiveTimeSetup
                    showPopupWindow = true
                },
                onShowPopupWindow = { state ->
                    popupState = state
                    showPopupWindow = true
                }
            )
        },
        portraitTablet = {
            BigScreenPortrait(
                audioViewModel = audioViewModel,
                plannerViewModel = plannerViewModel,
                context = context,
                onDeleteActivity = { id ->
                    // Pass the id of the main activity to mark it for deletion
                    plannerViewModel.onEvent(PlannerUiEvent.ActivityToBeDeletedIdChanged(id))

                    popupState = PopupState.DeleteActivity
                    showPopupWindow = true
                },
                onEditActivity = { id ->
                    // Pass the id of the main activity to mark it for editing
                    plannerViewModel.onEvent(PlannerUiEvent.ActivityToBeEditedIdChanged(id))

                    popupState = PopupState.EditActivity
                    showPopupWindow = true
                },
                onDeleteTask = {
                    popupState = PopupState.DeleteTask
                    showPopupWindow = true
                },
                onEditTask = {
                    popupState = PopupState.EditTask
                    showPopupWindow = true
                },
                onDeleteVoiceNote = { id ->
                    plannerViewModel.onEvent(PlannerUiEvent.VoiceNoteToBeDeletedIdChanged(id))

                    popupState = PopupState.DeleteVoiceNote
                    showPopupWindow = true
                },
                onPressActiveTime = {
                    popupState = PopupState.ActiveTimeSetup
                    showPopupWindow = true
                },
                onShowPopupWindow = { state ->
                    popupState = state
                    showPopupWindow = true
                }
            )
        },
        landscapeTablet = {
            BigScreenLandscape(
                audioViewModel = audioViewModel,
                plannerViewModel = plannerViewModel,
                context = context,
                onDeleteActivity = { id ->
                    // Pass the id of the main activity to mark it for deletion
                    plannerViewModel.onEvent(PlannerUiEvent.ActivityToBeDeletedIdChanged(id))

                    popupState = PopupState.DeleteActivity
                    showPopupWindow = true
                },
                onEditActivity = { id ->
                    // Pass the id of the main activity to mark it for editing
                    plannerViewModel.onEvent(PlannerUiEvent.ActivityToBeEditedIdChanged(id))

                    popupState = PopupState.EditActivity
                    showPopupWindow = true
                },
                onDeleteTask = {
                    popupState = PopupState.DeleteTask
                    showPopupWindow = true
                },
                onEditTask = {
                    popupState = PopupState.EditTask
                    showPopupWindow = true
                },
                onDeleteVoiceNote = { id ->
                    plannerViewModel.onEvent(PlannerUiEvent.VoiceNoteToBeDeletedIdChanged(id))

                    popupState = PopupState.DeleteVoiceNote
                    showPopupWindow = true
                },
                onPressActiveTime = {
                    popupState = PopupState.ActiveTimeSetup
                    showPopupWindow = true
                },
                onShowPopupWindow = { state ->
                    popupState = state
                    showPopupWindow = true
                }
            )
        }
    )

    if (showPopupWindow) {
        PopupDialog(
            onDismissRequest = {
                showPopupWindow = false
            }
        ) {
            when (popupState) {
                PopupState.ActiveTimeSetup -> {
                    // Show active time setup
                    ActiveTimeSetUp(
                        viewModel = plannerViewModel,
                        onBack = { showPopupWindow = false },
                        onClickSaveActiveTime = {
                            plannerViewModel.onEvent(PlannerUiEvent.SaveDay)
                            showPopupWindow = false
                        }
                    )
                }

                PopupState.ActivityRecorder -> {
                    ActivityRecorder(
                        audioRecorder = audioRecorder,
                        context = context,
                        modifier = Modifier
                            .padding(
                                horizontal = 5.dp,
                                vertical = 5.dp
                            )
                        ,
                        stopwatchService = stopwatchService,
                        plannerViewModel = plannerViewModel,
                        stopwatchViewModel = stopwatchViewModel,
                        onNavigateToActivityNoteEdit = {
                            popupState = PopupState.EditActivityNotes
                        }
                    )
                }

                PopupState.DeleteActivity -> {
                    DeleteScreen(
                        onBack = {
                        showPopupWindow = false
                    },
                        onDelete = {
                            plannerViewModel.onEvent(PlannerUiEvent.DeleteActivity)
                            showPopupWindow = false
                        },
                        deleteType = DeleteType.Activity
                    )
                }

                PopupState.DeleteTask -> {
                    DeleteScreen(onBack = {
                        showPopupWindow = false
                    }, onDelete = {
                        plannerViewModel.onEvent(PlannerUiEvent.DeleteTask)
                        showPopupWindow = false
                    })
                }

                PopupState.DeleteVoiceNote -> {
                    DeleteScreen(
                        onBack = {
                        showPopupWindow = false
                    },
                        onDelete = {
                            // FIXME
                            plannerViewModel.onEvent(PlannerUiEvent.DeleteVoiceNote)
                            showPopupWindow = false
                        },
                        deleteType = DeleteType.VoiceNote
                    )
                }

                PopupState.EditActivity -> {
                    ActivityEditScreen(
                        viewModel = plannerViewModel,
                        mode = ActivityEditMode.Full,
                        onBack = {
                            showPopupWindow = false
                        }
                    )
                }

                PopupState.EditActivityNotes -> {
                    ActivityEditScreen(
                        activityType = if (isSubActivityTimerRunning) EditedActivityType.Sub else EditedActivityType.Main,
                        viewModel = plannerViewModel,
                        onBack = {
                            popupState = PopupState.ActivityRecorder
                        }
                    )
                }

                PopupState.EditGoal -> {
                    GoalEditScreen(
                        viewModel = plannerViewModel,
                        onBack = {
                            showPopupWindow = false
                        }
                    )
                }

                PopupState.EditTask -> {
                    TaskEditScreen(
                        viewModel = plannerViewModel,
                        onBack = {
                            showPopupWindow = false
                        }
                    )
                }
            }
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                strokeWidth = 5.dp,
                color = Teal74
            )
        }
    }
}