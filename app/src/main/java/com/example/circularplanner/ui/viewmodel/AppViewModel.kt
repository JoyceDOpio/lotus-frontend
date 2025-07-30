package com.example.circularplanner.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.circularplanner.PlannerApplication
import com.example.circularplanner.data.Activity
import com.example.circularplanner.data.Day
import com.example.circularplanner.data.IActivitiesRepository
import com.example.circularplanner.data.IDaysRepository
import com.example.circularplanner.data.ITasksRepository
import com.example.circularplanner.data.IVoiceNotesRepository
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.data.VoiceNote
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import kotlin.String

// There is data I only want to read and there is data which I want to modify in response to user's actions (without saving this data into the database)
typealias Tasks = List<Task>
typealias Activities = List<Activity>
data class DayState (
    val date: LocalDate = LocalDate.now(),
    val activeTimeStart: Time = Time(6, 0),
    val activeTimeEnd: Time = Time(22, 0),
    val tasks: Tasks = emptyList(),
    val activities: Activities = emptyList()
)

data class DayUiState (
    val activeTimeStart: Time = Time(6, 0),
    val activeTimeEnd: Time = Time(22, 0),
    val isActiveTimeValid: Boolean = false,
    val isList: Boolean = false,
    val isActiveTimeSetUp: Boolean = false,
    val isActivityDisplay: Boolean = false
)

data class TaskUiState (
    val id: UUID? = null,
    var title: String = "",
    var startTime: Time = Time(0, 0),
    var endTime: Time = Time(0, 0),
    var description: String = ""
)

//typealias VoiceNotes = List<VoiceNote>
typealias VoiceNotesUiState = List<VoiceNoteUiState>
//@Parcelize
data class ActivityUiState (
    val id: UUID? = null,
    var title: String = "",
    var startTime: Time = Time(0, 0),// Default value: start of the day
    var endTime: Time = Time(23, 59),// Default value: end of the day
    var isTimerRunning: Boolean = false,
    var voiceNotesUiState: VoiceNotesUiState = emptyList()
)
//    : Parcelable

// For saving the voice note to the database
data class VoiceNoteUiState (
    val id: UUID? = null,
    val uri: String = "",
    val duration: Long = 0L,
    val timestamp: Long? = null,
    val activityId: UUID? = null,
    val isRecording: Boolean = false,
    val lastPlayedPosition: Long = 0L
)

data class UserInput(
    val selectedDate: LocalDate = LocalDate.now(),
)

class AppViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val daysRepository: IDaysRepository,
    private val tasksRepository: ITasksRepository,
    private val activitiesRepository: IActivitiesRepository,
    private val voiceNotesRepository: IVoiceNotesRepository
) : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return AppViewModel(
                    savedStateHandle,
                    (application as PlannerApplication).container.daysRepository,
                    (application as PlannerApplication).container.tasksRepository,
                    (application as PlannerApplication).container.activitiesRepository,
                    (application as PlannerApplication).container.voiceNotesRepository
                ) as T
            }
        }
    }

    // Activity details' UI state
    val activityUiState = MutableStateFlow(ActivityUiState())
//    val activityUiState = MutableStateFlow(savedStateHandle.get<ActivityUiState>("activity_state") ?: ActivityUiState())

    // Recorded activity state
    val recordedActivityState = MutableStateFlow(ActivityUiState())

    // Day UI state
    val dayUiState = MutableStateFlow(DayUiState())

    // Task details' UI state
    val taskUiState = MutableStateFlow(TaskUiState())

    val userInput = MutableStateFlow(UserInput())

    // Day state
    @OptIn(ExperimentalCoroutinesApi::class)
    val dayState = userInput.flatMapLatest { input: UserInput ->
        combine(
            flow = daysRepository.getDayStream(input.selectedDate.toString()),
            flow2 = tasksRepository.getAllTasksPerDayStream(input.selectedDate.toString()),
            flow3 = activitiesRepository.getAllActivitiesPerDayStream(input.selectedDate.toString())
        ) { day, tasks, activities ->
            // Update the day UI state with the actual active time start and end
            dayUiState.update {
                it.copy(
                    activeTimeStart = day?.activeTimeStart ?: Time(6, 0),
                    activeTimeEnd = day?.activeTimeEnd ?: Time(22, 0),
                )
            }

            DayState(
                date = input.selectedDate,
                activeTimeStart = day?.activeTimeStart ?: Time(6, 0),
                activeTimeEnd = day?.activeTimeEnd ?: Time(22, 0),
                tasks = tasks,
                activities = activities
            )
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
        initialValue = DayState()
    )

    fun addVoiceNoteToActivity(voiceNoteUiState: VoiceNoteUiState) {
        recordedActivityState.update {
            it.copy(
                voiceNotesUiState = it.voiceNotesUiState + voiceNoteUiState
            )
        }
//        savedStateHandle.set(
//            key = "activity_state",
//            activityUiState.value
//        )
    }

    fun clearRecordedActivity() {
        recordedActivityState.update {
            ActivityUiState()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            tasksRepository.deleteTask(task)
        }
    }

    fun deleteVoiceNote(voiceNote: VoiceNote) {
        viewModelScope.launch {
            voiceNotesRepository.deleteVoiceNote(voiceNote)
        }
    }

    fun saveActivity() {
        viewModelScope.launch {
            val voiceNotes = recordedActivityState.value.voiceNotesUiState.map { voiceNoteUiState -> voiceNoteUiState.toVoiceNote() }
            activitiesRepository.insertActivity(
                recordedActivityState.value.toActivity(),
                *voiceNotes.toTypedArray()
            )
        }
    }

    fun saveDay() {
        viewModelScope.launch {
            daysRepository.insertDay(
                dayState.value.toDay().copy(
                    activeTimeStart = dayUiState.value.activeTimeStart,
                    activeTimeEnd = dayUiState.value.activeTimeEnd
                )
            )
        }
    }

    fun saveTask() {
        viewModelScope.launch {
            // If task exists, update it
            val taskId = taskUiState.value.id

            if (taskId != null) {
                val task = dayState.value.tasks.find { task -> task.id == taskId }

                if (task != null) {
                    val updatedTask = taskUiState.value.toTask(userInput.value.selectedDate).copy(
                        id = taskId
                    )
                    tasksRepository.updateTask(updatedTask)
                }
            }
            // Else, save the new task
            else {
                // There are no tasks for the given day, save the day.
                if (dayState.value.tasks.size == 0) {
                    daysRepository.insertDay(dayState.value.toDay())
                }

                tasksRepository.insertTask(taskUiState.value.toTask(userInput.value.selectedDate))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun selectActivity(id: UUID?) {
        viewModelScope.launch {
            if (id != null) {
                val activity = dayState.value.activities.find { activity -> activity.id == id }
                if (activity != null) {

                    var voiceNotesUiState = emptyList<VoiceNoteUiState>()
                    voiceNotesRepository.getAllVoiceNotesPerActivity(id).collect { voiceNotes ->
                        for (voiceNote in voiceNotes) {
                            voiceNotesUiState = voiceNotesUiState + VoiceNoteUiState(
                                id = voiceNote.id,
                                uri = voiceNote.uri,
                                duration = voiceNote.duration,
                                timestamp = voiceNote.timestamp,
                                activityId = voiceNote.activityId
                            )
                        }

                        activityUiState.update {
                            ActivityUiState(
                                id = activity.id,
                                title = activity.title,
                                startTime = activity.startTime,
                                endTime = activity.endTime,
                                voiceNotesUiState = voiceNotesUiState
                            )
                        }
                    }
                }
            }
            else {
                activityUiState.update {
                    ActivityUiState()
                }
            }
        }
    }

    fun selectTask(id: UUID?) {
        val task = dayState.value.tasks.find { task -> task.id == id }
        if (task != null) {
            taskUiState.update {
                TaskUiState(
                    id = task.id,
                    title = task.title,
                    startTime = task.startTime,
                    endTime = task.endTime,
                    description = task.description
                )
            }
        } else {
            taskUiState.update {
                TaskUiState()
            }
        }
    }

    fun setActiveTimeEnd(time: Time) {
        dayUiState.update {
            it.copy(
                activeTimeEnd = time
            )
        }
    }

    fun setActiveTimeStart(time: Time) {
        dayUiState.update {
            it.copy(
                activeTimeStart = time
            )
        }
    }

    fun setActiveTimeIsValid(value: Boolean) {
        dayUiState.update {
            it.copy(
                isActiveTimeValid = value
            )
        }
    }

    fun setActivityEndTime(time: Time) {
        recordedActivityState.update {
            it.copy(
                endTime = time
            )
        }
//        savedStateHandle.set(
//            key = "activity_state",
//            activityUiState.value
//        )
    }

    fun setActivityId(id: UUID) {
        recordedActivityState.update {
            it.copy(
                id = id
            )
        }
//        savedStateHandle.set(
//            key = "activity_state",
//            activityUiState.value
//        )
    }

    fun setActivityStartTime(time: Time) {
        recordedActivityState.update {
            it.copy(
                startTime = time
            )
        }
//        savedStateHandle.set(
//            key = "activity_state",
//            activityUiState.value
//        )
    }

    fun setActivityTitle(title: String) {
        recordedActivityState.update {
            it.copy(
                title = title
            )
        }
//        savedStateHandle.set(
//            key = "activity_state",
//            activityUiState.value
//        )
    }

    fun setIsActivityDisplay(value: Boolean) {
        dayUiState.update {
            it.copy(
                isActivityDisplay = value
            )
        }
    }

    fun setIsActiveTimeSetUp(value: Boolean) {
        dayUiState.update {
            it.copy(
                isActiveTimeSetUp = value
            )
        }
    }

    fun setIsList(value: Boolean) {
        dayUiState.update {
            it.copy(
                isList = value
            )
        }
    }

    fun setIsTimerRunning(value: Boolean) {
        recordedActivityState.update {
            it.copy(
                isTimerRunning = value
            )
        }
    }

    fun setSelectedDate(date: LocalDate) {
        userInput.update {
            it.copy(
                selectedDate = date
            )
        }
    }

    fun setTaskDescription (description: String) {
        taskUiState.update {
            it.copy(
                description = description
            )
        }
    }

    fun setTaskEndTime(time: Time) {
        taskUiState.update {
            it.copy(
                endTime = time
            )
        }
    }

    fun setTaskStartTime(time: Time) {
        taskUiState.update {
            it.copy(
                startTime = time
            )
        }
    }

    fun setTaskTitle (title: String) {
        taskUiState.update {
            it.copy(
                title = title
            )
        }
    }

    fun updateLastPlayedPositionRecordedActivity(position: Long, itemIndex: Int) {
        recordedActivityState.update {
            it.copy(
                voiceNotesUiState = it.voiceNotesUiState.toMutableList().also { list ->
                    list[itemIndex] = list[itemIndex].copy(
                        lastPlayedPosition = position
                    )
                }
            )
        }
    }

    fun updateLastPlayedPosition(position: Long, itemIndex: Int) {
        activityUiState.update {
            it.copy(
                voiceNotesUiState = it.voiceNotesUiState.toMutableList().also { list ->
                    list[itemIndex] = list[itemIndex].copy(
                        lastPlayedPosition = position
                    )
                }
            )
        }
    }
}

fun ActivityUiState.toActivity(): Activity = Activity(
    date = LocalDate.now(),
    title = this.title,
    startTime = this.startTime,
    endTime = this.endTime,
    id = this.id!!
)

fun DayState.toDay(): Day = Day(
    date = this.date,
    activeTimeStart = this.activeTimeStart,
    activeTimeEnd = this.activeTimeEnd
)

fun TaskUiState.toTask(date: LocalDate): Task = Task(
    date = date,
    title = this.title,
    startTime = this.startTime,
    endTime = this.endTime,
    description = this.description
)

fun VoiceNoteUiState.toVoiceNote(): VoiceNote = VoiceNote(
    uri = this.uri,
    duration = this.duration,
    timestamp = this.timestamp!!,
    activityId = this.activityId!!,
    id = this.id!!
)