package com.eternalfairy.lotus.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eternalfairy.lotus.auth.AuthResponse
import com.eternalfairy.lotus.auth.IAuthRepository
import com.eternalfairy.lotus.view.screen.login.AuthUiState
import com.eternalfairy.lotus.view.screen.login.AuthUiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: IAuthRepository
) : ViewModel() {

    init {
        // Check whether we have the token when the app is opened
        authenticate()
    }

    var state by mutableStateOf(AuthUiState())
    private val responseChannel = Channel<AuthResponse<Unit>>()
    val authResponses = responseChannel.receiveAsFlow()

    private fun authenticate() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val response = repository.authenticate()
            responseChannel.send(response)
            state = state.copy(isLoading = false)
        }
    }

    fun onEvent(event: AuthUiEvent) {
        when(event) {
            is AuthUiEvent.LogInEmailChanged -> {
                state = state.copy(logInEmail = event.value)
            }
            is AuthUiEvent.LogInPasswordChanged -> {
                state = state.copy(logInPassword = event.value)
            }
            // The button click
            is AuthUiEvent.LogIn -> {
                logIn()
            }
            is AuthUiEvent.SignUpNameChanged -> {
                state = state.copy(signUpName = event.value)
            }
            is AuthUiEvent.SignUpEmailChanged -> {
                state = state.copy(signUpEmail = event.value)
            }
            is AuthUiEvent.SignUpPasswordChanged -> {
                state = state.copy(signUpPassword = event.value)
            }
            // The button click
            is AuthUiEvent.SignUp -> {
                signUp()
            }
        }
    }

    private fun logIn() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val response = repository.logIn(
                email = state.logInEmail,
                password = state.logInPassword
            )
            responseChannel.send(response)
            state = state.copy(isLoading = false)
        }
    }

    private fun signUp() {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val response = repository.signUp(
                name = state.signUpName,
                email = state.signUpEmail,
                password = state.signUpPassword
            )
            responseChannel.send(response)
            state = state.copy(isLoading = false)
        }
    }
}