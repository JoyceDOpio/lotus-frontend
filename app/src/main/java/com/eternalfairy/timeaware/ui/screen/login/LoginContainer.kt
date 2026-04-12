package com.eternalfairy.timeaware.ui.screen.login

import androidx.compose.runtime.Composable
import com.eternalfairy.timeaware.ui.component.MultiWindowSizeLayout
import com.eternalfairy.timeaware.ui.viewmodel.supabase.AuthResponse
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow

@Composable
fun LoginContainer(
    userInfo: UserInfo?,
    onSignInAnonymously: () -> Unit,
    onSignInWithEmail: (String, String) -> Unit,
    onSignUpWithEmail: (String, String, String, String) -> Unit
) {
    MultiWindowSizeLayout(
        default = {},
        portraitPhone = {
            SmallScreenPortrait(
                userInfo = userInfo,
//                onNavigateToNext = onNavigateToNext,
                onSignInAnonymously = onSignInAnonymously,
                onSignInWithEmail = onSignInWithEmail,
                onSignUpWithEmail = onSignUpWithEmail
            )
        },
        landscapePhone = {
        },
        portraitTablet = {
        },
        landscapeTablet = {
        }
    )
}