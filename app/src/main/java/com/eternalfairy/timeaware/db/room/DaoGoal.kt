package com.eternalfairy.timeaware.db.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface DaoGoal {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(goal: Goal)

    @Update
    suspend fun update(goal: Goal): Int

    @Delete
    suspend fun delete(goal: Goal)

    @Query("SELECT * FROM goals WHERE id = :id")
//    fun getGoal(id: UUID): Flow<Goal>
    fun getGoal(id: UUID): Flow<Goal?>

    // Return a list of Goal entities as Flow. Room keeps this Flow updated for you, which means you only need to explicitly get the data once.
    @Query("SELECT * FROM goals ORDER BY priority ASC")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT MAX(priority) FROM goals")
//    fun getLastPriority(): Flow<Int>
    fun getLastPriority(): Flow<Int?>

    @Transaction
    suspend fun updateGoals(firstGoal: Goal, secondGoal: Goal) {
        update(firstGoal)
        update(secondGoal)
    }
}