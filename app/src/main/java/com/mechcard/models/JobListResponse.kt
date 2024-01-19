package com.mechcard.models

data class JobListResponse(
    val actionresult: ActionResult,
    val jobListData: List<JobData>,
    val mechanic: Mechanic
)

data class JobResponse(
    val actionresult: ActionResult,
    val jobData: JobData,
    val mechanic: Mechanic
)