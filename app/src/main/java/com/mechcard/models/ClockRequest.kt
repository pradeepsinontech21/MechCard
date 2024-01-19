package com.mechcard.models

data class ClockRequest(
    val taskID: String?,
    val jobID: String,
    val serviceID: String,
    val serviceName: String
)

data class ImageS3Request(
    val mechanicID: String?,
    val base64Image: String,
    val extension: String
)


data class MechanicFaceRequest(
    val mechanicID: String?,
    val faceID: String,
    val pin: String
)