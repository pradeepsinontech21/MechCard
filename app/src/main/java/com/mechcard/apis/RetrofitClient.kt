package com.mechcard.apis

import android.util.Log
import com.mechcard.apis.Constants.BASE_URL
import com.mechcard.apis.Constants.DEV_CLIENT_ID
import com.mechcard.apis.Constants.DEV_SECRET
import com.mechcard.pref.MechCardPref
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


object RetrofitClient {

//    private val badRequestInterceptor = object : Interceptor {
//        override fun intercept(chain: Interceptor.Chain): Response {
//            val original = chain.request()
//            val result = chain.proceed(original)
//
//            if (result.code == 400) {
//                val newTokenResponse =
//                    apiService.getToken(DEV_CLIENT_ID, DEV_SECRET).execute()
//                if (newTokenResponse.isSuccessful) {
//                    val newToken = newTokenResponse.body()?.accessToken ?: ""
//                    val newRefreshToken = newTokenResponse.body()?.refreshToken ?: ""
//                    // Store new tokens
//                    MechCardPref.accessToken = newToken
//                    MechCardPref.refreshToken = newRefreshToken
//                    result.close()
//                    return chain.proceed(
//                        original.newBuilder()
//                            .header("Authorization", "Bearer $newToken")
//                            .build()
//                    )
//                }
//            }
//
//            return result
//        }
//    }

    private val authenticator = object : Authenticator {
        @Throws(IOException::class)
        override fun authenticate(route: Route?, response: Response): Request? {
            Log.e("RetrofitClient", "authenticate: ${response.code}")
            if (response.code == 401) {
                val accessToken = MechCardPref.accessToken
                val refreshToken = MechCardPref.refreshToken

                if (accessToken != null && refreshToken != null) {

                    val newTokenResponse =
                        apiService.refreshToken(accessToken, refreshToken).execute()

                    if (newTokenResponse.isSuccessful) {
                        val newToken = newTokenResponse.body()?.accessToken ?: ""
                        val newRefreshToken = newTokenResponse.body()?.refreshToken ?: ""
                        // Store new tokens
                        MechCardPref.accessToken = newToken
                        MechCardPref.refreshToken = newRefreshToken

                        return response.request.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .build()
                    }
                }
            }
            return response.request
        }
    }

    private val retrofit: Retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        Retrofit.Builder()
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
//                    .addInterceptor(badRequestInterceptor)
                    .authenticator(authenticator)
                    .build()
            )
            .baseUrl(BASE_URL)
//            .baseUrl(MechCardPref.apiEndPoint.orEmpty())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService
        get() = retrofit.create(ApiService::class.java)
}
