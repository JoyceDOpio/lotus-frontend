package com.eternalfairy.timeaware.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.timeaware.R
import com.eternalfairy.timeaware.ui.data.GoalUiState
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.ERROR_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.SELECTION_COLOR
import java.util.UUID

@Composable
fun GoalEditScreen(
    modifier: Modifier = Modifier,
    goalUiState: GoalUiState,
    lastPriority: Int,
    onBack: () -> Unit,
    saveGoal: () -> Unit,
    setGoalPriority: (Int) -> Unit,
    setGoalTitle: (String) -> Unit
) {
    // Texts
    val createLabelText = "CREATE A GOAL"// TODO: Read string from resource
    val editLabelText = "EDIT GOAL"// TODO: Read string from resource
    val titlePlaceholderText = "Title"// TODO: Read string from resource

    fun getLabel(goalId: UUID?): String {
        if (goalId == null) {
            return createLabelText
        }

        return editLabelText
    }

    val label: String = getLabel(goalUiState.id)
    val goalDetails = goalUiState

    // Validation:
    // - title cannot be empty
    var isTitle by remember { mutableStateOf(true) }

    // Warning text
    val emptyTitleWarning = "The title cannot be empty"// TODO: Read string from resource

    if (goalUiState.priority == null) {
        setGoalPriority(lastPriority + 1)
    }

    Scaffold (
        bottomBar = {
            BottomAppBar (
                containerColor = COMPONENT_BACKGROUND_COLOR,
                actions = {
                    // Close button
                    IconButton(onClick = {
                        // Navigate to previous stack entry
                        onBack()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.cancel_svgrepo_com),
                            contentDescription = "Cancel",
                            modifier = Modifier.fillMaxSize(0.8F),
                            tint = HEADER_TEXT_COLOR
                        )
                    }

                    // These actions should be at the end of the BottomAppBar. They use the default medium
                    // content alpha provided by BottomAppBar
                    // The Spacer pushes the other icons to the end of the app bar
                    Spacer(Modifier.weight(1f, true))

                    // Save button
                    IconButton(onClick = {
                        if (goalDetails.title == "") isTitle = false

                        if (isTitle) {
                            saveGoal()
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.save_alt_svgrepo_com),
                            contentDescription = "Save",
                            modifier = Modifier.fillMaxSize(0.6F),
                            tint = HEADER_TEXT_COLOR
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .background(COMPONENT_BACKGROUND_COLOR)
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
                color = HEADER_TEXT_COLOR
            )

            // Title field
            OutlinedTextField(
                value = goalDetails.title,
                onValueChange = { value ->
                    setGoalTitle(value)
                    if (value != "") isTitle = true
                },
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                textStyle = TextStyle(fontSize = 18.sp),
                label = { Text(titlePlaceholderText) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = false,
                shape = RoundedCornerShape(15.dp),
                // TODO: BOILERPLATE - create custom OutlinedTextField
                colors = OutlinedTextFieldDefaults.colors().copy(
                    cursorColor = HEADER_TEXT_COLOR,
                    focusedIndicatorColor = HEADER_TEXT_COLOR,
                    focusedLabelColor = HEADER_TEXT_COLOR,
                    textSelectionColors = TextSelectionColors(
                        handleColor = HEADER_TEXT_COLOR,
                        backgroundColor = SELECTION_COLOR
                    )
                )
            )

            // Warning
            AnimatedVisibility(
                visible = !isTitle
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 10.dp)
                        ,
                        text = emptyTitleWarning,
                        fontSize = 13.sp,
                        color = ERROR_TEXT_COLOR,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}