package com.eternalfairy.lotus.view.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object StopwatchRepo {
    private val _stopwatchMainState = MutableStateFlow("00:00:00")
    private val _stopwatchSubState = MutableStateFlow("00:00:00")
    val stopwatchMainState = _stopwatchMainState.asStateFlow()
    val stopwatchSubState = _stopwatchSubState.asStateFlow()

    fun updateStopwatchMain(value: String?) {
        value?.let {
            _stopwatchMainState.update { value }
        }
    }

    fun updateStopwatchSub(value: String?) {
        value?.let {
            _stopwatchSubState.update { value }
        }
    }
}