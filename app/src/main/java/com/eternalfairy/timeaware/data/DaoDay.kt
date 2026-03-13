package com.eternalfairy.timeaware.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoDay {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(day: Day)

    @Update
    suspend fun update(day: Day)

    @Delete
    suspend fun delete(day: Day)

    // With Flow as the return type, you receive notification whenever the data in the database changes. The Room keeps this Flow updated for you, which means you only need to explicitly get the data once. Because of the Flow return type, Room also runs the query on the background thread. You don't need to explicitly make it a suspend function and call it inside a coroutine scope.
    @Query("SELECT * FROM days WHERE date = :date")
//    fun getDay(date: String): Flow<Day>
    fun getDay(date: String): Flow<Day?>
}