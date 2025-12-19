package edu.corp.glitch.repository

import edu.corp.glitch.data.api.*
import edu.corp.glitch.data.models.AuthResponse
import edu.corp.glitch.data.models.User
import edu.corp.glitch.data.repository.GlitchRepository
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GlitchRepositoryTest {

    @Mock
    private lateinit var api: GlitchApiService

    private lateinit var repository: GlitchRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = GlitchRepository(api)
    }

    @Test
    fun `login should return success response`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val authResponse = AuthResponse(1, username, "token")
        `when`(api.login(LoginRequest(username, password)))
            .thenReturn(Response.success(authResponse))

        // When
        val result = repository.login(username, password)

        // Then
        assertTrue(result.isSuccessful)
        assertEquals(authResponse, result.body())
    }

    @Test
    fun `login should return error response on failure`() = runTest {
        // Given
        val username = "testuser"
        val password = "wrongpass"
        val errorBody = "Unauthorized".toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<AuthResponse>(401, errorBody)
        `when`(api.login(LoginRequest(username, password))).thenReturn(errorResponse)

        // When
        val result = repository.login(username, password)

        // Then
        assertFalse(result.isSuccessful)
        assertEquals(401, result.code())
    }

    @Test
    fun `register should return success response`() = runTest {
        // Given
        val username = "newuser"
        val password = "newpass"
        val email = "new@example.com"
        val authResponse = AuthResponse(2, username, "token")
        `when`(api.register(RegisterRequest(username, password, email)))
            .thenReturn(Response.success(authResponse))

        // When
        val result = repository.register(username, password, email)

        // Then
        assertTrue(result.isSuccessful)
        assertEquals(authResponse, result.body())
    }

    @Test
    fun `register should return error response on conflict`() = runTest {
        // Given
        val username = "existinguser"
        val password = "newpass"
        val email = "new@example.com"
        val errorBody = "User already exists".toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<AuthResponse>(409, errorBody)
        `when`(api.register(RegisterRequest(username, password, email))).thenReturn(errorResponse)

        // When
        val result = repository.register(username, password, email)

        // Then
        assertFalse(result.isSuccessful)
        assertEquals(409, result.code())
    }

    @Test
    fun `getCurrentUser should return user data`() = runTest {
        // Given
        val user = User(1, "testuser", "2024-01-01", "Bio", null, 10)
        `when`(api.getCurrentUser()).thenReturn(Response.success(user))

        // When
        val result = repository.getCurrentUser()

        // Then
        assertTrue(result.isSuccessful)
        assertEquals(user, result.body())
    }

    @Test
    fun `getUserByUsername should return user data`() = runTest {
        // Given
        val username = "testuser"
        val user = User(1, username, "2024-01-01", "Bio", null, 10)
        `when`(api.getUserByUsername(username)).thenReturn(Response.success(user))

        // When
        val result = repository.getUserByUsername(username)

        // Then
        assertTrue(result.isSuccessful)
        assertEquals(user, result.body())
    }

    @Test
    fun `searchUsers should return list of users`() = runTest {
        // Given
        val query = "test"
        val users = listOf(
            User(1, "testuser1", "2024-01-01", "Bio 1", null, 10),
            User(2, "testuser2", "2024-01-01", "Bio 2", null, 5)
        )
        `when`(api.searchUsers(query)).thenReturn(Response.success(users))

        // When
        val result = repository.searchUsers(query)

        // Then
        assertTrue(result.isSuccessful)
        assertEquals(2, result.body()?.size)
    }
}
