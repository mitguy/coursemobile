package edu.corp.glitch.data.models

data class ChatMessage(
    val id: Int,
    val username: String,
    val at: String,
    val message: String,
)
