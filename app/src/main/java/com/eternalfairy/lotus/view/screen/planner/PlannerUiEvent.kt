package com.eternalfairy.lotus.view.screen.planner

import com.eternalfairy.lotus.view.data.VoiceNoteUiState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

sealed class PlannerUiEvent {
    // ACTIVITY
    data class ActivityToBeDeletedIdChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid?): PlannerUiEvent()
    data class ActivityToBeEditedIdChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid?): PlannerUiEvent()
    data class ActivityNoteChanged constructor(val value: String): PlannerUiEvent()
    data class ActivityTitleChanged constructor(val value: String): PlannerUiEvent()
    data class SelectedActivityIdChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid?): PlannerUiEvent()

    object DeleteActivity: PlannerUiEvent()
    object SaveActivity: PlannerUiEvent()

    data class RecordedMainActivityIdChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid): PlannerUiEvent()
    data class RecordedMainActivityDateChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: LocalDate): PlannerUiEvent()
    data class RecordedMainActivityNoteChanged constructor(val value: String): PlannerUiEvent()
    data class RecordedMainActivityTitleChanged constructor(val value: String): PlannerUiEvent()
    data class RecordedMainActivityStartTimeChanged constructor(val value: LocalTime): PlannerUiEvent()
    data class RecordedMainActivityEndTimeChanged constructor(val value: LocalTime): PlannerUiEvent()

    object EditRecordedMainActivity: PlannerUiEvent()
    object StartMainActivity: PlannerUiEvent()
    object StopMainActivity: PlannerUiEvent()

    data class RecordedSubActivityIdChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid): PlannerUiEvent()
    data class RecordedSubActivityNoteChanged(val value: String): PlannerUiEvent()
    data class RecordedSubActivityTitleChanged(val value: String): PlannerUiEvent()
    data class RecordedSubActivityStartTimeChanged(val value: LocalTime): PlannerUiEvent()
    data class RecordedSubActivityEndTimeChanged(val value: LocalTime): PlannerUiEvent()
    data class RecordedSubActivityMainActivityIdChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid): PlannerUiEvent()

    object EditRecordedSubActivity: PlannerUiEvent()
    object StartSubActivity: PlannerUiEvent()
    object StopSubActivity: PlannerUiEvent()

    // DAY
    data class SelectedDateChanged(val value: LocalDate): PlannerUiEvent()
    data class ActiveTimeStartChanged(val value: LocalTime): PlannerUiEvent()
    data class ActiveTimeEndChanged(val value: LocalTime): PlannerUiEvent()
    data class ActualActiveTimeStartChanged(val value: LocalTime): PlannerUiEvent()
    data class ActualActiveTimeEndChanged(val value: LocalTime): PlannerUiEvent()

    object SaveDay: PlannerUiEvent()

    // GOAL
    data class GoalPriorityChanged(val value: Int): PlannerUiEvent()
    data class GoalTitleChanged(val value: String): PlannerUiEvent()
    data class GoalToBeDeletedIdChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid?): PlannerUiEvent()
    data class LastGoalPriorityChanged(val value: Int): PlannerUiEvent()
    data class SelectedGoalIdChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid?): PlannerUiEvent()

    object DeleteGoal: PlannerUiEvent()
    object SaveGoal: PlannerUiEvent()

    data class LastTaskPriorityChanged(val value: Int): PlannerUiEvent()
    // TASK
    data class SelectedTaskIdChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid?): PlannerUiEvent()
    data class TaskDateChanged(val value: LocalDate?): PlannerUiEvent()
    data class TaskDescriptionChanged(val value: String): PlannerUiEvent()
    data class TaskEndTimeChanged(val value: LocalTime?): PlannerUiEvent()
    data class TaskIdChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid): PlannerUiEvent()
    data class TaskPinnedChanged(val value: Boolean): PlannerUiEvent()
    data class TaskPriorityChanged(val value: Int?): PlannerUiEvent()
    data class TaskStartTimeChanged(val value: LocalTime?): PlannerUiEvent()
    data class TaskToBeDeletedIdChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid?): PlannerUiEvent()
    data class TaskTitleChanged(val value: String): PlannerUiEvent()

    object DeleteTask: PlannerUiEvent()
    object SaveTask: PlannerUiEvent()

    // VOICE NOTE
    data class RecordedVoiceNoteChanged constructor(val value: VoiceNoteUiState): PlannerUiEvent()
    data class VoiceNoteToBeDeletedIdChanged @OptIn(ExperimentalUuidApi::class) constructor(val value: Uuid?): PlannerUiEvent()

    object DeleteVoiceNote: PlannerUiEvent()
    object SaveVoiceNote: PlannerUiEvent()
}