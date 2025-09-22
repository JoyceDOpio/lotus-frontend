package com.example.circularplanner.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.R
import com.example.circularplanner.ui.viewmodel.DayState
import com.example.circularplanner.ui.viewmodel.DayUiState

@Composable
fun DayNoteEditScreen(// TODO: Merge with DayNoteEditScreen
    modifier: Modifier = Modifier,
    dayUiState: DayUiState,
    onBack: () -> Unit,
    saveDay: () -> Unit,
    setDayNote: (String) -> Unit
) {
    val label = "DAY NOTES"// TODO: Read text from string resource
    val dayDetails = dayUiState

    Scaffold (
        bottomBar = {
            BottomAppBar (
                containerColor = Color(BOTTOM_BAR_COLOR),
                actions = {
//                    // Leading icons should typically have a high content alpha
//                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
//                        IconButton(onClick = { /* doSomething() */ }) {
//                            Icon(Icons.Filled.Menu, contentDescription = "Localized description")
//                        }
//                    }

                    // Close button
                    IconButton(onClick = {
                        // Navigate to previous stack entry
                        onBack()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                            contentDescription = "Cancel",
                            modifier = Modifier.fillMaxSize(0.8F),
                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        )
                    }

                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))

                    // Save button
                    IconButton(onClick = {
                        saveDay()
                        onBack()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.save_alt_svgrepo_com),
                            contentDescription = "Save",
                            modifier = Modifier.fillMaxSize(0.6F),
                            tint = Color(BOTTOM_BAR_TEXT_COLOR)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(horizontal = 15.dp)
                .padding(innerPadding)
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Label
            Text(
                modifier = Modifier
                    .padding(bottom = 10.dp),
                text = label,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )

            // Day note
            OutlinedTextField(
                value = dayDetails.note,
                onValueChange = setDayNote,
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .fillMaxWidth(),
                textStyle = TextStyle(fontSize = 20.sp),
                label = { Text("Notes") },// TODO: Read text from string resource
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = false,
                shape = RoundedCornerShape(15.dp)
            )
        }
    }
}