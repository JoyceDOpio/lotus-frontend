package com.eternalfairy.lotus.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.lotus.R
import com.eternalfairy.lotus.view.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.lotus.view.theme.HEADER_TEXT_COLOR

@Composable
fun TopBar (
    componentHeight: Dp = 90.dp,
    componentWidth: Dp = 400.dp,
    paddingStart: Dp = 10.dp,
    paddingTop: Dp = 40.dp,
    paddingEnd: Dp = 10.dp,
    paddingBottom: Dp = 5.dp,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(componentHeight)
            .width(componentWidth)
            .padding(
                start = paddingStart,
                top = paddingTop,
                end = paddingEnd,
                bottom = paddingBottom
            )
            .clip(shape = RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp))
            .background(COMPONENT_BACKGROUND_COLOR)
        ,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.weight(1f, true))

        Text (
            text = title,
            color = HEADER_TEXT_COLOR,
            fontSize = 24.sp,
            modifier = Modifier.weight(2f, true)
        )



        Row (
            modifier = Modifier.weight(1f, true),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = {

                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.user_svgrepo_com),
                    contentDescription = "Activity recorder",
                    modifier = Modifier.fillMaxSize(0.75f),
                    tint = HEADER_TEXT_COLOR
                )
            }
        }
    }
}