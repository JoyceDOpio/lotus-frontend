package com.eternalfairy.lotus.view.screen.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun SmallScreenLandscape(
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
            .background(COMPONENT_BACKGROUND_COLOR)
            .verticalScroll(rememberScrollState())
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row (
            modifier = Modifier
                .fillMaxHeight()
                .width(400.dp)
//            .background(BACKGROUND_COLOR)
                .background(COMPONENT_BACKGROUND_COLOR)
                .padding(
                    horizontal = 30.dp,
                    vertical = 10.dp
                )
            ,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Sign-in screen
            AnimatedVisibility(
                visible = screenState == ScreenState.LogIn,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                LogInScreen(
                    viewModel = viewModel,
                    onSwitchScreen = { screenState = ScreenState.SignUp }
                )
            }

            // Sign-up screen
            AnimatedVisibility(
                visible = screenState == ScreenState.SignUp,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                SignUpScreen(
                    viewModel = viewModel,
                    onSwitchScreen = { screenState = ScreenState.LogIn }
                )
            }

//            // OR
//            Column (
//                modifier = Modifier
//                    .padding(
//                        horizontal = 30.dp
//                    ),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .width(1.dp)
//                        .background(HEADER_TEXT_COLOR)
//                )
//
//                Text(
//                    text = orText,
//                    color = HEADER_TEXT_COLOR,
//                    modifier = Modifier
//                        .padding(
//                            vertical = 10.dp
//                        )
//                )
//
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .width(1.dp)
//                        .background(HEADER_TEXT_COLOR)
//                )
//            }
//
//        Column (
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            // Google button
//            OutlinedButton(
//                onClick = onSignInWithGoogle,
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
//                    text = buttonGoogleSignUpText
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
}