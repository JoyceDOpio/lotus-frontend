package com.example.circularplanner.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.circularplanner.R
import com.example.circularplanner.data.Goal
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    goals: List<Goal>,
    modifier: Modifier = Modifier,
    onNext: () -> Unit
) {
    val firstGoal: Goal? = try {
        goals[0]
    } catch (e: Exception) { null }
    val secondGoal: Goal? = try {
        goals[1]
    } catch (e: Exception) { null }
    val thirdGoal: Goal? = try {
        goals[2]
    } catch (e: Exception) { null }
    val goalDelay = 2000L
    val buttonDelay = 1800L
    val goalDuration = 4000
    val buttonDuration = 1500

    var showFirstGoal by remember { mutableStateOf(false) }
    var showSecondGoal by remember { mutableStateOf(false) }
    var showThirdGoal by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    val firstGoalAlpha by animateFloatAsState(
        targetValue = if (showFirstGoal) 1f else 0f,
        animationSpec = tween(durationMillis = goalDuration),
        label = ""
    )
    val secondGoalAlpha by animateFloatAsState(
        targetValue = if (showSecondGoal) 1f else 0f,
        animationSpec = tween(durationMillis = goalDuration),
        label = ""
    )
    val thirdGoalAlpha by animateFloatAsState(
        targetValue = if (showThirdGoal) 1f else 0f,
        animationSpec = tween(durationMillis = goalDuration),
        label = ""
    )
    val buttonAlpha by animateFloatAsState(
        targetValue = if (showButton) 1f else 0f,
        animationSpec = tween(durationMillis = buttonDuration),
        label = ""
    )

    // At first the goals' list is empty, and afterwards it gets updated. That's why the LaunchedEffect is dependent on the value of the goals' list
    LaunchedEffect(goals) {
        if (firstGoal != null) {
            showFirstGoal = true
        }

        if (secondGoal != null) {
            delay(goalDelay)
            showSecondGoal = true
        }

        if (thirdGoal != null) {
            delay(goalDelay)
            showThirdGoal = true
        }

        delay(buttonDelay)// If I put this delay in an if() statement, the button is shown before the delay is carried out
        showButton = true
    }

    Scaffold () { innerPadding ->
        Column (
            modifier = modifier
                .padding(innerPadding)
                .padding(horizontal = 55.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (firstGoal != null) {
                Text(
                    text = firstGoal.title,
                    modifier = Modifier
                        .alpha(firstGoalAlpha)
                        .padding(vertical = 10.dp),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }

            if (secondGoal != null) {
                Text(
                    text = secondGoal.title,
                    modifier = Modifier
                        .alpha(secondGoalAlpha)
                        .padding(vertical = 10.dp),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }

            if (thirdGoal != null) {
                Text(
                    text = thirdGoal.title,
                    modifier = Modifier
                        .alpha(thirdGoalAlpha)
                        .padding(vertical = 10.dp),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .alpha(buttonAlpha)
                    .padding(vertical = 30.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colorScheme.primary)
                    .width(70.dp)
                    .aspectRatio(1f)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.arrow_right_svgrepo_com),
                    contentDescription = "Next",
                    modifier = Modifier.fillMaxSize(0.6f),
                    tint = Color(0xffffffff)
                )
            }
        }
    }
}