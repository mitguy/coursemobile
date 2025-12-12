package edu.corp.glitch.data.models

data class User(
    val id: Int,
    val username: String,
    val createdAt: String,
    val bio: String?,
    val profilePic: ByteArray?,
    val followersCount: Long,
)
