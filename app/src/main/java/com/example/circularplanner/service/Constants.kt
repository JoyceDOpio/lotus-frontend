package com.example.circularplanner.service

object Constants {
    // Service actions
    const val START = "START"
    const val STOP = "STOP"
    const val PAUSE = "PAUSE"
    const val RESUME = "RESUME"

    const val STOPWATCH_STATE = "STOPWATCH_STATE"

    const val NOTIFICATION_CHANNEL_ID = "Stopwatch_Notifications"
    const val NOTIFICATION_CHANNEL_NAME = "STOPWATCH_NOTIFICATION"
    const val MAIN_ACTIVITY_NOTIFICATION_ID = 10
    const val SUB_ACTIVITY_NOTIFICATION_ID = 11

    const val GROUP_KEY = "activity"

    const val CLICK_REQUEST_CODE = 100
    const val CANCEL_REQUEST_CODE = 101
    const val STOP_REQUEST_CODE = 102
    const val RESUME_REQUEST_CODE = 103
}