package com.eternalfairy.timeaware.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR

@Composable
fun PopupDialog (
    onDismissRequest: () -> Unit,
    content: @Composable() () -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .size(
                    420.dp,
                    450.dp
                )
                .padding(10.dp)
            ,
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(
                containerColor = COMPONENT_BACKGROUND_COLOR,
            )
        ) {
            content()
        }
    }
}