package com.eternalfairy.lotus.model.dao.postgres

import com.eternalfairy.lotus.model.dao.IGoalDAO
import com.eternalfairy.lotus.model.data.Goal
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GoalDAO(
    private val api: GoalApi
): IGoalDAO {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteGoal(id: Uuid) {
        return api.deleteGoal(id)
    }

    override suspend fun insertGoal(goal: Goal) {
        return api.insertGoal(goal)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getGoal(id: Uuid): Goal? {
        return api.getGoal(id)
    }

    override suspend fun getLastPriority(): Int? {
        return api.getLastPriority()
    }

    override suspend fun getGoals(): List<Goal> {
        return api.getGoals()
    }

    override suspend fun updateGoal(goal: Goal) {
        return api.updateGoal(goal)
    }
}