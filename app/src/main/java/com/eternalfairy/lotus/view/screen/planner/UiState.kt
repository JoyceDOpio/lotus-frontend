package com.eternalfairy.lotus.view.screen.planner

import com.eternalfairy.lotus.model.data.Activity
import com.eternalfairy.lotus.view.data.Activities
import com.eternalfairy.lotus.view.data.ActivityUiState
import com.eternalfairy.lotus.view.data.DayUiState
import com.eternalfairy.lotus.view.data.GoalUiState
import com.eternalfairy.lotus.view.data.TaskUiState
import com.eternalfairy.lotus.view.data.Tasks
import com.eternalfairy.lotus.view.data.VoiceNoteUiState
import kotlinx.datetime.LocalDate

data class UiState(
    val isLoading: Boolean = false,
    val activities: Activities = emptyList(),
    val activityToBeDeleted: ActivityUiState = ActivityUiState(),
    val editedActivity: ActivityUiState = ActivityUiState(),
    val editedTask: TaskUiState = TaskUiState(),
    val isSubActivityTimerRunning: Boolean = false,
    val lastGoalPriority: Int = 0,
    val lastTaskPriority: Int = 0,
    val mainRecordedActivity: ActivityUiState = ActivityUiState(),
    val selectedActivity: ActivityUiState = ActivityUiState(),
    val selectedDate: LocalDate = LocalDate.parse(java.time.LocalDate.now().toString()),
    val selectedDay: DayUiState = DayUiState(),
    val selectedGoal: GoalUiState = GoalUiState(),
    val selectedTask: TaskUiState = TaskUiState(),
    val selectedVoiceNote: VoiceNoteUiState = VoiceNoteUiState(),
    val subRecordedActivity: ActivityUiState = ActivityUiState(),
    val tasks: Tasks = emptyList(),
    val toDoTasks: Tasks = emptyList(),
    val taskToBeDeleted: TaskUiState = TaskUiState(),
    val voiceNoteToBeDeleted: VoiceNoteUiState = VoiceNoteUiState()
)
