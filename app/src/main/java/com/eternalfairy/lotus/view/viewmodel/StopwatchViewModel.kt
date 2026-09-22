package com.eternalfairy.lotus.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.eternalfairy.lotus.view.service.StopwatchRepo

class StopwatchViewModel() : ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return StopwatchViewModel() as T
            }
        }
    }

    val stopwatchStateMain = StopwatchRepo.stopwatchMainState
    val stopwatchStateSub = StopwatchRepo.stopwatchSubState
}