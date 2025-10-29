package com.example.circularplanner.ui.screen

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.circularplanner.data.Time
import com.example.circularplanner.data.VoiceNote
import com.example.circularplanner.service.StopwatchService
import com.example.circularplanner.ui.component.ActivityGraph
import com.example.circularplanner.ui.component.ActivityRecorder
import com.example.circularplanner.ui.component.ComparisonDial
import com.example.circularplanner.ui.component.PopupDialog
import com.example.circularplanner.ui.navigation.RecordedActivity
import com.example.circularplanner.ui.viewmodel.ActivityUiState
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.DayUiState
import com.example.circularplanner.utils.NoteModePopup
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun ActivityScreen(
    innerPadding: PaddingValues,
    dayState: DayState,
    onNavigateToTaskActivityComparison: () -> Unit,
    selectActivity: (UUID?) -> Unit,
    selectTask: (UUID?) -> Unit,
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