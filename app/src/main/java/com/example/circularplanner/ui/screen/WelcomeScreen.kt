package com.example.circularplanner.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.circularplanner.R
import com.example.circularplanner.data.Goal

@Composable
fun WelcomeScreen(
    goals: List<Goal>,
    modifier: Modifier = Modifier,
    onNext: () -> Unit
) {
    Scaffold () { innerPadding ->
        Column (
            modifier = modifier
                .padding(innerPadding)
                .padding(horizontal = 30.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val firstGoal = try { goals.get(0) } catch (e: Exception) { null }
            val secondGoal = try { goals.get(1) } catch (e: Exception) { null }
            val thirdGoal = try { goals.get(2) } catch (e: Exception) { null }
            var delay = 10000
            var duration = 5000

            if (firstGoal != null) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = duration, delayMillis = delay)),
                    exit = fadeOut(animationSpec = tween(2000))
                ) {
                    Text(
                        text = firstGoal.title,
                        modifier = Modifier
                            .padding(vertical = 10.dp),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }

                delay += 3000
            }

            if (secondGoal != null) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(
                        durationMillis = duration,
                        delayMillis = delay
                    )),
                    exit = fadeOut(animationSpec = tween(2000))
                ) {
                    Text(
                        text = secondGoal.title,
                        modifier = Modifier
                            .padding(vertical = 10.dp),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }

                delay += 3000
            }

            if (thirdGoal != null) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(
                        durationMillis = duration,
                        delayMillis = delay
                    )),
                    exit = fadeOut(animationSpec = tween(2000))
                ) {
                    Text(
                        text = thirdGoal.title,
                        modifier = Modifier
                            .padding(vertical = 10.dp),
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }

                delay += 2000
            }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(
                    durationMillis = duration,
                    delayMillis = delay
                )),
            ) {
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
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
}