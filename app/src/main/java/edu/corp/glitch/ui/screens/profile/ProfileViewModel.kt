package edu.corp.glitch.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.corp.glitch.data.models.ErrorResponse
import edu.corp.glitch.data.models.User
import edu.corp.glitch.data.preferences.UserPreferences
import edu.corp.glitch.data.repository.GlitchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
    @Inject
    constructor(
        private val repository: GlitchRepository,
        private val userPreferences: UserPreferences,
    ) : ViewModel() {
        private val _user = MutableStateFlow<User?>(null)
        val user: StateFlow<User?> = _user

        private val _isFollowing = MutableStateFlow(false)
        val isFollowing: StateFlow<Boolean> = _isFollowing

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage

        private val gson = Gson()

        fun loadCurrentUser() {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val response = repository.getCurrentUser()
                    if (response.isSuccessful) {
                        _user.value = response.body()
                    } else {
                        val errorMsg = parseErrorMessage(response.errorBody()?.string())
                        _errorMessage.value = "Failed to load profile: $errorMsg"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }

        fun loadUserByUsername(username: String) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val response = repository.getUserByUsername(username)
                    if (response.isSuccessful) {
                        _user.value = response.body()
                        checkIfFollowing(response.body()?.id ?: 0)
                    } else {
                        val errorMsg = parseErrorMessage(response.errorBody()?.string())
                        _errorMessage.value = "Failed to load user: $errorMsg"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }

        private suspend fun checkIfFollowing(userId: Int) {
            try {
                val response = repository.checkFollow(userId)
                _isFollowing.value = response.isSuccessful && response.body() != null
            } catch (e: Exception) {
                _isFollowing.value = false
            }
        }

        fun toggleFollow(userId: Int) {
            viewModelScope.launch {
                try {
                    if (_isFollowing.value) {
                        repository.deleteFollow(userId)
                        _isFollowing.value = false
                    } else {
                        repository.createFollow(userId)
                        _isFollowing.value = true
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error toggling follow: ${e.message}"
                }
            }
        }

        fun updateBio(bio: String) {
            viewModelScope.launch {
                try {
                    val response = repository.updateUser(bio)
                    if (response.isSuccessful) {
                        _user.value = response.body()
                    } else {
                        val errorMsg = parseErrorMessage(response.errorBody()?.string())
                        _errorMessage.value = "Failed to update bio: $errorMsg"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error: ${e.message}"
                }
            }
        }

        fun uploadProfilePic(file: File) {
            viewModelScope.launch {
                try {
                    val response = repository.uploadProfilePic(file)
                    if (response.isSuccessful) {
                        _user.value = response.body()
                    } else {
                        val errorMsg = parseErrorMessage(response.errorBody()?.string())
                        _errorMessage.value = "Failed to upload picture: $errorMsg"
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error: ${e.message}"
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

        suspend fun isOwnProfile(): Boolean {
            val currentUsername = userPreferences.username.first()
            return _user.value?.username == currentUsername
        }

        fun clearError() {
            _errorMessage.value = null
        }
    }
