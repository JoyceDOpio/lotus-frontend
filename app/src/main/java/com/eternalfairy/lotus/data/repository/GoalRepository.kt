package com.eternalfairy.lotus.data.repository

import com.eternalfairy.lotus.data.dao.IGoalDAO
import com.eternalfairy.lotus.data.dao.postgres.ApiResponse
import com.eternalfairy.lotus.data.model.Goal
import com.eternalfairy.lotus.domain.repository.IGoalRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Singleton
class GoalRepository @Inject constructor(
    private val dao: IGoalDAO
): IGoalRepository {
    override suspend fun addGoal(goal: Goal): ApiResponse<Unit> {
        return try {
            dao.insertGoal(goal)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun deleteGoal(id: Uuid): ApiResponse<Unit> {
        return try {
            dao.deleteGoal(id)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun editGoal(goal: Goal): ApiResponse<Unit> {
        return try {
            dao.updateGoal(goal)

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun getGoal(id: Uuid): ApiResponse<Goal> {
        return try {
            val goal = dao.getGoal(id)

            ApiResponse.Success(goal)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getGoals(): ApiResponse<List<Goal>> {
        return try {
            val goals = dao.getGoals()

            ApiResponse.Success(goals)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getLastPriority(): ApiResponse<Int> {
        return try {
            val priority = dao.getLastPriority()

            ApiResponse.Success(priority)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }
}