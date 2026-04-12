package com.eternalfairy.timeaware

import android.content.Context
import com.eternalfairy.timeaware.db.room.IActivitiesRepository
import com.eternalfairy.timeaware.db.room.IDaysRepository
import com.eternalfairy.timeaware.db.room.IGoalsRepository
import com.eternalfairy.timeaware.db.room.ITasksRepository
import com.eternalfairy.timeaware.db.room.IVoiceNotesRepository
import com.eternalfairy.timeaware.db.room.OfflineDatabase
import com.eternalfairy.timeaware.db.room.RepositoryActivities
import com.eternalfairy.timeaware.db.room.RepositoryDays
import com.eternalfairy.timeaware.db.room.RepositoryGoals
import com.eternalfairy.timeaware.db.room.RepositoryTasks
import com.eternalfairy.timeaware.db.room.RepositoryVoiceNotes

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