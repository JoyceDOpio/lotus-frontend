package com.eternalfairy.lotus.data.test

import android.util.Log
import com.eternalfairy.lotus.data.dao.postgres.ApiResponse
import com.eternalfairy.lotus.data.model.Goal
import com.eternalfairy.lotus.domain.repository.IGoalRepository
import kotlin.uuid.Uuid

class MockGoalRepository: IGoalRepository {
    var mockGoals = mutableListOf<Goal>(
        Goal(
            id = Uuid.Companion.parse("495502d1-4f23-4c31-8edb-4ec1107f35be"),
            title = "predefined goal",
            priority = 1
        ),
        Goal(
            id = Uuid.Companion.parse("5f6c53ca-b5d7-4aec-89af-4f725bc8c50f"),
            title = "predefined goal 2",
            priority = 2
        ),
        Goal(
            id = Uuid.Companion.parse("98e36da8-521f-4e14-a370-4740aa4498ff"),
            title = "predefined goal 3",
            priority = 3
        )
    )

    override suspend fun addGoal(goal: Goal): ApiResponse<Unit> {
        return try {
            mockGoals += goal
            Log.i("MockGoalRepository", "addGoal() mockGoals: $mockGoals")

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun deleteGoal(id: Uuid): ApiResponse<Unit> {
        return try {
            for (goal in mockGoals) {
                if (goal.id == id) {
                    mockGoals.remove(goal)
                }
            }
            Log.i("MockGoalRepository", "deleteGoal() mockGoals: $mockGoals")

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun editGoal(goal: Goal): ApiResponse<Unit> {
        return try {
            mockGoals.forEach { goalToBeEdited ->
                if (goalToBeEdited.id == goal.id) {
                    mockGoals.remove(goalToBeEdited)
                    mockGoals.add(goal)
                }
            }
            Log.i("MockGoalRepository", "editGoal() mockGoals: $mockGoals")

            ApiResponse.Success()
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getGoal(id: Uuid): ApiResponse<Goal> {
        return try {
            val goal = mockGoals.find { g -> g.id == id }

            ApiResponse.Success(goal)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getGoals(): ApiResponse<List<Goal>> {
        return try {
            val goals = mockGoals

            ApiResponse.Success(goals.sortedBy { it.priority })
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }

    override suspend fun getLastPriority(): ApiResponse<Int> {
        return try {
            var priority = 0
            mockGoals.forEach { goal ->
                goal.priority?.let {
                    if (it > priority) priority = it
                }
            }

            ApiResponse.Success(priority)
        } catch (e: Exception) {
            ApiResponse.Error(e.message)
        }
    }
}