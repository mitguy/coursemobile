package edu.corp.glitch.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.corp.glitch.data.models.ErrorResponse
import edu.corp.glitch.data.preferences.UserPreferences
import edu.corp.glitch.data.repository.GlitchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val repository: GlitchRepository,
        private val userPreferences: UserPreferences,
        private val okHttpClient: OkHttpClient,
    ) : ViewModel() {
        val isDarkMode = userPreferences.isDarkMode
        val username = userPreferences.username

        private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
        val updateState: StateFlow<UpdateState> = _updateState

        private val _email = MutableStateFlow<String?>(null)
        val email: StateFlow<String?> = _email

        private val gson = Gson()

        init {
            loadAuthInfo()
        }

        private fun loadAuthInfo() {
            viewModelScope.launch {
                try {
                    val response = repository.getAuth()
                    if (response.isSuccessful) {
                        _email.value = response.body()?.email
                    }
                } catch (e: Exception) {
                    _updateState.value = UpdateState.Error(e.message ?: "Error")
                }
            }
        }

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
                        _email.value = response.body()?.email
                        _updateState.value = UpdateState.Success("Email updated")
                    } else {
                        val errorMsg = parseErrorMessage(response.errorBody()?.string())
                        _updateState.value = UpdateState.Error(errorMsg)
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
                        val errorMsg = parseErrorMessage(response.errorBody()?.string())
                        _updateState.value = UpdateState.Error(errorMsg)
                    }
                } catch (e: Exception) {
                    _updateState.value = UpdateState.Error(e.message ?: "Error")
                }
            }
        }

        fun downloadCSV(onSuccess: (File) -> Unit) {
            viewModelScope.launch {
                _updateState.value = UpdateState.Loading
                try {
                    val token = userPreferences.authToken.first()
                    val request =
                        Request
                            .Builder()
                            .url("http://arch.local:8989/api/vods/export")
                            .addHeader("Authorization", "Bearer $token")
                            .build()

                    val response =
                        withContext(Dispatchers.IO) {
                            okHttpClient.newCall(request).execute()
                        }

                    if (response.isSuccessful) {
                        val file =
                            withContext(Dispatchers.IO) {
                                val tempFile = File.createTempFile("glitch_export_", ".csv")
                                FileOutputStream(tempFile).use { output ->
                                    response.body?.byteStream()?.copyTo(output)
                                }
                                tempFile
                            }
                        _updateState.value = UpdateState.Success("CSV downloaded")
                        onSuccess(file)
                    } else {
                        val errorMsg = parseErrorMessage(response.body?.string())
                        _updateState.value = UpdateState.Error("Failed to download CSV: $errorMsg")
                    }
                } catch (e: Exception) {
                    _updateState.value = UpdateState.Error(e.message ?: "Error downloading CSV")
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
