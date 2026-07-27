package com.eternalfairy.lotus

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PlannerApplication : Application() {
    // AppContainer instance used by the rest of classes to obtain dependencies. The variable is initialized during the call to onCreate(), so the variable needs to be marked with the lateinit modifier
//    lateinit var container: AppContainer
//    val dotenv = dotenv()

//    companion object {
//        const val POSTHOG_PROJECT_TOKEN = "phc_ACaUmuk3Vr3q3m5Qn7Hp3UPUfirbR5yHm8sPtegXtXE7"
//        const val POSTHOG_HOST = "https://us.i.posthog.com"
//    }

//    // Without init{} the container value is not initialized before it is accessed
//    init {
//        onCreate()
//
//
//    }

//    override fun onCreate() {
//        super.onCreate()
////        container = AppDataContainer(this)
//        // Create a PostHog Config with the given project token and host
//        val config = PostHogAndroidConfig(
//            apiKey = POSTHOG_PROJECT_TOKEN,
//            host = POSTHOG_HOST
//        )
//
//        // Setup PostHog with the given Context and Config
//        PostHogAndroid.setup(this, config)
//    }
}

// Extension function to queries for [Application] object and returns an instance of [PlannerApplication]
fun CreationExtras.plannerApplication(): PlannerApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as PlannerApplication)