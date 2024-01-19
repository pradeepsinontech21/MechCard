package com.mechcard.models

data class ServiceResponse(
    val actionresult: ActionResult,
    val mechanic: Mechanic,
    val serviceList: List<Service>
)