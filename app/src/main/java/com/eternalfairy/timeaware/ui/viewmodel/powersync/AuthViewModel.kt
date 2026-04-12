package com.eternalfairy.timeaware.ui.viewmodel.powersync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eternalfairy.timeaware.db.data.UserProfile
import com.eternalfairy.timeaware.db.powersync.UserProfilesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import io.github.cdimascio.dotenv.dotenv

sealed interface AuthResponse {
    data object Success: AuthResponse
    data class Error(val message: String?): AuthResponse
}

class AuthManager (
//class AuthManager @Inject constructor(
//    private val context: Context
) {
    val dotenv = dotenv()

    private val supabaseClient = createSupabaseClient(
        supabaseUrl = dotenv["SUPABASE_URL"],
        supabaseKey = dotenv["SUPABASE_KEY"]
    ) {
        install(Auth)
    }

    fun getUser(): Flow<UserInfo?> = flow {
        try {
            emit(supabaseClient.auth.currentUserOrNull())
        } catch (e: Exception) {
            emit(null)
        }
    }

    fun getSessionStatus(): Flow<SessionStatus> {
        try {
            return supabaseClient.auth.sessionStatus
        } catch (e: Exception) {

        }
        TODO("Provide the return value")
    }

    fun linkEmail(emailValue: String): Flow<AuthResponse> = flow {
        try {
            // Attempt to update the user with the existing email
            supabaseClient.auth.updateUser {
                email = emailValue
            }
        } catch (e: Exception) {
            // Handle the error (since the email belongs to an existing user)
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    fun logOut(): Flow<AuthResponse> = flow {
        try {
            supabaseClient.auth.signOut()
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    fun signUpWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            supabaseClient.auth.signUpWith(Email) {
                email = emailValue
                password = passwordValue
            }
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    fun signInWithEmail(emailValue: String, passwordValue: String): Flow<AuthResponse> = flow {
        try {
            supabaseClient.auth.signInWith(Email) {
                email = emailValue
                password = passwordValue
            }
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    fun signInAnonymously(): Flow<AuthResponse> = flow {
        try {
            supabaseClient.auth.signInAnonymously()
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

    // Convert an anonymous user to a permanent user
    fun linkEmailIdentityToUser(emailValue: String): Flow<AuthResponse> = flow {
        try {
            supabaseClient.auth.updateUser {
                email = emailValue
            }
            emit(AuthResponse.Success)
        } catch (e: Exception) {
            emit(AuthResponse.Error(e.localizedMessage))
        }
    }

//    suspend fun linkOAuthIdentityToUser() {
//        supabase.auth.linkIdentity(Google)
//    }
}

@HiltViewModel
class AuthViewModel  @Inject constructor(
//class AuthViewModel (
    private val userProfilesRepository: UserProfilesRepository
//    private val context: Context
) : ViewModel() {
    //    val authManager = AuthManager(context)
    val authManager = AuthManager()

    val sessionStatus = authManager
        .getSessionStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = null
        )

    val user = authManager
        .getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000L),
            initialValue = null
        )

    fun saveUserProfile(firstName: String, lastName: String, email: String) {
        viewModelScope.launch {
            userProfilesRepository.insertUserProfile(
                UserProfile(
                    id = UUID.fromString(user.value!!.id),
                    firstName = firstName,
                    lastName =  lastName,
                    email = email
                )
            )
        }
    }
}