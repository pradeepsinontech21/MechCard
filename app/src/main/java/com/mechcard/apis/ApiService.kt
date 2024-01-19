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
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("api/v1/Authentication/Token")
    fun getToken(
        @Field("clientID") clientID: String,
        @Field("clientSecret") clientSecret: String
    ): Call<TokenResponse>

    @FormUrlEncoded
    @POST("api/v1/Authentication/RefreshToken")
    fun refreshToken(
        @Field("AccessToken") accessToken: String,
        @Field("RefreshToken") refreshToken: String
    ): Call<TokenResponse>

    @FormUrlEncoded
    @POST("api/v1/Login/Signin")
    fun signIn(
        @Header("Authorization") bearerToken: String,
        @Field("AccessToken") accessToken: String?,
        @Field("RefreshToken") refreshToken: String?,
        @Field("FaceID") faceID: String,
    ): Call<SignInResponse>

    @GET("api/v1/Mechanics/Mechanics")
    fun getMechanics(
        @Query("pageSize") pageSize: Int,
        @Query("pageNo") pageNo: Int,
        @Query("orderBy") orderBy: String,
        @Query("sortOrder") sortOrder: String,
        @Query("SearchkeyWord") searchKeyword: String,
        @Header("Authorization") bearerToken: String
    ): Call<MechanicsResponse>

    @GET("api/v1/Jobs/JobList")
    fun getJobList(
        @Query("id") id: String,
        @Query("SearchkeyWord") searchKeyword: String,
        @Query("PageSize") pageSize: Int,
        @Query("PageNo") pageNo: Int,
        @Query("OrderBy") orderBy: String,
        @Query("SortOrder") sortOrder: String,
        @Header("Authorization") mechanicToken: String
    ): Call<JobListResponse>

    @GET("api/v1/Jobs/Job")
    fun getJob(
        @Query("id") jobId: String,
        @Header("Authorization") mechanicToken: String
    ): Call<JobResponse>

    @GET("api/v1/Jobs/Services")
    fun getServices(
        @Query("JobID") jobId: String,
        @Query("SearchkeyWord") searchKeyword: String,
        @Query("PageSize") pageSize: Int,
        @Query("PageNo") pageNo: Int,
        @Query("OrderBy") orderBy: String,
        @Query("SortOrder") sortOrder: String,
        @Header("Authorization") mechanicToken: String
    ): Call<ServiceResponse>

    @GET("api/v1/Jobs/Time")
    fun getTime(
        @Header("Authorization") mechanicToken: String
    ): Call<TimeResponse>

    @POST("api/v1/Jobs/Clockin")
    fun clockIn(
        @Body request: ClockRequest,
        @Header("Authorization") bearerToken: String
    ): Call<ClockResponse>

    @POST("api/v1/Jobs/Pause")
    fun pause(
        @Body request: ClockRequest,
        @Header("Authorization") bearerToken: String
    ): Call<ClockResponse>

    @POST("api/v1/Jobs/Resume")
    fun resume(
        @Body request: ClockRequest,
        @Header("Authorization") bearerToken: String
    ): Call<ClockResponse>

    @POST("api/v1/Jobs/Clockout")
    fun clockOut(
        @Body request: ClockRequest,
        @Header("Authorization") bearerToken: String
    ): Call<ClockResponse>

    @POST("api/v1/Images/UploadImagetoS3")
    fun uploadImageToS3(
        @Body request: ImageS3Request,
        @Header("Authorization") bearerToken: String
    ): Call<ImageS3Response>

    @POST("api/v1/Images/clear")
    fun deleteImageToS3(
        @Header("Authorization") bearerToken: String
    ): Call<Any>

    @GET("api/v1/Images/awsconfig")
    fun getAwsConfig(
        @Header("Authorization") mechanicToken: String
    ): Call<AWSConfigResponse>

    @POST("api/v1/Mechanics/Addface")
    fun addMechanicFace(
        @Body request: MechanicFaceRequest,
        @Header("Authorization") bearerToken: String
    ): Call<MechanicsResponse>


    @GET("api/v1/Jobs/Pending")
    fun getPendingJobList(
        @Query("PageSize") pageSize: Int,
        @Query("PageNo") pageNo: Int,
        @Query("OrderBy") orderBy: String,
        @Query("SortOrder") sortOrder: String,
        @Query("jobid") jobID: String,
        @Query("vehicleid") vehicleID: String,
        @Query("customername") customerName: String,
        @Query("status") status: String,
        @Header("Authorization") mechanicToken: String
    ): Call<PendingJobModel>
}
