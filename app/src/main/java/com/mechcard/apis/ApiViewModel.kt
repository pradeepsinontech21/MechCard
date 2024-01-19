package com.mechcard.apis

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mechcard.models.AWSConfigResponse
import com.mechcard.models.ActionResult
import com.mechcard.models.ClockRequest
import com.mechcard.models.ClockResponse
import com.mechcard.models.ConfigData
import com.mechcard.models.DateAndTime
import com.mechcard.models.ImageS3Request
import com.mechcard.models.ImageS3Response
import com.mechcard.models.JobData
import com.mechcard.models.JobListResponse
import com.mechcard.models.JobResponse
import com.mechcard.models.Mechanic
import com.mechcard.models.MechanicFaceRequest
import com.mechcard.models.MechanicsResponse
import com.mechcard.models.PendingJobModel
import com.mechcard.models.PendingJobs
import com.mechcard.models.Service
import com.mechcard.models.ServiceResponse
import com.mechcard.models.SignInResponse
import com.mechcard.models.TaskData
import com.mechcard.models.TimeResponse
import com.mechcard.models.TokenResponse
import com.mechcard.models.UploadedData
import com.mechcard.pref.MechCardPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.RowId

class ApiViewModel : ViewModel() {
    private val repository = ApiRepository()

    fun getToken(clientId: String, clientSecret: String, result: (TokenResponse?) -> Unit) {
        val call = repository.getToken(clientId, clientSecret)
        call.enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    // Store the access token and refresh token using Kotpref
                    MechCardPref.accessToken = tokenResponse?.accessToken
                    MechCardPref.refreshToken = tokenResponse?.refreshToken

                    Log.e("Token Response Body",tokenResponse.toString())
                    Log.e("Access Token",tokenResponse?.accessToken.toString())
                    Log.e("Refresh Token",tokenResponse?.refreshToken.toString())
                    result.invoke(tokenResponse)
                    // Do something with the token response
                } else {
                    // Handle the API call failure
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                // Handle the API call failure
                t.printStackTrace()
            }
        })
    }

    fun signIn(
        accessToken: String?,
        refreshToken: String?,
        faceId: String,
        result: (SignInResponse?) -> Unit
    ) {
        val call = repository.signIn(accessToken, refreshToken, faceId)

        call.enqueue(object : Callback<SignInResponse> {
            override fun onResponse(
                call: Call<SignInResponse>,
                response: Response<SignInResponse>
            ) {
                if (response.isSuccessful) {
                    val signInResponse = response.body()

                    Log.e("Login Response",signInResponse.toString())
                    MechCardPref.signedInMechanic = signInResponse
                    result.invoke(signInResponse)
                } else {
                    result.invoke(null)
                }
            }

            override fun onFailure(call: Call<SignInResponse>, t: Throwable) {
                result.invoke(null)
            }
        })
    }

    fun getMechanics(
        pageSize: Int,
        pageNo: Int,
        orderBy: String,
        sortOrder: String,
        searchKeyword: String,
        bearerToken: String,
        result: (List<Mechanic>) -> Unit
    ) {

        val call = repository.getMechanics(
            pageSize,
            pageNo,
            orderBy,
            sortOrder,
            searchKeyword,
            bearerToken
        )
        call.enqueue(object : Callback<MechanicsResponse> {
            override fun onResponse(
                call: Call<MechanicsResponse>,
                response: Response<MechanicsResponse>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val mechanicsResponse = response.body()

                            result.invoke(mechanicsResponse?.mechanicList.orEmpty())
                        } else {
                            result.invoke(listOf())
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<MechanicsResponse>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(listOf())
                    }
                }
            }
        })
    }

    fun getJobList(
        id: String,
        searchKeyword: String,
        pageSize: Int,
        pageNo: Int,
        orderBy: String,
        sortOrder: String,
        mechanicToken: String,
        result: (List<JobData>) -> Unit
    ) {

        val call = repository.getJobList(
            id,
            searchKeyword,
            pageSize,
            pageNo,
            orderBy,
            sortOrder,
            mechanicToken
        )
        call.enqueue(object : Callback<JobListResponse> {
            override fun onResponse(
                call: Call<JobListResponse>,
                response: Response<JobListResponse>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val mechanicsResponse = response.body()
                            Log.e("Job List",mechanicsResponse.toString())
                            result.invoke(mechanicsResponse?.jobListData.orEmpty())
                        } else {
                            result.invoke(listOf())
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<JobListResponse>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(listOf())
                    }
                }
            }
        })
    }

    fun getJobData(
        id: String,
        mechanicToken: String,
        result: (JobData?) -> Unit
    ) {

        val call = repository.getJobData(
            id,
            mechanicToken
        )
        call.enqueue(object : Callback<JobResponse> {
            override fun onResponse(
                call: Call<JobResponse>,
                response: Response<JobResponse>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val mechanicsResponse = response.body()
                            result.invoke(mechanicsResponse?.jobData)
                        } else {
                            result.invoke(null)
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<JobResponse>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(null)
                    }
                }
            }
        })
    }


    fun getServices(
        jobId: String,
        searchKeyword: String,
        pageSize: Int,
        pageNo: Int,
        orderBy: String,
        sortOrder: String,
        mechanicToken: String,
        result: (List<Service>) -> Unit
    ) {

        val call = repository.getServices(
            jobId,
            searchKeyword,
            pageSize,
            pageNo,
            orderBy,
            sortOrder,
            mechanicToken
        )
        call.enqueue(object : Callback<ServiceResponse> {
            override fun onResponse(
                call: Call<ServiceResponse>,
                response: Response<ServiceResponse>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val mechanicsResponse = response.body()
                            result.invoke(
                                mechanicsResponse?.serviceList.orEmpty()
                                    .filterNot { it.serviceName.isBlank() })
                        } else {
                            result.invoke(listOf())
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ServiceResponse>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(listOf())
                    }
                }
            }
        })
    }

    fun getTime(
        mechanicToken: String,
        result: (DateAndTime?) -> Unit
    ) {

        val call = repository.getTime(
            mechanicToken
        )
        call.enqueue(object : Callback<TimeResponse> {
            override fun onResponse(
                call: Call<TimeResponse>,
                response: Response<TimeResponse>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val mechanicsResponse = response.body()
                            result.invoke(mechanicsResponse?.dateAndTime)
                        } else {
                            result.invoke(null)
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<TimeResponse>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(null)
                    }
                }
            }
        })
    }

    fun clockIn(
        request: ClockRequest,
        bearerToken: String,
        result: (ClockResponse?) -> Unit
    ) {

        val call = repository.clockIn(request, bearerToken)
        call.enqueue(object : Callback<ClockResponse> {
            override fun onResponse(
                call: Call<ClockResponse>,
                response: Response<ClockResponse>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val mechanicsResponse = response.body()
                            result.invoke(mechanicsResponse)
                        } else {
                            result.invoke(null)
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ClockResponse>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(null)
                    }
                }
            }
        })
    }

    fun pause(
        request: ClockRequest,
        bearerToken: String,
        result: (ClockResponse?) -> Unit
    ) {

        val call = repository.pause(request, bearerToken)
        call.enqueue(object : Callback<ClockResponse> {
            override fun onResponse(
                call: Call<ClockResponse>,
                response: Response<ClockResponse>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val mechanicsResponse = response.body()
                            result.invoke(mechanicsResponse)
                        } else {
                            result.invoke(null)
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ClockResponse>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(null)
                    }
                }
            }
        })
    }

    fun resume(
        request: ClockRequest,
        bearerToken: String,
        result: (ClockResponse?) -> Unit
    ) {

        val call = repository.resume(request, bearerToken)
        call.enqueue(object : Callback<ClockResponse> {
            override fun onResponse(
                call: Call<ClockResponse>,
                response: Response<ClockResponse>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val mechanicsResponse = response.body()
                            result.invoke(mechanicsResponse)
                        } else {
                            result.invoke(null)
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ClockResponse>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(null)
                    }
                }
            }
        })
    }

    fun clockOut(
        request: ClockRequest,
        bearerToken: String,
        result: (ClockResponse?) -> Unit
    ) {

        val call = repository.clockOut(request, bearerToken)
        call.enqueue(object : Callback<ClockResponse> {
            override fun onResponse(
                call: Call<ClockResponse>,
                response: Response<ClockResponse>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val mechanicsResponse = response.body()
                            result.invoke(mechanicsResponse)
                        } else {
                            result.invoke(null)
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ClockResponse>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(null)
                    }
                }
            }
        })
    }

    fun uploadToS3(
        request: ImageS3Request,
        bearerToken: String,
        result: (UploadedData?) -> Unit
    ) {

        val call = repository.uploadToS3(request, bearerToken)
        call.enqueue(object : Callback<ImageS3Response> {
            override fun onResponse(
                call: Call<ImageS3Response>,
                response: Response<ImageS3Response>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val response = response.body()
                            result.invoke(response?.uploadeddata)
                        } else {
                            result.invoke(null)
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ImageS3Response>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(null)
                    }
                }
            }
        })
    }

    fun deleteFromS3(
        bearerToken: String
    ) {

        val call = repository.deleteImageToS3(bearerToken)
        call.enqueue(object : Callback<Any> {
            override fun onResponse(
                call: Call<Any>,
                response: Response<Any>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val response = response.body()
//                            result.invoke(response)
                        } else {
//                            result.invoke(null)
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
//                        result.invoke(null)
                    }
                }
            }
        })
    }


    fun addMechanicFace(
        request: MechanicFaceRequest,
        bearerToken: String,
        result: (MechanicsResponse?, ActionResult?) -> Unit
    ) {

        val call = repository.addMechanicFace(request, bearerToken)
        call.enqueue(object : Callback<MechanicsResponse> {
            override fun onResponse(
                call: Call<MechanicsResponse>,
                response: Response<MechanicsResponse>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val response = response.body()
                            result.invoke(response, response?.actionresult)
                        } else {
                            result.invoke(null, response.body()?.actionresult)
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<MechanicsResponse>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(null, null)
                    }
                }
            }
        })
    }

    fun getAWSConfigData(
        mechanicToken: String,
        result: (ConfigData?) -> Unit
    ) {

        val call = repository.getAwsConfigData(
            mechanicToken
        )
        call.enqueue(object : Callback<AWSConfigResponse> {
            override fun onResponse(
                call: Call<AWSConfigResponse>,
                response: Response<AWSConfigResponse>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val mechanicsResponse = response.body()
                            result.invoke(mechanicsResponse?.configdata)
                        } else {
                            result.invoke(null)
                            // Handle the API call failure
                        }
                    }
                }
            }

            override fun onFailure(call: Call<AWSConfigResponse>, t: Throwable) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(null)
                    }
                }
            }
        })
    }


    fun getPendingJobList(
        pageSize: Int,
        pageNo: Int,
        orderBy: String,
        sortOrder: String,
        jobId: String,
        vehicleId:String,
        customerName:String,
        status: String,
        mechanicToken: String,
        result: (PendingJobModel?) -> Unit
    ) {

        val call = repository.getPendingjobsList(
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
        call.enqueue(object : Callback<PendingJobModel> {
            override fun onResponse(
                call: Call<PendingJobModel>,
                response: Response<PendingJobModel>
            ) {
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val mechanicsResponse = response.body()
                            Log.e("Job List",mechanicsResponse.toString())
                            result.invoke(response.body())
                        } else {
                            result.invoke(null)
                            // Handle the API call failure
                        }
                    }
                }
            }
            override fun onFailure(call: Call<PendingJobModel>, t: Throwable) {
                Log.e("Failure pending job",t.message.toString())
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        result.invoke(null)
                    }
                }
            }
        })
    }
}
