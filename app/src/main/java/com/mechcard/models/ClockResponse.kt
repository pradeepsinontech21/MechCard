package com.mechcard.models

import com.google.gson.annotations.SerializedName


data class ClockResponse(
    val actionresult: ActionResult,
    val mechanic: Mechanic,
    val taskData: TaskData,
    val pausedData: TaskData,
    val resumedData: TaskData,
    val clockoutData: TaskData,
)

data class ImageS3Response(
    val actionresult: ActionResult,
    val uploadeddata: UploadedData
)

data class UploadedData(
    val bucketname: String,
    val keyname: String
)


data class AWSConfigResponse(
    val actionresult: ActionResult,
    val configdata: ConfigData
)

data class ConfigData(
    val awsAccessKey: String?,
    val awsBucketName: String?,
    val awsIdentityPoolId: String?,
    val awsSecret: String?,
    val projectID: String?
)