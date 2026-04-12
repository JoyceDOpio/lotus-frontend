package com.eternalfairy.timeaware.ui.viewmodel.supabase

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eternalfairy.timeaware.db.Time
import com.eternalfairy.timeaware.db.data.Activity
import com.eternalfairy.timeaware.db.data.Day
import com.eternalfairy.timeaware.db.data.Goal
import com.eternalfairy.timeaware.db.data.Task
import com.eternalfairy.timeaware.db.data.VoiceNote
import com.eternalfairy.timeaware.db.supabase.ActivitiesRepository
import com.eternalfairy.timeaware.db.supabase.ApiResponse
import com.eternalfairy.timeaware.db.supabase.DaysRepository
import com.eternalfairy.timeaware.db.supabase.GoalsRepository
import com.eternalfairy.timeaware.db.supabase.TasksRepository
import com.eternalfairy.timeaware.db.supabase.VoiceNotesRepository
import com.eternalfairy.timeaware.ui.data.ActivityUiState
import com.eternalfairy.timeaware.ui.data.DayState
import com.eternalfairy.timeaware.ui.data.DayUiState
import com.eternalfairy.timeaware.ui.data.GoalUiState
import com.eternalfairy.timeaware.ui.data.TaskUiState
import com.eternalfairy.timeaware.ui.data.UserInput
import com.eternalfairy.timeaware.ui.data.VoiceNoteUiState
import com.eternalfairy.timeaware.utils.PowerSync
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
    @PowerSync private val activitiesRepository: ActivitiesRepository,
    private val daysRepository: DaysRepository,
    private val goalsRepository: GoalsRepository,
    private val tasksRepository: TasksRepository,
    private val voiceNotesRepository: VoiceNotesRepository
) : ViewModel() {
//    // Supabase
//    val authManager = AuthManager(context)

    // 1. Sign in anonymously (assuming the user is already signed in anonymously)

//    const { data: anonData, error: anonError } = await supabase.auth.getSessionStatus()

    init {
        viewModelScope.launch {
            activitiesRepository.getMainRecordedActivityStream().first().also { response ->
                when (response) {
                    is ApiResponse.Success -> {
                        val activity = response.data

                        recordedMainActivityUiState.value = ActivityUiState(
                            date = activity.date.toLocalDate(),
                            id = activity.id,
                            title = activity.title,
                            note = activity.note,
                            startTime = Time.parseFromTimestamptz(activity.startTime)
                        )
                    }

                    is ApiResponse.Loading -> {

                    }

                    is ApiResponse.Error -> {

                    }
                }

            }

            activitiesRepository.getSubRecordedActivityStream().first().also { response ->
                when (response) {
                    is ApiResponse.Success -> {
                        val activity = response.data

                        recordedSubActivityUiState.value = ActivityUiState(
                            date = activity.date.toLocalDate(),
                            id = activity.id,
                            title = activity.title,
                            note = activity.note,
                            startTime = Time.parseFromTimestamptz(activity.startTime),
                            mainActivityId = activity.mainActivityId
                        )
                    }

                    is ApiResponse.Loading -> {

                    }

                    is ApiResponse.Error -> {

                    }
                }

            }

            tasksRepository.getTasksWithoutDateStream().collect { response ->
                when (response) {
                    is ApiResponse.Success -> {
                        val toDoTasks = response.data.map { task ->
                            TaskUiState(
                                id = task.id,
                                title = task.title,
                                description = task.description,
                                date = task.date?.toLocalDate(),
                                startTime = Time.parseFromTimestamptz(task.startTime!!),// Tasks from day state should have a date, start and end time
                                endTime = task.endTime?.let { Time.parseFromTimestamptz(task.endTime!!) },
                                priority = task.priority,
                                pinned = task.pinned
                            )
                        }

                        _toDoTasks.value = toDoTasks
                    }
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Error -> {

                    }
                }
            }
        }
    }

    // ACTIVITIES
    // Activity details' UI state
    val selectedActivityId = MutableStateFlow(savedStateHandle.get<UUID>(ACTIVITY_ID_SAVED_STATE_KEY))

    inline fun <reified T> Collection<Flow<T>>.combine(): Flow<List<T>> {
        if (isEmpty()) return flowOf(emptyList())
        return combine(this) { it.toList() }
    }

    // The state of the activity that is displayed on the UI
    @OptIn(ExperimentalCoroutinesApi::class)
    val activityUiState = selectedActivityId.flatMapLatest { activityId ->
        if (activityId == null) return@flatMapLatest flowOf(ActivityUiState())

        val flowOfActivityUiState = activitiesRepository.getActivityStream(activityId).map(::ActivityUiState)
        val flowOfSubActivitiesUiState = activitiesRepository.getSubActivitiesPerMainActivityStream(activityId)
            .map(::ActivityUiStateList)
            // If I use map() instead of flatMapLatest(), I get Flow<Flow<List<ActivityUiState>>> instead of  Flow<List<ActivityUiState>>
            .flatMapLatest { subActivities ->
                val subActivitiesUiState = subActivities.map { subActivity ->
                    voiceNotesRepository.getVoiceNotesPerActivityStream(subActivity.id!!)
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
            voiceNotesRepository.getVoiceNotesPerActivityStream(activityId).map(::VoiceNoteUiStateList),
            flowOfSubActivitiesUiState
        ) {
            activityUiState,
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
    val activityToBeEditedId = MutableStateFlow(savedStateHandle.get<UUID>(
        ACTIVITY_TO_BE_EDITED_ID_SAVED_STATE_KEY
    ))
    val _activityEditState = MutableStateFlow(ActivityUiState())

    // The activityEditState is there only to update the _activityEditState
    @OptIn(ExperimentalCoroutinesApi::class)
    val activityEditState = activityToBeEditedId.flatMapLatest { id ->
        if (id == null) return@flatMapLatest flowOf(ActivityUiState())

        val flowOfActivityUiState = activitiesRepository.getActivityStream(id).map(::ActivityUiState)
        val flowOfSubActivitiesUiState = activitiesRepository.getSubActivitiesPerMainActivityStream(id)
            .map(::ActivityUiStateList)
            .flatMapLatest { subActivities ->
            val subActivitiesUiState = subActivities.map { subActivity ->
                voiceNotesRepository.getVoiceNotesPerActivityStream(subActivity.id!!)
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
            voiceNotesRepository.getVoiceNotesPerActivityStream(id).map(::VoiceNoteUiStateList),
            flowOfSubActivitiesUiState
        ) {
                activityUiState,
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
    val activityToBeDeletedId = MutableStateFlow(savedStateHandle.get<UUID>(
        ACTIVITY_TO_BE_DELETED_ID_SAVED_STATE_KEY
    ))
    @OptIn(ExperimentalCoroutinesApi::class)
    val activityToBeDeletedState = activityToBeDeletedId.flatMapLatest { id ->
        if (id == null) return@flatMapLatest flowOf(ActivityUiState())

        val flowOfActivityUiState = activitiesRepository.getActivityStream(id).map(::ActivityUiState)
        val flowOfSubActivities = activitiesRepository.getSubActivitiesPerMainActivityStream(id)
            .map(::ActivityUiStateList)
            .flatMapLatest { subActivities ->
            val subActivitiesUiState = subActivities.map { subActivity ->
                voiceNotesRepository.getVoiceNotesPerActivityStream(subActivity.id!!)
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
            voiceNotesRepository.getVoiceNotesPerActivityStream(id).map(::VoiceNoteUiStateList),
            flowOfSubActivities
        ) {
            activityUiState,
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

    val userInput = MutableStateFlow((savedStateHandle.get<UserInput>(
        USER_INPUT_SAVED_STATE_KEY
    )).let {
        it ?: UserInput()
    })

    val zoneOffset = MutableStateFlow<ZoneOffset?>(null)

    // Day UI state
    val dayEditState = MutableStateFlow(DayUiState())

    // Day state
    @OptIn(ExperimentalCoroutinesApi::class)
    val dayUiState = userInput.flatMapLatest { input: UserInput ->
        combine(
            flow = daysRepository.getDayStream(input.selectedDate.toString()).map(::toDayState),
            flow2 = tasksRepository.getTasksPerDayStream(input.selectedDate.toString()).map(::TaskUiStateList),
            flow3 = activitiesRepository.getMainActivitiesPerDayStream(input.selectedDate.toString()).map(::ActivityUiStateList)
        ) {
            day,
            tasksUiState,
            activitiesUiState ->
            // Update the day UI state with the actual active time start and end
            day?.let {

                dayEditState.update {
                    it.copy(
                        activeTimeStart = Time.parseFromTimestamptz(day.activeTimeStart) ?: Time(6, 0),//TODO: Take the default value from settings
                        activeTimeEnd = Time.parseFromTimestamptz(day.activeTimeEnd) ?: Time(22, 0),//TODO: Take the default value from settings
                        actualActiveTimeStart = day.actualActiveTimeStart?.let { Time.parseFromTimestamptz(day.actualActiveTimeStart) },
                        actualActiveTimeEnd = day.actualActiveTimeEnd?.let { Time.parseFromTimestamptz(day.actualActiveTimeEnd) },
                        tasks = tasksUiState.sortedWith { a, b -> a.compareTo(b) },// Sort the tasks based on their start time
                        activities = activitiesUiState,
                        note = day.note ?: ""
                    )
                }

                zoneOffset.update {
                    day.date.offset
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
                    activeTimeStart = Time.parseFromTimestamptz(day.activeTimeStart) ?: Time(6, 0),//TODO: Take the default value from settings
                    activeTimeEnd = Time.parseFromTimestamptz(day.activeTimeEnd) ?: Time(22, 0),//TODO: Take the default value from settings
                    actualActiveTimeStart = day.actualActiveTimeStart?.let { Time.parseFromTimestamptz(day.actualActiveTimeStart) },
                    actualActiveTimeEnd = day.actualActiveTimeEnd?.let { Time.parseFromTimestamptz(day.actualActiveTimeEnd) },
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

        goalsRepository
            .getGoalStream(id)
            .map(::GoalUiState)
            .map { goal ->
                _goalEditState.update {
                    goal
                }

                goal
            }
    }

    val goals = goalsRepository
        .getGoalsStream()
        .map(::GoalUiStateList)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = emptyList<GoalUiState>()
        )

    val lastGoalPriority = goalsRepository
        .getLastPriority()
        .map { response ->
            when (response) {
                is ApiResponse.Success -> {
                    val priority = response.data
                    priority
                }
                else -> null
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = 0
        )

    // The priority of the last task
    val lastTaskPriority = tasksRepository
        .getLastPriority()
        .map { response ->
            when (response) {
                is ApiResponse.Success -> {
                    val priority = response.data
                    priority
                }
                else -> null
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = 0
        )

    // Task details' UI state
    val _taskEditState = MutableStateFlow(TaskUiState())
//    val selectedTaskId = MutableStateFlow(savedStateHandle.get<UUID>(TASK_ID_SAVED_STATE_KEY))
//    val taskUiState = MutableStateFlow((savedStateHandle.get<TaskSavedState>(TASK_SAVED_STATE_KEY)).let { it ->
    val taskEditState = savedStateHandle.getStateFlow<UUID?>(TASK_ID_SAVED_STATE_KEY, null).flatMapLatest { id ->
        if (id == null) {
            _taskEditState.update { TaskUiState() }

            flowOf(TaskUiState())
//            return@flatMapLatest TaskUiState()
        } else {
            tasksRepository
                .getTaskStream(id)
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

    val voiceNoteToBeDeletedId = MutableStateFlow(savedStateHandle.get<UUID>(
        VOICE_NOTE_TO_BE_DELETED_ID_SAVED_STATE_KEY
    ))
    @OptIn(ExperimentalCoroutinesApi::class)
    val voiceNoteToBeDeletedState = voiceNoteToBeDeletedId.flatMapLatest { id ->
        if (id == null) return@flatMapLatest flowOf(VoiceNoteUiState())

        voiceNotesRepository.getVoiceNoteStream(id).map(::VoiceNoteUiState)
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
            activitiesRepository.deleteActivity(id).collect { response ->
                when (response) {
                    is ApiResponse.Success -> {

                    }
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Error -> {

                    }
                }
            }
        }
    }

    fun deleteGoal(id: UUID) {
        viewModelScope.launch {
            goalsRepository.deleteGoal(id).collect { response ->
                when (response) {
                    is ApiResponse.Success -> {

                    }
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Error -> {

                    }
                }
            }
        }
    }

    fun deleteTask(id: UUID) {
        viewModelScope.launch {
            tasksRepository.deleteTask(id).collect { response ->
                when (response) {
                    is ApiResponse.Success -> {

                    }
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Error -> {

                    }
                }
            }
        }
    }

    fun deleteVoiceNote(id: UUID) {
        viewModelScope.launch {
            voiceNotesRepository.deleteVoiceNote(id).collect { response ->
                when (response) {
                    is ApiResponse.Success -> {

                    }
                    is ApiResponse.Loading -> {

                    }
                    is ApiResponse.Error -> {

                    }
                }
            }
        }
    }

    fun saveActivity() {
        viewModelScope.launch {
            // If activity exists, update it
            val activityId = _activityEditState.value.id

            activityId?.let {
                val activityUiState = dayUiState.value.activities.find { it.id == activityId }

                if (activityUiState == null) {
                    activitiesRepository.insertActivity(
                        _activityEditState.value.toActivity(zoneOffset.value)
                    )
                } else {
                    activitiesRepository.updateActivity(_activityEditState.value.toActivity(zoneOffset.value))
                }
            }
        }
    }

    fun saveDay() {
        viewModelScope.launch {
            daysRepository.insertDay(
                dayUiState.value.toDay(zoneOffset.value).copy(
                    activeTimeStart = dayEditState.value.activeTimeStart.parseToTimestamptz(dayUiState.value.date),
                    activeTimeEnd = dayEditState.value.activeTimeEnd.parseToTimestamptz(dayUiState.value.date),
                    actualActiveTimeStart = dayEditState.value.actualActiveTimeStart?.let {
                        dayEditState.value.actualActiveTimeStart!!.parseToTimestamptz(
                            dayUiState.value.date
                        )
                    },
                    actualActiveTimeEnd = dayEditState.value.actualActiveTimeEnd?.let {
                        dayEditState.value.actualActiveTimeEnd!!.parseToTimestamptz(
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
                    goalsRepository.updateGoal(updatedGoal)
                }
            }
            // Else, save the new goal
            else {
                goalsRepository.insertGoal(_goalEditState.value.toGoal())
            }
        }
    }

    fun saveGoal(goal: GoalUiState) {//FIXME: Optimise this method
        viewModelScope.launch {
            // If goal exists, update it
            val found = goals.value.find { searchedGoal -> searchedGoal.id == goal.id }

            if (found != null) {
                goalsRepository.updateGoal(goal.toGoal())
            }
            // Else, save the new goal
            else {
                goalsRepository.insertGoal(goal.toGoal())
            }
        }
    }

    fun saveMainRecordedActivity() {
        viewModelScope.launch {
            // If activity exists, update it
            val activityId = recordedMainActivityUiState.value.id

            val activityUiState = dayUiState.value.activities.find { it.id == activityId }

            if (activityUiState == null) {
                activitiesRepository.insertActivity(
                    recordedMainActivityUiState.value.toActivity(zoneOffset.value)
                )
            } else {
                activitiesRepository.updateActivity(recordedMainActivityUiState.value.toActivity(zoneOffset.value))
            }
        }
    }

    fun saveSubRecordedActivity() {
        viewModelScope.launch {
            // If activity exists, update it
            val activityId = recordedSubActivityUiState.value.id

            val activityUiState = dayUiState.value.activities.find { it.id == activityId }

            if (activityUiState == null) {
                activitiesRepository.insertActivity(
                    recordedSubActivityUiState.value.toActivity(zoneOffset.value)
                )
            } else {
                activitiesRepository.updateActivity(recordedSubActivityUiState.value.toActivity(zoneOffset.value))
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
                    val updatedTask = _taskEditState.value.toTask(zoneOffset.value).copy(
                        id = taskId
                    )
                    tasksRepository.updateTask(updatedTask)
                }
                // Else, save the new task
                else {
                    tasksRepository.insertTask(_taskEditState.value.toTask(zoneOffset.value))
                }
            }
            // Else, save the new task
            else {
//                // There are no tasks for the given day, save the day.
//                if (dayState.value.tasks.isEmpty()) {//FIXME: If I save the day before I save the task the taskUiState is emptied. The task can still be successfully saved to the database without the corresponding day being persisted in the database.
//                    daysRepository.insertDay(dayState.value.toDay())
//                }

                tasksRepository.insertTask(_taskEditState.value.toTask(zoneOffset.value))
            }
        }
    }

    fun saveTask(task: TaskUiState) {
        viewModelScope.launch {
            val tasks = dayUiState.value.tasks + toDoTasks.value
            val found = tasks.find { iteratedTask -> iteratedTask.id == task.id }

            if (found != null) {
                tasksRepository.updateTask(task.toTask(zoneOffset.value))
            }
            else {
                tasksRepository.insertTask(task.toTask(zoneOffset.value))
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
            voiceNotesRepository.insertVoiceNote(voiceNote.toVoiceNote())
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

    fun setTaskDate (date: LocalDate?) {
        _taskEditState.update {
            it.copy(
                date = date
            )
        }
//        saveTaskToStateHandle()
    }

    fun setTaskDescription (description: String) {
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

    fun setTaskTitle (title: String) {
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
            tasksRepository.updateTask(task.toTask(zoneOffset.value))
        }
    }
}

fun ActivityUiState(response: ApiResponse<Activity>): ActivityUiState = when (response) {
    is ApiResponse.Success -> {
        val activity = response.data

        ActivityUiState(
            date = activity.date.toLocalDate(),
            title = activity.title,
            note = activity.note,
            startTime = Time.parseFromTimestamptz(activity.startTime),
            endTime = activity.endTime?.let { Time.parseFromTimestamptz(activity.endTime!!) },
            id = activity.id!!,
            mainActivityId = activity.mainActivityId
        )
    }

    is ApiResponse.Loading -> {

    }

    is ApiResponse.Error -> {

    }
} as ActivityUiState

fun ActivityUiStateList(response: ApiResponse<List<Activity>>): List<ActivityUiState> = when (response) {
    is ApiResponse.Success -> {
        response.data.map { activity ->
            ActivityUiState(
                date = activity.date.toLocalDate(),
                title = activity.title,
                note = activity.note,
                startTime = Time.parseFromTimestamptz(activity.startTime),
                endTime = activity.endTime?.let { Time.parseFromTimestamptz(activity.endTime!!) },
                id = activity.id!!,
                mainActivityId = activity.mainActivityId
            )
        }
    }

    is ApiResponse.Loading -> {

    }

    is ApiResponse.Error -> {

    }
} as List<ActivityUiState>

fun LocalDate.toOffsetDateTime(time: Time, zoneOffset: ZoneOffset?): OffsetDateTime {//FIXME: I'm not sure zoneOffset can be null
    val localTime = LocalTime.of(time.hour, time.minute)
    val localDateTime = LocalDateTime.of(this, localTime)
    val offsetDateTime = OffsetDateTime.of(localDateTime, zoneOffset)

    return offsetDateTime
}

//fun ActivityUiState.toActivity(userId: UUID): Activity = Activity(
fun ActivityUiState.toActivity(zoneOffset: ZoneOffset?): Activity = Activity(
//    userId = userId,
    date = this.date.toOffsetDateTime(this.startTime, zoneOffset),
    title = this.title,
    note = this.note,
    startTime = this.startTime.parseToTimestamptz(this.date),
    endTime = this.endTime?.parseToTimestamptz(this.date),
    id = this.id!!,
    mainActivityId = this.mainActivityId
)

//fun ActivityUiState.toSavedState(): ActivitySavedState = ActivitySavedState(
//    date = this.date,
//    id = this.id!!,
//    title = this.title,
//    startTime = this.startTime.toString()
//)

fun toDayState(response: ApiResponse<Day>): DayState = when (response) {
    is ApiResponse.Success -> {
        val day = response.data

        DayState(
            date = day.date,
            activeTimeStart = day.activeTimeStart,
            activeTimeEnd = day.activeTimeEnd,
            actualActiveTimeStart = day.actualActiveTimeStart,
            actualActiveTimeEnd = day.actualActiveTimeEnd,
            note = day.note
        )
    }
    is ApiResponse.Loading -> {

    }
    is ApiResponse.Error -> {

    }
} as DayState

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
    date = this.date,
    activeTimeStart = this.activeTimeStart,
    activeTimeEnd = this.activeTimeEnd,
    actualActiveTimeStart = this.actualActiveTimeStart,
    actualActiveTimeEnd = this.actualActiveTimeEnd,
    note = this.note
)

fun DayUiState.toDay(zoneOffset: ZoneOffset?): Day = Day(
//    userId = userId,
    date = this.date.toOffsetDateTime(this.activeTimeStart, zoneOffset),
    activeTimeStart = this.activeTimeStart.parseToTimestamptz(this.date),
    activeTimeEnd = this.activeTimeEnd.parseToTimestamptz(this.date),
    actualActiveTimeStart = this.actualActiveTimeStart?.parseToTimestamptz(this.date),
    actualActiveTimeEnd = this.actualActiveTimeEnd?.parseToTimestamptz(this.date),
    note = this.note
)

fun GoalUiState(response: ApiResponse<Goal>): GoalUiState = when (response) {
    is ApiResponse.Success -> {
        val goal = response.data

        GoalUiState(
            id = goal.id,
            title = goal.title,
            priority = goal.priority
        )
    }
    is ApiResponse.Loading -> {

    }
    is ApiResponse.Error -> {

    }
} as GoalUiState

fun GoalUiStateList(response: ApiResponse<List<Goal>>): List<GoalUiState> = when (response) {
    is ApiResponse.Success -> {
        response.data.map { goal ->
            GoalUiState(
                id = goal.id,
                title = goal.title,
                priority = goal.priority
            )
        }
    }
    is ApiResponse.Loading -> {

    }
    is ApiResponse.Error -> {

    }
} as List<GoalUiState>

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

fun TaskUiState(response: ApiResponse<Task>): TaskUiState = when (response) {
    is ApiResponse.Success -> {
        val task = response.data

        TaskUiState(
            date = task.date?.toLocalDate(),
            id = task.id,
            title = task.title,
            startTime = task.startTime?.let { Time.parse(task.startTime!!) },
            endTime = task.endTime?.let { Time.parse(task.endTime!!) },
            description = task.description,
            priority = task.priority,
            pinned = task.pinned
        )
    }
    is ApiResponse.Loading -> {

    }
    is ApiResponse.Error -> {

    }
} as TaskUiState

// This method inserts a random UUID value as the task's id
//fun TaskUiState.toTask(userId: UUID): Task = Task(
fun TaskUiState.toTask(zoneOffset: ZoneOffset?): Task = Task(
//    userId = userId,
    date = this.date?.let { this.date.toOffsetDateTime(this.startTime!!, zoneOffset) },
    title = this.title,
    startTime = this.date?.let { this.startTime?.parseToTimestamptz(this.date) },
    endTime = this.date?.let { this.endTime?.parseToTimestamptz(this.date) },
    description = this.description,
    priority = this.priority,
    pinned = this.pinned
)

fun TaskUiStateList(response: ApiResponse<List<Task>>): List<TaskUiState> = when (response) {
    is ApiResponse.Success -> {
        val tasks = response.data

        tasks.map { task ->
            TaskUiState(
                date = task.date?.toLocalDate(),
                id = task.id,
                title = task.title,
                startTime = task.startTime?.let { Time.parse(task.startTime!!) },
                endTime = task.endTime?.let { Time.parse(task.endTime!!) },
                description = task.description,
                priority = task.priority,
                pinned = task.pinned
            )
        }
    }
    is ApiResponse.Loading -> {

    }
    is ApiResponse.Error -> {

    }
} as List<TaskUiState>

fun VoiceNoteUiState(response: ApiResponse<VoiceNote>): VoiceNoteUiState = when (response) {
    is ApiResponse.Success -> {
        val voiceNote = response.data

        VoiceNoteUiState(
            id = voiceNote.id,
            uri = voiceNote.uri,
            duration = voiceNote.duration.toLong(),
            timestamp = voiceNote.timestamp.toLong()
        )
    }
    is ApiResponse.Loading -> {

    }
    is ApiResponse.Error -> {

    }
} as VoiceNoteUiState

fun VoiceNoteUiStateList(response: ApiResponse<List<VoiceNote>>): List<VoiceNoteUiState> = when (response) {
    is ApiResponse.Success -> {
        response.data.map { voiceNote ->
            VoiceNoteUiState(
                id = voiceNote.id,
                uri = voiceNote.uri,
                duration = voiceNote.duration.toLong(),
                timestamp = voiceNote.timestamp.toLong()
            )
        }
    }
    is ApiResponse.Loading -> {

    }
    is ApiResponse.Error -> {

    }
} as List<VoiceNoteUiState>

fun VoiceNoteUiState.toVoiceNote(): VoiceNote = VoiceNote(
    uri = this.uri,
    duration = this.duration.toString(),
    timestamp = this.timestamp!!.toString(),
    activityId = this.activityId!!,
    id = this.id!!
)