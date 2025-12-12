package edu.corp.glitch.data.models

data class Stream(
    val id: Int,
    val username: String,
    val live: Boolean,
    val title: String?,
    val startedAt: String?,
    val viewers: Int,
)
