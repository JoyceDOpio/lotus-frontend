package com.eternalfairy.timeaware.utils

import com.eternalfairy.timeaware.db.supabase.ActivitiesRepository
import com.eternalfairy.timeaware.db.supabase.ActivityDataSource
import com.eternalfairy.timeaware.db.supabase.DayDataSource
import com.eternalfairy.timeaware.db.supabase.DaysRepository
import com.eternalfairy.timeaware.db.supabase.GoalDataSource
import com.eternalfairy.timeaware.db.supabase.GoalsRepository
import com.eternalfairy.timeaware.db.supabase.TaskDataSource
import com.eternalfairy.timeaware.db.supabase.TasksRepository
import com.eternalfairy.timeaware.db.supabase.UserProfileDataSource
import com.eternalfairy.timeaware.db.supabase.UserProfilesRepository
import com.eternalfairy.timeaware.db.supabase.VoiceNoteDataSource
import com.eternalfairy.timeaware.db.supabase.VoiceNotesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.cdimascio.dotenv.dotenv
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Supabase

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {
    val dotenv = dotenv()

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = dotenv["SUPABASE_URL"],
            supabaseKey = dotenv["SUPABASE_KEY"]
        ) {
            install(Postgrest)
        }
    }

    @Provides
    @Singleton
    fun provideActivityDataSource(supabaseClient: SupabaseClient): ActivityDataSource {
        return ActivityDataSource(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideDayDataSource(supabaseClient: SupabaseClient): DayDataSource {
        return DayDataSource(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideGoalDataSource(supabaseClient: SupabaseClient): GoalDataSource {
        return GoalDataSource(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideTaskDataSource(supabaseClient: SupabaseClient): TaskDataSource {
        return TaskDataSource(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideUserProfileDataSource(supabaseClient: SupabaseClient): UserProfileDataSource {
        return UserProfileDataSource(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideVoiceNoteDataSource(supabaseClient: SupabaseClient): VoiceNoteDataSource {
        return VoiceNoteDataSource(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideActivitiesRepository(@Supabase dataSource: ActivityDataSource): ActivitiesRepository {
        return ActivitiesRepository(dataSource)
    }

    @Provides
    @Singleton
    fun provideDaysRepository(dataSource: DayDataSource): DaysRepository {
        return DaysRepository(dataSource)
    }

    @Provides
    @Singleton
    fun provideGoalsRepository(dataSource: GoalDataSource): GoalsRepository {
        return GoalsRepository(dataSource)
    }

    @Provides
    @Singleton
    fun provideTasksRepository(dataSource: TaskDataSource): TasksRepository {
        return TasksRepository(dataSource)
    }

    @Provides
    @Singleton
    fun provideUserProfilesRepository(dataSource: UserProfileDataSource): UserProfilesRepository {
        return UserProfilesRepository(dataSource)
    }

    @Provides
    @Singleton
    fun provideVoiceNotesRepository(dataSource: VoiceNoteDataSource): VoiceNotesRepository {
        return VoiceNotesRepository(dataSource)
    }
}