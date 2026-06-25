package com.eternalfairy.lotus.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eternalfairy.lotus.model.data.Time
import com.eternalfairy.lotus.model.data.Activity
import com.eternalfairy.lotus.model.data.Day
import com.eternalfairy.lotus.model.data.Goal
import com.eternalfairy.lotus.model.data.Task
import com.eternalfairy.lotus.model.data.VoiceNote
import com.eternalfairy.lotus.model.repository.ActivityRepository
import com.eternalfairy.lotus.model.repository.DayRepository
import com.eternalfairy.lotus.model.repository.GoalRepository
import com.eternalfairy.lotus.model.repository.TaskRepository
import com.eternalfairy.lotus.model.repository.VoiceNoteRepository
import com.eternalfairy.lotus.view.data.ActivityUiState
import com.eternalfairy.lotus.view.data.DayState
import com.eternalfairy.lotus.view.data.DayUiState
import com.eternalfairy.lotus.view.data.GoalUiState
import com.eternalfairy.lotus.view.data.TaskUiState
import com.eternalfairy.lotus.view.data.UserInput
import com.eternalfairy.lotus.view.data.VoiceNoteUiState
import com.eternalfairy.lotus.view.screen.planner.UiState
import com.eternalfairy.lotus.viewmodel.utils.TimeZoneUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

////const val ACTIVITY_SAVED_STATE_KEY = "activity"
const val ACTIVITY_ID_SAVED_STATE_KEY = "activity_id"
const val ACTIVITY_TO_BE_DELETED_ID_SAVED_STATE_KEY = "activity_to_be_deleted_id"
const val ACTIVITY_TO_BE_EDITED_ID_SAVED_STATE_KEY = "activity_to_be_edited_id"

//const val MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY = "recorded_activity_main"
//const val SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY = "recorded_activity_sub"
////const val SUB_ACTIVITIES_SAVED_STATE_KEY = "sub_activities"
//const val TASK_SAVED_STATE_KEY = "task"
const val GOAL_ID_SAVED_STATE_KEY = "goal_id"
const val TASK_ID_SAVED_STATE_KEY = "task_id"
const val USER_INPUT_SAVED_STATE_KEY = "user_input"

//const val VOICE_NOTES_SAVED_STATE_KEY = "voice_notes"
const val VOICE_NOTE_TO_BE_DELETED_ID_SAVED_STATE_KEY = "voice_note_to_be_deleted_id"

//@Parcelize
//data class TaskSavedState (// TODO: Instead of saving a whole task to the saved state, save only its id
//    val date: LocalDate?,
//    val id: UUID,
//    val title: String,
//    val startTime: String?,
//    val endTime: String?,
//    val description: String,
//    val priority: Int?,
//    val pinned: Boolean
//) : Parcelable
//

//
//@Parcelize
//data class ActivitySavedState (
//    val date: LocalDate,
//    val id: UUID,
//    val title: String = "",
//    val note: String = "",
//    val startTime: String = "",
//    val endTime: String = "",
//    val mainActivityId: UUID? = null
//) : Parcelable
//
//@Parcelize
//data class VoiceNoteSavedState (
//    val id: UUID,
//    val uri: String,
//    val duration: Long,
//    val timestamp: Long,
//    val activityId: UUID
//) : Parcelable
//

// -----------------------------------------------------------------------------------------------------------------------------
//typealias VoiceNotes = List<VoiceNote>
//typealias VoiceNotesUiState = List<VoiceNote>


////@Serializable
//data class UserInput(
//    val selectedDate: OffsetDateTime = OffsetDateTime.now()
//)

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
//    private val context: Context,
//    private val supabaseClient: SupabaseClient
//    private val auth: AuthManager,
    private val activityRepository: ActivityRepository,
    private val dayRepository: DayRepository,
    private val goalRepository: GoalRepository,
    private val taskRepository: TaskRepository,
    private val voiceNoteRepository: VoiceNoteRepository
) : ViewModel() {

    var state by mutableStateOf(UiState())

    init {
        viewModelScope.launch {

            activityRepository.getMainRecordedActivityStream().first()?.let { activity ->
                recordedMainActivityUiState.value = ActivityUiState(
                    date = activity.date.toLocalDate(),
                    id = activity.id,
                    title = activity.title,
                    note = activity.note,
                    startTime = Time.parseFromTimestampTz(activity.startTime)
                )
            }

            activityRepository.getSubRecordedActivityStream().first()?.let { activity ->
                recordedSubActivityUiState.value = ActivityUiState(
                    date = activity.date.toLocalDate(),
                    id = activity.id,
                    title = activity.title,
                    note = activity.note,
                    startTime = Time.parseFromTimestampTz(activity.startTime),
                    mainActivityId = activity.mainActivityId
                )
            }

            taskRepository.getTasksWithoutDateStream().collect { tasks ->
                _toDoTasks.value = tasks.map { task ->
                    TaskUiState(
//                        id = task.id,
                        id = UUID.fromString(task.id),
                        title = task.title,
                        description = task.description,
//                        date = task.date?.toLocalDate(),
//                        date = task.date,
                        date = task.date?.let { LocalDate.parse(task.date) },
                        startTime = task.startTime?.let { Time.parseFromTimestampTz(task.startTime!!) },// Tasks from day state should have a date, start and end time
                        endTime = task.endTime?.let { Time.parseFromTimestampTz(task.endTime!!) },
                        priority = task.priority,
                        pinned = task.pinned != 0
                    )
                }
            }

            user = authManager
                .getUser()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
                    initialValue = null
                )
        }
    }

    // ACTIVITIES
    // Activity details' UI state
    val selectedActivityId =
        MutableStateFlow(savedStateHandle.get<UUID>(ACTIVITY_ID_SAVED_STATE_KEY))

    inline fun <reified T> Collection<Flow<T>>.combine(): Flow<List<T>> {
        if (isEmpty()) return flowOf(emptyList())
        return combine(this) { it.toList() }
    }

    // The state of the activity that is displayed on the UI
    @OptIn(ExperimentalCoroutinesApi::class)
    val activityUiState = selectedActivityId.flatMapLatest { activityId ->
        if (activityId == null) return@flatMapLatest flowOf(ActivityUiState())

        val flowOfActivityUiState = activityRepository
            .getActivityStream(activityId.toString())
//            .flatMapLatest { activity ->
//                if (activity == null) ActivityUiState()
//                else ActivityUiState(activity)
//            }
            .map(::ActivityUiState)
        val flowOfSubActivitiesUiState =
            activityRepository.getSubActivitiesPerMainActivityStream(activityId.toString())
                .map(::ActivityUiStateList)
                // If I use map() instead of flatMapLatest(), I get Flow<Flow<List<ActivityUiState>>> instead of  Flow<List<ActivityUiState>>
                .flatMapLatest { subActivities ->
                    val subActivitiesUiState = subActivities.map { subActivity ->
                        voiceNoteRepository.getVoiceNotesPerActivityStream(subActivity.id!!.toString())
                            .map(::VoiceNoteUiStateList)
                            .map { voiceNotesUiState ->
                                ActivityUiState(
                                    date = subActivity.date,
                                    id = subActivity.id,
                                    title = subActivity.title,
                                    note = subActivity.note,
                                    startTime = subActivity.startTime,
                                    endTime = subActivity.endTime,
                                    voiceNotesUiState = voiceNotesUiState,
                                    mainActivityId = subActivity.mainActivityId
                                )
                            }
                    }
                    // Apparently, when the voiceNotesRepository.getAllVoiceNotesPerActivity(subActivity.id) is called for a non-existent sub-activity the flow it creates never emits a value and the whole code execution hangs up. Therefore, we're using an inline function to return a flow of an empty list instead.
                    subActivitiesUiState.combine()
                }

        combine(
            flowOfActivityUiState,
            voiceNoteRepository.getVoiceNotesPerActivityStream(activityId.toString())
                .map(::VoiceNoteUiStateList),
            flowOfSubActivitiesUiState
        ) { activityUiState,
            voiceNotesUiState,
            subActivitiesUiState ->

            activityUiState.copy(
                voiceNotesUiState = voiceNotesUiState,
                subActivitiesUiState = subActivitiesUiState
            )
        }

    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = ActivityUiState()
        )

    // The state of the activity that is edited. We're making a distinction between the activity that is displayed and the one that is edited because sometimes it might not be the same activity, e.g. a main activity is displayed and its subactivity is edited
    val activityToBeEditedId = MutableStateFlow(
        savedStateHandle.get<UUID>(
            ACTIVITY_TO_BE_EDITED_ID_SAVED_STATE_KEY
        )
    )
    val _activityEditState = MutableStateFlow(ActivityUiState())

    // The activityEditState is there only to update the _activityEditState
    @OptIn(ExperimentalCoroutinesApi::class)
    val activityEditState = activityToBeEditedId.flatMapLatest { id ->
        if (id == null) return@flatMapLatest flowOf(ActivityUiState())

        val flowOfActivityUiState =
            activityRepository.getActivityStream(id.toString()).map(::ActivityUiState)
        val flowOfSubActivitiesUiState =
            activityRepository.getSubActivitiesPerMainActivityStream(id.toString())
                .map(::ActivityUiStateList)
                .flatMapLatest { subActivities ->
                    val subActivitiesUiState = subActivities.map { subActivity ->
                        voiceNoteRepository.getVoiceNotesPerActivityStream(subActivity.id!!.toString())
                            .map(::VoiceNoteUiStateList)
                            .map { voiceNotesUiState ->
                                ActivityUiState(
                                    date = subActivity.date,
                                    id = subActivity.id,
                                    title = subActivity.title,
                                    note = subActivity.note,
                                    startTime = subActivity.startTime,
                                    endTime = subActivity.endTime,
                                    voiceNotesUiState = voiceNotesUiState,
                                    mainActivityId = subActivity.mainActivityId
                                )
                            }
                    }
                    // Apparently, when the voiceNotesRepository.getAllVoiceNotesPerActivity(subActivity.id) is called for a non-existent sub-activity the flow it creates never emits a value and the whole code execution hangs up. Therefore, we're using an inline function to return a flow of an empty list instead.
                    subActivitiesUiState.combine()
                }

        combine(
            flowOfActivityUiState,
            voiceNoteRepository.getVoiceNotesPerActivityStream(id.toString()).map(::VoiceNoteUiStateList),
            flowOfSubActivitiesUiState
        ) { activityUiState,
            voiceNotesUiState,
            subActivitiesUiState ->
            _activityEditState.update {
                activityUiState.copy(
                    voiceNotesUiState = voiceNotesUiState,
                    subActivitiesUiState = subActivitiesUiState
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = ActivityUiState()
        )

    // The state of the activity that is to be deleted
    val activityToBeDeletedId = MutableStateFlow(
        savedStateHandle.get<UUID>(
            ACTIVITY_TO_BE_DELETED_ID_SAVED_STATE_KEY
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activityToBeDeletedState = activityToBeDeletedId.flatMapLatest { id ->
        if (id == null) return@flatMapLatest flowOf(ActivityUiState())

        val flowOfActivityUiState =
            activityRepository.getActivityStream(id.toString()).map(::ActivityUiState)
        val flowOfSubActivities = activityRepository.getSubActivitiesPerMainActivityStream(id.toString())
            .map(::ActivityUiStateList)
            .flatMapLatest { subActivities ->
                val subActivitiesUiState = subActivities.map { subActivity ->
                    voiceNoteRepository.getVoiceNotesPerActivityStream(subActivity.id!!.toString())
                        .map(::VoiceNoteUiStateList)
                        .map { voiceNotes ->
                            ActivityUiState(
                                date = subActivity.date,
                                id = subActivity.id,
                                title = subActivity.title,
                                note = subActivity.note,
                                startTime = subActivity.startTime,
                                endTime = subActivity.endTime,
                                voiceNotesUiState = voiceNotes,
                                mainActivityId = subActivity.mainActivityId
                            )
                        }
                }
                // Apparently, when the voiceNotesRepository.getAllVoiceNotesPerActivity(subActivity.id) is called for a non-existent sub-activity the flow it creates never emits a value and the whole code execution hangs up. Therefore, we're using an inline function to return a flow of an empty list instead.
                subActivitiesUiState.combine()
            }

        combine(
            flowOfActivityUiState,
            voiceNoteRepository.getVoiceNotesPerActivityStream(id.toString()).map(::VoiceNoteUiStateList),
            flowOfSubActivities
        ) { activityUiState,
            voiceNotesUiState,
            subActivitiesUiState ->
            activityUiState.copy(
                voiceNotesUiState = voiceNotesUiState,
                subActivitiesUiState = subActivitiesUiState
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = ActivityUiState()
        )

    val recordedMainActivityUiState = MutableStateFlow(ActivityUiState())
    val recordedSubActivityUiState = MutableStateFlow(ActivityUiState())

    val userInput = MutableStateFlow(
        (savedStateHandle.get<UserInput>(
            USER_INPUT_SAVED_STATE_KEY
        )).let {
            it ?: UserInput()
        })

    val zoneOffset: ZoneOffset = OffsetDateTime.now().offset

    // Day UI state
    val dayEditState = MutableStateFlow(DayUiState())

    // Day state
    @OptIn(ExperimentalCoroutinesApi::class)
    val dayUiState = userInput.flatMapLatest { input: UserInput ->
        combine(
            flow = dayRepository
                .getDayStream(input.selectedDate.toString())
                .map(::toDayState),
            flow2 = taskRepository
                .getTasksPerDayStream(input.selectedDate.toString())
                .map(::TaskUiStateList),
            flow3 = activityRepository
                .getMainActivitiesPerDayStream(input.selectedDate.toString())
                .map(::ActivityUiStateList)
        ) { day,
            tasksUiState,
            activitiesUiState ->
            Log.i("PlannerViewModel", "tasksUiState $tasksUiState")
            // Update the day UI state with the actual active time start and end
            day.let {
                dayEditState.update {
                    it.copy(
                        activeTimeStart = Time.parseFromTimestampTz(day.activeTimeStart) ?: Time(
                            6,
                            0
                        ),//TODO: Take the default value from settings
                        activeTimeEnd = Time.parseFromTimestampTz(day.activeTimeEnd) ?: Time(
                            22,
                            0
                        ),//TODO: Take the default value from settings
                        actualActiveTimeStart = day.actualActiveTimeStart?.let {
                            Time.parseFromTimestampTz(
                                day.actualActiveTimeStart
                            )
                        },
                        actualActiveTimeEnd = day.actualActiveTimeEnd?.let {
                            Time.parseFromTimestampTz(
                                day.actualActiveTimeEnd
                            )
                        },
//                        tasks = tasksUiState.sortedWith { a, b -> a.compareTo(b) },// Sort the tasks based on their start time
                        tasks = tasksUiState,
                        activities = activitiesUiState,
                        note = day.note ?: ""
                    )
                }

                //            DayState(
                //                date = input.selectedDate,
                //                activeTimeStart = day.activeTimeStart,
                //                activeTimeEnd = day.activeTimeEnd,
                //                actualActiveTimeStart = day.actualActiveTimeStart,
                //                actualActiveTimeEnd = day.actualActiveTimeEnd,
                ////                tasks = tasksUiState.sortedWith{ a, b -> a.compareTo(b)},// Sort the tasks based on their start time
                ////                activities = activitiesUiState
                //                tasksUiState = tasksUiState.sortedWith { a, b -> a.compareTo(b) },// Sort the tasks based on their start time
                //                activitiesUiState = activitiesUiState
                //            )

                DayUiState(
                    date = input.selectedDate,
                    activeTimeStart = Time.parseFromTimestampTz(day.activeTimeStart) ?: Time(
                        6,
                        0
                    ),//TODO: Take the default value from settings
                    activeTimeEnd = Time.parseFromTimestampTz(day.activeTimeEnd) ?: Time(
                        22,
                        0
                    ),//TODO: Take the default value from settings
                    actualActiveTimeStart = day.actualActiveTimeStart?.let {
                        Time.parseFromTimestampTz(
                            day.actualActiveTimeStart
                        )
                    },
                    actualActiveTimeEnd = day.actualActiveTimeEnd?.let {
                        Time.parseFromTimestampTz(
                            day.actualActiveTimeEnd
                        )
                    },
                    tasks = tasksUiState.sortedWith { a, b -> a.compareTo(b) },// Sort the tasks based on their start time
                    activities = activitiesUiState,
                    note = day.note
                )
            } ?: run {
                DayUiState()
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = DayUiState()
        )

    val selectedGoalId = MutableStateFlow(savedStateHandle.get<UUID>(GOAL_ID_SAVED_STATE_KEY))

    val _goalEditState = MutableStateFlow(GoalUiState())

    // The goalEditState is there only to update the _goalEditState
    @OptIn(ExperimentalCoroutinesApi::class)
    val goalEditState = selectedGoalId.flatMapLatest { id ->
        if (id == null) return@flatMapLatest flowOf(GoalUiState())

        goalRepository
            .getGoalStream(id.toString())
            .map(::GoalUiState)
            .map { goal ->
                _goalEditState.update {
                    goal
                }

                goal
            }
    }

    val goals = goalRepository
        .getGoalsStream()
        .map(::GoalUiStateList)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = emptyList<GoalUiState>()
        )

    val lastGoalPriority = goalRepository
        .getLastPriority()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = 0
        )

    // The priority of the last task
    val lastTaskPriority = taskRepository
        .getLastPriority()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = 0
        )

    // Task details' UI state
    val _taskEditState = MutableStateFlow(
        TaskUiState(
//        userId = UUID.fromString(user.value?.id)
        )
    )

    //    val selectedTaskId = MutableStateFlow(savedStateHandle.get<UUID>(TASK_ID_SAVED_STATE_KEY))
//    val taskUiState = MutableStateFlow((savedStateHandle.get<TaskSavedState>(TASK_SAVED_STATE_KEY)).let { it ->
    val taskEditState =
        savedStateHandle.getStateFlow<UUID?>(TASK_ID_SAVED_STATE_KEY, null).flatMapLatest { id ->
            if (id == null) {
                _taskEditState.update {
                    TaskUiState(
                        userId = UUID.fromString(user.value?.id)
                    )
                }

                flowOf(TaskUiState())
//            return@flatMapLatest TaskUiState()
            } else {
                taskRepository
                    .getTaskStream(id.toString())
                    .map(::TaskUiState)
                    .map { task ->
                        _taskEditState.update { task }
                        task
                    }
            }
        }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = TaskUiState(),
//        )

    val _toDoTasks = MutableStateFlow(emptyList<TaskUiState>())
    val toDoTasks: StateFlow<List<TaskUiState>> = _toDoTasks

    val timestamptzFormatter = DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss.SSSS+HH:mm")

//    val _userProfile = MutableStateFlow(UserProfileUiState())

    val voiceNoteToBeDeletedId = MutableStateFlow(
        savedStateHandle.get<UUID>(
            VOICE_NOTE_TO_BE_DELETED_ID_SAVED_STATE_KEY
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val voiceNoteToBeDeletedState = voiceNoteToBeDeletedId.flatMapLatest { id ->
        if (id == null) return@flatMapLatest flowOf(VoiceNoteUiState())

        voiceNoteRepository.getVoiceNoteStream(id.toString()).map(::VoiceNoteUiState)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = VoiceNoteUiState()
        )

    fun clearMainRecordedActivity() {
        recordedMainActivityUiState.update {
            ActivityUiState()
        }
//        savedStateHandle.set(
//            key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//            value = null
//        )
    }

    fun clearSubRecordedActivity() {
        recordedSubActivityUiState.update {
            ActivityUiState()
        }
//        savedStateHandle.set(
//            key = SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//            value = null
//        )
    }

    fun deleteActivity(id: UUID) {
        viewModelScope.launch {
            activityRepository.deleteActivity(id.toString())
        }
    }

    fun deleteGoal(id: UUID) {
        viewModelScope.launch {
            goalRepository.deleteGoal(id.toString())
        }
    }

    fun deleteTask(id: UUID) {
        viewModelScope.launch {
            taskRepository.deleteTask(id.toString())
        }
    }

    fun deleteVoiceNote(id: UUID) {
        viewModelScope.launch {
            voiceNoteRepository.deleteVoiceNote(id.toString())
        }
    }

    fun saveActivity() {
        viewModelScope.launch {
            // If activity exists, update it
            val activityId = _activityEditState.value.id

            activityId?.let {
                val activityUiState = dayUiState.value.activities.find { it.id == activityId }

                if (activityUiState == null) {
                    activityRepository.addActivity(
                        _activityEditState.value.toActivity(zoneOffset)
                    )
                } else {
                    activityRepository.editActivity(
                        _activityEditState.value.toActivity(
                            zoneOffset
                        )
                    )
                }
            }
        }
    }

    fun saveDay() {
        viewModelScope.launch {
            dayRepository.addDay(
                dayUiState.value.toDay(zoneOffset).copy(
                    activeTimeStart = dayEditState.value.activeTimeStart.parseToTimestampTz(
                        dayUiState.value.date
                    ),
                    activeTimeEnd = dayEditState.value.activeTimeEnd.parseToTimestampTz(dayUiState.value.date),
                    actualActiveTimeStart = dayEditState.value.actualActiveTimeStart?.let {
                        dayEditState.value.actualActiveTimeStart!!.parseToTimestampTz(
                            dayUiState.value.date
                        )
                    },
                    actualActiveTimeEnd = dayEditState.value.actualActiveTimeEnd?.let {
                        dayEditState.value.actualActiveTimeEnd!!.parseToTimestampTz(
                            dayUiState.value.date
                        )
                    },
                    note = dayEditState.value.note
                )
            )
        }
    }

    fun saveGoalFromState() {//FIXME: Optimise this method
        viewModelScope.launch {
            // If goal exists, update it
            val goalId = _goalEditState.value.id

            if (goalId != null) {
                var goal = goals.value.find { goal -> goal.id == goalId }

                if (goal != null) {
                    val updatedGoal = _goalEditState.value.toGoal().copy(
                        id = goalId
                    )
                    goalRepository.editGoal(updatedGoal)
                }
            }
            // Else, save the new goal
            else {
                goalRepository.addGoal(_goalEditState.value.toGoal())
            }
        }
    }

    fun saveGoal(goal: GoalUiState) {//FIXME: Optimise this method
        viewModelScope.launch {
            // If goal exists, update it
            val found = goals.value.find { searchedGoal -> searchedGoal.id == goal.id }

            if (found != null) {
                goalRepository.editGoal(goal.toGoal())
            }
            // Else, save the new goal
            else {
                goalRepository.addGoal(goal.toGoal())
            }
        }
    }

    fun saveMainRecordedActivity() {
        viewModelScope.launch {
            // If activity exists, update it
            val activityId = recordedMainActivityUiState.value.id

            val activityUiState = dayUiState.value.activities.find { it.id == activityId }

            if (activityUiState == null) {
                activityRepository.addActivity(
                    recordedMainActivityUiState.value.toActivity(zoneOffset)
                )
            } else {
                activityRepository.editActivity(
                    recordedMainActivityUiState.value.toActivity(
                        zoneOffset
                    )
                )
            }
        }
    }

    fun saveSubRecordedActivity() {
        viewModelScope.launch {
            // If activity exists, update it
            val activityId = recordedSubActivityUiState.value.id

            val activityUiState = dayUiState.value.activities.find { it.id == activityId }

            if (activityUiState == null) {
                activityRepository.addActivity(
                    recordedSubActivityUiState.value.toActivity(zoneOffset)
                )
            } else {
                activityRepository.editActivity(
                    recordedSubActivityUiState.value.toActivity(
                        zoneOffset
                    )
                )
            }
        }
    }

    fun saveTaskFromState() {
        viewModelScope.launch {
            // If task exists, update it
            val taskId = _taskEditState.value.id

            if (taskId != null) {
                val tasks = dayUiState.value.tasks + toDoTasks.value
                val task = tasks.find { task -> task.id == taskId }

                if (task != null) {
//                    val updatedTask = _taskEditState.value.toTask(zoneOffset.value).copy(
                    val updatedTask = _taskEditState.value
                        .toTask(
//                            userId = user.value!!.id
//                            userId = UUID.fromString("963c4d72-5b16-4a77-a602-cd29e43b5e2a")
                            userId = UUID.fromString("963c4d72-5b16-4a77-a602-cd29e43b5e2a")
                        )
                        .copy(
//                            id = taskId
                            id = taskId.toString()
                        )
                    Log.i("PlannerViewModel", "_taskEditState ${_taskEditState.value}")
                    Log.i("PlannerViewModel", "updatedTask ${updatedTask}")
                    taskRepository.editTask(updatedTask)
                }
                // Else, save the new task
                else {
//                    tasksRepository.insertTask(_taskEditState.value.toTask(zoneOffset.value))
//                    tasksRepository.insertTask(_taskEditState.value.toTask(userId = user.value!!.id))
                    taskRepository.addTask(
                        _taskEditState.value.toTask(
                            userId = UUID.fromString("963c4d72-5b16-4a77-a602-cd29e43b5e2a")
//                            userId = UUID.fromString("963c4d72-5b16-4a77-a602-cd29e43b5e2a")
                        )
                    )
                }
            }
            // Else, save the new task
            else {
//                // There are no tasks for the given day, save the day.
//                if (dayState.value.tasks.isEmpty()) {//FIXME: If I save the day before I save the task the taskUiState is emptied. The task can still be successfully saved to the database without the corresponding day being persisted in the database.
//                    daysRepository.insertDay(dayState.value.toDay())
//                }

                Log.i("PlannerViewModel", "zoneOffset: ${zoneOffset}")
                Log.i(
                    "PlannerViewModel",
                    "OffsetDateTime.now().offset: ${OffsetDateTime.now().offset}"
                )
//                tasksRepository.insertTask(_taskEditState.value.toTask(zoneOffset.value))
//                tasksRepository.insertTask(_taskEditState.value.toTask(userId = user.value?.id))
                taskRepository.addTask(
                    _taskEditState.value.toTask(
                        userId = UUID.fromString("963c4d72-5b16-4a77-a602-cd29e43b5e2a")
                    )
                )
            }
        }
    }

    fun saveTask(task: TaskUiState) {
        viewModelScope.launch {
            val tasks = dayUiState.value.tasks + toDoTasks.value
            val found = tasks.find { iteratedTask -> iteratedTask.id == task.id }

            if (found != null) {
//                tasksRepository.updateTask(task.toTask(zoneOffset.value))
                taskRepository.editTask(task.toTask(
                    userId = UUID.fromString("963c4d72-5b16-4a77-a602-cd29e43b5e2a")
                ))
            } else {
//                tasksRepository.insertTask(task.toTask(zoneOffset.value))
//                tasksRepository.insertTask(task.toTask(userId = user.value!!.id))
                taskRepository.addTask(
                    task.toTask(
                        userId = UUID.fromString("963c4d72-5b16-4a77-a602-cd29e43b5e2a")
                    )
                )
            }
        }
    }

//    fun saveTaskToStateHandle() {
//        if (taskUiState.value.id != null) {
//            savedStateHandle.set(
//                key = TASK_SAVED_STATE_KEY,
//                value = taskUiState.value.toSavedState()
//            )
//        }
//    }

    fun saveVoiceNote(voiceNote: VoiceNoteUiState) {
        viewModelScope.launch {
            voiceNoteRepository.addVoiceNote(voiceNote.toVoiceNote())
        }
    }

    fun selectActivity(id: UUID?) {
        selectedActivityId.update { id }

        // Save the selected id to the saved state
        savedStateHandle.set(
            key = ACTIVITY_ID_SAVED_STATE_KEY,
            value = id
        )
    }

    fun selectActivityForDeletion(id: UUID?) {
        activityToBeDeletedId.update { id }

        // Save the selected id to the saved state
        savedStateHandle.set(
            key = ACTIVITY_TO_BE_DELETED_ID_SAVED_STATE_KEY,
            value = id
        )
    }

    fun selectActivityForEditing(id: UUID?) {
        activityToBeEditedId.update { id }

        // Save the selected id to the saved state
        savedStateHandle.set(
            key = ACTIVITY_TO_BE_EDITED_ID_SAVED_STATE_KEY,
            value = id
        )
    }

    fun selectGoal(id: UUID?) {
        selectedGoalId.update { id }

        // Save the selected id to the saved state
        savedStateHandle.set(
            key = GOAL_ID_SAVED_STATE_KEY,
            value = id
        )
    }

    fun selectTask(id: UUID?) {
        val tasks = dayUiState.value.tasks + toDoTasks.value
        val task = tasks.find { task -> task.id == id }

        if (task != null) {
            _taskEditState.update {
                TaskUiState(
                    id = task.id,
                    title = task.title,
                    description = task.description,
                    date = task.date,
                    startTime = task.startTime,// Tasks from day state should have a date, start and end time
                    endTime = task.endTime,
                    priority = task.priority,
                    pinned = task.pinned
                )
            }
        } else {
            _taskEditState.update {
                TaskUiState()
            }
        }

        // Set the selected task saved state
        savedStateHandle.set(
            key = TASK_ID_SAVED_STATE_KEY,
            value = if (task == null) null else _taskEditState.value.id
//            key = TASK_ID_SAVED_STATE_KEY,
//            value = task?.id
        )
    }

    fun selectVoiceNoteForDeletion(id: UUID?) {
        voiceNoteToBeDeletedId.update { id }
    }

    fun setActiveTimeEnd(time: Time) {
        dayEditState.update {
            it.copy(
                activeTimeEnd = time
            )
        }
    }

    fun setActualActiveTimeEnd(time: Time) {
        dayEditState.update {
            it.copy(
                actualActiveTimeEnd = time
            )
        }
    }

    fun setActiveTimeStart(time: Time) {
        dayEditState.update {
            it.copy(
                activeTimeStart = time
            )
        }
    }

    fun setActualActiveTimeStart(time: Time) {
        dayEditState.update {
            it.copy(
                actualActiveTimeStart = time
            )
        }
    }

    fun setActivityNote(note: String) {
        _activityEditState.update {
            it.copy(
                note = note
            )
        }
    }

    fun setActivityTitle(title: String) {
        _activityEditState.update {
            it.copy(
                title = title
            )
        }
    }

    fun setRecordedMainActivityDate(date: LocalDate) {
        recordedMainActivityUiState.update {
            it.copy(
                date = date
            )
        }
    }

    fun setRecordedMainActivityEndTime(time: Time) {
        recordedMainActivityUiState.update {
            it.copy(
                endTime = time
            )
        }
    }

    fun setRecordedMainActivityId(id: UUID) {
        recordedMainActivityUiState.update {
            it.copy(
                id = id
            )
        }
//        if (recordedMainActivityUiState.value.id != null) {
//            savedStateHandle.set(
//                key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//                value = recordedMainActivityUiState.value.toSavedState()
//            )
//        }
    }

    fun setRecordedMainActivityNote(note: String) {
        recordedMainActivityUiState.update {
            it.copy(
                note = note
            )
        }
//        if (recordedMainActivityUiState.value.id != null) {
//            savedStateHandle.set(
//                key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//                value = recordedMainActivityUiState.value.toSavedState()
//            )
//        }
    }

    fun setRecordedMainActivityStartTime(time: Time) {
        recordedMainActivityUiState.update {
            it.copy(
                startTime = time
            )
        }
//        if (recordedMainActivityUiState.value.id != null) {
//            savedStateHandle.set(
//                key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//                value = recordedMainActivityUiState.value.toSavedState()
//            )
//        }
    }

    fun setRecordedMainActivityTitle(title: String) {
        recordedMainActivityUiState.update {
            it.copy(
                title = title
            )
        }
//        if (recordedMainActivityUiState.value.id != null) {
//            savedStateHandle.set(
//                key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//                value = recordedMainActivityUiState.value.toSavedState()
//            )
//        }
    }

    fun setSelectedDate(date: LocalDate) {
        userInput.update {
            it.copy(
                selectedDate = date
            )
        }
        savedStateHandle.set(
            key = USER_INPUT_SAVED_STATE_KEY,
            value = UserInput(date)
        )
    }

    fun setRecordedSubActivityEndTime(time: Time) {
        recordedSubActivityUiState.update {
            it.copy(
                endTime = time
            )
        }
//        if (recordedSubActivityUiState.value.id != null) {
//            savedStateHandle.set(
//                key = SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//                value = recordedSubActivityUiState.value.toSavedState()
//            )
//        }
    }

    fun setRecordedSubActivityId(id: UUID) {
        recordedSubActivityUiState.update {
            it.copy(
                id = id
            )
        }
//        if (recordedSubActivityUiState.value.id != null) {
//            savedStateHandle.set(
//                key = SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//                value = recordedSubActivityUiState.value.toSavedState()
//            )
//        }
    }

    fun setRecordedSubActivityMainActivityId(mainActivityId: UUID) {
        recordedSubActivityUiState.update {
            it.copy(
                mainActivityId = mainActivityId
            )
        }
//        if (recordedSubActivityUiState.value.id != null) {
//            savedStateHandle.set(
//                key = SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//                value = recordedSubActivityUiState.value.toSavedState()
//            )
//        }
    }

    fun setRecordedSubActivityNote(note: String) {
        recordedSubActivityUiState.update {
            it.copy(
                note = note
            )
        }
//        if (recordedSubActivityUiState.value.id != null) {
//            savedStateHandle.set(
//                key = SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//                value = recordedSubActivityUiState.value.toSavedState()
//            )
//        }
    }

    fun setRecordedSubActivityStartTime(time: Time) {
        recordedSubActivityUiState.update {
            it.copy(
                startTime = time
            )
        }
//        if (recordedSubActivityUiState.value.id != null) {
//            savedStateHandle.set(
//                key = SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//                value = recordedSubActivityUiState.value.toSavedState()
//            )
//        }
    }

    fun setRecordedSubActivityTitle(title: String) {
        recordedSubActivityUiState.update {
            it.copy(
                title = title
            )
        }
//        if (recordedSubActivityUiState.value.id != null) {
//            savedStateHandle.set(
//                key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//                value = recordedSubActivityUiState.value.toSavedState()
//            )
//        }
    }

    fun setDayNote(note: String) {
        dayEditState.update {
            it.copy(
                note = note
            )
        }
    }

    fun setGoalPriority(value: Int) {
        _goalEditState.update { it ->
            it.copy(
                priority = value
            )
        }
    }

    fun setGoalTitle(value: String) {
        _goalEditState.update { it ->
            it.copy(
                title = value
            )
        }
    }

    fun setIsActivityDisplay(value: Boolean) {
        dayEditState.update {
            it.copy(
                isActivityDisplay = value
            )
        }
    }

    fun setIsActiveTimeSetUp(value: Boolean) {
        dayEditState.update {
            it.copy(
                isActiveTimeSetUp = value
            )
        }
    }

    fun setTaskDate(date: LocalDate?) {
        _taskEditState.update {
            it.copy(
                date = date
            )
        }
//        saveTaskToStateHandle()
    }

    fun setTaskDescription(description: String) {
        _taskEditState.update {
            it.copy(
                description = description
            )
        }
//        saveTaskToStateHandle()
    }

    fun setTaskEndTime(time: Time) {
        _taskEditState.update {
            it.copy(
                endTime = time
            )
        }
//        saveTaskToStateHandle()
    }

    fun setTaskPinned(value: Boolean) {
        _taskEditState.update {
            it.copy(
                pinned = value
            )
        }
        Log.i("PlannerViewModel", "_taskEditState ${_taskEditState.value}")
//        saveTaskToStateHandle()
    }

    fun setTaskPriority(value: Int?) {
        _taskEditState.update {
            it.copy(
                priority = value
            )
        }
//        saveTaskToStateHandle()
    }

    fun setTaskStartTime(time: Time) {
        _taskEditState.update {
            it.copy(
                startTime = time
            )
        }
//        saveTaskToStateHandle()
    }

    fun setTaskTitle(title: String) {
        _taskEditState.update {
            it.copy(
                title = title
            )
        }
//        saveTaskToStateHandle()
    }

    fun updateLastPlayedPosition(position: Long, itemIndex: Int) {
        _activityEditState.update {
            it.copy(
                voiceNotesUiState = it.voiceNotesUiState.toMutableList().also { list ->
                    list[itemIndex] = list[itemIndex].copy(
                        lastPlayedPosition = position
                    )
                }
            )
        }
    }

    fun updateTask(task: TaskUiState) {
        viewModelScope.launch {
            taskRepository.editTask(
                task.toTask(
                    zoneOffset,
//                user.value!!.id
                    UUID.fromString("963c4d72-5b16-4a77-a602-cd29e43b5e2a")
                )
            )
        }
    }
}

fun ActivityUiState(activity: Activity?): ActivityUiState {
    return (activity?.also {
        ActivityUiState(
            date = activity.date.toLocalDate(),
            title = activity.title,
            note = activity.note,
            startTime = Time.parseFromTimestampTz(activity.startTime),
            endTime = activity.endTime?.let { Time.parseFromTimestampTz(activity.endTime!!) },
            id = activity.id!!,
            mainActivityId = activity.mainActivityId
        )
    } ?: {
        ActivityUiState()
    }) as ActivityUiState
}

fun ActivityUiStateList(activities: List<Activity>): List<ActivityUiState> =
    activities.map { activity ->
        ActivityUiState(
            date = activity.date.toLocalDate(),
            title = activity.title,
            note = activity.note,
            startTime = Time.parseFromTimestampTz(activity.startTime),
            endTime = activity.endTime?.let { Time.parseFromTimestampTz(activity.endTime!!) },
            id = activity.id!!,
            mainActivityId = activity.mainActivityId
        )
    }

fun LocalDate.toOffsetDateTime(
    time: Time,
    zoneOffset: ZoneOffset? = OffsetDateTime.now().offset
): OffsetDateTime {//FIXME: I'm not sure zoneOffset can be null
    val localTime = LocalTime.of(time.hour, time.minute)
    val localDateTime = LocalDateTime.of(this, localTime)
    val offsetDateTime = OffsetDateTime.of(localDateTime, zoneOffset)

    return offsetDateTime
}

//fun ActivityUiState.toActivity(userId: UUID): Activity = Activity(
fun ActivityUiState.toActivity(zoneOffset: ZoneOffset? = OffsetDateTime.now().offset): Activity =
    Activity(
//    userId = userId,
        date = this.date.toOffsetDateTime(this.startTime, zoneOffset),
        title = this.title,
        note = this.note,
        startTime = this.startTime.parseToTimestampTz(this.date),
        endTime = this.endTime?.parseToTimestampTz(this.date),
        id = this.id!!,
        mainActivityId = this.mainActivityId
    )

//fun ActivityUiState.toSavedState(): ActivitySavedState = ActivitySavedState(
//    date = this.date,
//    id = this.id!!,
//    title = this.title,
//    startTime = this.startTime.toString()
//)

//fun toDayState(day: Day?): DayState {
//    return (day?.also {
//        DayState(
//            date = day.date,
//            activeTimeStart = day.activeTimeStart,
//            activeTimeEnd = day.activeTimeEnd,
//            actualActiveTimeStart = day.actualActiveTimeStart,
//            actualActiveTimeEnd = day.actualActiveTimeEnd,
//            note = day.note
//        )
//    } ?: {
//        DayState()
//    }) as DayState
//}

fun toDayState(day: Day?) = day?.let {
    DayState(
        date = LocalDate.parse(day.date),
        activeTimeStart = day.activeTimeStart,
        activeTimeEnd = day.activeTimeEnd,
        actualActiveTimeStart = day.actualActiveTimeStart,
        actualActiveTimeEnd = day.actualActiveTimeEnd,
        note = day.note
    )
} ?: DayState()

//fun DayUiState(response: ApiResponse<Day>): toDayState = when (response) {
//    is ApiResponse.Success -> {
//        val day = response.data
//
//        toDayState(
//            date = day.date.toLocalDate(),
//            activeTimeStart = Time.parseFromTimestamptz(day.activeTimeStart),
//            activeTimeEnd = Time.parseFromTimestamptz(day.activeTimeEnd),
//            actualActiveTimeStart = day.actualActiveTimeStart?.let { Time.parseFromTimestamptz(day.actualActiveTimeStart) },
//            actualActiveTimeEnd = day.actualActiveTimeEnd?.let { Time.parseFromTimestamptz(day.actualActiveTimeEnd) },
//            note = day.note
//        )
//    }
//    is ApiResponse.Loading -> {
//
//    }
//    is ApiResponse.Error -> {
//
//    }
//} as toDayState

//fun DayState.toDay(userId: UUID): Day = Day(
//fun DayState.toDay(userId: UUID): Day = Day(
fun DayState.toDay(): Day = Day(
//    userId = userId,
    date = this.date.toString(),
    activeTimeStart = this.activeTimeStart,
    activeTimeEnd = this.activeTimeEnd,
    actualActiveTimeStart = this.actualActiveTimeStart,
    actualActiveTimeEnd = this.actualActiveTimeEnd,
    note = this.note
)

fun DayUiState.toDay(zoneOffset: ZoneOffset?): Day = Day(
//    userId = userId,
    date = this.date.toString(),
    activeTimeStart = this.activeTimeStart.parseToTimestampTz(this.date),
    activeTimeEnd = this.activeTimeEnd.parseToTimestampTz(this.date),
    actualActiveTimeStart = this.actualActiveTimeStart?.parseToTimestampTz(this.date),
    actualActiveTimeEnd = this.actualActiveTimeEnd?.parseToTimestampTz(this.date),
    note = this.note
)

fun GoalUiState(goal: Goal?): GoalUiState {
    return (goal?.also {
        GoalUiState(
            id = goal.id,
            title = goal.title,
            priority = goal.priority
        )
    } ?: {
        GoalUiState()
    }) as GoalUiState
}

fun GoalUiStateList(goals: List<Goal>): List<GoalUiState> = goals.map { goal ->
    GoalUiState(
        id = goal.id,
        title = goal.title,
        priority = goal.priority
    )
}

fun GoalUiState.toGoal(): Goal = Goal(
    id = this.id!!,
    title = this.title,
    priority = this.priority!!
)

//fun TaskUiState.toSavedState(): TaskSavedState = TaskSavedState(
//    date = this.date,
//    id = this.id!!,
//    title = this.title,
//    startTime = this.startTime?.toString(),
//    endTime = this.endTime?.toString(),
//    description = this.description,
//    priority = this.priority,
//    pinned = this.pinned
//)

fun TaskUiState(task: Task?): TaskUiState {
    return (task?.also {
        TaskUiState(
//            date = task.date?.toLocalDate(),
//            id = task.id,
            id = UUID.fromString(task.id),
            createdAt = task.createdAt,
//            userId = task.userId,
            userId = UUID.fromString(task.userId),
//            date = task.date,
            date = LocalDate.parse(task.date),
            title = task.title,
            startTime = task.startTime?.let { Time.parseFromTimeTz(task.startTime!!) },
            endTime = task.endTime?.let { Time.parseFromTimeTz(task.endTime!!) },
            description = task.description,
            priority = task.priority,
            pinned = task.pinned != 0
        )
    } ?: {
        TaskUiState()
    }) as TaskUiState
}

// This method inserts a random UUID value as the task's id
//fun TaskUiState.toTask(userId: UUID): Task = Task(
fun TaskUiState.toTask(
    zoneOffset: ZoneOffset? = OffsetDateTime.now().offset,
//    userId: String
    userId: UUID
): Task = Task(
//    id = this.id ?: UUID.randomUUID(),
    id = this.id?.let { this.id.toString() } ?: UUID.randomUUID().toString(),
    createdAt = TimeZoneUtils.createTimestampTz(),
//    userId = userId,
    userId = userId.toString(),
//    date = this.date?.let { this.date.toOffsetDateTime(this.startTime!!, zoneOffset) },
//    date = this.date?.let { TimeZoneUtils.createDateString(this.date) },
//    date = this.date,
    date = this.date?.let { this.date.toString() },
    title = this.title,
    startTime = this.startTime?.parseToTimeTz(),
    endTime = this.endTime?.parseToTimeTz(),
    description = this.description,
    priority = this.priority,
    pinned = if (this.pinned) 1 else 0
)

//fun TaskUiStateList(tasks: List<Task>): List<TaskUiState> = tasks.map { task ->
//    Log.i("TaskUiStateList", task.toString())
//
//    TaskUiState(
////        date = task.date?.toLocalDate(),
//        id = UUID.fromString(task.id),
//        createdAt = task.createdAt,
//        userId =  UUID.fromString(task.userId),
//        date = task.date?.let { LocalDate.parse(task.date) },
//        title = task.title,
//        startTime = task.startTime?.let { Time.parseFromTimeTz(task.startTime!!) },
//        endTime = task.endTime?.let { Time.parseFromTimeTz(task.endTime!!) },
//        description = task.description,
//        priority = task.priority,
//        pinned = task.pinned
//    )
//}

fun TaskUiStateList(tasks: List<Task>): List<TaskUiState> {
    Log.i("TaskUiStateList", tasks.toString())

    return tasks.map { task ->
        Log.i("TaskUiStateList", task.toString())

        TaskUiState(
//        date = task.date?.toLocalDate(),
//            id = task.id,
            id = UUID.fromString(task.id),
            createdAt = task.createdAt,
//            userId = task.userId,
            userId = UUID.fromString(task.userId),
//            date = task.date,
            date = LocalDate.parse(task.date),
            title = task.title,
            startTime = task.startTime?.let { Time.parseFromTimeTz(task.startTime!!) },
            endTime = task.endTime?.let { Time.parseFromTimeTz(task.endTime!!) },
            description = task.description,
            priority = task.priority,
            pinned = task.pinned != 0
        )
    }
}

fun VoiceNoteUiState(voiceNote: VoiceNote?): VoiceNoteUiState {
    return (voiceNote?.also {
        VoiceNoteUiState(
            id = voiceNote.id,
            uri = voiceNote.uri,
            duration = voiceNote.duration.toLong(),
            recordedAt = voiceNote.recordedAt.toLong()
        )
    } ?: {
        VoiceNoteUiState()
    }) as VoiceNoteUiState
}

fun VoiceNoteUiStateList(voiceNotes: List<VoiceNote>): List<VoiceNoteUiState> =
    voiceNotes.map { voiceNote ->
        VoiceNoteUiState(
            id = voiceNote.id,
            uri = voiceNote.uri,
            duration = voiceNote.duration.toLong(),
            recordedAt = voiceNote.recordedAt.toLong()
        )
    }

fun VoiceNoteUiState.toVoiceNote(): VoiceNote = VoiceNote(
    uri = this.uri,
    duration = this.duration.toString(),
    recordedAt = this.recordedAt!!.toString(),
    activityId = this.activityId!!,
    id = this.id!!
)