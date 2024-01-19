package com.mechcard.models

data class Service(
    val jobID: String,
    val taskID: String,
    val serviceCode: String,
    val serviceName: String,
    val serviceRemarks: String,
    val serviceStandardTimeInHrsMins: String,
    val slno: String,
    val status: String,
    val clockinDateTime: String,
    val lastPausedDateTime: String,
    val resumedDateTime: String,
    val clockOutDateTime: String,
    val currentDateTime: String,
    val lastStartOrResumedTime: String,
    val runningTimeinMinsUntilLastStartorResume: String,

) {
    override fun toString(): String {
        return serviceName
    }
}