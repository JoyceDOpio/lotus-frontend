package com.example.circularplanner.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PopupDialog (
    onDismissRequest: () -> Unit,
    content: @Composable() () -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .size(
                    380.dp,
                    400.dp
                )
                .padding(10.dp)
            ,
            shape = RoundedCornerShape(15.dp),
        ) {
            content()
        }
    }
}