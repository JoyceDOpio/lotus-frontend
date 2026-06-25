package com.eternalfairy.lotus.view.screen.login

data class AuthState(
    val isLoading: Boolean = false,
    val signUpName: String = "",
    val signUpEmail: String = "",
    val signUpPassword: String = "",
    val logInEmail: String = "",
    val logInPassword: String = ""
)
