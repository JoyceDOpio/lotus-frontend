package com.example.circularplanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.circularplanner.ui.navigation.Navigation

@Composable
fun PlannerApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    Navigation(
        navController = navController,
    )
}