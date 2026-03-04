package com.eternalfairy.timeaware

import android.content.Context
import com.eternalfairy.timeaware.data.IActivitiesRepository
import com.eternalfairy.timeaware.data.IDaysRepository
import com.eternalfairy.timeaware.data.IGoalsRepository
import com.eternalfairy.timeaware.data.ITasksRepository
import com.eternalfairy.timeaware.data.IVoiceNotesRepository
import com.eternalfairy.timeaware.data.OfflineDatabase
import com.eternalfairy.timeaware.data.RepositoryActivities
import com.eternalfairy.timeaware.data.RepositoryDays
import com.eternalfairy.timeaware.data.RepositoryGoals
import com.eternalfairy.timeaware.data.RepositoryTasks
import com.eternalfairy.timeaware.data.RepositoryVoiceNotes

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