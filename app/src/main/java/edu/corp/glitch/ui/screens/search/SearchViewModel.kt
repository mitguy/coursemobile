package edu.corp.glitch.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.corp.glitch.data.models.Stream
import edu.corp.glitch.data.models.User
import edu.corp.glitch.data.repository.GlitchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val repository: GlitchRepository,
    ) : ViewModel() {
        private val _streams = MutableStateFlow<List<Stream>>(emptyList())
        val streams: StateFlow<List<Stream>> = _streams

        private val _searchQuery = MutableStateFlow("")
        val searchQuery: StateFlow<String> = _searchQuery

        private val _searchedUsers = MutableStateFlow<List<User>>(emptyList())
        val searchedUsers: StateFlow<List<User>> = _searchedUsers

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading

        init {
            loadLiveStreams()
        }

        fun loadLiveStreams() {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val response = repository.getLiveStreams()
                    if (response.isSuccessful) {
                        _streams.value = response.body() ?: emptyList()
                    }
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    _isLoading.value = false
                }
            }
        }

        fun searchUser(query: String) {
            _searchQuery.value = query

            if (query.isBlank()) {
                _searchedUsers.value = emptyList()
                loadLiveStreams()
                return
            }

            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val response = repository.searchUsers(query)
                    if (response.isSuccessful) {
                        _searchedUsers.value = response.body() ?: emptyList()
                    }
                } catch (e: Exception) {
                    _searchedUsers.value = emptyList()
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
