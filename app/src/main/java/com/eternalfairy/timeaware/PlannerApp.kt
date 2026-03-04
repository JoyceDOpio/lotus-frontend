package com.eternalfairy.timeaware

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.eternalfairy.timeaware.service.StopwatchService
import com.eternalfairy.timeaware.ui.navigation.Navigation
import com.eternalfairy.timeaware.utils.WindowSize

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun PlannerApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    stopwatchService: StopwatchService,
    windowSize: WindowSize
) {
    Navigation(
        navController = navController,
        stopwatchService = stopwatchService,
        windowSize = windowSize
    )
}