package edu.corp.glitch.viewmodel

import edu.corp.glitch.data.models.User
import edu.corp.glitch.data.preferences.UserPreferences
import edu.corp.glitch.data.repository.GlitchRepository
import edu.corp.glitch.ui.screens.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @Mock
    private lateinit var repository: GlitchRepository

    @Mock
    private lateinit var userPreferences: UserPreferences

    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ProfileViewModel(repository, userPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadCurrentUser should update user state on success`() = runTest {
        // Given
        val user = User(1, "testuser", "2024-01-01", "Test bio", null, 10)
        `when`(repository.getCurrentUser()).thenReturn(Response.success(user))

        // When
        viewModel.loadCurrentUser()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val result = viewModel.user.first()
        assertNotNull(result)
        assertEquals("testuser", result.username)
        assertEquals("Test bio", result.bio)
    }

    @Test
    fun `loadCurrentUser should update error state on failure`() = runTest {
        // Given
        val errorBody = "Not found".toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<User>(404, errorBody)
        `when`(repository.getCurrentUser()).thenReturn(errorResponse)

        // When
        viewModel.loadCurrentUser()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val error = viewModel.errorMessage.first()
        assertNotNull(error)
    }

    @Test
    fun `loadUserByUsername should update user state on success`() = runTest {
        // Given
        val username = "otheruser"
        val user = User(2, username, "2024-01-01", "Other bio", null, 5)
        `when`(repository.getUserByUsername(username)).thenReturn(Response.success(user))

        // When
        viewModel.loadUserByUsername(username)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val result = viewModel.user.first()
        assertNotNull(result)
        assertEquals(username, result.username)
    }

    @Test
    fun `updateBio should update user state on success`() = runTest {
        // Given
        val newBio = "Updated bio"
        val updatedUser = User(1, "testuser", "2024-01-01", newBio, null, 10)
        `when`(repository.updateUser(newBio)).thenReturn(Response.success(updatedUser))

        // When
        viewModel.updateBio(newBio)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val result = viewModel.user.first()
        assertNotNull(result)
        assertEquals(newBio, result.bio)
    }

    @Test
    fun `isOwnProfile should return true for current user`() = runTest {
        // Given
        val currentUsername = "testuser"
        `when`(userPreferences.username).thenReturn(flowOf(currentUsername))
        val user = User(1, currentUsername, "2024-01-01", "Bio", null, 10)
        `when`(repository.getCurrentUser()).thenReturn(Response.success(user))

        viewModel.loadCurrentUser()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        val result = viewModel.isOwnProfile()

        // Then
        assertEquals(true, result)
    }
}
