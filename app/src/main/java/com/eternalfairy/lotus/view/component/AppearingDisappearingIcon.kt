package com.eternalfairy.lotus.view.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource

@Composable
fun AppearingDisappearingIcon(
    modifier: Modifier = Modifier,
    imageVectorResource: Int,
    contentDescription: String,
    tint: Color
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box (
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxSize()
            .graphicsLayer { alpha = animatedAlpha },
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = ImageVector.vectorResource(id = imageVectorResource),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(0.7F),
            tint = tint
        )
    }
}