package edu.corp.glitch.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.corp.glitch.data.models.ErrorResponse
import edu.corp.glitch.data.preferences.UserPreferences
import edu.corp.glitch.data.repository.GlitchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        private val repository: GlitchRepository,
        private val userPreferences: UserPreferences,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
        val uiState: StateFlow<AuthUiState> = _uiState

        private val gson = Gson()

        fun login(
            username: String,
            password: String,
        ) {
            viewModelScope.launch {
                _uiState.value = AuthUiState.Loading
                try {
                    val response = repository.login(username, password)
                    if (response.isSuccessful && response.body() != null) {
                        val auth = response.body()!!
                        userPreferences.saveAuthData(auth.token, auth.id, auth.username)
                        _uiState.value = AuthUiState.Success
                    } else {
                        val errorMessage = parseErrorMessage(response.errorBody()?.string())
                        _uiState.value = AuthUiState.Error(errorMessage)
                    }
                } catch (e: Exception) {
                    _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
                }
            }
        }

        fun register(
            username: String,
            password: String,
            email: String,
        ) {
            viewModelScope.launch {
                _uiState.value = AuthUiState.Loading
                try {
                    val response = repository.register(username, password, email)
                    if (response.isSuccessful && response.body() != null) {
                        val auth = response.body()!!
                        userPreferences.saveAuthData(auth.token, auth.id, auth.username)
                        _uiState.value = AuthUiState.Success
                    } else {
                        val errorMessage = parseErrorMessage(response.errorBody()?.string())
                        _uiState.value = AuthUiState.Error(errorMessage)
                    }
                } catch (e: Exception) {
                    _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
                }
            }
        }

        private fun parseErrorMessage(errorBody: String?): String =
            try {
                if (errorBody != null) {
                    val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                    errorResponse.message ?: errorResponse.error ?: "Unknown error"
                } else {
                    "Unknown error"
                }
            } catch (e: Exception) {
                errorBody ?: "Unknown error"
            }

        fun logout() {
            viewModelScope.launch {
                userPreferences.clearAuthData()
            }
        }

        fun resetState() {
            _uiState.value = AuthUiState.Idle
        }
    }

sealed class AuthUiState {
    object Idle : AuthUiState()

    object Loading : AuthUiState()

    object Success : AuthUiState()

    data class Error(
        val message: String,
    ) : AuthUiState()
}
