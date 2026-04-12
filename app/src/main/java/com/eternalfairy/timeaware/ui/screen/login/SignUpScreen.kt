package com.eternalfairy.timeaware.ui.screen.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.SELECTION_COLOR

@Composable
fun SignUpScreen (
    onSignUpWithEmail: (String, String, String, String) -> Unit,
    onSwitchScreen: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }// TODO: Add input validation
    var password by remember { mutableStateOf("") }// TODO: Add input validation
    var showPassword by remember { mutableStateOf(false) }

    // Texts
    val signUpHeaderText = "Welcome to Time Aware"
    val firstNameText = "First Name"// TODO: Read from resource
    val lastNameText = "Last Name"// TODO: Read from resource
    val emailText = "E-mail"// TODO: Read from resource
    val passwordText = "Password"// TODO: Read from resource
    val buttonSignInText = "Sign In"// TODO: Read from resource
    val buttonSignUpText = "Sign Up"// TODO: Read from resource
    val textButtonSignInText = "Already have an account?"// TODO: Read from resource

    fun validateForm(): Boolean {
        return firstName != "" && lastName != "" && email != "" && password != ""
    }

    Column () {

        // Header
        Text(
            text = signUpHeaderText,
            color = HEADER_TEXT_COLOR,
            modifier = Modifier
                .padding(
                    horizontal = 10.dp,
                    vertical = 20.dp
                ),
            fontSize = 26.sp,
//            fontWeight = FontWeight.Bold
        )

        // First name
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            modifier = Modifier
                .padding(vertical = 5.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(fontSize = 20.sp),
            label = { Text(firstNameText) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            // TODO: BOILERPLATE - create custom OutlinedTextField
            colors = OutlinedTextFieldDefaults.colors().copy(
                cursorColor = HEADER_TEXT_COLOR,
                focusedIndicatorColor = HEADER_TEXT_COLOR,
                focusedLabelColor = HEADER_TEXT_COLOR,
                textSelectionColors = TextSelectionColors(
                    handleColor = HEADER_TEXT_COLOR,
                    backgroundColor = SELECTION_COLOR
                ),
//                        focusedContainerColor = COMPONENT_BACKGROUND_COLOR,
//                        unfocusedContainerColor = COMPONENT_BACKGROUND_COLOR,
//                        disabledContainerColor = COMPONENT_BACKGROUND_COLOR
            )
        )

        // Last name
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            modifier = Modifier
                .padding(vertical = 5.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(fontSize = 20.sp),
            label = { Text(lastNameText) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            // TODO: BOILERPLATE - create custom OutlinedTextField
            colors = OutlinedTextFieldDefaults.colors().copy(
                cursorColor = HEADER_TEXT_COLOR,
                focusedIndicatorColor = HEADER_TEXT_COLOR,
                focusedLabelColor = HEADER_TEXT_COLOR,
                textSelectionColors = TextSelectionColors(
                    handleColor = HEADER_TEXT_COLOR,
                    backgroundColor = SELECTION_COLOR
                ),
//                        focusedContainerColor = COMPONENT_BACKGROUND_COLOR,
//                        unfocusedContainerColor = COMPONENT_BACKGROUND_COLOR,
//                        disabledContainerColor = COMPONENT_BACKGROUND_COLOR
            )
        )

        // E-mail
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .padding(vertical = 5.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(fontSize = 20.sp),
            label = { Text(emailText) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            // TODO: BOILERPLATE - create custom OutlinedTextField
            colors = OutlinedTextFieldDefaults.colors().copy(
                cursorColor = HEADER_TEXT_COLOR,
                focusedIndicatorColor = HEADER_TEXT_COLOR,
                focusedLabelColor = HEADER_TEXT_COLOR,
                textSelectionColors = TextSelectionColors(
                    handleColor = HEADER_TEXT_COLOR,
                    backgroundColor = SELECTION_COLOR
                ),
//                focusedContainerColor = COMPONENT_BACKGROUND_COLOR,
//                unfocusedContainerColor = COMPONENT_BACKGROUND_COLOR,
//                disabledContainerColor = COMPONENT_BACKGROUND_COLOR
            )
        )

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .padding(vertical = 5.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(fontSize = 20.sp),
            label = { Text(passwordText) },
            trailingIcon = {
                val image = if (showPassword)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                // Please provide localized description for accessibility services
                val description = if (showPassword) "Hide password" else "Show password"

                IconButton(onClick = { showPassword = !showPassword} ){
                    Icon(imageVector  = image, description)
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            // TODO: BOILERPLATE - create custom OutlinedTextField
            colors = OutlinedTextFieldDefaults.colors().copy(
                cursorColor = HEADER_TEXT_COLOR,
                focusedIndicatorColor = HEADER_TEXT_COLOR,
                focusedLabelColor = HEADER_TEXT_COLOR,
                textSelectionColors = TextSelectionColors(
                    handleColor = HEADER_TEXT_COLOR,
                    backgroundColor = SELECTION_COLOR
                ),
                focusedTrailingIconColor = HEADER_TEXT_COLOR,
                unfocusedTrailingIconColor = HEADER_TEXT_COLOR,
//                focusedContainerColor = COMPONENT_BACKGROUND_COLOR,
//                unfocusedContainerColor = COMPONENT_BACKGROUND_COLOR,
//                disabledContainerColor = COMPONENT_BACKGROUND_COLOR
            )
        )

        // Sign-up button
        OutlinedButton(
            onClick = {
                onSignUpWithEmail(firstName, lastName, email, password)
            },
//                enabled = isFormFilled,
            enabled = validateForm(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HEADER_TEXT_COLOR,
                contentColor = COMPONENT_BACKGROUND_COLOR
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 20.dp,
                    bottom = 10.dp
                )
                .height(50.dp)
        ) {
            Text(
                text = buttonSignUpText
            )
        }

        TextButton(
            onClick = onSwitchScreen,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Text (
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Light,
                            color = HEADER_TEXT_COLOR
                        )
                    ) {
                        append(textButtonSignInText)
                    }

                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Light,
                            color = HEADER_TEXT_COLOR
                        )
                    ) {
                        append(" ")
                    }

                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = HEADER_TEXT_COLOR
                        )
                    ) {
                        append(buttonSignInText)
                    }
                },
                textAlign = TextAlign.Center
            )
        }
    }
}