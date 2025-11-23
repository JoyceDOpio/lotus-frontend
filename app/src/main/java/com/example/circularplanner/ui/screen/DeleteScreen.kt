package com.example.circularplanner.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class DeleteType {
    Activity,
    Goal,
    Task,
    VoiceNote
}

class Deletable()

@Composable
fun DeleteScreen (
    onBack: () -> Unit,
    onDelete: () -> Unit,
//    onDelete: (Deletable) -> Unit,
    deleteType: DeleteType = DeleteType.Task
) {
//    val icon = R.drawable.delete_24dp_5f6368_fill0_wght400_grad0_opsz24
    val deleteQuestion = "Would you like to delete this ${when (deleteType) {
        DeleteType.Activity -> "activity"
        DeleteType.Goal -> "goal"
        DeleteType.Task -> "task"
        DeleteType.VoiceNote -> "voice note"
    }
    }?"//TODO: Read string from resource
    val cancelButtonText = "Cancel"
    val okButtonText = "Delete"

    Scaffold () { innerPadding ->
        Column (
            modifier = Modifier
                .padding(30.dp)
                .padding(innerPadding)
                .fillMaxSize()
            ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text (
                text = deleteQuestion
            )

            Spacer(Modifier.height(10.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                ,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = cancelButtonText,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = okButtonText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}