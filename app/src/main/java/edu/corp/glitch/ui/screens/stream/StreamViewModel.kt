package edu.corp.glitch.ui.screens.stream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import edu.corp.glitch.data.models.ChatMessage
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

    private var webSocket: WebSocket? = null
    private val gson = Gson()

    fun loadStream(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getStreamByUsername(username)
                if (response.isSuccessful) {
                    _stream.value = response.body()
                }
            } catch (e: Exception) {
                // Handle error
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
                }
            } catch (e: Exception) {
                // Handle error
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
                // Reload user to update follower count
                _user.value?.username?.let { loadUser(it) }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun connectToChat(streamUsername: String) {
        viewModelScope.launch {
            try {
                val token = userPreferences.authToken.first() ?: return@launch

                val request = Request.Builder()
                    .url("ws://arch.local:8989/api/chat/$streamUsername")
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
                            // Handle parsing error
                        }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        // Handle connection failure
                    }
                })
            } catch (e: Exception) {
                // Handle error
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

    override fun onCleared() {
        super.onCleared()
        disconnectChat()
    }
}

