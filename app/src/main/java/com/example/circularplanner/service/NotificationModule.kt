package com.example.circularplanner.service

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.circularplanner.PlannerApplication
import com.example.circularplanner.R
import com.example.circularplanner.data.Activity
import com.example.circularplanner.data.OfflineDatabase
import com.example.circularplanner.data.RepositoryActivities
import com.example.circularplanner.service.Constants.NOTIFICATION_CHANNEL_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Module
@InstallIn(ServiceComponent::class)
// Updates and modifies the notification along the stopwatch
object NotificationModule {
    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(@ApplicationContext context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
//            .setContentTitle(recordedActivity?.title ?: "Activity")//TODO: Set it to the activity's title
            .setContentTitle("Activity")//TODO: Set it to the activity's title
            // Default text of the notification
            .setContentText("00:00:00")
            .setSmallIcon(R.drawable.calendar_success_svgrepo_com)//TODO: Change to my logo
            // An ongoing notification cannot be dismissed by the user. It has to be cancelled by the service or application
            .setOngoing(true)
//            .addAction(0, "Stop", ServiceHelper.stopPendingIntent(context))
//            .addAction(0, "Cancel", ServiceHelper.cancelPendingIntent(context))
            .setContentIntent(ServiceHelper.clickPendingIntent(context))
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}