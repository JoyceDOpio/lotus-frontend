package com.eternalfairy.lotus

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.eternalfairy.lotus.view.service.StopwatchService
import com.eternalfairy.lotus.view.navigation.Navigation
import com.eternalfairy.lotus.view.viewmodel.StopwatchViewModel

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun PlannerApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
//    screenState: SmallScreenState?,
    stopwatchService: StopwatchService,
    stopwatchViewModel: StopwatchViewModel
) {
    Navigation(
        navController = navController,
//        screenState = screenState,
        stopwatchViewModel = stopwatchViewModel,
        stopwatchService = stopwatchService
    )
}