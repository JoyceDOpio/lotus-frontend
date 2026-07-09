package com.eternalfairy.lotus.view.screen.planner

import com.eternalfairy.lotus.view.data.Activities
import com.eternalfairy.lotus.view.data.ActivityUiState
import com.eternalfairy.lotus.view.data.DayUiState
import com.eternalfairy.lotus.view.data.GoalUiState
import com.eternalfairy.lotus.view.data.TaskUiState
import com.eternalfairy.lotus.view.data.Tasks
import com.eternalfairy.lotus.view.data.VoiceNoteUiState
import com.eternalfairy.lotus.view.data.VoiceNotesUiState
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class PlannerUiState @OptIn(ExperimentalUuidApi::class) constructor(
    val isLoading: Boolean = false,
    // Activity
    val activities: Activities = emptyList(),
//    val activityToBeDeleted: ActivityUiState = ActivityUiState(),
    val activityToBeDeletedId: Uuid? = null,
//    val activityToBeEditedId: Uuid? = null,
    val editedActivity: ActivityUiState = ActivityUiState(),
//    val isSubActivityTimerRunning: Boolean = false,
    val recordedActivityMain: ActivityUiState = ActivityUiState(),
    val recordedActivitySub: ActivityUiState = ActivityUiState(),
    val selectedActivity: ActivityUiState = ActivityUiState(),
//    val selectedActivityId: Uuid? = null,
    // Sub-activities of the selected activity
    val subActivities: Activities = emptyList(),

    // Day
    val editedDay: DayUiState = DayUiState(),
    val selectedDate: LocalDate = LocalDate.parse(java.time.LocalDate.now().toString()),
    val selectedDay: DayUiState = DayUiState(),

    // Goal
    val editedGoal: GoalUiState = GoalUiState(),
    val goals: List<GoalUiState> = emptyList(),
    val goalToBeDeletedId: Uuid? = null,
    val lastGoalPriority: Int = 0,
    val selectedGoal: GoalUiState = GoalUiState(),
//    val selectedGoalId: Uuid? = null,

    // Task
    val editedTask: TaskUiState = TaskUiState(),
    val lastTaskPriority: Int = 0,
    val selectedTask: TaskUiState = TaskUiState(),
    val tasks: Tasks = emptyList(),
    val taskToBeDeletedId: Uuid? = null,
    val toDoTasks: Tasks = emptyList(),

    // Voice note
    val recordedVoiceNote: VoiceNoteUiState = VoiceNoteUiState(),
    val selectedVoiceNote: VoiceNoteUiState = VoiceNoteUiState(),
    // Voice notes of the selected activity
    val voiceNotes: VoiceNotesUiState = emptyList(),
    val voiceNoteToBeDeletedId: Uuid? = null
)
