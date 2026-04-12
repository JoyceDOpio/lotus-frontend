package com.eternalfairy.timeaware.utils

import android.content.Context
import com.eternalfairy.timeaware.db.powersync.ActivitiesRepository
import com.eternalfairy.timeaware.db.powersync.ActivityDataSource
import com.eternalfairy.timeaware.db.powersync.AppSchema
import com.eternalfairy.timeaware.db.powersync.DayDataSource
import com.eternalfairy.timeaware.db.powersync.DaysRepository
import com.eternalfairy.timeaware.db.powersync.GoalDataSource
import com.eternalfairy.timeaware.db.powersync.GoalsRepository
import com.eternalfairy.timeaware.db.powersync.SupabaseConnector
import com.eternalfairy.timeaware.db.powersync.TaskDataSource
import com.eternalfairy.timeaware.db.powersync.TasksRepository
import com.eternalfairy.timeaware.db.powersync.UserProfileDataSource
import com.eternalfairy.timeaware.db.powersync.UserProfilesRepository
import com.eternalfairy.timeaware.db.powersync.VoiceNoteDataSource
import com.eternalfairy.timeaware.db.powersync.VoiceNotesRepository
import com.powersync.DatabaseDriverFactory
import com.powersync.PowerSyncDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.cdimascio.dotenv.dotenv
import io.github.jan.supabase.SupabaseClient
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PowerSync

@Module
@InstallIn(SingletonComponent::class)
object PowerSyncModule {
    //    @Provides
//    @Singleton
//    fun provideApplicationContext(application: Application): Context {
//        return application.applicationContext
//    }
    val dotenv = dotenv()

    @Provides
    @Singleton
    suspend fun providePowerSyncDatabase(@ApplicationContext context: Context, supabaseClient: SupabaseClient): PowerSyncDatabase {
        val driverFactory = DatabaseDriverFactory(context)
        val powersyncDatabase = PowerSyncDatabase(
            factory = driverFactory,
            schema = AppSchema,
            dbFilename = "powersync.db"
        )

//        val supabaseClient = createSupabaseClient(
//            supabaseUrl = dotenv["SUPABASE_URL"],
//            supabaseKey = dotenv["SUPABASE_KEY"]
//        ) {
//            install(Postgrest)
//        }

        powersyncDatabase.connect(SupabaseConnector(
            supabaseClient,
            dotenv["POWERSYNC_URL"]
        ))

        return powersyncDatabase
    }

    @Provides
    @Singleton
    @PowerSync
    fun provideActivityDataSource(powerSyncDatabase: PowerSyncDatabase): ActivityDataSource {
        return ActivityDataSource(powerSyncDatabase)
    }

    @Provides
    @Singleton
    @PowerSync
    fun provideDayDataSource(powerSyncDatabase: PowerSyncDatabase): DayDataSource {
        return DayDataSource(powerSyncDatabase)
    }

    @Provides
    @Singleton
    @PowerSync
    fun provideGoalDataSource(powerSyncDatabase: PowerSyncDatabase): GoalDataSource {
        return GoalDataSource(powerSyncDatabase)
    }

    @Provides
    @Singleton
    @PowerSync
    fun provideTaskDataSource(powerSyncDatabase: PowerSyncDatabase): TaskDataSource {
        return TaskDataSource(powerSyncDatabase)
    }

    @Provides
    @Singleton
    @PowerSync
    fun provideUserProfileDataSource(powerSyncDatabase: PowerSyncDatabase): UserProfileDataSource {
        return UserProfileDataSource(powerSyncDatabase)
    }

    @Provides
    @Singleton
    @PowerSync
    fun provideVoiceNoteDataSource(powerSyncDatabase: PowerSyncDatabase): VoiceNoteDataSource {
        return VoiceNoteDataSource(powerSyncDatabase)
    }


    @Provides
    @Singleton
    @PowerSync
    fun provideActivitiesRepository(@PowerSync dataSource: ActivityDataSource): ActivitiesRepository {
        return ActivitiesRepository(dataSource)
    }

    @Provides
    @Singleton
    @PowerSync
    fun provideDaysRepository(dataSource: DayDataSource): DaysRepository {
        return DaysRepository(dataSource)
    }

    @Provides
    @Singleton
    @PowerSync
    fun provideGoalsRepository(dataSource: GoalDataSource): GoalsRepository {
        return GoalsRepository(dataSource)
    }

    @Provides
    @Singleton
    @PowerSync
    fun provideTasksRepository(dataSource: TaskDataSource): TasksRepository {
        return TasksRepository(dataSource)
    }

    @Provides
    @Singleton
    @PowerSync
    fun provideUserProfilesRepository(dataSource: UserProfileDataSource): UserProfilesRepository {
        return UserProfilesRepository(dataSource)
    }

    @Provides
    @Singleton
    @PowerSync
    fun provideVoiceNotesRepository(dataSource: VoiceNoteDataSource): VoiceNotesRepository {
        return VoiceNotesRepository(dataSource)
    }
}