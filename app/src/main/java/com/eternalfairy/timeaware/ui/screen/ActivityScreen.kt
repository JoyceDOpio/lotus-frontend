package com.eternalfairy.timeaware.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eternalfairy.timeaware.ui.component.ActivityGraph
import com.eternalfairy.timeaware.ui.component.ComparisonDial
import com.eternalfairy.timeaware.ui.viewmodel.DayState
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun ActivityScreen(
    innerPadding: PaddingValues = PaddingValues(0.dp),
    dayState: DayState,
    onNavigateToTaskActivityComparison: () -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row (
            modifier = Modifier
                .weight(1.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActivityGraph(
                dayState = dayState,
                drawClockHand = true,
                onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
                selectActivity = selectActivity,
                selectTask = selectTask
            )
        }

        Row (
            modifier = Modifier
                .weight(3.5f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ComparisonDial(
                dayState = dayState,
                drawClockHand = true,
                onNavigateToTaskActivityComparison = onNavigateToTaskActivityComparison,
                selectActivity = selectActivity,
                selectTask = selectTask
            )
        }
    }
}