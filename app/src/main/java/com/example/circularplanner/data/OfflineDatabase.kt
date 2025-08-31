package com.example.circularplanner.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Database class with a singleton Instance object
@Database(
    entities = [
        Day::class,
        Task::class,
        Activity:: class,
        VoiceNote::class,
        Goal::class
    ],
    // Whenever you change the schema of the database table, you have to increase the version number
    version = 10,
    // Keep schema version history backups
    exportSchema = false,
//    exportSchema = true,
//    autoMigrations = [
//        AutoMigration (from = 8, to = 9)
//    ]
)
@TypeConverters(Converters::class
)
abstract class OfflineDatabase : RoomDatabase() {
    abstract fun dayDao(): DaoDay
    abstract fun taskDao() : DaoTask
    abstract fun activityDao() : DaoActivity
    abstract fun voiceNoteDao(): DaoVoiceNote
    abstract fun goalDao(): DaoGoal

    companion object {
        // The value of a volatile variable is never cached, and all reads and writes are to and from the main memory. These features help ensure the value of Instance is always up to date and is the same for all execution threads. It means that changes made by one thread to Instance are immediately visible to all other threads.
        @Volatile
        private var Instance: OfflineDatabase? = null

        fun getDatabase(applicationContext: Context): OfflineDatabase {
            // Multiple threads can potentially ask for a database instance at the same time, which results in two databases instead of one. This issue is known as a race condition. Wrapping the code to get the database inside a synchronized block means that only one thread of execution at a time can enter this block of code, which makes sure the database only gets initialized once. Use synchronized{} block to avoid the race condition.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context = applicationContext,
                    klass = OfflineDatabase::class.java,
                    name = "test_database"
                )
                    // Normally, you would provide a migration object with a migration strategy for when the schema changes. A migration object is an object that defines how you take all rows with the old schema and convert them to rows in the new schema, so that no data is lost.
                    .fallbackToDestructiveMigration(false)
                    .build()
                    // Assign Instance = it to keep a reference to the recently created db instance
                    .also { Instance = it }
            }
        }
    }
}