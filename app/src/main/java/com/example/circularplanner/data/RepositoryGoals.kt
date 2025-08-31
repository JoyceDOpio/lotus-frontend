package com.example.circularplanner.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RepositoryGoals(private val goalDao: DaoGoal): IGoalsRepository {
    override fun getAllGoals(): Flow<List<Goal>> {
        return goalDao.getAllGoals()
    }

    override fun getGoalStream(id: UUID): Flow<Goal?> {
        return goalDao.getGoal(id)
    }

    override fun getLastPriority(): Flow<Int?> {
        return goalDao.getLastPriority()
    }

    override suspend fun insertGoal(goal: Goal) {
        return goalDao.insert(goal)
    }

    override suspend fun deleteGoal(goal: Goal) {
        return goalDao.delete(goal)
    }

    override suspend fun updateGoal(goal: Goal): Int {
        return goalDao.update(goal)
    }

    override suspend fun updateGoals(
        firstGoal: Goal,
        secondGoal: Goal
    ) {
        return goalDao.updateGoals(
            firstGoal,
            secondGoal
        )
    }
}