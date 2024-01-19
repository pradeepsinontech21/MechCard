package com.mechcard

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Handler.Callback
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.livinglifetechway.k4kotlin.core.toastNow
import com.mechcard.apis.ApiViewModel
import com.mechcard.apis.Constants.DEV_CLIENT_ID
import com.mechcard.apis.Constants.DEV_SECRET
import com.mechcard.databinding.ActivityMainBinding
import com.mechcard.pref.MechCardPref
import com.mechcard.ui.`interface`.InactivityListener
import com.mechcard.utils.face.CaptureListener

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ApiViewModel
    private lateinit var mBinding: ActivityMainBinding
    private var inactivityListener: InactivityListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
//        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(ApiViewModel::class.java)

//        if (MechCardPref.accessToken == null || MechCardPref.refreshToken == null) {
//            viewModel.getToken(DEV_CLIENT_ID, DEV_SECRET)
//        }
        viewModel.getToken(DEV_CLIENT_ID, DEV_SECRET){

        }

//        val navHostFragment = mBinding.navigationAuthContainer.getFragment() as NavHostFragment
//        val graphInflater = navHostFragment.navController.navInflater
//        val navGraph = graphInflater.inflate(R.navigation.navigation_main)
//        val navController = navHostFragment.navController
///
//        val destination =
//            if (MechCardPref.isUserLogIn) R.id.jobsFragment else R.id.captureFaceFragment
//        navGraph.setStartDestination(destination)
//        navController.graph = navGraph
    }

    companion object {
        const val DISCONNECT_TIMEOUT: Long = 1888770000 // 5 min = 5 * 60 * 1000 ms
    }


    private val disconnectHandler: Handler = Handler {
        TODO("Not yet implemented")
    }

    private val disconnectCallback = Runnable {
        // Perform any required operation on disconnect
        runOnUiThread {
            inactivityListener?.onInactivityCallback()
        }
    }

    fun resetDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback)
        disconnectHandler.postDelayed(disconnectCallback, DISCONNECT_TIMEOUT)
    }

    private fun stopDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback)
    }

    override fun onUserInteraction() {
        resetDisconnectTimer()
    }

    override fun onResume() {
        super.onResume()
        resetDisconnectTimer()
    }

    override fun onStop() {
        super.onStop()
        stopDisconnectTimer()
    }

    fun setInactivityListener(listener: InactivityListener) {
        this.inactivityListener = listener
    }
}