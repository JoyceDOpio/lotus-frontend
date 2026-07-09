package com.eternalfairy.lotus.model.dao.powersync

import android.util.Log
import com.eternalfairy.lotus.model.data.Goal
import com.powersync.db.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Singleton
class GoalDAO @Inject constructor(
    private val dataSource: OnlineSyncDataSource
) {
    suspend fun deleteGoalById(id: String): Unit {
        try {
            dataSource.getDatabase().writeTransaction { transaction ->
                transaction.execute(
                    sql = "DELETE FROM goals WHERE id = ?",
                    parameters = listOf(id)
                )
            }
        } catch (e: Exception) {
            Log.e("GoalDAO", e.printStackTrace().toString())
        }
    }

    fun getLastPriority(): Flow<Int?> {
        return flow {
            try {
                val priority = dataSource.getDatabase().getOptional(
                    sql = "SELECT MAX(priority) FROM goals AS last_priority"//FIXME: Column last_priority not found
                ) { cursor ->
                    cursor.getString("last_priority").toInt()
                }
                emit(priority)
            } catch (e: Exception) {
                Log.e("GoalDAO", e.printStackTrace().toString())
            }
        }
    }

    suspend fun insertGoal(goal: Goal): Unit {
        try {
            dataSource.getDatabase().writeTransaction { transaction ->
                transaction.execute(
                    sql = "INSERT INTO goals (created_at, user_id, title, priority) VALUES (?, ?, ?, ?)",
                    parameters = listOf(
                        goal.title,
                        goal.priority
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("GoalDAO", e.printStackTrace().toString())
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun selectGoals(): Flow<List<Goal>> {
        try {
            val goals = dataSource.getDatabase().watch(
                sql = "SELECT * FROM goals"
            ) { cursor ->
                Goal(
                    id = Uuid.parse(cursor.getString("id")),
                    title = cursor.getString("title"),
                    priority = cursor.getString("priority").toInt()
                )
            }
            return goals
        } catch (e: Exception) {
            Log.e("GoalDAO", e.printStackTrace().toString())
            return emptyFlow()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun selectGoalById(id: String): Flow<Goal?> {
        return flow {
            try {
                val goal = dataSource.getDatabase().getOptional(
                    sql = "SELECT * FROM goals WHERE id = ?",
                    parameters = listOf(id)
                ) { cursor ->
                    Goal(
                        id = Uuid.parse(cursor.getString("id")),
                        title = cursor.getString("title"),
                        priority = cursor.getString("priority").toInt()
                    )
                }
                emit(goal)
            } catch (e: Exception) {
                Log.e("GoalDAO", e.printStackTrace().toString())
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun updateGoal(goal: Goal) {
        try {
            dataSource.getDatabase().execute(
                sql = "UPDATE goals SET title = ?, priority = ? WHERE id = ?",
                parameters = listOf(
                    goal.title,
                    goal.priority,
                    goal.id
                )
            )
        } catch (e: Exception) {
            Log.e("GoalDAO", e.printStackTrace().toString())
        }
    }
}
