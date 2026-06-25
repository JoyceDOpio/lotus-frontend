package com.eternalfairy.lotus.model.repository

import com.eternalfairy.lotus.model.dao.postgres.ApiResponse
import com.eternalfairy.lotus.model.data.Goal
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface IGoalRepository {
    suspend fun addGoal(goal: Goal): ApiResponse<Unit>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun deleteGoal(id: Uuid): ApiResponse<Unit>

    suspend fun editGoal(goal: Goal): ApiResponse<Unit>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun getGoal(id: Uuid): ApiResponse<Goal>

    suspend fun getGoals(): ApiResponse<List<Goal>>

    suspend fun getLastPriority(): ApiResponse<Int>
}