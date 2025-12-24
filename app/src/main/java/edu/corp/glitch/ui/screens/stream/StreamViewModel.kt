package edu.corp.glitch.ui.screens.stream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import edu.corp.glitch.data.api.UpdateStreamRequest
import edu.corp.glitch.data.models.ChatMessage
import edu.corp.glitch.data.models.ErrorResponse
import edu.corp.glitch.data.models.Stream
import edu.corp.glitch.data.models.User
import edu.corp.glitch.data.preferences.UserPreferences
import edu.corp.glitch.data.repository.GlitchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.*
import javax.inject.Inject

@HiltViewModel
class StreamViewModel @Inject constructor(
    private val repository: GlitchRepository,
    private val userPreferences: UserPreferences,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _stream = MutableStateFlow<Stream?>(null)
    val stream: StateFlow<Stream?> = _stream

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isOwnStream = MutableStateFlow(false)
    val isOwnStream: StateFlow<Boolean> = _isOwnStream

    private var webSocket: WebSocket? = null
    private val gson = Gson()

    fun loadStream(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getStreamByUsername(username)
                if (response.isSuccessful) {
                    _stream.value = response.body()
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    _errorMessage.value = "Failed to load stream: $errorMsg"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading stream: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUser(username: String) {
        viewModelScope.launch {
            try {
                val response = repository.getUserByUsername(username)
                if (response.isSuccessful) {
                    _user.value = response.body()
                    response.body()?.id?.let { checkIfFollowing(it) }
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    _errorMessage.value = "Failed to load user: $errorMsg"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading user: ${e.message}"
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

    fun checkIsOwnStream(streamUsername: String) {
        viewModelScope.launch {
            val currentUsername = userPreferences.username.first()
            _isOwnStream.value = streamUsername == currentUsername
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
                _user.value?.username?.let { loadUser(it) }
            } catch (e: Exception) {
                _errorMessage.value = "Error toggling follow: ${e.message}"
            }
        }
    }

    fun updateStreamTitle(newTitle: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = UpdateStreamRequest(title = newTitle)
                val response = repository.updateStream(request)
                if (response.isSuccessful) {
                    _stream.value = response.body()
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    _errorMessage.value = "Failed to update stream title: $errorMsg"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating stream title: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun connectToChat(streamUsername: String) {
        viewModelScope.launch {
            try {
                val token = userPreferences.authToken.first() ?: return@launch

                val request = Request.Builder()
                    // .url("ws://arch.local:8989/api/chat/$streamUsername")
                    .url("ws://10.87.7.197:8989/api/chat/$streamUsername")
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("chat", streamUsername)
                    .build()

                webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        try {
                            val message = gson.fromJson(text, ChatMessage::class.java)
                            val currentMessages = _chatMessages.value.toMutableList()
                            currentMessages.add(message)
                            _chatMessages.value = currentMessages
                        } catch (e: Exception) {
                            _errorMessage.value = "Error connecting to chat: ${e.message}"
                        }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        _errorMessage.value = "Chat connection failed: ${t.message}"
                    }
                })
            } catch (e: Exception) {
                _errorMessage.value = "Error connecting to chat: ${e.message}"
            }
        }
    }

    fun sendMessage(message: String) {
        webSocket?.let { ws ->
            val json = gson.toJson(mapOf("message" to message))
            ws.send(json)
        }
    }

    fun disconnectChat() {
        webSocket?.close(1000, "User left")
        webSocket = null
        _chatMessages.value = emptyList()
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody != null) {
                val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                errorResponse.message ?: errorResponse.error ?: "Unknown error"
            } else {
                "Unknown error"
            }
        } catch (e: Exception) {
            errorBody ?: "Unknown error"
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        disconnectChat()
    }
}
