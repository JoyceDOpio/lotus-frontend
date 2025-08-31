package com.example.circularplanner.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.circularplanner.PlannerApplication
import com.example.circularplanner.data.Goal
import com.example.circularplanner.data.IGoalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class GoalUiState (
    val id: UUID? = null,
    var title: String = "",
    var priority: Int? = null,
)

class GoalViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val goalsRepository: IGoalsRepository
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

                return GoalViewModel(
                    savedStateHandle,
                    (application as PlannerApplication).container.goalsRepository
                ) as T
            }
        }
    }

    val goalUiState = MutableStateFlow(GoalUiState())
    val goals = goalsRepository
        .getAllGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = emptyList()
        )
    val lastPriority = goalsRepository
        .getLastPriority()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = 0
        )

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            goalsRepository.deleteGoal(goal)
        }
    }

    fun saveGoal() {//FIXME: Optimise this method
        viewModelScope.launch {
            // If goal exists, update it
            val goalId = goalUiState.value.id

            if (goalId != null) {
                var goal = goals.value.find { goal -> goal.id == goalId }

                if (goal != null) {
                    val updatedGoal = goalUiState.value.toGoal().copy(
                        id = goalId
                    )
                    goalsRepository.updateGoal(updatedGoal)
                }
            }
            // Else, save the new goal
            else {
                goalsRepository.insertGoal(goalUiState.value.toGoal())
            }
        }
    }

    fun selectGoal(id: UUID?) {
        if (id != null) {
            viewModelScope.launch {
                val goal = goalsRepository.getGoalStream(id).firstOrNull()

                if (goal != null) {
                    goalUiState.update {
                        GoalUiState(
                            id = goal.id,
                            title = goal.title,
                            priority = goal.priority
                        )
                    }
                }
            }
        }
        else {
            goalUiState.update { GoalUiState() }
        }
    }

    fun setPriority(value: Int) {
        goalUiState.update { it ->
            it.copy(
                priority = value
            )
        }
    }

    fun setTitle(value: String) {
        goalUiState.update { it ->
            it.copy(
                title = value
            )
        }
    }

    fun switchPriorities(firstId: UUID, secondId: UUID) {
        viewModelScope.launch {
            var firstGoal = goalsRepository.getGoalStream(firstId).firstOrNull()
            var secondGoal = goalsRepository.getGoalStream(secondId).firstOrNull()

            if (firstGoal != null && secondGoal != null) {
                val firstGoalPriority = firstGoal.priority
                val secondGoalPriority = secondGoal.priority

                firstGoal = firstGoal.copy(
                    priority = secondGoalPriority
                )
                secondGoal = secondGoal.copy(
                    priority = firstGoalPriority
                )

                Log.i("firstGoal", firstGoal.toString())
                Log.i("secondGoal", secondGoal.toString())

                goalsRepository.updateGoals(
                    firstGoal,
                    secondGoal
                )
            }
        }
    }
}

fun GoalUiState.toGoal(): Goal = Goal(
    title = this.title,
    priority = this.priority!!
)