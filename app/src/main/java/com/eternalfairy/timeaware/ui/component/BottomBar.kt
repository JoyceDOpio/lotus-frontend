package com.eternalfairy.timeaware.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.eternalfairy.timeaware.ui.screen.BOTTOM_BAR_COLOR
import com.eternalfairy.timeaware.ui.screen.TOP_BAR_COLOR

enum class Direction {
    Horizontal,
    Vertical
}
@Composable
fun BottomBar (
    modifier: Modifier = Modifier,
    direction: Direction = Direction.Vertical,
    content: @Composable () -> Unit
) {
    when (direction) {
        Direction.Horizontal ->
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(Color(BOTTOM_BAR_COLOR))
                ,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }

        Direction.Vertical ->
            Column(
                modifier = modifier
                    .fillMaxHeight()
                    .background(Color(BOTTOM_BAR_COLOR))
                ,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
    }
}