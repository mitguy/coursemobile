package edu.corp.glitch.data.api

import edu.corp.glitch.data.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface GlitchApiService {
    // Auth
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): Response<AuthResponse>

    @GET("api/auth")
    suspend fun getAuth(): Response<AuthInfo>

    @POST("api/auth/password")
    suspend fun updatePassword(
        @Body request: PasswordUpdateRequest,
    ): Response<Unit>

    @POST("api/auth/email")
    suspend fun updateEmail(
        @Body request: EmailUpdateRequest,
    ): Response<AuthInfo>

    @DELETE("api/auth")
    suspend fun deleteAccount(): Response<AuthInfo>

    // Users
    @GET("api/users")
    suspend fun getCurrentUser(): Response<User>

    @GET("api/users/{username}")
    suspend fun getUserByUsername(
        @Path("username") username: String,
    ): Response<User>

    @POST("api/users/update")
    suspend fun updateUser(
        @Body request: UpdateUserRequest,
    ): Response<User>

    @Multipart
    @POST("api/users/upload")
    suspend fun uploadProfilePic(
        @Part profilePic: MultipartBody.Part,
    ): Response<User>

    // Streams
    @GET("api/streams")
    suspend fun getCurrentStream(): Response<Stream>

    @GET("api/streams/live")
    suspend fun getLiveStreams(): Response<List<Stream>>

    @GET("api/streams/{username}")
    suspend fun getStreamByUsername(
        @Path("username") username: String,
    ): Response<Stream>

    @POST("api/streams/update")
    suspend fun updateStream(
        @Body request: UpdateStreamRequest,
    ): Response<Stream>

    // Follows
    @GET("api/follows")
    suspend fun getFollows(): Response<List<Follow>>

    @GET("api/follows/{to}")
    suspend fun checkFollow(
        @Path("to") to: Int,
    ): Response<Follow>

    @GET("api/follows/count/{id}")
    suspend fun countFollowers(
        @Path("id") id: Int,
    ): Response<Long>

    @GET("api/follows/live")
    suspend fun getLiveFollows(): Response<List<Follow>>

    @POST("api/follows/create")
    suspend fun createFollow(
        @Body request: FollowRequest,
    ): Response<Follow>

    @POST("api/follows/delete")
    suspend fun deleteFollow(
        @Body request: FollowRequest,
    ): Response<Unit>
}
