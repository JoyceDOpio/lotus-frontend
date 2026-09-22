package com.eternalfairy.lotus.view.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eternalfairy.lotus.data.dao.postgres.ApiResponse
import com.eternalfairy.lotus.data.model.Activity
import com.eternalfairy.lotus.data.model.Day
import com.eternalfairy.lotus.data.model.Goal
import com.eternalfairy.lotus.data.model.Task
import com.eternalfairy.lotus.data.model.VoiceNote
import com.eternalfairy.lotus.domain.repository.IActivityRepository
import com.eternalfairy.lotus.domain.repository.IDayRepository
import com.eternalfairy.lotus.domain.repository.IGoalRepository
import com.eternalfairy.lotus.domain.repository.ITaskRepository
import com.eternalfairy.lotus.domain.repository.IVoiceNoteRepository
import com.eternalfairy.lotus.view.data.ActivityUiState
import com.eternalfairy.lotus.view.data.DayUiState
import com.eternalfairy.lotus.view.data.GoalUiState
import com.eternalfairy.lotus.view.data.TaskUiState
import com.eternalfairy.lotus.view.data.VoiceNoteUiState
import com.eternalfairy.lotus.view.screen.planner.PlannerUiEvent
import com.eternalfairy.lotus.view.screen.planner.PlannerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val activityRepository: IActivityRepository,
    private val dayRepository: IDayRepository,
    private val goalRepository: IGoalRepository,
    private val taskRepository: ITaskRepository,
    private val voiceNoteRepository: IVoiceNoteRepository
) : ViewModel() {

    @OptIn(ExperimentalUuidApi::class)
    var state by mutableStateOf(PlannerUiState())
    private val responseChannel = Channel<ApiResponse<Unit>>()
    val apiResponses = responseChannel.receiveAsFlow()
    val today = LocalDate.parse(java.time.LocalDate.now().toString())

    init {
        loadInitialData()
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun loadInitialData() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            loadDay(today)
            loadTasks(today)
            loadActivities(today)
            loadRecordedMainActivity()
            loadRecordedSubActivity()
            loadGoals()
            loadToDoTasks()

            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun onEvent(event: PlannerUiEvent) {
        when (event) {
            // ACTIVITY
            is PlannerUiEvent.ActivityToBeDeletedIdChanged -> {
                state = state.copy(
                    activityToBeDeletedId = event.value
                )
            }

            is PlannerUiEvent.ActivityToBeEditedIdChanged -> {
                if (event.value == null) {
                    state = state.copy(
                        editedActivity = ActivityUiState()
                    )
                } else {
                    for (activity in (state.activities + state.subActivities)) {
                        if (activity.id == event.value) {
                            state = state.copy(
                                editedActivity = activity
                            )
                            break
                        }
                    }
                }
            }

            is PlannerUiEvent.DeleteActivity -> {
                deleteActivity(state.activityToBeDeletedId!!)

                if(state.activityToBeDeletedId == state.selectedActivity.id) {
                    state = state.copy(
                        selectedActivity = ActivityUiState()
                    )
                }
                state = state.copy(
                    activityToBeDeletedId = null
                )
                loadActivities(state.selectedDate)
                loadSubActivities()
                loadVoiceNotes()
            }

            is PlannerUiEvent.ActivityNoteChanged -> {
                state = state.copy(
                    editedActivity = state.editedActivity.copy(
                        note = event.value
                    )
                )
            }

            is PlannerUiEvent.ActivityTitleChanged -> {
                state = state.copy(
                    editedActivity = state.editedActivity.copy(
                        title = event.value
                    )
                )
            }

            is PlannerUiEvent.SaveActivity -> {
                editActivity(state.editedActivity.toActivity())
                state = state.copy(
                    editedActivity = ActivityUiState()
                )

                for (activity in state.activities) {
                    if (activity.id == state.selectedActivity.id) {
                        state = state.copy(
                            selectedActivity = activity,
//                                editedActivity = activity
                        )
                    }
                    break
                }

                loadSubActivities()
                loadVoiceNotes()
                loadActivities(state.selectedDate)
            }

            is PlannerUiEvent.SelectedActivityIdChanged -> {
                if (event.value == null) {
                    state = state.copy(
                        editedActivity = ActivityUiState(),
                        selectedActivity = ActivityUiState(),
                        subActivities = emptyList(),
                        voiceNotes = emptyList()
                    )
                } else {
//                    loadActivities(state.selectedDate)
                    for (activity in state.activities) {
                        if (activity.id == event.value) {
                            state = state.copy(
                                editedActivity = activity,
                                selectedActivity = activity
                            )
                            loadSubActivities()
                            loadVoiceNotes()
                            break
                        }
                    }
                }
            }

            // - main
            is PlannerUiEvent.RecordedMainActivityIdChanged -> {
                state = state.copy(
                    recordedActivityMain = state.recordedActivityMain.copy(
                        id = event.value
                    )
                )
            }

            is PlannerUiEvent.RecordedMainActivityDateChanged -> {
                state = state.copy(
                    recordedActivityMain = state.recordedActivityMain.copy(
                        date = event.value
                    )
                )
            }

            is PlannerUiEvent.RecordedMainActivityNoteChanged -> {
                state = state.copy(
                    recordedActivityMain = state.recordedActivityMain.copy(
                        note = event.value
                    )
                )
            }

            is PlannerUiEvent.RecordedMainActivityTitleChanged -> {
                state = state.copy(
                    recordedActivityMain = state.recordedActivityMain.copy(
                        title = event.value
                    )
                )
            }

            is PlannerUiEvent.RecordedMainActivityStartTimeChanged -> {
                state = state.copy(
                    recordedActivityMain = state.recordedActivityMain.copy(
                        startTime = event.value
                    )
                )
            }

            is PlannerUiEvent.RecordedMainActivityEndTimeChanged -> {
                state = state.copy(
                    recordedActivityMain = state.recordedActivityMain.copy(
                        endTime = event.value
                    )
                )
            }

            is PlannerUiEvent.EditRecordedMainActivity -> {
                editActivity(state.recordedActivityMain.toActivity())
                loadRecordedMainActivity()
                loadActivities(today)
            }

            is PlannerUiEvent.StartMainActivity -> {
                startActivity(state.recordedActivityMain.toActivity())
                loadRecordedMainActivity()
                loadActivities(today)
            }

            is PlannerUiEvent.StopMainActivity -> {
                editActivity(state.recordedActivityMain.toActivity())
                state = state.copy(
                    recordedActivityMain = ActivityUiState()
                )
                loadRecordedMainActivity()
                loadActivities(today)
            }

            // - sub
            is PlannerUiEvent.RecordedSubActivityIdChanged -> {
                state = state.copy(
                    recordedActivitySub = state.recordedActivitySub.copy(
                        id = event.value
                    )
                )
            }

            is PlannerUiEvent.RecordedSubActivityNoteChanged -> {
                state = state.copy(
                    recordedActivitySub = state.recordedActivitySub.copy(
                        note = event.value
                    )
                )
            }

            is PlannerUiEvent.RecordedSubActivityTitleChanged -> {
                state = state.copy(
                    recordedActivitySub = state.recordedActivitySub.copy(
                        title = event.value
                    )
                )
            }

            is PlannerUiEvent.RecordedSubActivityStartTimeChanged -> {
                state = state.copy(
                    recordedActivitySub = state.recordedActivitySub.copy(
                        startTime = event.value
                    )
                )
            }

            is PlannerUiEvent.RecordedSubActivityEndTimeChanged -> {
                state = state.copy(
                    recordedActivitySub = state.recordedActivitySub.copy(
                        endTime = event.value
                    )
                )
            }

            is PlannerUiEvent.RecordedSubActivityMainActivityIdChanged -> {
                state = state.copy(
                    recordedActivitySub = state.recordedActivitySub.copy(
                        mainActivityId = event.value
                    )
                )
            }

            is PlannerUiEvent.EditRecordedSubActivity -> {
                editActivity(state.recordedActivitySub.toActivity())
                loadRecordedSubActivity()
                loadActivities(today)
            }

            is PlannerUiEvent.StartSubActivity -> {
                startActivity(state.recordedActivitySub.toActivity())
                loadRecordedSubActivity()
                loadActivities(today)
            }

            is PlannerUiEvent.StopSubActivity -> {
                editActivity(state.recordedActivitySub.toActivity())
                state = state.copy(
                    recordedActivitySub = ActivityUiState()
                )
                loadRecordedSubActivity()
                loadActivities(today)
            }

            // DAY
            is PlannerUiEvent.ActiveTimeStartChanged -> {
                state = state.copy(editedDay = state.editedDay.copy(activeTimeStart = event.value))
            }

            is PlannerUiEvent.ActiveTimeEndChanged -> {
                state = state.copy(editedDay = state.editedDay.copy(activeTimeEnd = event.value))
            }

            is PlannerUiEvent.ActualActiveTimeStartChanged -> {
                state =
                    state.copy(editedDay = state.editedDay.copy(actualActiveTimeStart = event.value))
            }

            is PlannerUiEvent.ActualActiveTimeEndChanged -> {
                state =
                    state.copy(editedDay = state.editedDay.copy(actualActiveTimeEnd = event.value))
            }

            is PlannerUiEvent.SelectedDateChanged -> {
                state = state.copy(
                    selectedDate = event.value
                )
                loadDay(state.selectedDate)
                loadTasks(state.selectedDate)
                loadActivities(state.selectedDate)
            }

            is PlannerUiEvent.SaveDay -> {
                saveDay(state.editedDay.toDay())
                loadDay(state.selectedDate)
            }

            // GOAL
            is PlannerUiEvent.DeleteGoal -> {
                val id = state.selectedGoal.id

                id?.let {
                    deleteGoal(id)
                    state = state.copy(
//                        goalToBeDeletedId = null
                        editedGoal = GoalUiState(),
                        selectedGoal = GoalUiState()
                    )
                    loadGoals()
                }
            }

            is PlannerUiEvent.GoalPriorityChanged -> {
                state = state.copy(
                    editedGoal = state.editedGoal.copy(
                        priority = event.value
                    )
                )
            }

            is PlannerUiEvent.GoalTitleChanged -> {
                state = state.copy(
                    editedGoal = state.editedGoal.copy(
                        title = event.value
                    )
                )
            }

//            is PlannerUiEvent.GoalToBeDeletedIdChanged -> {
//                state = state.copy(
//                    goalToBeDeletedId = event.value
//                )
//            }

            is PlannerUiEvent.LastGoalPriorityChanged -> {
                state = state.copy(
                    lastGoalPriority = event.value
                )
            }

            is PlannerUiEvent.SaveGoal -> {
                saveGoal(state.editedGoal.toGoal())
//                state = state.copy(
//                    editedGoal = GoalUiState()
//                )
                loadGoals()
                selectGoal(state.selectedGoal.id)
            }

            is PlannerUiEvent.SelectedGoalIdChanged -> {
                selectGoal(event.value)
            }

            // TASK
            is PlannerUiEvent.DeleteTask -> {
                val idToBeDeleted = state.selectedTask.id

                idToBeDeleted?.let {
                    deleteTask(idToBeDeleted)
                    state = state.copy(
//                            taskToBeDeletedId = null
                        selectedTask = TaskUiState()
                    )
                    loadTasks(state.selectedDate)
                    loadToDoTasks()
                }
            }

            is PlannerUiEvent.LastTaskPriorityChanged -> {
                state = state.copy(
                    lastTaskPriority = event.value
                )
            }

            is PlannerUiEvent.SaveTask -> {
                saveTask(state.editedTask.toTask())

                loadTasks(state.selectedDate)
                loadToDoTasks()
                selectTask(state.editedTask.id)
            }

            is PlannerUiEvent.SelectedTaskIdChanged -> {
                selectTask(event.value)
            }

            is PlannerUiEvent.TaskEndTimeChanged -> {
                state = state.copy(
                    editedTask = state.editedTask.copy(
                        endTime = event.value
                    )
                )
            }

            is PlannerUiEvent.TaskDateChanged -> {
                state = state.copy(
                    editedTask = state.editedTask.copy(
                        date = event.value
                    )
                )
            }

            is PlannerUiEvent.TaskIdChanged -> {
                state = state.copy(
                    editedTask = state.editedTask.copy(
                        id = event.value
                    )
                )
            }

            is PlannerUiEvent.TaskDescriptionChanged -> {
                state = state.copy(
                    editedTask = state.editedTask.copy(
                        description = event.value
                    )
                )
            }

            is PlannerUiEvent.TaskPinnedChanged -> {
                state = state.copy(
                    editedTask = state.editedTask.copy(
                        pinned = event.value
                    )
                )
            }

            is PlannerUiEvent.TaskPriorityChanged -> {
                state = state.copy(
                    editedTask = state.editedTask.copy(
                        priority = event.value
                    )
                )
            }

            is PlannerUiEvent.TaskStartTimeChanged -> {
                state = state.copy(
                    editedTask = state.editedTask.copy(
                        startTime = event.value
                    )
                )
            }

            is PlannerUiEvent.TaskTitleChanged -> {
                state = state.copy(
                    editedTask = state.editedTask.copy(
                        title = event.value
                    )
                )
            }

//            is PlannerUiEvent.TaskToBeDeletedIdChanged -> {
//                state = state.copy(
//                    taskToBeDeletedId = event.value
//                )
//            }

            // VOICE NOTE
            is PlannerUiEvent.SaveVoiceNote -> {
                addVoiceNote(state.recordedVoiceNote.toVoiceNote())
                state = state.copy(
                    recordedVoiceNote = VoiceNoteUiState()
                )
            }

            is PlannerUiEvent.DeleteVoiceNote -> {
                deleteVoiceNote(state.voiceNoteToBeDeletedId!!)
                state = state.copy(
                    voiceNoteToBeDeletedId = null
                )
            }

            is PlannerUiEvent.RecordedVoiceNoteChanged -> {
                state = state.copy(
                    recordedVoiceNote = event.value
                )
            }

            is PlannerUiEvent.VoiceNoteToBeDeletedIdChanged -> {
                state = state.copy(
                    voiceNoteToBeDeletedId = event.value
                )
            }
        }
    }

    // ACTIVITY
    @OptIn(ExperimentalUuidApi::class)
    fun deleteActivity(id: Uuid) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val response = activityRepository.deleteActivity(id)
            responseChannel.send(response)

            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun editActivity(activity: Activity) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val response = activityRepository.editActivity(activity)
//            responseChannel.send(response)

            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun loadActivities(date: LocalDate) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            when (val response = activityRepository.getMainActivitiesPerDay(date)) {
                is ApiResponse.Success -> {
                    if (response.data != null) state = state.copy(
                        activities = ActivityUiStateList(response.data)
                    )
                }

                is ApiResponse.Error -> {
                    // TODO: Handle error
                }
            }

            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun loadRecordedMainActivity() {
        viewModelScope.launch {
            when (val response = activityRepository.getRecordedActivityMain()) {
                is ApiResponse.Success -> {
                    if (response.data != null) state = state.copy(
                        recordedActivityMain = response.data.toActivityUiState()
                    )
                }

                is ApiResponse.Error -> {
                    // TODO: Handle error
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun loadRecordedSubActivity() {
        viewModelScope.launch {
            when (val response = activityRepository.getRecordedActivitySub()) {
                is ApiResponse.Success -> {
                    if (response.data != null) state = state.copy(
                        recordedActivitySub = response.data.toActivityUiState()
                    )
                }

                is ApiResponse.Error -> {
                    // TODO: Handle error
                }
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun loadSubActivities() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val selectedActivityId = state.selectedActivity.id

            selectedActivityId?.let {
                when (val response = activityRepository.getSubActivitiesPerMainActivity(it)) {
                    is ApiResponse.Success -> {
                        if (response.data != null) state = state.copy(
                            subActivities = ActivityUiStateList(response.data)
                        )
                    }

                    is ApiResponse.Error -> {
                        // TODO: Handle error
                    }
                }
            }

            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun startActivity(activity: Activity) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val response = activityRepository.addActivity(activity)
//            responseChannel.send(response)

            state = state.copy(isLoading = false)
        }
    }

    // DAY
    @OptIn(ExperimentalUuidApi::class)
    fun loadDay(date: LocalDate) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val day = DayUiState(dayRepository.getDay(date))
            state = state.copy(
                selectedDay = day,
                editedDay = day
            )

            val activities = ActivityUiStateList(activityRepository.getMainActivitiesPerDay(date))
            state = state.copy(
                activities = activities
            )

            val tasks = TaskUiStateList(taskRepository.getTasksPerDay(date))
            state = state.copy(
                tasks = tasks
            )



            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveDay(day: Day) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val response = dayRepository.editDay(day)
            responseChannel.send(response)

            state = state.copy(isLoading = false)
        }
    }

    // GOAL
    @OptIn(ExperimentalUuidApi::class)
    fun deleteGoal(id: Uuid) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val response = goalRepository.deleteGoal(id)

            responseChannel.send(response)

            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun loadGoals() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            when (val response = goalRepository.getGoals()) {
                is ApiResponse.Success -> {
                    if (response.data != null) state = state.copy(goals = GoalUiStateList(response.data))
                }

                is ApiResponse.Error -> {
                    // TODO: Handle error
                }
            }

            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveGoal(goal: Goal) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            var priorityVal = 1
            val lastPriorityResponse = goalRepository.getLastPriority()

            when (lastPriorityResponse) {
                is ApiResponse.Success -> {
                    lastPriorityResponse.data?.let {
                        priorityVal = it + 1
                    }
                }

                else -> {}
            }

            val response = if (goal.id == null) {
                goalRepository.addGoal(
                    goal.copy(
                        id = Uuid.random(),
                        priority = priorityVal
                    )
                )
            } else {
                goalRepository.editGoal(goal)
            }
//            responseChannel.send(response) <- fixme: jak to jest odcommentowane, to program się zawiesza

            state = state.copy(isLoading = false)
        }
    }

    fun selectGoal(id: Uuid?) {
        if (id == null) {
            state = state.copy(
                editedGoal = GoalUiState(),
                selectedGoal = GoalUiState(),
//                selectedGoalId = null
            )
        } else {
            for (goal in state.goals) {
                if (goal.id == id) {
                    state = state.copy(
                        editedGoal = goal,
                        selectedGoal = goal,
//                        selectedGoalId = goal.id
                    )
                }
            }
        }
    }

    // TASK
    @OptIn(ExperimentalUuidApi::class)
    fun deleteTask(id: Uuid) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val response = taskRepository.deleteTask(id)
            responseChannel.send(response)

            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun loadTasks(date: LocalDate) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            when (val response = taskRepository.getTasksPerDay(date)) {
                is ApiResponse.Success -> {
                    if (response.data != null) {
                        state = state.copy(
                            tasks = TaskUiStateList(response)
                        )
                    }
                }

                is ApiResponse.Error -> {
                    // TODO: Handle error
                }
            }

            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun loadToDoTasks() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            when (val response = taskRepository.getToDoTasks()) {
                is ApiResponse.Success -> {
                    if (response.data != null) {
                        state = state.copy(
                            toDoTasks = TaskUiStateList(response)
                        )
                    }
                }

                is ApiResponse.Error -> {
                    // TODO: Handle error
                }
            }

            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveTask(task: Task) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            var priorityVal: Int = 1
            val lastPriorityResponse = taskRepository.getLastPriority()

            if (task.date == null) {
                when (lastPriorityResponse) {
                    is ApiResponse.Success -> {
                        lastPriorityResponse.data?.let {
                            priorityVal = it + 1
                        }
                    }

                    else -> {}
                }
            }

            val response = if (task.id == null) {
                taskRepository.addTask(
                    task.copy(
//                        id = Uuid.generateV4()
                        id = Uuid.random(),
                        priority = if (task.date == null) priorityVal else null
                    )
                )
            } else {
                taskRepository.editTask(task)
            }

//            responseChannel.send(response) // <- czemu to służy?

            state = state.copy(isLoading = false)
        }
    }

    fun selectTask(id: Uuid?) {
        if (id == null) {
            state = state.copy(
                editedTask = TaskUiState(),
                selectedTask = TaskUiState(),
//                selectedTaskId = null
            )
        } else {
            for (task in (state.tasks + state.toDoTasks)) {
                if (task.id == id) {
                    state = state.copy(
                        editedTask = task,
                        selectedTask = task,
//                        selectedTaskId = task.id
                    )
                    break
                }
            }
        }
    }

    // VOICE NOTE
    @OptIn(ExperimentalUuidApi::class)
    fun addVoiceNote(voiceNote: VoiceNote) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val response = voiceNoteRepository.addVoiceNote(voiceNote)
            responseChannel.send(response)
            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun deleteVoiceNote(id: Uuid) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val response = voiceNoteRepository.deleteVoiceNote(id)
            responseChannel.send(response)
            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun loadVoiceNotes() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val selectedActivityId = state.selectedActivity.id

            selectedActivityId?.let {
                when (val response = voiceNoteRepository.getVoiceNotesOfActivity(it)) {
                    is ApiResponse.Success -> {
                        if (response.data != null) state = state.copy(
                            voiceNotes = VoiceNoteUiStateList(response.data)
                        )
                    }

                    is ApiResponse.Error -> {
                        // TODO: Handle error
                    }
                }
            }

            state = state.copy(isLoading = false)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun updateLastPlayedPosition(position: Long, itemIndex: Int) {
        state = state.copy(
            voiceNotes = state.voiceNotes.toMutableList().also { list ->
                list[itemIndex] = list[itemIndex].copy(
                    lastPlayedPosition = position
                )
            }
        )
    }


    @OptIn(ExperimentalUuidApi::class)
    fun ActivityUiState(activity: Activity?): ActivityUiState {
        return (activity?.let {
            ActivityUiState(
                id = activity.id,
                date = activity.date,
                title = activity.title,
                note = activity.note ?: "",
                startTime = activity.startTime,
                endTime = activity.endTime,
                mainActivityId = activity.mainActivityId
            )
        } ?: run {
            ActivityUiState()
        })
    }

    fun ActivityUiStateList(activities: List<Activity>): List<ActivityUiState> =
        activities.map(::ActivityUiState)

    @OptIn(ExperimentalUuidApi::class)
    fun Activity.toActivityUiState(): ActivityUiState = ActivityUiState(
        id = this.id,
        date = this.date,
        title = this.title,
        note = this.note ?: "",
        startTime = this.startTime,
        endTime = this.endTime,
        mainActivityId = this.mainActivityId
    )

    //fun ActivityUiState.toActivity(userId: UUID): Activity = Activity(
    @OptIn(ExperimentalUuidApi::class)
    fun ActivityUiState.toActivity(): Activity =
        Activity(
            id = this.id!!,
            date = this.date,
            title = this.title,
            note = this.note,
            startTime = this.startTime!!,
            endTime = this.endTime,
            mainActivityId = this.mainActivityId
        )

    @OptIn(ExperimentalUuidApi::class)
    fun ActivityUiStateList(response: ApiResponse<List<Activity>>): List<ActivityUiState> = when (response) {
        is ApiResponse.Success -> {
            response.data?.map(::ActivityUiState)
        }

        is ApiResponse.Error -> {
            // TODO: Handle error
            emptyList()
        }
    } as List<ActivityUiState>

    @OptIn(ExperimentalUuidApi::class)
    fun DayUiState(response: ApiResponse<Day>): DayUiState = when (response) {
        is ApiResponse.Success -> {
            val day = response.data

            day?.let {
                DayUiState(
                    date = day.date,
                    activeTimeStart = day.activeTimeStart,
                    activeTimeEnd = day.activeTimeEnd,
                    actualActiveTimeStart = day.actualActiveTimeStart,
                    actualActiveTimeEnd = day.actualActiveTimeEnd,
//            note = day.note
                )
            } ?: run {
                DayUiState()
            }
        }

        is ApiResponse.Error -> {
            // TODO: Handle error
            DayUiState()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun DayUiState.toDay(): Day = Day(
        id = this.id,
        date = this.date,
        activeTimeStart = this.activeTimeStart,
        activeTimeEnd = this.activeTimeEnd,
        actualActiveTimeStart = this.actualActiveTimeStart,
        actualActiveTimeEnd = this.actualActiveTimeEnd,
//    note = this.note
    )

    @OptIn(ExperimentalUuidApi::class)
    fun GoalUiState(goal: Goal?): GoalUiState {
        return goal?.let {
            GoalUiState(
                id = goal.id,
                title = goal.title,
                priority = goal.priority ?: 1
            )
        } ?: run {
            GoalUiState()
        }
    }

    fun GoalUiStateList(goals: List<Goal>): List<GoalUiState> = goals.map(::GoalUiState)

    @OptIn(ExperimentalUuidApi::class)
    fun GoalUiState.toGoal(): Goal = Goal(
        id = this.id,
        title = this.title,
        priority = this.priority
    )

    fun TaskUiStateList(response: ApiResponse<List<Task>>): List<TaskUiState> = when(response) {
        is ApiResponse.Success -> {
            response.data?.map(::TaskUiState)
        }
        is ApiResponse.Error -> {
            // TODO: Handle error
            emptyList()
        }
    } as List<TaskUiState>

    @OptIn(ExperimentalUuidApi::class)
    fun TaskUiState(task: Task?): TaskUiState {
        return (task?.let {
            TaskUiState(
                id = task.id,
                date = task.date,
                title = task.title,
                startTime = task.startTime,
                endTime = task.endTime,
                description = task.description ?: "",
                priority = task.priority,
                pinned = task.pinned
            )
        } ?: run {
            TaskUiState()
        })
    }

    @OptIn(ExperimentalUuidApi::class)
    fun TaskUiState.toTask(): Task = Task(
        id = this.id,
        date = this.date,
        title = this.title,
        startTime = this.startTime,
        endTime = this.endTime,
        description = this.description,
        priority = this.priority,
        pinned = this.pinned
    )

    @OptIn(ExperimentalUuidApi::class)
    fun VoiceNoteUiState(voiceNote: VoiceNote?): VoiceNoteUiState {
        return (voiceNote?.let {
            VoiceNoteUiState(
                id = voiceNote.id,
                uri = voiceNote.uri,
                duration = voiceNote.duration.toLong(),
                recordedAt = voiceNote.recordedAt.toLong()
            )
        } ?: run {
            VoiceNoteUiState()
        })
    }

    @OptIn(ExperimentalUuidApi::class)
    fun VoiceNoteUiStateList(voiceNotes: List<VoiceNote>): List<VoiceNoteUiState> =
        voiceNotes.map { voiceNote ->
            VoiceNoteUiState(
                id = voiceNote.id,
                uri = voiceNote.uri,
                duration = voiceNote.duration.toLong(),
                recordedAt = voiceNote.recordedAt.toLong()
            )
        }

    @OptIn(ExperimentalUuidApi::class)
    fun VoiceNoteUiState.toVoiceNote(): VoiceNote = VoiceNote(
        id = this.id!!,
        uri = this.uri!!,
        duration = this.duration.toString(),
        recordedAt = this.recordedAt!!.toString(),
        activityId = this.activityId!!
    )
}