package com.eternalfairy.timeaware.ui.screen.login

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.eternalfairy.timeaware.ui.theme.BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.timeaware.ui.theme.HEADER_TEXT_COLOR
import com.eternalfairy.timeaware.ui.theme.SELECTION_COLOR
import com.eternalfairy.timeaware.ui.viewmodel.supabase.AuthResponse
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

enum class ScreenState{
    SignIn,
    SignUp
}

@Composable
fun SmallScreenPortrait(
    userInfo: UserInfo?,
    onSignInAnonymously: () -> Unit,
    onSignInWithEmail: (String, String) -> Unit,
    onSignUpWithEmail: (String, String, String, String) -> Unit
) {
    var screenState by remember { mutableStateOf(ScreenState.SignUp) }

    // Texts
    val orText = "OR"// TODO: Read from resource
    val buttonAnonymousSignUpText = "Continue without an account"// TODO: Read from resource

    Column (
        modifier = Modifier
            .fillMaxSize()
//            .background(BACKGROUND_COLOR)
            .background(COMPONENT_BACKGROUND_COLOR)
            .padding(
                horizontal = 30.dp,
                vertical = 50.dp
            )
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Sign-in screen
        AnimatedVisibility(
            visible = screenState == ScreenState.SignIn
        ) {
            SignInScreen(
                onSignInWithEmail = onSignInWithEmail,
                onSwitchScreen = { screenState = ScreenState.SignUp }
            )
        }

        // Sign-up screen
        AnimatedVisibility(
            visible = screenState == ScreenState.SignUp
        ) {
            SignUpScreen(
                onSignUpWithEmail = onSignUpWithEmail,
                onSwitchScreen = { screenState = ScreenState.SignUp }
            )
        }


//        // The anonymous sign-in option
//        AnimatedVisibility(
//            visible = userInfo == null
//        ) {
//            // OR
//            Row (
//                modifier = Modifier
//                    .padding(
//                        vertical = 30.dp
//                    ),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(1.dp)
//                        .background(HEADER_TEXT_COLOR)
//                )
//
//                Text(
//                    text = orText,
//                    color = HEADER_TEXT_COLOR,
//                    modifier = Modifier
//                        .padding(
//                            horizontal = 10.dp
//                        )
//                )
//
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(1.dp)
//                        .background(HEADER_TEXT_COLOR)
//                )
//            }
//
//            // Anonymous sign-up button
//            OutlinedButton(
//                onClick = onSignInAnonymously,
//                shape = RoundedCornerShape(10.dp),
//                colors = ButtonDefaults.buttonColors(
////                containerColor = COMPONENT_BACKGROUND_COLOR,
//                    containerColor = Color.Transparent,
//                    contentColor = HEADER_TEXT_COLOR
//                ),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp)
//            ) {
//                Text(
//                    text = buttonAnonymousSignUpText
//                )
//            }
//        }
    }
}