package edu.corp.glitch.viewmodel

import edu.corp.glitch.data.models.Stream
import edu.corp.glitch.data.models.User
import edu.corp.glitch.data.repository.GlitchRepository
import edu.corp.glitch.ui.screens.search.SearchViewModel
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @Mock
    private lateinit var repository: GlitchRepository

    private lateinit var viewModel: SearchViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = SearchViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadLiveStreams should update streams state`() = runTest {
        // Given
        val streams = listOf(
            Stream(1, "user1", true, "Stream 1", "2024-01-01", 100),
            Stream(2, "user2", true, "Stream 2", "2024-01-01", 50)
        )
        `when`(repository.getLiveStreams()).thenReturn(Response.success(streams))

        // When
        viewModel.loadLiveStreams()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val result = viewModel.streams.first()
        assertEquals(2, result.size)
        assertEquals("user1", result[0].username)
    }

    @Test
    fun `loadLiveStreams should handle error`() = runTest {
        // Given
        val errorBody = "Server error".toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<List<Stream>>(500, errorBody)
        `when`(repository.getLiveStreams()).thenReturn(errorResponse)

        // When
        viewModel.loadLiveStreams()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val result = viewModel.streams.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `searchUser should update searched users state`() = runTest {
        // Given
        val query = "test"
        val users = listOf(
            User(1, "testuser1", "2024-01-01", "Bio 1", null, 10),
            User(2, "testuser2", "2024-01-01", "Bio 2", null, 5)
        )
        `when`(repository.searchUsers(query)).thenReturn(Response.success(users))

        // When
        viewModel.searchUser(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val result = viewModel.searchedUsers.first()
        assertEquals(2, result.size)
        assertTrue(result[0].username.contains("test"))
    }

    @Test
    fun `searchUser with empty query should clear results`() = runTest {
        // When
        viewModel.searchUser("")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val result = viewModel.searchedUsers.first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `searchUser should handle error`() = runTest {
        // Given
        val query = "test"
        val errorBody = "Not found".toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<List<User>>(404, errorBody)
        `when`(repository.searchUsers(query)).thenReturn(errorResponse)

        // When
        viewModel.searchUser(query)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val result = viewModel.searchedUsers.first()
        assertTrue(result.isEmpty())
    }
}
