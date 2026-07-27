package com.eternalfairy.lotus.view.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.eternalfairy.lotus.R
import com.eternalfairy.lotus.model.repository.ActivityRepository
//import com.eternalfairy.timeaware.db.room.IActivitiesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@AndroidEntryPoint
class StopwatchService: Service() {
    companion object {
        // Service actions
        // - start the main activity
        const val START = "START"
        // - stop both activities
        const val STOP = "STOP"
        // - pause the main activity and start the sub-activity
        const val PAUSE = "PAUSE"
        // - resume the main activity and pause the sub-activity
        const val RESUME = "RESUME"

        const val STOPWATCH_STATE = "STOPWATCH_STATE"

        const val NOTIFICATION_CHANNEL_ID = "Stopwatch_Notifications"
        const val NOTIFICATION_CHANNEL_NAME = "STOPWATCH_NOTIFICATION"
        const val MAIN_ACTIVITY_NOTIFICATION_ID = 1
        const val SUB_ACTIVITY_NOTIFICATION_ID = 2

        const val GROUP_KEY = "activities"

        const val CLICK_REQUEST_CODE = 100
        const val CANCEL_REQUEST_CODE = 101
        const val STOP_REQUEST_CODE = 102
        const val RESUME_REQUEST_CODE = 103
    }

    @Inject
    lateinit var activityRepository: ActivityRepository

    private lateinit var notificationManager: NotificationManager
    private val binder = StopwatchBinder()

    private var job: Job? = null
//    private var jobMain: Job? = null
//    private var jobSub: Job? = null
//    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val scope = CoroutineScope(Dispatchers.Default)

    private var stopwatches = ConcurrentHashMap<Int, Stopwatch>()

    private fun buildNotification(): Notification {
        val title = "Activity"//TODO: Read the activity's title from the database

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setOngoing(true)
            .setContentText("00:00:00")
            .setColorized(true)
            .setColor("#BEAEE2".toColorInt())
            .setSmallIcon(R.drawable.calendar_success_svgrepo_com)//TODO: Change to my logo
            .setOngoing(true)
//            .addAction(0, "Stop", ServiceHelper.stopPendingIntent(this))
//            .addAction(0, "Cancel", ServiceHelper.cancelPendingIntent(this))
            .setContentIntent(ServiceHelper.clickPendingIntent(this))
            .setGroup(GROUP_KEY)
            .build()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is not in the Support Library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                // IMPORTANCE_HIGH makes the notification hang at the top of the screen which obscures the screen
                NotificationManager.IMPORTANCE_DEFAULT
            )
//            notificationChannel.setSound(null, null)
            notificationChannel.setShowBadge(true)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    fun getStopwatch(id: Int): Stopwatch? {
        return stopwatches.get(id)
    }

    private fun getNotificationManager() {
        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java,
        ) as NotificationManager
    }

    override fun onBind(intent: Intent?) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread(Runnable {
            createNotificationChannel()
            getNotificationManager()
            startForegroundService()

            intent?.action.let {
                when (it) {
                    START -> startStopwatch { contentText->
                        updateNotification(contentText)
                    }
                    PAUSE -> pauseStopwatch({ contentText ->
                        updateNotification(contentText, SUB_ACTIVITY_NOTIFICATION_ID)
                    })
                    RESUME -> resumeStopwatch({ contentText ->
                        updateNotification(contentText)
                    })
                    STOP -> stopStopwatch()
                }
            }
        }).start()

        return super.onStartCommand(intent, flags, startId)
    }

//    @SuppressLint("RestrictedApi")
//    private fun setStopButton() {
//        notificationBuilder.mActions.removeAt(0)
//        notificationBuilder.mActions.add(
//            0, NotificationCompat.Action(
//                0, "Stop", ServiceHelper.stopPendingIntent(this)
//            )
//        )
//        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
//    }

    private fun pauseStopwatch(onTick: (contentText: String) -> Unit) {
//        // Cancel the coroutine which updates the main activity stopwatch
//        job?.cancel()

        // Pause the main activity stopwatch
        stopwatches.get(MAIN_ACTIVITY_NOTIFICATION_ID)?.pause()

        // Create a stopwatch for the sub-activity
        stopwatches.getOrPut(SUB_ACTIVITY_NOTIFICATION_ID) {
            Stopwatch()
        }

        job = scope.launch {
//        jobSub = scope.launch {
            // Start the sub-activity stopwatch
            val stopwatch = stopwatches.get(SUB_ACTIVITY_NOTIFICATION_ID)
            stopwatch?.start()

            while (true) {
                runStopwatch(stopwatch, onTick)

                delay(20)// OPTIMIZE Why 20 not 1000 millis?
//                    delay(1000)
            }
        }
    }

    private fun resumeStopwatch(onTick: (contentText: String) -> Unit) {
        // Cancel the coroutine which updates the sub-activity stopwatch
        job?.cancel()

        // Cancel the sub-activity notification
        notificationManager.cancel(SUB_ACTIVITY_NOTIFICATION_ID)

        // Stop the sub activity stopwatch
        stopwatches.get(SUB_ACTIVITY_NOTIFICATION_ID)?.stop()

        job = scope.launch {
            // Resume the main activity stopwatch
            val stopwatch = stopwatches.get(MAIN_ACTIVITY_NOTIFICATION_ID)
            stopwatch?.resume()

            while (true) {
                runStopwatch(stopwatch, onTick)

                delay(20)// OPTIMIZE Why 20 not 1000 millis?
//                    delay(1000)
            }
        }
    }

//    @SuppressLint("RestrictedApi")
//    private fun setResumeButton() {
//        notificationBuilder.mActions.removeAt(0)
//        notificationBuilder.mActions.add(
//            0, NotificationCompat.Action(
//                0, "Resume", ServiceHelper.resumePendingIntent(this)
//            )
//        )
//        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
//    }

    private fun runStopwatch(stopwatch: Stopwatch?, callback: (contentText: String) -> Unit) {
        if (stopwatch == null) return

        // Update the stopwatch
        stopwatch.updateRunningState()

        // Read the time values from the stopwatch
        val hours = stopwatch.hours
        val minutes = stopwatch.minutes
        val seconds = stopwatch.seconds

        val formattedText = stopwatch.format()

        // Update the notification with the new time values
        callback(formattedText)
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        startForeground(MAIN_ACTIVITY_NOTIFICATION_ID, buildNotification())
    }

    private fun startStopwatch(onTick: (contentText: String) -> Unit) {
        // Create a stopwatch for the main activity
        stopwatches.getOrPut(MAIN_ACTIVITY_NOTIFICATION_ID) {
            Stopwatch()
        }

        if (job == null) {
            job = scope.launch {
                // Start the stopwatch
                val stopwatch = stopwatches.get(MAIN_ACTIVITY_NOTIFICATION_ID)
                stopwatch?.start()

                while (true) {
                    runStopwatch(stopwatch, onTick)

//                    delay(20)// OPTIMIZE Why 20 not 1000 millis?
                    delay(1000)
                }
            }
        }
    }

    private fun stopForegroundService() {
        notificationManager.cancelAll()
        stopForeground(STOP_FOREGROUND_REMOVE)
        // Stop the service
        stopSelf()
    }

    private fun stopStopwatch() {
        // Stop both stopwatches
        stopwatches.forEach { entry -> entry.value.stop() }

        // Cancel coroutine
        job?.cancel()
        stopForegroundService()
    }

    private fun updateNotification(text: String, notificationId: Int = MAIN_ACTIVITY_NOTIFICATION_ID) {
            notificationManager.notify(
                notificationId,
                NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//            .setContentTitle(title)
                    .setOngoing(true)
                    // For devices running Android 7.1 (API level 25) or lower
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentText(text)
                    .setColorized(true)
                    .setColor("#BEAEE2".toColorInt())
//                    .setColor(Color.parseColor("0xffBEAEE2"))
                    .setSmallIcon(R.drawable.calendar_success_svgrepo_com)//TODO: Change to my logo
                    .setOngoing(true)
//            .addAction(0, "Stop", ServiceHelper.stopPendingIntent(this))
//            .addAction(0, "Cancel", ServiceHelper.cancelPendingIntent(this))
                    .setContentIntent(ServiceHelper.clickPendingIntent(this))
                    .setGroup(GROUP_KEY)
                    .build()
            )
    }

    inner class StopwatchBinder : Binder() {
        fun getService(): StopwatchService = this@StopwatchService
    }
}