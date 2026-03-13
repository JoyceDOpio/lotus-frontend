package com.eternalfairy.timeaware.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR

enum class Direction {
    Horizontal,
    Vertical
}
@Composable
fun BottomBar (
    componentHeight: Dp = 330.dp,
    componentWidth: Dp = 400.dp,
    paddingStart: Dp = 10.dp,
    paddingTop: Dp = 5.dp,
    paddingEnd: Dp = 10.dp,
    paddingBottom: Dp = 20.dp,
    paddingHorizontal: Dp = 10.dp,
    paddingVertical: Dp = 5.dp,
    modifier: Modifier = Modifier,
    direction: Direction = Direction.Horizontal,
    content: @Composable () -> Unit
) {
    when (direction) {
        Direction.Horizontal ->
            Row(
                modifier = modifier
                    .padding(
                        start = paddingStart,
                        top = paddingTop,
                        end = paddingEnd,
                        bottom = paddingBottom
                    )
                    .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
                    .width(componentWidth)
                    .background(COMPONENT_BACKGROUND_COLOR)
                ,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }

        Direction.Vertical ->
            Column(
                modifier = modifier
                    .padding(
                        vertical = paddingVertical,
                        horizontal = paddingHorizontal
                    )
                    .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
                    .height(componentHeight)
                    .background(COMPONENT_BACKGROUND_COLOR)
                ,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
    }
}