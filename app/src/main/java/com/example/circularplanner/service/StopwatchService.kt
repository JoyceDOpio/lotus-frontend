package com.example.circularplanner.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.circularplanner.R
import com.example.circularplanner.data.IActivitiesRepository
import com.example.circularplanner.service.Constants.PAUSE
import com.example.circularplanner.service.Constants.START
import com.example.circularplanner.service.Constants.STOP
import com.example.circularplanner.service.Constants.NOTIFICATION_CHANNEL_ID
import com.example.circularplanner.service.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.circularplanner.service.Constants.NOTIFICATION_ID
import com.example.circularplanner.service.Constants.RESUME
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@AndroidEntryPoint
class StopwatchService: Service() {
    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var activityRepository: IActivitiesRepository

//    val context = LocalContext.current
//    var activityRepository: IActivitiesRepository = RepositoryActivities(OfflineDatabase.getDatabase(this).activityDao())

    private val binder = StopwatchBinder()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    //    private var duration: Duration = Duration.ZERO
    //    private var subDuration: Duration = Duration.ZERO
    var duration: Duration = Duration.ZERO
    var subDuration: Duration = Duration.ZERO
    private lateinit var timer: Timer
    private lateinit var subTimer: Timer
//
    var seconds = mutableIntStateOf(0)
        private set
    var minutes = mutableIntStateOf(0)
        private set
    var hours = mutableIntStateOf(0)
        private set
    var subSeconds = mutableIntStateOf(0)
        private set
    var subMinutes = mutableIntStateOf(0)
        private set
    var subHours = mutableIntStateOf(0)
        private set
    var currentState = mutableStateOf(StopwatchState.Main)
        private set

    private fun buildNotification(): Notification {
        val title = "Activity"//TODO: Read the activity's title from the database

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setOngoing(true)
            .setContentText(
                "${"%02d".format(hours)}:${"%02d".format(minutes)}:${
                    "%02d".format(
                        seconds
                    )
                }"
            )
            .setColorized(true)
            .setColor(Color.parseColor("#BEAEE2"))
            .setSmallIcon(R.drawable.calendar_success_svgrepo_com)//TODO: Change to my logo
            .setOngoing(true)
//            .addAction(0, "Stop", ServiceHelper.stopPendingIntent(this))
//            .addAction(0, "Cancel", ServiceHelper.cancelPendingIntent(this))
            .setContentIntent(ServiceHelper.clickPendingIntent(this))
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.setSound(null, null)
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
            getNotificationManager()

            intent?.action.let {
                when (it) {
                    START -> startStopwatch { hours, minutes, seconds ->
                        updateNotification(hours = hours, minutes = minutes, seconds = seconds)
                    }
                    PAUSE -> pauseStopwatch({ hours, minutes, seconds ->
                        updateNotification(hours = hours, minutes = minutes, seconds = seconds)
                    })
                    RESUME -> resumeStopwatch({ hours, minutes, seconds ->
                        updateNotification(hours = hours, minutes = minutes, seconds = seconds)
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

    private fun resumeStopwatch(onTick: (hr: String, min: String, sec: String) -> Unit) {
        if (this::subTimer.isInitialized) subTimer.cancel()
        currentState.value = StopwatchState.Main
        // Start the main timer again
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.plus(1.
            seconds)
            updateTimeUnits()
            onTick(
                hours.value.pad(),
                minutes.value.pad(),
                seconds.value.pad()
            )
        }
    }

    private fun pauseStopwatch(onTick: (hr: String, min: String, sec: String) -> Unit) {
        if (this::timer.isInitialized) timer.cancel()
        currentState.value = StopwatchState.Sub
        // Start the sub-timer
        subTimer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            subDuration = subDuration.plus(1.
            seconds)
            updateTimeUnits()
            onTick(
                subHours.value.pad(),
                subMinutes.value.pad(),
                subSeconds.value.pad()
            )
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

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun startStopwatch(onTick: (hr: String, min: String, sec: String) -> Unit) {
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.plus(1.seconds)
            updateTimeUnits()
            onTick(
                hours.value.pad(),
                minutes.value.pad(),
                seconds.value.pad()
            )
        }
    }

    private fun stopForegroundService() {
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        // Cancel coroutines
        job.cancel()
        // Stop the service
        stopSelf()
    }

    private fun stopStopwatch() {
        duration = Duration.ZERO
        currentState.value = StopwatchState.Main
        stopForegroundService()
    }

    private fun updateNotification(hours: String, minutes: String, seconds: String) {
            notificationManager.notify(
                NOTIFICATION_ID, notificationBuilder.setContentText(
                    formatTime(
                        hours = hours,
                        minutes = minutes,
                        seconds = seconds
                    )
                ).build()
            )
    }

//    private fun updateNotification() {
//        notificationManager.notify(
//            1,
//            buildNotification()
//        )
//    }

    private fun updateTimeUnits() {
        when (currentState.value) {
             StopwatchState.Main -> duration.toComponents { hours, minutes, seconds, _ ->
                this@StopwatchService.hours.value = hours.toInt()
                this@StopwatchService.minutes.value = minutes
                this@StopwatchService.seconds.value = seconds
            }
            StopwatchState.Sub -> subDuration.toComponents { hours, minutes, seconds, _ ->
                this@StopwatchService.subHours.value = hours.toInt()
                this@StopwatchService.subMinutes.value = minutes
                this@StopwatchService.subSeconds.value = seconds
            }
        }

    }

    inner class StopwatchBinder : Binder() {
        fun getService(): StopwatchService = this@StopwatchService
    }
}

//enum class StopwatchState {
//    Idle,
//    Started,
//    Stopped,
//    Canceled
//}

enum class StopwatchState {
    Main,
    Sub
}