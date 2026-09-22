package com.eternalfairy.lotus.view.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.eternalfairy.lotus.view.screen.login.LoginContainer
import com.eternalfairy.lotus.view.screen.planner.PlannerContainer
import com.eternalfairy.lotus.view.service.StopwatchService
import com.eternalfairy.lotus.view.viewmodel.AuthViewModel
import com.eternalfairy.lotus.view.viewmodel.PlannerViewModel
import com.eternalfairy.lotus.view.viewmodel.StopwatchViewModel
import com.eternalfairy.lotus.view.viewmodel.utils.AudioRecorder

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun Navigation(
    navController: NavHostController,
//    screenState: SmallScreenState?,
    stopwatchService: StopwatchService,
    stopwatchViewModel: StopwatchViewModel
) {
    val plannerViewModel: PlannerViewModel = hiltViewModel()

    val context = LocalContext.current
    val audioRecorder = AudioRecorder(context)

    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
//        startDestination = WelcomeRoute// TODO: Display welcoming (goals) once every day
        startDestination = LoginRoute
    ) {
        composable<LoginRoute> { backStackEntry ->
            LoginContainer(
                viewModel = authViewModel,
                onNavigateToNext = { navController.navigate(route = PlannerRoute) },
            )
        }

        composable<PlannerRoute> { _ ->
            PlannerContainer(
                audioRecorder = audioRecorder,
                context = context,
//                screenState = screenState,
                stopwatchService = stopwatchService,
                plannerViewModel = plannerViewModel,
                stopwatchViewModel = stopwatchViewModel,
                onLogout = {
//                    authViewModel.authManager.logOut()
                },
                onNavigateToLogin = { navController.navigate(route = LoginRoute) },
            )
        }

//        composable<WelcomeRoute>{ backStackEntry ->
//            WelcomeScreen(
//                goals = goals,
//                onNext = { navController.navigate(route = PlannerRoute) }
//            )
//        }
    }
}