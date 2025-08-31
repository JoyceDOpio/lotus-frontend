package com.example.circularplanner.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface IGoalsRepository {
    fun getAllGoals(): Flow<List<Goal>>

    fun getGoalStream(id: UUID): Flow<Goal?>

    fun getLastPriority(): Flow<Int?>

    suspend fun insertGoal(goal: Goal)

    suspend fun deleteGoal(goal: Goal)

    suspend fun updateGoals(firstGoal: Goal, secondGoal: Goal)

    suspend fun updateGoal(goal: Goal): Int
}