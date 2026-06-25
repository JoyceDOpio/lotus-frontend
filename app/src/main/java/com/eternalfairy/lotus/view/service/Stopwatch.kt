package com.eternalfairy.lotus.view.service

sealed class StopwatchState {
    data class Running(val startTime: Long, val elapsedTime: Long) : StopwatchState()
    data class Paused(val elapsedTime: Long) : StopwatchState()
}

class Stopwatch () {
    var currentState: StopwatchState = StopwatchState.Paused(0L)
        private set
    var hours = 0L
    var minutes = 0L
    var seconds = 0L

    fun calculateTime(timestamp: Long) {
        seconds = timestamp / 1000
        minutes = seconds / 60
        hours = minutes / 60
    }

    fun pause() {
        val oldState = currentState as StopwatchState.Running
        val elapsedTime = if (oldState.startTime < System.currentTimeMillis()) System.currentTimeMillis() - oldState.startTime + oldState.elapsedTime else 0L
        currentState = StopwatchState.Paused(elapsedTime)
    }

    fun resume() {
        val oldState = currentState as StopwatchState.Paused
        val elapsedTime = oldState.elapsedTime
        currentState = StopwatchState.Running(System.currentTimeMillis(), elapsedTime)
    }

    fun updateRunningState() {
        val oldState = currentState as StopwatchState.Running
        val elapsedTime = if (oldState.startTime < System.currentTimeMillis()) System.currentTimeMillis() - oldState.startTime + oldState.elapsedTime else 0L

        currentState = StopwatchState.Running(oldState.startTime, elapsedTime)
        calculateTime(elapsedTime)
    }

    fun start() {
        currentState = StopwatchState.Running(System.currentTimeMillis(), 0)
    }

    fun stop() {
        currentState = StopwatchState.Paused(0L)
    }

    fun format(): String {
        val secondsFormatted = (seconds % 60).pad(2)
        val minutesFormatted = (minutes % 60).pad(2)
        val hoursFormatted = (hours / 60).pad(2)

        return "$hoursFormatted:$minutesFormatted:$secondsFormatted"
    }

    private fun Long.pad(desiredLength: Int) = this.toString().padStart(desiredLength, '0')
}