package com.eternalfairy.timeaware.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.eternalfairy.timeaware.ui.screen.TOP_BAR_COLOR
import com.eternalfairy.timeaware.ui.screen.TOP_BAR_TEXT_COLOR

@Composable
fun TopBar (
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(TOP_BAR_COLOR))
        ,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text (
            text = title,
            color = Color(TOP_BAR_TEXT_COLOR)
        )
    }
}