package edu.corp.glitch.ui.screens.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.corp.glitch.data.models.Follow
import edu.corp.glitch.data.models.User
import edu.corp.glitch.data.repository.GlitchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveViewModel
    @Inject
    constructor(
        private val repository: GlitchRepository,
    ) : ViewModel() {
        private val _liveFollows = MutableStateFlow<List<Follow>>(emptyList())
        val liveFollows: StateFlow<List<Follow>> = _liveFollows

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading

        private val _usersMap = MutableStateFlow<Map<String, User>>(emptyMap())
        val usersMap: StateFlow<Map<String, User>> = _usersMap

        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage

        init {
            loadLiveFollows()
        }

        fun loadLiveFollows() {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val response = repository.getLiveFollows()
                    if (response.isSuccessful) {
                        val follows = response.body() ?: emptyList()
                        _liveFollows.value = follows

                        val users = mutableMapOf<String, User>()
                        follows.forEach { follow ->
                            follow.toStream?.username?.let { username ->
                                try {
                                    val userResponse = repository.getUserByUsername(username)
                                    if (userResponse.isSuccessful) {
                                        userResponse.body()?.let { user ->
                                            users[username] = user
                                        }
                                    }
                                } catch (e: Exception) {
                                    _errorMessage.value = "Error: ${e.message}"
                                }
                            }
                        }
                        _usersMap.value = users
                    }
                } catch (e: Exception) {
                    _errorMessage.value = "Error: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
