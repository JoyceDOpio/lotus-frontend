package com.eternalfairy.lotus

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.eternalfairy.lotus.view.service.StopwatchBroadcastReceiver
import com.eternalfairy.lotus.view.service.StopwatchService
import com.eternalfairy.lotus.view.theme.LotusTheme
import com.eternalfairy.lotus.view.viewmodel.StopwatchViewModel
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var isBound by mutableStateOf(false)
    private var isRegistered by mutableStateOf(false)
    private lateinit var stopwatchReceiver: BroadcastReceiver
    private lateinit var stopwatchService: StopwatchService
    private lateinit var stopwatchViewModel: StopwatchViewModel



    private val connection = object : ServiceConnection {
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            val binder = service as StopwatchService.StopwatchBinder
            stopwatchService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }

    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        var screenState: SmallScreenState? = null
////        val extras: Bundle? = intent.extras
////        extras?.let {
////            screenState = (SmallScreenState) it.getSerializable(SCREEN_STATE)
////        }
//        screenState = intent.getSerializableExtra(SCREEN_STATE) as SmallScreenState?
//
//        Log.i("MainActivity", "screenState $screenState")

        // Initialize the Google Mobile Ads SDK on a background thread.
        MobileAds.initialize(this@MainActivity) {}

        // Receiving time values from service
        val stopwatchFilter = IntentFilter()
        stopwatchFilter.addAction(StopwatchService.STOPWATCH_ACTION)
        stopwatchReceiver = StopwatchBroadcastReceiver()
        registerReceiver(stopwatchReceiver, stopwatchFilter, RECEIVER_NOT_EXPORTED)
        isRegistered = true

        stopwatchViewModel = StopwatchViewModel()

        setContent {
            LotusTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (isBound) {
                        PlannerApp(
                            modifier = Modifier.padding(innerPadding),
//                            screenState = screenState,
                            stopwatchService = stopwatchService,
                            stopwatchViewModel = stopwatchViewModel
                        )
                    }
                }
            }
        }

        requestPermissions()
    }

    override fun onStart() {
        super.onStart()
        // Bind the MainActivity with the service
        Intent(this, StopwatchService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(connection)
        }
        if (isRegistered) {
            unregisterReceiver(stopwatchReceiver)
        }
        isBound = false
    }

    private fun requestPermissions() {
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            result.entries.forEach {
                Log.d("MainActivity", "${it.key} = ${it.value}")
            }
        }
    }
}

//@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
//@Preview(showBackground = true)
//@Composable
//fun DayPreview() {
//    CircularPlannerTheme {
//        PlannerApp()
//    }
//}
