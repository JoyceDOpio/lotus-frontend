package com.eternalfairy.lotus.view.screen.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eternalfairy.lotus.view.theme.COMPONENT_BACKGROUND_COLOR
import com.eternalfairy.lotus.viewmodel.AuthViewModel

@Composable
fun BigScreen(
    viewModel: AuthViewModel
) {
    var screenState by remember { mutableStateOf(ScreenState.LogIn) }

    // Texts
    val orText = "OR"// TODO: Read from resource
    val buttonAnonymousSignUpText = "Continue without an account"// TODO: Read from resource
    val buttonGoogleSignUpText = "Continue with Google"// TODO: Read from resource

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
            visible = screenState == ScreenState.LogIn
        ) {
            LogInScreen(
                viewModel = viewModel,
//                onSignInWithEmail = onSignInWithEmail,
                onSwitchScreen = { screenState = ScreenState.SignUp }
            )
        }

        // Sign-up screen
        AnimatedVisibility(
            visible = screenState == ScreenState.SignUp
        ) {
            SignUpScreen(
                viewModel = viewModel,
//                onSignUpWithEmail = onSignUpWithEmail,
                onSwitchScreen = { screenState = ScreenState.SignUp }
            )
        }

//        // OR
//        Row (
//            modifier = Modifier
//                .padding(
//                    vertical = 30.dp
//                ),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .height(1.dp)
//                    .background(HEADER_TEXT_COLOR)
//            )
//
//            Text(
//                text = orText,
//                color = HEADER_TEXT_COLOR,
//                modifier = Modifier
//                    .padding(
//                        horizontal = 10.dp
//                    )
//            )
//
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .height(1.dp)
//                    .background(HEADER_TEXT_COLOR)
//            )
//        }
//
//        // Google button
//        OutlinedButton(
//            onClick = onSignInWithGoogle,
//            shape = RoundedCornerShape(10.dp),
//            colors = ButtonDefaults.buttonColors(
////                containerColor = COMPONENT_BACKGROUND_COLOR,
//                containerColor = Color.Transparent,
//                contentColor = HEADER_TEXT_COLOR
//            ),
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(50.dp)
//        ) {
//            Text(
//                text = buttonGoogleSignUpText
//            )
//        }
//
//        // Anonymous sign-in button
//        OutlinedButton(
//            onClick = onSignInAnonymously,
//            shape = RoundedCornerShape(10.dp),
//            colors = ButtonDefaults.buttonColors(
////                containerColor = COMPONENT_BACKGROUND_COLOR,
//                containerColor = Color.Transparent,
//                contentColor = HEADER_TEXT_COLOR
//            ),
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(50.dp)
//        ) {
//            Text(
//                text = buttonAnonymousSignUpText
//            )
//        }
    }
}