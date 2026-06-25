package com.eternalfairy.lotus.view.screen.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.eternalfairy.lotus.auth.AuthResponse
import com.eternalfairy.lotus.view.component.MultiWindowSizeLayout
import com.eternalfairy.lotus.view.theme.Teal74
import com.eternalfairy.lotus.viewmodel.AuthViewModel

@Composable
fun LoginContainer(
    viewModel: AuthViewModel,
    onNavigateToNext: () -> Unit,
) {
    val state = viewModel.state
    // For showing toasts
    val context = LocalContext.current

    // To collect flow safely
    LaunchedEffect(viewModel, context) {
        viewModel.authResponses.collect { response ->
            when(response) {
                is AuthResponse.Authorized -> {
                    onNavigateToNext()
                }
                is AuthResponse.Unauthorized -> {
                    Toast.makeText(
                        context,
                        "You're not authorized",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is AuthResponse.UnknownError -> {
                    Toast.makeText(
                        context,
                        "An unknown error occurred",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    MultiWindowSizeLayout(
        default = {},
        portraitPhone = {
            SmallScreenPortrait(
                viewModel = viewModel
            )
        },
        landscapePhone = {
            SmallScreenLandscape(
                viewModel = viewModel
            )
        },
        portraitTablet = {
            BigScreen(
                viewModel = viewModel
            )
        },
        landscapeTablet = {
            BigScreen(
                viewModel = viewModel
            )
        }
    )

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                strokeWidth = 5.dp,
                color = Teal74
            )
        }
    }
}