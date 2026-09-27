package com.eternalfairy.lotus.presentation.screen.login

data class AuthUiState(
    val isLoading: Boolean = false,
    val signUpName: String = "",
    val signUpEmail: String = "",
    val signUpPassword: String = "",
    val logInEmail: String = "",
    val logInPassword: String = ""
)
