package edu.corp.glitch.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.corp.glitch.data.models.User
import edu.corp.glitch.data.preferences.UserPreferences
import edu.corp.glitch.data.repository.GlitchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

        fun loadCurrentUser() {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val response = repository.getCurrentUser()
                    if (response.isSuccessful) {
                        _user.value = response.body()
                    }
                } catch (e: Exception) {
                    // Handle error
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
                    }
                } catch (e: Exception) {
                    // Handle error
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
                    // Handle error
                }
            }
        }

        fun updateBio(bio: String) {
            viewModelScope.launch {
                try {
                    val response = repository.updateUser(bio)
                    if (response.isSuccessful) {
                        _user.value = response.body()
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }

        fun uploadProfilePic(file: File) {
            viewModelScope.launch {
                try {
                    val response = repository.uploadProfilePic(file)
                    if (response.isSuccessful) {
                        _user.value = response.body()
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }

        suspend fun isOwnProfile(): Boolean {
            val currentUsername = userPreferences.username.first()
            return _user.value?.username == currentUsername
        }
    }
