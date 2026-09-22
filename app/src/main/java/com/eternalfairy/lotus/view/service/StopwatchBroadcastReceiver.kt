package com.eternalfairy.lotus.view.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopwatchBroadcastReceiver() : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        if(p1?.action == StopwatchService.STOPWATCH_ACTION) {
            val stopwatchId = p1.getIntExtra(StopwatchService.STOPWATCH_ID, 1)
            val stopwatchState = p1.getStringExtra(StopwatchService.TIME_ELAPSED)

            when (stopwatchId) {
                1 -> {
                    StopwatchRepo.updateStopwatchMain(stopwatchState)
                }
                2 -> {
                    StopwatchRepo.updateStopwatchSub(stopwatchState)
                }
            }
        }
    }
}