package com.example.circularplanner.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.circularplanner.data.IActivitiesRepository
import com.example.circularplanner.data.Time
import com.example.circularplanner.service.Constants.ACTION_SERVICE_START
import com.example.circularplanner.service.Constants.ACTION_SERVICE_STOP
import com.example.circularplanner.service.Constants.NOTIFICATION_CHANNEL_ID
import com.example.circularplanner.service.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.circularplanner.service.Constants.NOTIFICATION_ID
import com.example.circularplanner.service.Constants.STOPWATCH_STATE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
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

    private var duration: Duration = Duration.ZERO
    private lateinit var timer: Timer

    var seconds = mutableStateOf(0)
        private set
    var minutes = mutableStateOf(0)
        private set
    var hours = mutableStateOf(0)
        private set
    var currentState = mutableStateOf(StopwatchState.Idle)
        private set

//    var activityId: UUID? = null
//    var activityTitle: String? = null

//    private suspend fun cancelStopwatch() {
    private fun cancelStopwatch() {
        duration = Duration.ZERO
        currentState.value = StopwatchState.Idle
        updateTimeUnits()
//        // TODO: Update the recorded activity with end time
////        scope.launch {
////            val endTime = Time(
////                LocalDateTime.now().hour,
////                LocalDateTime.now().minute
////            )
////            val recodedActivity = activityRepository.getRecordedActivity().first()
////
////            if (recodedActivity != null) {
////                recodedActivity.endTime = endTime
////                activityRepository.updateActivity(recodedActivity)
////            }
////        }
//        val endTime = Time(
//            LocalDateTime.now().hour,
//            LocalDateTime.now().minute
//        )
//
//        val recordedActivity = activityRepository.getRecordedActivity().first()
//        Log.i("StopwatchService", recordedActivity.toString())
//
//        if (recordedActivity != null) {
//            recordedActivity.endTime = endTime
//            activityRepository.updateActivity(recordedActivity)
//        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread(Runnable {
            when (intent?.getStringExtra(STOPWATCH_STATE)) {
                StopwatchState.Started.name -> {
//                    setStopButton()
                    startForegroundService()
                    startStopwatch { hours, minutes, seconds ->
                        updateNotification(hours = hours, minutes = minutes, seconds = seconds)
                    }
//                    activityId = UUID.fromString(intent.getStringExtra("activity_id"))
//                    activityTitle = intent.getStringExtra("activity_title")
                }
//            StopwatchState.Stopped.name -> {
//                stopStopwatch()
//                setResumeButton()
//            }
                StopwatchState.Stopped.name -> {
                    stopStopwatch()
                    scope.launch {
                        cancelStopwatch()
                    }
                    stopForegroundService()
                }
//            StopwatchState.Canceled.name -> {
//                stopStopwatch()
//                cancelStopwatch()
//                stopForegroundService()
//            }
            }

            intent?.action.let {
                when (it) {
                    ACTION_SERVICE_START -> {
//                        setStopButton()
                        startForegroundService()
                        startStopwatch { hours, minutes, seconds ->
                            updateNotification(hours = hours, minutes = minutes, seconds = seconds)
                        }
                    }
//                ACTION_SERVICE_STOP -> {
//                    stopStopwatch()
//                    setResumeButton()
//                }
                    ACTION_SERVICE_STOP -> {
                        stopStopwatch()
                        cancelStopwatch()
//                        scope.launch {
//                            cancelStopwatch()
//                        }
                        stopForegroundService()
                    }
//                ACTION_SERVICE_CANCEL -> {
//                    stopStopwatch()
//                    cancelStopwatch()
//                    stopForegroundService()
//                }
                }
            }
        }).start()


        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("RestrictedApi")
    private fun setStopButton() {
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0, NotificationCompat.Action(
                0, "Stop", ServiceHelper.stopPendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun startStopwatch(onTick: (hr: String, min: String, sec: String) -> Unit) {
        currentState.value = StopwatchState.Started
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
        if (this::timer.isInitialized) timer.cancel()
        currentState.value = StopwatchState.Stopped
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

    private fun updateTimeUnits() {
        duration.toComponents { hours, minutes, seconds, _ ->
            this@StopwatchService.hours.value = hours.toInt()
            this@StopwatchService.minutes.value = minutes.toInt()
            this@StopwatchService.seconds.value = seconds.toInt()
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

    inner class StopwatchBinder : Binder() {
        fun getService(): StopwatchService = this@StopwatchService
    }
}

enum class StopwatchState {
    Idle,
    Started,
    Stopped,
    Canceled
}