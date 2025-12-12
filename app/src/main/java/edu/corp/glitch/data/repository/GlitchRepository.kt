package edu.corp.glitch.data.repository

import edu.corp.glitch.data.api.*
import edu.corp.glitch.data.models.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlitchRepository
    @Inject
    constructor(
        private val api: GlitchApiService,
    ) {
        suspend fun register(
            username: String,
            password: String,
            email: String,
        ) = api.register(RegisterRequest(username, password, email))

        suspend fun login(
            username: String,
            password: String,
        ) = api.login(LoginRequest(username, password))

        suspend fun getAuth() = api.getAuth()

        suspend fun updatePassword(
            old: String,
            new: String,
        ) = api.updatePassword(PasswordUpdateRequest(old, new))

        suspend fun updateEmail(email: String) = api.updateEmail(EmailUpdateRequest(email))

        suspend fun deleteAccount() = api.deleteAccount()

        suspend fun getCurrentUser() = api.getCurrentUser()

        suspend fun getUserByUsername(username: String) = api.getUserByUsername(username)

        suspend fun updateUser(bio: String) = api.updateUser(UpdateUserRequest(bio))

        suspend fun uploadProfilePic(file: File): retrofit2.Response<User> {
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("profilePic", file.name, requestFile)
            return api.uploadProfilePic(body)
        }

        suspend fun getLiveStreams() = api.getLiveStreams()

        suspend fun getStreamByUsername(username: String) = api.getStreamByUsername(username)

        suspend fun getLiveFollows() = api.getLiveFollows()

        suspend fun getFollows() = api.getFollows()

        suspend fun createFollow(userId: Int) = api.createFollow(FollowRequest(userId))

        suspend fun deleteFollow(userId: Int) = api.deleteFollow(FollowRequest(userId))

        suspend fun checkFollow(userId: Int) = api.checkFollow(userId)
    }
