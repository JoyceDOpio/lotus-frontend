package com.eternalfairy.timeaware

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

// A container is an object that contains the dependencies that the app requires
interface AppContainer {
    val daysRepository: IDaysRepository
    val tasksRepository: ITasksRepository
    val activitiesRepository: IActivitiesRepository
    val voiceNotesRepository: IVoiceNotesRepository
    val goalsRepository: IGoalsRepository
}

class AppDataContainer(
    private val context: Context
) : AppContainer {
    override val daysRepository: IDaysRepository by lazy {
        RepositoryDays(OfflineDatabase.Companion.getDatabase(context).dayDao())
    }

    override val tasksRepository: ITasksRepository by lazy {
        RepositoryTasks(OfflineDatabase.Companion.getDatabase(context).taskDao())
    }

    override val activitiesRepository: IActivitiesRepository by lazy {
        RepositoryActivities(OfflineDatabase.Companion.getDatabase(context).activityDao())
    }

    override val voiceNotesRepository: IVoiceNotesRepository by lazy {
        RepositoryVoiceNotes(OfflineDatabase.Companion.getDatabase(context).voiceNoteDao())
    }

    override val goalsRepository: IGoalsRepository by lazy {
        RepositoryGoals(OfflineDatabase.Companion.getDatabase(context).goalDao())
    }
}