package com.mechcard.models

data class TaskData(
    val taskID: String?,
    val jobID: String?,
    val mechanicID: String?,
    val mechanicName: String?,
    val serviceID: String?,
    val serviceName: String?,
    val status: String?,
    val clockinDateTime: String?,
    val clockOutDateTime: String?,
    val pausedDateTime: String?,
    val currentDateTime: String?,
    val lastStartOrResumedTime: String?,
    val lastPausedDateTime: String?,
    val resumedDateTime: String?,
    val runningTimeinMinsUntilLastStartorResume: String?
)