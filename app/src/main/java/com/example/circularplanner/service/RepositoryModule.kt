package com.example.circularplanner.service

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.circularplanner.PlannerApplication
import com.example.circularplanner.data.IActivitiesRepository
import com.example.circularplanner.data.OfflineDatabase
import com.example.circularplanner.data.RepositoryActivities
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object RepositoryModule {
    @ServiceScoped
    @Provides
    fun provideActivityRepository(@ApplicationContext context: Context): IActivitiesRepository {
//        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Get the Application object from extras
//        val application = checkNotNull(extras[APPLICATION_KEY])
//        return (application as PlannerApplication).container.activitiesRepository
//        return (context as PlannerApplication).container.activitiesRepository
        return RepositoryActivities(OfflineDatabase.getDatabase(context).activityDao())
    }
}