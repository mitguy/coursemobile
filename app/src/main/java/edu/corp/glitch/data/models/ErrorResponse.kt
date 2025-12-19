package edu.corp.glitch.data.models

data class ErrorResponse(
    val path: String?,
    val error: String?,
    val message: String?,
    val timestamp: String?,
    val status: Int?,
)
