package com.mechcard.models
import com.google.gson.annotations.SerializedName
data class SignInResponse(
    val mechanicaccessToken: String,
    val expiresIn: String,
    val refreshToken: String,
    val mechanicid: String,
    val mechanicName: String,
    val traceid: String,
    val taskData: TaskData?,
//    @field:SerializedName("role")
//    val role: String? = ""
    val role:String
)