package com.eternalfairy.timeaware.db.supabase

import com.eternalfairy.timeaware.db.data.Goal
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GoalsRepository @Inject constructor(
    private val dataSource: GoalDataSource
) {
    fun deleteGoal(id: UUID): Flow<ApiResponse<Unit>> {
        return dataSource.deleteGoalById(id)
    }

    fun getGoalsStream(): Flow<ApiResponse<List<Goal>>> {
        return dataSource.selectGoals()
    }

    fun getGoalStream(id: UUID): Flow<ApiResponse<Goal>> {
        return dataSource.selectGoalById(id)
    }

    fun insertGoal(goal: Goal): Flow<ApiResponse<Unit>> {
        return dataSource.insertGoal(goal)
    }

    fun getLastPriority(): Flow<ApiResponse<Int>> {
        return dataSource.getLastPriority()
    }

    fun updateGoal(goal: Goal): Flow<ApiResponse<Unit>> {
        return dataSource.updateGoal(goal)
    }

}