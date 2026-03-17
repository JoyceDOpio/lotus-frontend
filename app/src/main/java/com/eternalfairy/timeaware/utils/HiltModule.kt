package com.eternalfairy.timeaware.utils

import android.app.Application
import android.content.Context
import com.eternalfairy.timeaware.data.room.IActivitiesRepository
import com.eternalfairy.timeaware.data.room.IDaysRepository
import com.eternalfairy.timeaware.data.room.IGoalsRepository
import com.eternalfairy.timeaware.data.room.ITasksRepository
import com.eternalfairy.timeaware.data.room.IVoiceNotesRepository
import com.eternalfairy.timeaware.data.room.OfflineDatabase
import com.eternalfairy.timeaware.data.room.RepositoryActivities
import com.eternalfairy.timeaware.data.room.RepositoryDays
import com.eternalfairy.timeaware.data.room.RepositoryGoals
import com.eternalfairy.timeaware.data.room.RepositoryTasks
import com.eternalfairy.timeaware.data.room.RepositoryVoiceNotes
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
//@InstallIn(ViewModelComponent::class, ServiceComponent::class)
@InstallIn(SingletonComponent::class)
object HiltModule {
//    @Provides
//    @Singleton
//    fun provideApplicationContext(application: Application): Context {
//        return application.applicationContext
//    }

    // Repositories
    @Singleton
    @Provides
    fun provideActivitiesRepository(@ApplicationContext context: Context) = RepositoryActivities(OfflineDatabase.getDatabase(context).activityDao())

    @Singleton
    @Provides
    fun provideDaysRepository(@ApplicationContext context: Context) = RepositoryDays(OfflineDatabase.getDatabase(context).dayDao())

    @Singleton
    @Provides
    fun provideGoalsRepository(@ApplicationContext context: Context) = RepositoryGoals(OfflineDatabase.getDatabase(context).goalDao())

    @Singleton
    @Provides
    fun provideTasksRepository(@ApplicationContext context: Context) = RepositoryTasks(OfflineDatabase.getDatabase(context).taskDao())

    @Singleton
    @Provides
    fun provideVoiceNotesRepository(@ApplicationContext context: Context) =
        RepositoryVoiceNotes(OfflineDatabase.getDatabase(context).voiceNoteDao())
}

@Module
//@InstallIn(ViewModelComponent::class, ServiceComponent::class)
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindActivitiesRepository(activitiesRepository: RepositoryActivities): IActivitiesRepository

    @Binds
    abstract fun bindDaysRepository(daysRepository: RepositoryDays): IDaysRepository

    @Binds
    abstract fun bindGoalsRepository(goalsRepository: RepositoryGoals): IGoalsRepository

    @Binds
    abstract fun bindTasksRepository(tasksRepository: RepositoryTasks): ITasksRepository

    @Binds
    abstract fun bindVoiceNotesRepository(voiceNotesRepository: RepositoryVoiceNotes): IVoiceNotesRepository
}