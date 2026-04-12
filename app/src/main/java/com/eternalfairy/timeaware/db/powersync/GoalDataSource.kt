package com.eternalfairy.timeaware.db.powersync

import com.eternalfairy.timeaware.db.data.Activity
import com.eternalfairy.timeaware.db.data.Goal
import com.powersync.PowerSyncDatabase
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject

class GoalDataSource @Inject constructor(
    private val powerSyncDatabase:PowerSyncDatabase
) {
    suspend fun deleteGoalById(id: UUID): Unit {
        try {
            powerSyncDatabase.writeTransaction { transaction ->
                transaction.execute(
                    sql = "DELETE FROM goals WHERE id = ?",
                    parameters = listOf(id)
                )
            }
        } catch (e: Exception) {
        }
    }

    fun getLastPriority(): Flow<Int> {
        return flow {
            try {
                val priority = powerSyncDatabase.get(
                    sql = "SELECT MAX(priority) FROM goals AS last_priority"
                ) { cursor ->
                    cursor.getString("last_priority").toInt()
                }
                emit(priority)
            } catch (e: Exception) {
            }
        }
    }

    suspend fun insertGoal(goal: Goal): Unit {
        try {
            powerSyncDatabase.writeTransaction { transaction ->
                transaction.execute(
                    sql = "INSERT INTO goals (created_at, user_id, title, priority) VALUES (?, ?, ?, ?)",
                    parameters = listOf(
                        goal.createdAt,
                        goal.userId,
                        goal.title,
                        goal.priority
                    )
                )
            }
        } catch (e: Exception) {
        }
    }

    fun selectGoals(): Flow<List<Goal>> {
        try {
            val goals = powerSyncDatabase.watch(
                sql = "SELECT * FROM goals"
            ) { cursor ->
                Goal(
                    id = UUID.fromString(cursor.getString("id")),
                    createdAt = cursor.getString("created_at"),
                    userId = UUID.fromString(cursor.getString("user_id")),
                    title = cursor.getString("title"),
                    priority = cursor.getString("priority").toInt()
                )
            }
            return goals
        } catch (e: Exception) {
            return emptyFlow()
        }
    }

    fun selectGoalById(id: UUID): Flow<Goal> {
        return flow {
            try {
                val goal = powerSyncDatabase.get(
                    sql = "SELECT * FROM goals WHERE id = ?",
                    parameters = listOf(id)
                ) { cursor ->
                    Goal(
                        id = UUID.fromString(cursor.getString("id")),
                        createdAt = cursor.getString("created_at"),
                        userId = UUID.fromString(cursor.getString("user_id")),
                        title = cursor.getString("title"),
                        priority = cursor.getString("priority").toInt()
                    )
                }
                emit(goal)
            } catch (e: Exception) {
            }
        }
    }

    suspend fun updateGoal(goal: Goal): Unit {
        try {
            powerSyncDatabase.execute(
                sql = "UPDATE goals SET title = ?, priority = ? WHERE id = ?",
                parameters = listOf(
                    goal.title,
                    goal.priority,
                    goal.id
                )
            )
        } catch (e: Exception) {
        }
    }
}
