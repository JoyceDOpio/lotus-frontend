package com.example.circularplanner.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.circularplanner.MainActivity
import com.example.circularplanner.service.Constants.CANCEL_REQUEST_CODE
import com.example.circularplanner.service.Constants.CLICK_REQUEST_CODE
import com.example.circularplanner.service.Constants.RESUME_REQUEST_CODE
import com.example.circularplanner.service.Constants.STOPWATCH_STATE
import com.example.circularplanner.service.Constants.STOP_REQUEST_CODE
import java.util.UUID

// Specifies the behaviour for the notification
object ServiceHelper {
    private val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    fun clickPendingIntent(context: Context): PendingIntent {
        // When the notification is clicked, the MainActivity should be opened
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(STOPWATCH_STATE, StopwatchState.Started.name)
        }

        return PendingIntent.getActivity(
            context,
            CLICK_REQUEST_CODE,
            clickIntent,
            flag
        )
    }

//    fun resumePendingIntent(context: Context): PendingIntent {
//        val resumeIntent = Intent(context, StopwatchService::class.java).apply {
//            putExtra(STOPWATCH_STATE, StopwatchState.Started.name)
//        }
//
//        return PendingIntent.getService(
//            context,
//            RESUME_REQUEST_CODE,
//            resumeIntent,
//            flag
//        )
//    }
//
//    fun cancelPendingIntent(context: Context): PendingIntent {
//        val cancelIntent = Intent(context, StopwatchService::class.java).apply {
//            putExtra(STOPWATCH_STATE, StopwatchState.Canceled.name)
//        }
//
//        return PendingIntent.getService(
//            context,
//            CANCEL_REQUEST_CODE,
//            cancelIntent,
//            flag
//        )
//    }
//
//    fun stopPendingIntent(context: Context): PendingIntent {
//        val stopIntent = Intent(context, StopwatchService::class.java).apply {
//            putExtra(STOPWATCH_STATE, StopwatchState.Stopped.name)
//        }
//
//        return PendingIntent.getService(
//            context,
//            STOP_REQUEST_CODE,
//            stopIntent,
//            flag
//        )
//    }

    fun triggerForegroundService(context: Context, action: String) {
        Intent(context, StopwatchService::class.java).apply {
            this.action = action
            context.startService(this)
        }
    }
}