package edu.corp.glitch.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.corp.glitch.data.preferences.UserPreferences
import edu.corp.glitch.data.repository.GlitchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
                        _uiState.value = AuthUiState.Error(response.message())
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
                        _uiState.value = AuthUiState.Error(response.message())
                    }
                } catch (e: Exception) {
                    _uiState.value = AuthUiState.Error(e.message ?: "Unknown error")
                }
            }
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
