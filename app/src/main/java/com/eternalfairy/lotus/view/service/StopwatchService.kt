package com.eternalfairy.lotus.view.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.util.Log
import androidx.compose.ui.graphics.toColorLong
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.eternalfairy.lotus.R
import com.eternalfairy.lotus.view.theme.Teal9
//import com.eternalfairy.timeaware.db.room.IActivitiesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.java

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

        const val NOTIFICATION_CHANNEL_ID = "Stopwatch_Notifications"
        const val NOTIFICATION_CHANNEL_NAME = "STOPWATCH_NOTIFICATION"
        const val MAIN_ACTIVITY_NOTIFICATION_ID = 1
        const val SUB_ACTIVITY_NOTIFICATION_ID = 2

        const val GROUP_KEY = "activities"

        const val CLICK_REQUEST_CODE = 100
        const val CANCEL_REQUEST_CODE = 101
        const val STOP_REQUEST_CODE = 102
        const val RESUME_REQUEST_CODE = 103

        // Intent Extras
        const val STOPWATCH_ACTION = "STOPWATCH_ACTION"
        const val TIME_ELAPSED = "TIME_ELAPSED"
        const val IS_STOPWATCH_RUNNING = "IS_STOPWATCH_RUNNING"
        const val STOPWATCH_ID = "STOPWATCH_ID"

        // Intent Actions
        const val STOPWATCH_TICK = "STOPWATCH_TICK"
        const val STOPWATCH_STATUS = "STOPWATCH_STATUS"
    }

//    @Inject
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    val notificationGroupBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.calendar_success_svgrepo_com)//TODO: Change to my logo
        .setColor(Teal9.toColorLong().toColorInt())
        .setContentTitle("Recording Activities")
        .setGroup(GROUP_KEY)
        .setGroupSummary(true)
        .setOnlyAlertOnce(true)

    private val binder = StopwatchBinder()

    private var job: Job? = null
//    private var jobMain: Job? = null
//    private var jobSub: Job? = null
//    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val scope = CoroutineScope(Dispatchers.IO)
//    private val scope = CoroutineScope(Dispatchers.Default)

//    private var stopwatches = ConcurrentHashMap<Int, Stopwatch>()
    private var stopwatchMain = Stopwatch()
    private var stopwatchSub = Stopwatch()
    private var isStopwatchRunningMain = false
    private var isStopwatchRunningSub = false

    private fun getNotificationBuilder() {
//        val title = "Activity"//TODO: Read the activity's title from the database

        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Main activity")
            .setOngoing(true)
            .setContentText(stopwatchMain.format())
            .setColor(Teal9.toColorLong().toColorInt())
            .setSmallIcon(R.drawable.calendar_success_svgrepo_com)//TODO: Change to my logo
//            .addAction(0, "Stop", ServiceHelper.stopPendingIntent(this))
//            .addAction(0, "Cancel", ServiceHelper.cancelPendingIntent(this))
            .setContentIntent(ServiceHelper.clickPendingIntent(this))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(GROUP_KEY)
            .setOnlyAlertOnce(true)
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
            getNotificationBuilder()
            getNotificationManager()
            startForegroundService()

            intent?.action.let {
                when (it) {
                    START -> startStopwatch { contentText->
                        val stopwatchIntent = Intent()
                        stopwatchIntent.action = STOPWATCH_ACTION
                        stopwatchIntent.putExtra(STOPWATCH_ID, MAIN_ACTIVITY_NOTIFICATION_ID)
                        stopwatchIntent.putExtra(TIME_ELAPSED, contentText)
//                        Log.i("StopwatchService", "stopwatchIntent $stopwatchIntent")
                        sendBroadcast(stopwatchIntent)

                        updateNotification(contentText)
                    }
                    PAUSE -> pauseStopwatch({ contentText ->
                        val stopwatchIntent = Intent()
                        stopwatchIntent.action = STOPWATCH_ACTION
                        stopwatchIntent.putExtra(STOPWATCH_ID, SUB_ACTIVITY_NOTIFICATION_ID)
                        stopwatchIntent.putExtra(TIME_ELAPSED, contentText)
                        sendBroadcast(stopwatchIntent)

                        updateNotification(contentText, SUB_ACTIVITY_NOTIFICATION_ID)
                    })
                    RESUME -> resumeStopwatch({ contentText ->
                        val stopwatchIntent = Intent()
                        stopwatchIntent.action = STOPWATCH_ACTION
                        stopwatchIntent.putExtra(STOPWATCH_ID, MAIN_ACTIVITY_NOTIFICATION_ID)
                        stopwatchIntent.putExtra(TIME_ELAPSED, contentText)
                        sendBroadcast(stopwatchIntent)

                        updateNotification(contentText)

                        notificationManager.cancel(SUB_ACTIVITY_NOTIFICATION_ID)
                    })
                    STOP -> stopStopwatch()
                }
            }
        }).start()

//        return super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun pauseStopwatch(onTick: (contentText: String) -> Unit) {
        isStopwatchRunningMain = false
        stopwatchMain.pause()
        job?.cancel()

        job = scope.launch {
            stopwatchSub.start()
            isStopwatchRunningSub = true

            while (isStopwatchRunningSub) {
                runStopwatch(stopwatchSub, onTick)

//                delay(20)// OPTIMIZE Why 20 not 1000 millis?
                delay(1000)
            }
        }
    }

    private fun resumeStopwatch(onTick: (contentText: String) -> Unit) {
        isStopwatchRunningSub = false
        stopwatchSub.stop()
        job?.cancel()

        // Cancel the sub-activity notification
        notificationManager.cancel(SUB_ACTIVITY_NOTIFICATION_ID)

        stopwatchMain.resume()
        // Update the stopwatch state so that it doesn't show it initial state (i.e. 0)
        stopwatchMain.updateRunningState()
        isStopwatchRunningMain = true

        job = scope.launch {
            while (isStopwatchRunningMain) {
                runStopwatch(stopwatchMain, onTick)
//                delay(20)// OPTIMIZE Why 20 not 1000 millis?
                delay(1000)
            }
        }
    }

    private fun runStopwatch(stopwatch: Stopwatch?, callback: (contentText: String) -> Unit) {
        if (stopwatch == null) return

        stopwatch.updateRunningState()

        val formattedText = stopwatch.format()

        // Update the notification with the new time values
        callback(formattedText)
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

//    @SuppressLint("RestrictedApi")
//    private fun setStopButton() {
//        notificationBuilderMain.mActions.removeAt(0)
//        notificationBuilderMain.mActions.add(
//            0, NotificationCompat.Action(
//                0, "Stop", ServiceHelper.stopPendingIntent(this)
//            )
//        )
//        notificationManager.notify(MAIN_ACTIVITY_NOTIFICATION_ID, notificationBuilderMain.build())
//    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        startForeground(MAIN_ACTIVITY_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun startStopwatch(onTick: (contentText: String) -> Unit) {
        job?.cancel()

        stopwatchMain.start()
        isStopwatchRunningMain = true

        job = scope.launch {
            while (isStopwatchRunningMain) {
                runStopwatch(stopwatchMain, onTick)

//                    delay(20)// OPTIMIZE Why 20 not 1000 millis?
                delay(1000)
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
        job?.cancel()

        stopwatchMain.stop()
        stopwatchSub.stop()

        notificationManager.cancel(MAIN_ACTIVITY_NOTIFICATION_ID)
        notificationManager.cancel(SUB_ACTIVITY_NOTIFICATION_ID)

        stopForegroundService()
    }

    private fun updateNotification(text: String, notificationId: Int = MAIN_ACTIVITY_NOTIFICATION_ID) {
        notificationManager.apply {
            notify(
                notificationId,
                notificationBuilder
                    .setContentTitle(if(notificationId == MAIN_ACTIVITY_NOTIFICATION_ID) "Main" else "Sub")
                    .setContentText(text)
                    .build()
            )
            notify(
                3,
                notificationGroupBuilder.build()
            )
        }
    }

    inner class StopwatchBinder : Binder() {
        fun getService(): StopwatchService = this@StopwatchService
    }
}