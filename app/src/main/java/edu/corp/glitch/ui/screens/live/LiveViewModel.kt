package edu.corp.glitch.ui.screens.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.corp.glitch.data.models.Follow
import edu.corp.glitch.data.repository.GlitchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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

        init {
            loadLiveFollows()
        }

        fun loadLiveFollows() {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val response = repository.getLiveFollows()
                    if (response.isSuccessful) {
                        _liveFollows.value = response.body() ?: emptyList()
                    }
                } catch (e: Exception) {
                    // Handle error
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
