package com.example.circularplanner.ui.viewmodel

import android.os.Parcelable
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.util.UUID
import kotlin.String
import kotlin.collections.sortedWith

const val ACTIVITY_SAVED_STATE_KEY = "activity"
const val MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY = "recorded_activity_main"
const val SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY = "recorded_activity_sub"
const val SUB_ACTIVITIES_SAVED_STATE_KEY = "sub_activities"
const val TASK_SAVED_STATE_KEY = "task"
const val USER_INPUT_SAVED_STATE_KEY = "user_input"
const val VOICE_NOTES_SAVED_STATE_KEY = "voice_notes"

// There is data I only want to read and there is data which I want to modify in response to user's actions (without saving this data into the database)
typealias Tasks = List<Task>
typealias Activities = List<Activity>
data class DayState (
    val date: LocalDate = LocalDate.now(),
    val activeTimeStart: Time = Time(6, 0),
    val activeTimeEnd: Time = Time(22, 0),
    val actualActiveTimeStart: Time? = null,
    val actualActiveTimeEnd: Time? = null,
    val tasks: Tasks = emptyList(),
    val activities: Activities = emptyList(),
    val note: String = ""
)

data class DayUiState (
    val activeTimeStart: Time = Time(6, 0),
    val activeTimeEnd: Time = Time(22, 0),
    val actualActiveTimeStart: Time? = null,
    val actualActiveTimeEnd: Time? = null,
    val isActiveTimeValid: Boolean = false,
    val isActiveTimeSetUp: Boolean = false,
    val isActivityDisplay: Boolean = false,
    val note: String = ""
)

data class TaskUiState (
    val date: LocalDate? = null,
    val id: UUID? = null,
    var title: String = "",
    var startTime: Time? = null,
    var endTime: Time? = null,
    var description: String = "",
    var priority: Int? = null
)

@Parcelize
data class TaskSavedState (
    val date: LocalDate?,
    val id: UUID,
    val title: String,
    val startTime: String?,
    val endTime: String?,
    val description: String
) : Parcelable

//typealias VoiceNotes = List<VoiceNote>
typealias VoiceNotesUiState = List<VoiceNoteUiState>
data class ActivityUiState (
    val date: LocalDate = LocalDate.now(),
    val id: UUID? = null,
    var title: String = "",
    var note: String = "",
    var startTime: Time = Time(0, 0),// Default value: start of the day//TODO: Maybe I should change the default value to null
    var endTime: Time? = null,
    var voiceNotesUiState: VoiceNotesUiState = emptyList(),
    val mainActivityId: UUID? = null,
    var subActivitiesUiState: List<ActivityUiState> = emptyList()
)

@Parcelize
data class ActivitySavedState (
    val date: LocalDate,
    val id: UUID,
    val title: String = "",
    val note: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val mainActivityId: UUID? = null
) : Parcelable

data class VoiceNoteUiState (
    val id: UUID? = null,
    val uri: String = "",
    val duration: Long = 0L,
    val timestamp: Long? = null,
    val activityId: UUID? = null,
    val lastPlayedPosition: Long = 0L
)

@Parcelize
data class VoiceNoteSavedState (
    val id: UUID,
    val uri: String,
    val duration: Long,
    val timestamp: Long,
    val activityId: UUID
) : Parcelable

@Parcelize
data class UserInput(
    val selectedDate: LocalDate = LocalDate.now()
) : Parcelable

class DayViewModel(
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

                return DayViewModel(
                    savedStateHandle,
                    (application as PlannerApplication).container.daysRepository,
                    application.container.tasksRepository,
                    application.container.activitiesRepository,
                    application.container.voiceNotesRepository
                ) as T
            }
        }
    }

    init {
        viewModelScope.launch {
            activitiesRepository.getMainRecordedActivity().first()?.also { recordedActivity ->
                mainRecordedActivityUiState.value = ActivityUiState(
                    date = recordedActivity.date,
                    id = recordedActivity.id,
                    title = recordedActivity.title,
                    note = recordedActivity.note,
                    startTime = recordedActivity.startTime
                )
            }

            activitiesRepository.getSubRecordedActivity().first()?.also { recordedActivity ->
                subRecordedActivityUiState.value = ActivityUiState(
                    date = recordedActivity.date,
                    id = recordedActivity.id,
                    title = recordedActivity.title,
                    note = recordedActivity.note,
                    startTime = recordedActivity.startTime,
                    mainActivityId = recordedActivity.mainActivityId
                )
            }
        }
    }

    // Activity details' UI state
    val activityUiState = MutableStateFlow((savedStateHandle.get<ActivitySavedState>(ACTIVITY_SAVED_STATE_KEY)).let {
        it ->
        if (it != null) {
            val voiceNotesUiState = (savedStateHandle.get<List<VoiceNoteSavedState>>(VOICE_NOTES_SAVED_STATE_KEY) ?: emptyList()).map { it ->
                VoiceNoteUiState(
                    id = it.id,
                    uri = it.uri,
                    duration = it.duration,
                    timestamp = it.timestamp,
                    activityId = it.activityId
                )
            }
            val subActivities = (savedStateHandle.get<List<ActivitySavedState>>(SUB_ACTIVITIES_SAVED_STATE_KEY) ?: emptyList()).map { it ->
                ActivityUiState(
                    date = it.date,
                    id = it.id,
                    title = it.title,
                    note = it.note,
                    startTime = Time.parse(it.startTime),
                    endTime = if (it.endTime != "") Time.parse(it.startTime) else Time(23, 59),
                    voiceNotesUiState = voiceNotesUiState
                )
            }

            ActivityUiState(
                date = it.date,
                id = it.id,
                title = it.title,
                note = it.note,
                startTime = Time.parse(it.startTime),
                endTime = if (it.endTime != "") Time.parse(it.startTime) else Time(23, 59),
                voiceNotesUiState = voiceNotesUiState,
                subActivitiesUiState = subActivities
            )
        } else {
            ActivityUiState()
        }
    })

    val mainRecordedActivityUiState = MutableStateFlow(ActivityUiState())
    val subRecordedActivityUiState = MutableStateFlow(ActivityUiState())

    // Day UI state
    val dayUiState = MutableStateFlow(DayUiState())

    val lastTaskPriority = tasksRepository.getLastPriority()

    // Task details' UI state
    val taskUiState = MutableStateFlow((savedStateHandle.get<TaskSavedState>(TASK_SAVED_STATE_KEY)).let { it ->
        if (it != null) {
            TaskUiState(
                date = it.date,
                id = it.id,
                title = it.title,
                startTime = if (it.startTime != null) Time.parse(it.startTime) else null,
                endTime = if (it.endTime != null) Time.parse(it.endTime) else null,
                description = it.description
            )
        } else {
            TaskUiState()
        }
    })

    val userInput = MutableStateFlow((savedStateHandle.get<UserInput>(USER_INPUT_SAVED_STATE_KEY)).let { it ->
        it ?: UserInput()
    })

    val toDoTasks = tasksRepository.getAllTasksWithoutDate().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
        initialValue = emptyList()
    )

    // Day state
    @OptIn(ExperimentalCoroutinesApi::class)
    val dayState = userInput.flatMapLatest { input: UserInput ->
        combine(
            flow = daysRepository.getDayStream(input.selectedDate.toString()),
            flow2 = tasksRepository.getAllTasksPerDayStream(input.selectedDate.toString()),
            flow3 = activitiesRepository.getMainActivitiesPerDayStream(input.selectedDate.toString())
        ) { day, tasks, activities ->
            // Update the day UI state with the actual active time start and end
            dayUiState.update {
                it.copy(
                    activeTimeStart = day?.activeTimeStart ?: Time(6, 0),//TODO: Take the default value from settings
                    activeTimeEnd = day?.activeTimeEnd ?: Time(22, 0),//TODO: Take the default value from settings
                    actualActiveTimeStart = day?.actualActiveTimeStart,
                    actualActiveTimeEnd = day?.actualActiveTimeEnd ,
                    note = day?.note ?: ""
                )
            }

            DayState(
                date = input.selectedDate,
                activeTimeStart = day?.activeTimeStart ?: Time(6, 0),//TODO: Take the default value from settings
                activeTimeEnd = day?.activeTimeEnd ?: Time(22, 0),//TODO: Take the default value from settings
                actualActiveTimeStart = day?.actualActiveTimeStart,
                actualActiveTimeEnd = day?.actualActiveTimeEnd,
                tasks = tasks.sortedWith{ a, b -> a.compareTo(b)},// Sort the tasks based on their start time
                activities = activities
            )
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
        initialValue = DayState()
    )

//    fun clearRecordedActivity() {
//        mainRecordedActivityUiState.update {
//            ActivityUiState()
//        }
//        savedStateHandle.set(
//            key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
//            value = null
//        )
//    }

    fun clearMainRecordedActivity() {
        mainRecordedActivityUiState.update {
            ActivityUiState()
        }
        savedStateHandle.set(
            key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
            value = null
        )
    }

    fun clearSubRecordedActivity() {
        subRecordedActivityUiState.update {
            ActivityUiState()
        }
        savedStateHandle.set(
            key = SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY,
            value = null
        )
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            activitiesRepository.deleteActivity(activity)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
//            if (task.priority != null) {
//                val taskPriority = task.priority
//
//                toDoTasks.collect { list ->
//                    list.forEach { toDoTask ->
//                        toDoTask.priority?.let {
//                            if (it > taskPriority!!) {
//                                tasksRepository.updateTask(toDoTask.copy(
//                                    priority = toDoTask.priority!! - 1
//                                ))
//                            }
//                        }
//                    }
//                }
//            }
            tasksRepository.deleteTask(task)
        }
    }

    fun deleteVoiceNote(voiceNote: VoiceNote) {
        viewModelScope.launch {
            voiceNotesRepository.deleteVoiceNote(voiceNote)
        }
    }

    fun saveDay() {
        viewModelScope.launch {
            daysRepository.insertDay(
                dayState.value.toDay().copy(
                    activeTimeStart = dayUiState.value.activeTimeStart,
                    activeTimeEnd = dayUiState.value.activeTimeEnd,
                    actualActiveTimeStart = dayUiState.value.actualActiveTimeStart,
                    actualActiveTimeEnd = dayUiState.value.actualActiveTimeEnd,
                    note = dayUiState.value.note
                )
            )
        }
    }

    fun saveActivity() {
        viewModelScope.launch {
            // If activity exists, update it
            val activityId = activityUiState.value.id

            val activity = dayState.value.activities.find { activity -> activity.id == activityId }

            if (activity == null) {
                activitiesRepository.insertActivity(
                    activityUiState.value.toActivity()
                )
            } else {
                activitiesRepository.updateActivity(activityUiState.value.toActivity())
            }
        }
    }

    fun saveMainRecordedActivity() {
        viewModelScope.launch {
            // If activity exists, update it
            val activityId = mainRecordedActivityUiState.value.id

            var activity = dayState.value.activities.find { activity -> activity.id == activityId }

            if (activity == null) {
                activitiesRepository.insertActivity(
                    mainRecordedActivityUiState.value.toActivity()
                )
            } else {
                activitiesRepository.updateActivity(mainRecordedActivityUiState.value.toActivity())
            }
        }
    }

    fun saveSubRecordedActivity() {
        viewModelScope.launch {
            // If activity exists, update it
            val activityId = subRecordedActivityUiState.value.id

            val activity = dayState.value.activities.find { activity -> activity.id == activityId }

            if (activity == null) {
                activitiesRepository.insertActivity(
                    subRecordedActivityUiState.value.toActivity()
                )
            } else {
                activitiesRepository.updateActivity(subRecordedActivityUiState.value.toActivity())
            }
        }
    }

    fun saveTask() {
        viewModelScope.launch {
            // If task exists, update it
            val taskId = taskUiState.value.id

            if (taskId != null) {
                val tasks = dayState.value.tasks + toDoTasks.value
                val task = tasks.find { task -> task.id == taskId }

                if (task != null) {
                    val updatedTask = taskUiState.value.toTask().copy(
                        id = taskId
                    )
                    tasksRepository.updateTask(updatedTask)
                }
                // Else, save the new task
                else {
                    tasksRepository.insertTask(taskUiState.value.toTask())
                }
            }
            // Else, save the new task
            else {
//                // There are no tasks for the given day, save the day.
//                if (dayState.value.tasks.isEmpty()) {//FIXME: If I save the day before I save the task the taskUiState is emptied. The task can still be successfully saved to the database without the corresponding day being persisted in the database.
//                    daysRepository.insertDay(dayState.value.toDay())
//                }

                tasksRepository.insertTask(taskUiState.value.toTask())
            }
        }
    }

    fun saveTask(task: Task) {
        viewModelScope.launch {
            val tasks = dayState.value.tasks + toDoTasks.value
            val found = tasks.find { task -> task.id == task.id }

            if (found != null) {
                tasksRepository.updateTask(task)
            }
            else {
                tasksRepository.insertTask(task)
            }
        }
    }

    fun saveTaskToStateHandle() {
        if (taskUiState.value.id != null) {
            savedStateHandle.set(
                key = TASK_SAVED_STATE_KEY,
                value = taskUiState.value.toSavedState()
            )
        }
    }

    fun saveVoiceNote(voiceNote: VoiceNote) {
        viewModelScope.launch {
            voiceNotesRepository.insertVoiceNotes(voiceNote)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun selectActivity(id: UUID?) {
        viewModelScope.launch {
            if (id != null) {
                val activity = dayState.value.activities.find { activity -> activity.id == id }
                if (activity != null) {
//                    combine(
//                        flow = voiceNotesRepository.getAllVoiceNotesPerActivity(id),
//                        flow2 = activitiesRepository.getSubActivitiesPerMainActivity(id)
//                    ) { voiceNotes, subActivities ->
//                        var voiceNotesUiState = emptyList<VoiceNoteUiState>()
//                        var subActivitiesUiState = emptyList<ActivityUiState>()
//
//                        for (voiceNote in voiceNotes) {
//                            // Append the mapped voice note to the voice notes list
//                            voiceNotesUiState = voiceNotesUiState + VoiceNoteUiState(
//                                id = voiceNote.id,
//                                uri = voiceNote.uri,
//                                duration = voiceNote.duration,
//                                timestamp = voiceNote.timestamp,
//                                activityId = voiceNote.activityId
//                            )
//                        }
//                        for (subActivity in subActivities) {
//                            subActivitiesUiState = subActivitiesUiState + ActivityUiState(
//                                id = subActivity.id,
//                                title = subActivity.title,
//                                note = subActivity.note,
//                                startTime = subActivity.startTime,
//                                endTime = subActivity.endTime,
////                                voiceNotesUiState = TODO: Add the voice notes of the sub-activity
//                                mainActivityId = subActivity.mainActivityId
//                            )
//                        }
//                        activityUiState.update {
//                            ActivityUiState(
//                                id = activity.id,
//                                title = activity.title,
//                                note = activity.note,
//                                startTime = activity.startTime,
//                                endTime = activity.endTime,
//                                voiceNotesUiState = voiceNotesUiState,
//                                subActivitiesUiState = subActivitiesUiState
//                            )
//                        }
//                    }



//                    var voiceNotesUiState = emptyList<VoiceNoteUiState>()
//                    voiceNotesRepository.getAllVoiceNotesPerActivity(id).collect { voiceNotes ->
//                        for (voiceNote in voiceNotes) {
//                            // Append the mapped voice note to the voice notes list
//                            voiceNotesUiState = voiceNotesUiState + VoiceNoteUiState(
//                                id = voiceNote.id,
//                                uri = voiceNote.uri,
//                                duration = voiceNote.duration,
//                                timestamp = voiceNote.timestamp,
//                                activityId = voiceNote.activityId
//                            )
//                        }
//
//                        activityUiState.update {
//                            ActivityUiState(
//                                id = activity.id,
//                                title = activity.title,
//                                startTime = activity.startTime,
//                                endTime = activity.endTime,
//                                voiceNotesUiState = voiceNotesUiState
//                            )
//                        }
//                    }


                    var subActivitiesUiState = emptyList<ActivityUiState>()
                    activitiesRepository.getSubActivitiesPerMainActivity(id).collect { subActivities ->
                        for (subActivity in subActivities) {
                            var voiceNotesUiState = emptyList<VoiceNoteUiState>()
//                            voiceNotesRepository.getAllVoiceNotesPerActivity(subActivity.id).collect { voiceNotes ->
//                                for (voiceNote in voiceNotes) {
//                                    // Append the mapped voice note to the voice notes list
//                                    voiceNotesUiState = voiceNotesUiState + VoiceNoteUiState(
//                                        id = voiceNote.id,
//                                        uri = voiceNote.uri,
//                                        duration = voiceNote.duration,
//                                        timestamp = voiceNote.timestamp,
//                                        activityId = voiceNote.activityId
//                                    )
//                                }
//
//                                subActivitiesUiState = subActivitiesUiState + ActivityUiState(
//                                    id = subActivity.id,
//                                    title = subActivity.title,
//                                    note = subActivity.note,
//                                    startTime = subActivity.startTime,
//                                    endTime = subActivity.endTime,
//                                    voiceNotesUiState = voiceNotesUiState,//TODO: Add the voice notes of the sub-activity
//                                    mainActivityId = subActivity.mainActivityId
//                                )
//                            }

                            subActivitiesUiState = subActivitiesUiState + ActivityUiState(
                                date = subActivity.date,
                                id = subActivity.id,
                                title = subActivity.title,
                                note = subActivity.note,
                                startTime = subActivity.startTime,
                                endTime = subActivity.endTime,
                                voiceNotesUiState = voiceNotesUiState,//TODO: Add the voice notes of the sub-activity
                                mainActivityId = subActivity.mainActivityId
                            )
                        }

                        activityUiState.update {
                            ActivityUiState(
                                date = activity.date,
                                id = activity.id,
                                title = activity.title,
                                note = activity.note,
                                startTime = activity.startTime,
                                endTime = activity.endTime,
                                subActivitiesUiState = subActivitiesUiState
                            )
                        }

                        var voiceNotesUiState = emptyList<VoiceNoteUiState>()
                        voiceNotesRepository.getAllVoiceNotesPerActivity(id).collect { voiceNotes ->
                            for (voiceNote in voiceNotes) {
                                // Append the mapped voice note to the voice notes list
                                voiceNotesUiState = voiceNotesUiState + VoiceNoteUiState(
                                    id = voiceNote.id,
                                    uri = voiceNote.uri,
                                    duration = voiceNote.duration,
                                    timestamp = voiceNote.timestamp,
                                    activityId = voiceNote.activityId
                                )
                            }

                            activityUiState.update { it ->
                                it.copy(
                                    voiceNotesUiState = voiceNotesUiState
                                )
                            }
                        }
                    }

                    // Set the selected activity saved state
                    savedStateHandle.set(
                        key = ACTIVITY_SAVED_STATE_KEY,
                        value = activityUiState.value.toSavedState()
                    )

                    // TODO: Set the selected activity's voice notes saved state
//                    savedStateHandle.set(
//                        key = VOICE_NOTES_SAVED_STATE_KEY,
//                        value = activityUiState.value.voiceNotesUiState.toSavedState()
//                    )

                    // TODO: Set the selected activity's sub-activities saved state
//                    savedStateHandle.set(
//                        key = SUB_ACTIVITIES_SAVED_STATE_KEY,
//                        value = activityUiState.value.toSavedState()
//                    )
                }
            }
            else {
                activityUiState.update {
                    ActivityUiState()
                }
                // Reset the selected activity saved state
                savedStateHandle.set(
                    key = ACTIVITY_SAVED_STATE_KEY,
                    value = null
                )
                savedStateHandle.set(
                    key = VOICE_NOTES_SAVED_STATE_KEY,
                    value = null
                )
                savedStateHandle.set(
                    key = SUB_ACTIVITIES_SAVED_STATE_KEY,
                    value = null
                )
            }
        }
    }

    fun selectTask(id: UUID?) {
        val tasks = dayState.value.tasks + toDoTasks.value
        val task = tasks.find { task -> task.id == id }

        if (task != null) {
            taskUiState.update {
                TaskUiState(
                    id = task.id,
                    title = task.title,
                    description = task.description,
                    date = task.date,
                    startTime = task.startTime,// Tasks from day state should have a date, start and end time
                    endTime = task.endTime,
                    priority = task.priority
                )
            }
        } else {
            taskUiState.update {
                TaskUiState()
            }
        }

        // Set the selected task saved state
        savedStateHandle.set(
            key = TASK_SAVED_STATE_KEY,
            value = if (task == null) task else taskUiState.value.toSavedState()
        )
    }

    fun setActiveTimeEnd(time: Time) {
        dayUiState.update {
            it.copy(
                activeTimeEnd = time
            )
        }
    }

    fun setActualActiveTimeEnd(time: Time) {
        dayUiState.update {
            it.copy(
                actualActiveTimeEnd = time
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

    fun setActualActiveTimeStart(time: Time) {
        dayUiState.update {
            it.copy(
                actualActiveTimeStart = time
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

//    fun setRecordedActivityEndTime(time: Time) {
//        mainRecordedActivityUiState.update {
//            it.copy(
//                endTime = time
//            )
//        }
//    }

    fun setMainRecordedActivityEndTime(time: Time) {
        mainRecordedActivityUiState.update {
            it.copy(
                endTime = time
            )
        }
    }

    fun setSubRecordedActivityEndTime(time: Time) {
        subRecordedActivityUiState.update {
            it.copy(
                endTime = time
            )
        }
        if (subRecordedActivityUiState.value.id != null) {
            savedStateHandle.set(
                key = SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY,
                value = subRecordedActivityUiState.value.toSavedState()
            )
        }
    }

//    fun setRecordedActivityId(id: UUID) {
//        mainRecordedActivityUiState.update {
//            it.copy(
//                id = id
//            )
//        }
//    }

    fun setMainRecordedActivityId(id: UUID) {
        mainRecordedActivityUiState.update {
            it.copy(
                id = id
            )
        }
        if (mainRecordedActivityUiState.value.id != null) {
            savedStateHandle.set(
                key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
                value = mainRecordedActivityUiState.value.toSavedState()
            )
        }
    }
    fun setSubRecordedActivityId(id: UUID) {
        subRecordedActivityUiState.update {
            it.copy(
                id = id
            )
        }
        if (subRecordedActivityUiState.value.id != null) {
            savedStateHandle.set(
                key = SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY,
                value = subRecordedActivityUiState.value.toSavedState()
            )
        }
    }

    fun setSubRecordedActivityMainActivityId(mainActivityId: UUID) {
        subRecordedActivityUiState.update {
            it.copy(
                mainActivityId = mainActivityId
            )
        }
        if (subRecordedActivityUiState.value.id != null) {
            savedStateHandle.set(
                key = SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY,
                value = subRecordedActivityUiState.value.toSavedState()
            )
        }
    }

    fun setActivityNote(note: String) {
        activityUiState.update {
            it.copy(
                note = note
            )
        }
    }

    fun setActivityTitle(title: String) {
        activityUiState.update {
            it.copy(
                title = title
            )
        }
    }

    fun setMainRecordedActivityNote(note: String) {
        mainRecordedActivityUiState.update {
            it.copy(
                note = note
            )
        }
        if (mainRecordedActivityUiState.value.id != null) {
            savedStateHandle.set(
                key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
                value = mainRecordedActivityUiState.value.toSavedState()
            )
        }
    }

    fun setSubRecordedActivityNote(note: String) {
        subRecordedActivityUiState.update {
            it.copy(
                note = note
            )
        }
        if (subRecordedActivityUiState.value.id != null) {
            savedStateHandle.set(
                key = SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY,
                value = subRecordedActivityUiState.value.toSavedState()
            )
        }
    }

    fun setMainRecordedActivityStartTime(time: Time) {
        mainRecordedActivityUiState.update {
            it.copy(
                startTime = time
            )
        }
        if (mainRecordedActivityUiState.value.id != null) {
            savedStateHandle.set(
                key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
                value = mainRecordedActivityUiState.value.toSavedState()
            )
        }
    }

    fun setSubRecordedActivityStartTime(time: Time) {
        subRecordedActivityUiState.update {
            it.copy(
                startTime = time
            )
        }
        if (subRecordedActivityUiState.value.id != null) {
            savedStateHandle.set(
                key = SUB_RECORDED_ACTIVITY_SAVED_STATE_KEY,
                value = subRecordedActivityUiState.value.toSavedState()
            )
        }
    }

    fun setMainRecordedActivityTitle(title: String) {
        mainRecordedActivityUiState.update {
            it.copy(
                title = title
            )
        }
        if (mainRecordedActivityUiState.value.id != null) {
            savedStateHandle.set(
                key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
                value = mainRecordedActivityUiState.value.toSavedState()
            )
        }
    }

    fun setSubRecordedActivityTitle(title: String) {
        subRecordedActivityUiState.update {
            it.copy(
                title = title
            )
        }
        if (subRecordedActivityUiState.value.id != null) {
            savedStateHandle.set(
                key = MAIN_RECORDED_ACTIVITY_SAVED_STATE_KEY,
                value = subRecordedActivityUiState.value.toSavedState()
            )
        }
    }

    fun setDayNote(note: String) {
        dayUiState.update {
            it.copy(
                note = note
            )
        }
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

    fun setTaskDate (date: LocalDate?) {
        taskUiState.update {
            it.copy(
                date = date
            )
        }
        saveTaskToStateHandle()
    }

    fun setTaskDescription (description: String) {
        taskUiState.update {
            it.copy(
                description = description
            )
        }
        saveTaskToStateHandle()
    }

    fun setTaskEndTime(time: Time) {
        taskUiState.update {
            it.copy(
                endTime = time
            )
        }
        saveTaskToStateHandle()
    }

    fun setTaskPriority(value: Int?) {
        taskUiState.update {
            it.copy(
                priority = value
            )
        }
        saveTaskToStateHandle()
    }

    fun setTaskStartTime(time: Time) {
        taskUiState.update {
            it.copy(
                startTime = time
            )
        }
        saveTaskToStateHandle()
    }

    fun setTaskTitle (title: String) {
        taskUiState.update {
            it.copy(
                title = title
            )
        }
        saveTaskToStateHandle()
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

    fun updateTask(task: Task) {
        viewModelScope.launch {
            tasksRepository.updateTask(task)
        }
    }
}

fun ActivityUiState.toActivity(): Activity = Activity(
    date = this.date,
    title = this.title,
    note = this.note,
    startTime = this.startTime,
    endTime = this.endTime,
    id = this.id!!,
    mainActivityId = this.mainActivityId
)

fun ActivityUiState.toSavedState(): ActivitySavedState = ActivitySavedState(
    date = this.date,
    id = this.id!!,
    title = this.title,
    startTime = this.startTime.toString()
)

fun DayState.toDay(): Day = Day(
    date = this.date,
    activeTimeStart = this.activeTimeStart,
    activeTimeEnd = this.activeTimeEnd,
    actualActiveTimeStart = this.actualActiveTimeStart,
    actualActiveTimeEnd = this.actualActiveTimeEnd,
    note = this.note
)

fun TaskUiState.toSavedState(): TaskSavedState = TaskSavedState(
    date = this.date,
    id = this.id!!,
    title = this.title,
    startTime = this.startTime?.toString(),
    endTime = this.endTime?.toString(),
    description = this.description
)

// This method inserts a random UUID value as the task's id
fun TaskUiState.toTask(): Task = Task(
    date = this.date,
    title = this.title,
    startTime = this.startTime,
    endTime = this.endTime,
    description = this.description,
    priority = this.priority
)

fun VoiceNoteUiState.toVoiceNote(): VoiceNote = VoiceNote(
    uri = this.uri,
    duration = this.duration,
    timestamp = this.timestamp!!,
    activityId = this.activityId!!,
    id = this.id!!
)