package com.example.circularplanner.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.circularplanner.PlannerApplication
import com.example.circularplanner.data.Day
import com.example.circularplanner.data.IDaysRepository
import com.example.circularplanner.data.ITasksRepository
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
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
data class DayState (
    val date: LocalDate = LocalDate.now(),
    val activeTimeStart: Time = Time(6, 0),
    val activeTimeEnd: Time = Time(22, 0),
    val tasks: Tasks = emptyList()
)

data class DayUiState (
    val activeTimeStart: Time = Time(6, 0),
    val activeTimeEnd: Time = Time(22, 0),
    val isActiveTimeValid: Boolean = false,
    val isList: Boolean = false,
    val isActiveTimeSetUp: Boolean = false
)

data class TaskUiState (
    val id: UUID? = null,
    var title: String = "",
    var startTime: Time = Time(0, 0),
    var endTime: Time = Time(0, 0),
    var description: String = ""
)

data class UserInput(
    val selectedDate: LocalDate = LocalDate.now(),
)

class TaskDisplayViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val daysRepository: IDaysRepository,
    private val tasksRepository: ITasksRepository
) : ViewModel() {
    companion object {
        private const val MILLS = 5_000L

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

                return TaskDisplayViewModel(
                    savedStateHandle,
                    (application as PlannerApplication).container.daysRepository,
                    (application as PlannerApplication).container.tasksRepository,
                ) as T
            }
        }
    }

    val userInput = MutableStateFlow(UserInput())

    // Day state
    @OptIn(ExperimentalCoroutinesApi::class)
    val dayState = userInput.flatMapLatest { input: UserInput ->
        combine(
            flow = daysRepository.getDayStream(input.selectedDate.toString()),
            flow2 = tasksRepository.getAllTasksPerDayStream(input.selectedDate.toString())
        ) { day, tasks ->
            DayState(
                date = input.selectedDate,
                activeTimeStart = day?.activeTimeStart ?: Time(6, 0),
                activeTimeEnd = day?.activeTimeEnd ?: Time(22, 0),
                tasks = tasks
            )
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
        initialValue = DayState()
    )

    // Task details' UI state
    val taskUiState = MutableStateFlow(TaskUiState())

    // Day UI state
    val dayUiState = MutableStateFlow(DayUiState())

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            tasksRepository.deleteTask(task)
        }
    }

    fun saveDay() {
        viewModelScope.launch {
            daysRepository.insertDay(dayState.value.toDay())
        }
    }

    fun saveTask() {
        viewModelScope.launch {
            // If tasks exists, update it
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
}

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