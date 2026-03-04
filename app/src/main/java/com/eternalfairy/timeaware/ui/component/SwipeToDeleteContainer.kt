package com.eternalfairy.timeaware.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.eternalfairy.timeaware.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeToDeleteContainer(
    modifier: Modifier = Modifier,
    item: T,
    onDelete: (T) -> Unit,
    animationDuration: Long = 500,
    content: @Composable (T) -> Unit
) {
    var isRemoved = remember {
        mutableStateOf(false)
    }

    var state = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                isRemoved.value = true
                true
            } else {
                false
            }
        }
    )

    LaunchedEffect(key1 = isRemoved.value) {
        if (isRemoved.value){
            delay(animationDuration)
            onDelete(item)
        }
    }

    AnimatedVisibility(
        visible = !isRemoved.value,
        exit = shrinkVertically(
            animationSpec = tween(
                durationMillis = animationDuration.toInt()
            ),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismissBox(
            state = state,
            backgroundContent = { DeleteBackground(state) },
            modifier = modifier,
            content = { content(item) },
            enableDismissFromStartToEnd = false
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteBackground(
    swipeDismissState: SwipeToDismissBoxState
) {
    var color = if (swipeDismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart){
        MaterialTheme.colorScheme.error
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.delete_24dp_5f6368_fill0_wght400_grad0_opsz24),
            contentDescription = "Remove task",
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize(),
            tint = Color.White
        )
    }
}