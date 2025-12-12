package edu.corp.glitch.data.models

data class Follow(
    val id: Int,
    val from: Int,
    val to: Int,
    val followedAt: String,
    val toUser: User?,
    val toStream: Stream?,
)
