package edu.corp.glitch.viewmodel

import edu.corp.glitch.data.models.AuthResponse
import edu.corp.glitch.data.preferences.UserPreferences
import edu.corp.glitch.data.repository.GlitchRepository
import edu.corp.glitch.ui.screens.auth.AuthUiState
import edu.corp.glitch.ui.screens.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import retrofit2.Response
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @Mock
    private lateinit var repository: GlitchRepository

    @Mock
    private lateinit var userPreferences: UserPreferences

    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(repository, userPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with valid credentials should update state to Success`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val authResponse = AuthResponse(1, username, "test_token")
        `when`(repository.login(username, password)).thenReturn(Response.success(authResponse))

        // When
        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state is AuthUiState.Success)
        verify(userPreferences).saveAuthData("test_token", 1, username)
    }

    @Test
    fun `login with invalid credentials should update state to Error`() = runTest {
        // Given
        val username = "testuser"
        val password = "wrongpass"
        val errorBody = "Unauthorized".toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<AuthResponse>(401, errorBody)
        `when`(repository.login(username, password)).thenReturn(errorResponse)

        // When
        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state is AuthUiState.Error)
    }

    @Test
    fun `login with exception should update state to Error`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        `when`(repository.login(username, password)).thenThrow(RuntimeException("Network error"))

        // When
        viewModel.login(username, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state is AuthUiState.Error)
    }

    @Test
    fun `register with valid data should update state to Success`() = runTest {
        // Given
        val username = "newuser"
        val password = "newpass"
        val email = "new@example.com"
        val authResponse = AuthResponse(2, username, "new_token")
        `when`(repository.register(username, password, email))
            .thenReturn(Response.success(authResponse))

        // When
        viewModel.register(username, password, email)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state is AuthUiState.Success)
        verify(userPreferences).saveAuthData("new_token", 2, username)
    }

    @Test
    fun `register with error should update state to Error`() = runTest {
        // Given
        val username = "newuser"
        val password = "newpass"
        val email = "new@example.com"
        val errorBody = "User already exists".toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<AuthResponse>(409, errorBody)
        `when`(repository.register(username, password, email)).thenReturn(errorResponse)

        // When
        viewModel.register(username, password, email)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state is AuthUiState.Error)
    }

    @Test
    fun `logout should clear auth data`() = runTest {
        // When
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(userPreferences).clearAuthData()
    }
}

