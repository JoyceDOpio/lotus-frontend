package com.eternalfairy.lotus.view.screen.login

sealed class AuthUiEvent {
    data class SignUpNameChanged(val value: String): AuthUiEvent()
    data class SignUpEmailChanged(val value: String): AuthUiEvent()
    data class SignUpPasswordChanged(val value: String): AuthUiEvent()
    object SignUp: AuthUiEvent()

    data class LogInEmailChanged(val value: String): AuthUiEvent()
    data class LogInPasswordChanged(val value: String): AuthUiEvent()
    object LogIn: AuthUiEvent()
}