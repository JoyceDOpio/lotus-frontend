package com.eternalfairy.lotus.data.dao

import com.eternalfairy.lotus.data.model.Goal
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface IGoalDAO {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun deleteGoal(id: Uuid)

    suspend fun insertGoal(goal: Goal)

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getGoal(id: Uuid): Goal?

    suspend fun getGoals(): List<Goal>

    suspend fun getLastPriority(): Int?

    suspend fun updateGoal(goal: Goal): Unit
}