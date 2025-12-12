package edu.corp.glitch.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.corp.glitch.data.preferences.UserPreferences
import edu.corp.glitch.data.repository.GlitchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val repository: GlitchRepository,
        private val userPreferences: UserPreferences,
    ) : ViewModel() {
        val isDarkMode = userPreferences.isDarkMode
        val username = userPreferences.username

        private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
        val updateState: StateFlow<UpdateState> = _updateState

        fun toggleDarkMode() {
            viewModelScope.launch {
                val current = userPreferences.isDarkMode
                userPreferences.setDarkMode(!current.first())
            }
        }

        fun updateEmail(email: String) {
            viewModelScope.launch {
                _updateState.value = UpdateState.Loading
                try {
                    val response = repository.updateEmail(email)
                    if (response.isSuccessful) {
                        _updateState.value = UpdateState.Success("Email updated")
                    } else {
                        _updateState.value = UpdateState.Error(response.message())
                    }
                } catch (e: Exception) {
                    _updateState.value = UpdateState.Error(e.message ?: "Error")
                }
            }
        }

        fun updatePassword(
            oldPassword: String,
            newPassword: String,
        ) {
            viewModelScope.launch {
                _updateState.value = UpdateState.Loading
                try {
                    val response = repository.updatePassword(oldPassword, newPassword)
                    if (response.isSuccessful) {
                        _updateState.value = UpdateState.Success("Password updated")
                    } else {
                        _updateState.value = UpdateState.Error(response.message())
                    }
                } catch (e: Exception) {
                    _updateState.value = UpdateState.Error(e.message ?: "Error")
                }
            }
        }

        fun logout() {
            viewModelScope.launch {
                userPreferences.clearAuthData()
            }
        }

        fun resetState() {
            _updateState.value = UpdateState.Idle
        }
    }

sealed class UpdateState {
    object Idle : UpdateState()

    object Loading : UpdateState()

    data class Success(
        val message: String,
    ) : UpdateState()

    data class Error(
        val message: String,
    ) : UpdateState()
}
