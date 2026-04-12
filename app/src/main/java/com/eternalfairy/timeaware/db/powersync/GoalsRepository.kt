package com.eternalfairy.timeaware.db.powersync

import com.eternalfairy.timeaware.db.data.Goal
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class GoalsRepository @Inject constructor(
    private val dataSource: GoalDataSource
) {
    suspend fun deleteGoal(id: UUID): Unit {
        return dataSource.deleteGoalById(id)
    }

    fun getGoalsStream(): Flow<List<Goal>> {
        return dataSource.selectGoals()
    }

    fun getGoalStream(id: UUID): Flow<Goal> {
        return dataSource.selectGoalById(id)
    }

    suspend fun insertGoal(goal: Goal): Unit {
        return dataSource.insertGoal(goal)
    }

    fun getLastPriority(): Flow<Int> {
        return dataSource.getLastPriority()
    }

    suspend fun updateGoal(goal: Goal): Unit {
        return dataSource.updateGoal(goal)
    }

}