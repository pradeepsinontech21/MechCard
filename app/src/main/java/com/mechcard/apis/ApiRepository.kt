package com.mechcard.apis

import com.mechcard.models.AWSConfigResponse
import com.mechcard.models.ClockRequest
import com.mechcard.models.ClockResponse
import com.mechcard.models.ImageS3Request
import com.mechcard.models.ImageS3Response
import com.mechcard.models.JobListResponse
import com.mechcard.models.JobResponse
import com.mechcard.models.MechanicFaceRequest
import com.mechcard.models.MechanicsResponse
import com.mechcard.models.PendingJobModel
import com.mechcard.models.ServiceResponse
import com.mechcard.models.SignInResponse
import com.mechcard.models.TimeResponse
import com.mechcard.models.TokenResponse
import retrofit2.Call

class ApiRepository {
    private val apiService = RetrofitClient.apiService

    fun getToken(clientId: String, clientSecret: String): Call<TokenResponse> {
        return apiService.getToken(clientId, clientSecret)
    }

    fun signIn(
        accessToken: String?,
        refreshToken: String?,
        faceID: String
    ): Call<SignInResponse> {
        return apiService.signIn("Bearer $accessToken", accessToken, refreshToken, faceID)
//        return apiService.signIn(refreshToken, faceID)
    }

    fun getMechanics(
        pageSize: Int,
        pageNo: Int,
        orderBy: String,
        sortOrder: String,
        searchKeyword: String,
        bearerToken: String
    ): Call<MechanicsResponse> {
        return apiService.getMechanics(
            pageSize,
            pageNo,
            orderBy,
            sortOrder,
            searchKeyword,
            bearerToken
        )
    }

    fun getJobList(
        id: String,
        searchKeyword: String,
        pageSize: Int,
        pageNo: Int,
        orderBy: String,
        sortOrder: String,
        mechanicToken: String
    ): Call<JobListResponse> {
        return apiService.getJobList(
            id,
            searchKeyword,
            pageSize,
            pageNo,
            orderBy,
            sortOrder,
            mechanicToken
        )
    }

    fun getJobData(
        id: String,
        mechanicToken: String
    ): Call<JobResponse> {
        return apiService.getJob(
            id,
            mechanicToken
        )
    }

    fun getServices(
        jobId: String,
        searchKeyword: String,
        pageSize: Int,
        pageNo: Int,
        orderBy: String,
        sortOrder: String,
        mechanicToken: String
    ): Call<ServiceResponse> {
        return apiService.getServices(
            jobId,
            searchKeyword,
            pageSize,
            pageNo,
            orderBy,
            sortOrder,
            mechanicToken
        )
    }

    fun getTime(
        mechanicToken: String
    ): Call<TimeResponse> {
        return apiService.getTime(
            mechanicToken
        )
    }

    fun clockIn(
        request: ClockRequest,
        bearerToken: String
    ): Call<ClockResponse> {
        return apiService.clockIn(request, bearerToken)
    }

    fun pause(
        request: ClockRequest,
        bearerToken: String
    ): Call<ClockResponse> {
        return apiService.pause(request, bearerToken)
    }

    fun resume(
        request: ClockRequest,
        bearerToken: String
    ): Call<ClockResponse> {
        return apiService.resume(request, bearerToken)
    }

    fun clockOut(
        request: ClockRequest,
        bearerToken: String
    ): Call<ClockResponse> {
        return apiService.clockOut(request, bearerToken)
    }

    fun uploadToS3(
        request: ImageS3Request,
        bearerToken: String
    ): Call<ImageS3Response> {
        return apiService.uploadImageToS3(request, bearerToken)
    }

    fun deleteImageToS3(
        bearerToken: String?
    ): Call<Any> {
        return apiService.deleteImageToS3(bearerToken.orEmpty())
    }

    fun getAwsConfigData(
        bearerToken: String
    ): Call<AWSConfigResponse> {
        return apiService.getAwsConfig(bearerToken)
    }

    fun addMechanicFace(
        request: MechanicFaceRequest,
        bearerToken: String
    ): Call<MechanicsResponse> {
        return apiService.addMechanicFace(request, bearerToken)
    }

    fun getPendingjobsList(
        pageSize: Int,
        pageNo: Int,
        orderBy: String,
        sortOrder: String,
        jobId:String,
        vehicleId:String,
        customerName:String,
        status:String,
        mechanicToken: String
    ): Call<PendingJobModel> {
        return apiService.getPendingJobList(
            pageSize,
            pageNo,
            orderBy,
            sortOrder,
            jobId,
            vehicleId,
            customerName,
            status,
            mechanicToken
        )
    }
}
