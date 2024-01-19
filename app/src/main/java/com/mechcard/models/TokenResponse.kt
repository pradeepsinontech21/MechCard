package com.mechcard.models

data class TokenResponse(
    val accessToken: String,
    val expiresIn: String,
    val refreshToken: String,
    val traceid: String
)

