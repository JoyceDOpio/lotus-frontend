package com.eternalfairy.lotus

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PlannerApplication : Application() {
    // AppContainer instance used by the rest of classes to obtain dependencies. The variable is initialized during the call to onCreate(), so the variable needs to be marked with the lateinit modifier
//    lateinit var container: AppContainer
//    val dotenv = dotenv()

//    // Without init{} the container value is not initialized before it is accessed
//    init {
//        onCreate()
//
//
//    }
}

// Extension function to queries for [Application] object and returns an instance of [PlannerApplication]
fun CreationExtras.plannerApplication(): PlannerApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as PlannerApplication)