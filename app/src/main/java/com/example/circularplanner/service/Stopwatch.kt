package com.example.circularplanner.service

import androidx.compose.runtime.mutableStateOf
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

enum class StopwatchState {
    Idle,
    Started,
    Stopped,
    Canceled
}

class Stopwatch {
    var duration: Duration = Duration.ZERO
    private lateinit var timer: Timer

    var seconds = mutableStateOf(0)
        private set
    var minutes = mutableStateOf(0)
        private set
    var hours = mutableStateOf(0)
        private set
    var currentState = mutableStateOf(StopwatchState.Idle)
        private set

    fun cancelStopwatch() {
        duration = Duration.ZERO
        currentState.value = StopwatchState.Idle
    }

    fun startStopwatch(onTick: (hr: String, min: String, sec: String) -> Unit) {
        currentState.value = StopwatchState.Started
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L) {
            duration = duration.plus(1.seconds)
            onTick(
                hours.value.pad(),
                minutes.value.pad(),
                seconds.value.pad()
            )
        }
    }

    fun stopStopwatch() {
        if (this::timer.isInitialized) timer.cancel()
        currentState.value = StopwatchState.Stopped
    }

    fun updateTimeUnits() {
        duration.toComponents { hours, minutes, seconds, _ ->
            this@Stopwatch.hours.value = hours.toInt()
            this@Stopwatch.minutes.value = minutes.toInt()
            this@Stopwatch.seconds.value = seconds.toInt()
        }
    }
}