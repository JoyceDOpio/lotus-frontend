package com.eternalfairy.lotus.view.service

import android.util.Log

sealed class StopwatchState {
    data class Running(val startTime: Long, val prevElapsedTime: Long) : StopwatchState()
    data class Paused(val elapsedTime: Long) : StopwatchState()
}

class Stopwatch () {
    companion object {
        const val SECOND_IN_MILLISECONDS = 1000
        const val MINUTE_IN_MILLISECONDS = SECOND_IN_MILLISECONDS * 60
        const val HOUR_IN_MILLISECONDS = MINUTE_IN_MILLISECONDS * 60
    }
    var currentState: StopwatchState = StopwatchState.Paused(0L)
        private set
    var hours = 0L
    var minutes = 0L
    var seconds = 0L

    fun calculateTime(timestamp: Long) {
        hours = timestamp / HOUR_IN_MILLISECONDS
        minutes = (timestamp % HOUR_IN_MILLISECONDS) / MINUTE_IN_MILLISECONDS
        seconds = (timestamp % MINUTE_IN_MILLISECONDS) / SECOND_IN_MILLISECONDS
    }

    fun pause() {
        val oldState = currentState as StopwatchState.Running
        val elapsedTime = if (oldState.startTime < System.currentTimeMillis()) System.currentTimeMillis() - oldState.startTime + oldState.prevElapsedTime else 0L
        currentState = StopwatchState.Paused(elapsedTime)
    }

    fun resume() {
        val oldState = currentState as StopwatchState.Paused
        currentState = StopwatchState.Running(System.currentTimeMillis(), oldState.elapsedTime)

        calculateTime(oldState.elapsedTime)
    }

    fun updateRunningState() {
        val oldState = currentState as StopwatchState.Running
        val elapsedTime = if (oldState.startTime < System.currentTimeMillis()) System.currentTimeMillis() - oldState.startTime + oldState.prevElapsedTime else 0L

        calculateTime(elapsedTime)
    }

    fun start() {
        currentState = StopwatchState.Running(System.currentTimeMillis(), 0L)
    }

    fun stop() {
        currentState = StopwatchState.Paused(0L)
        hours = 0L
        minutes = 0L
        seconds = 0L
    }

    fun format(): String {
        val secondsFormatted = (seconds % 60).pad(2)
        val minutesFormatted = (minutes % 60).pad(2)
        val hoursFormatted = (hours / 60).pad(2)

        val oldState = currentState
        if (oldState is StopwatchState.Running) {
            val elapsedTime = if (oldState.startTime < System.currentTimeMillis()) System.currentTimeMillis() - oldState.startTime + oldState.prevElapsedTime else 0L
            Log.i("Stopwatch", "elapsedTime $elapsedTime")
            Log.i("Stopwatch", "hours $hours minutes $minutes seconds $seconds")
        }

        return "$hoursFormatted:$minutesFormatted:$secondsFormatted"
    }

    private fun Long.pad(desiredLength: Int) = this.toString().padStart(desiredLength, '0')
}