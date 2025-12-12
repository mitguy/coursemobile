package edu.corp.glitch.data.api

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
)

data class LoginRequest(
    val username: String,
    val password: String,
)

data class PasswordUpdateRequest(
    val old: String,
    val _new: String,
)

data class EmailUpdateRequest(
    val email: String,
)

data class UpdateUserRequest(
    val bio: String,
)

data class UpdateStreamRequest(
    val title: String,
)

data class FollowRequest(
    val to: Int,
)

data class AuthInfo(
    val id: Int,
    val username: String,
    val email: String,
)
