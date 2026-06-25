package com.eternalfairy.lotus.view.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GoalCard (
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    title: String,
    displayType: CardDisplayType = CardDisplayType.Full,
) {
    Column (
        modifier = modifier
            .padding(horizontal = if (displayType == CardDisplayType.Popup) 20.dp else 30.dp)
            .padding(top = if (displayType == CardDisplayType.Popup) 15.dp else 0.dp)
            .fillMaxWidth()
            .fillMaxHeight()
        ,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        // If the display width is that of a popup window, narrow down the layout
        if (displayType == CardDisplayType.Popup) {
            Column () {
                // Title
                Row (
                    modifier = modifier
                        .padding(bottom = 5.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        modifier = modifier
                            .weight(2f)
                        ,
                        fontSize = 28.sp,
                        lineHeight = 32.sp
                    )
                }

            }
        }
        // Else, display the content normally
        else {
            // Title
            Row (
                modifier = modifier
                    .padding(bottom = 5.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    modifier = modifier.weight(2f),
                    fontSize = 28.sp,
                    lineHeight = 32.sp
                )
            }

        }
    }
}