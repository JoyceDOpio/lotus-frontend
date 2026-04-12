package com.eternalfairy.timeaware.db.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface DaoTask {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task): Int

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :id")
//    fun getTask(id: UUID): Flow<Task>
    fun getTask(id: UUID): Flow<Task?>

    // Return a list of Task entities as Flow. Room keeps this Flow updated for you, which means you only need to explicitly get the data once.
    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY start_time ASC")
    fun getAllTasks(date: String): Flow<List<Task>>

    @Query("SELECT MAX(priority) FROM tasks")
//    fun getLastPriority(): Flow<Int>
    fun getLastPriority(): Flow<Int?>

    @Query("SELECT * FROM tasks WHERE date IS NULL ORDER BY priority ASC")
    fun getTasksWithoutDate(): Flow<List<Task>>
}