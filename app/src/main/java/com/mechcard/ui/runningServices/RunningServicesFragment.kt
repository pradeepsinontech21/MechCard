package com.mechcard.ui.runningServices

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.livinglifetechway.k4kotlin.core.androidx.toastNow
import com.livinglifetechway.k4kotlin.core.hide
import com.livinglifetechway.k4kotlin.core.orZero
import com.livinglifetechway.k4kotlin.core.show
import com.mechcard.MainActivity
import com.mechcard.R
import com.mechcard.apis.ApiViewModel
import com.mechcard.databinding.FragmentManageServicesBinding
import com.mechcard.databinding.FragmentRunningServicesBinding
import com.mechcard.databinding.ItemServiceDropDownBinding
import com.mechcard.models.ClockRequest
import com.mechcard.models.Service
import com.mechcard.models.TaskData
import com.mechcard.pref.MechCardPref
import com.mechcard.ui.custom.MechProgressDialog
import com.mechcard.ui.`interface`.InactivityListener
import com.mechcard.ui.services.ManageServicesFragmentArgs
import com.mechcard.ui.services.RunningServicesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RunningServicesFragment : Fragment() {

    companion object {
        fun newInstance() = RunningServicesFragment()
    }

    private lateinit var viewModel: RunningServicesViewModel
    private lateinit var apiViewModel: ApiViewModel
    private lateinit var mBinding: FragmentRunningServicesBinding

//    private val args by navArgs<ManageServicesFragmentArgs>()

    private lateinit var adapter: ArrayAdapter<Service>

    private var serviceList = ArrayList<Service>()
    private var selectedService: Service? = null
    private var selectedTask: TaskData? = null

    private var isTimerStarted = false
    private var serverTimeDelta = 0L
    private var clockInDateAndTime = 0L
    private var currentDateTime = 0L
    private var lastPausedDateAndTime = 0L
    private var lastStartOrResumedTime = 0L
    private var runningTimeinMinsUntilLastStartorResume = 0L
    private var clockOutDateTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_running_services, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(RunningServicesViewModel::class.java)
        apiViewModel = ViewModelProvider(this).get(ApiViewModel::class.java)

        selectedTask = MechCardPref.signedInMechanic?.taskData
        getJobList(selectedTask?.jobID.orEmpty())

        mBinding.tvMechanicName.text =
            "${MechCardPref.signedInMechanic?.mechanicName} (${MechCardPref.signedInMechanic?.mechanicid})"


        apiViewModel.getTime("Bearer ${MechCardPref.signedInMechanic?.mechanicaccessToken}") { dateAndTime ->
            dateAndTime?.let {
                try {
                    val date =
                        SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse("${dateAndTime.date} ${dateAndTime.time}")
                    serverTimeDelta = System.currentTimeMillis() - date.time
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        mBinding.textBack.setOnClickListener {
            findNavController().navigate(R.id.action_runningServicesFragment_to_jobsFragment)
        }

        mBinding.actService.setOnFocusChangeListener { view, b ->
            mBinding.actService.showDropDown()
        }

        mBinding.actService.setOnClickListener { view ->
            mBinding.actService.showDropDown()
        }

        mBinding.actService.setOnItemClickListener { adapterView, view, i, l ->
            selectedService = serviceList.get(i)
            selectedService?.let {
                if (it.clockinDateTime.isNotEmpty()) {
                    clockInDateAndTime =
                        SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(it.clockinDateTime)?.time ?: System.currentTimeMillis()
                }
                if (it.currentDateTime.isNotEmpty()) {
                    currentDateTime =
                        SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(it.currentDateTime)?.time ?: System.currentTimeMillis()
                }

                if (it.lastPausedDateTime.isNotEmpty()) {
                    lastPausedDateAndTime =
                        SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(it.lastPausedDateTime)?.time ?: System.currentTimeMillis()
                }
                if (it.lastStartOrResumedTime.isNotEmpty()) {
                    lastStartOrResumedTime =
                        SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(it.lastStartOrResumedTime)?.time ?: System.currentTimeMillis()
                }
                if (it.clockOutDateTime.isNotEmpty()) {
                    clockOutDateTime =
                        SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(it.clockOutDateTime)?.time ?: System.currentTimeMillis()
                }
                if (it.runningTimeinMinsUntilLastStartorResume.isNotEmpty() && it.runningTimeinMinsUntilLastStartorResume.toInt()
                        .orZero() > 0
                ) {
                    runningTimeinMinsUntilLastStartorResume =
                        it.runningTimeinMinsUntilLastStartorResume.toLong()
                            .orZero() * 60 * 1000
                }
            }

            updateUIForSelectedService()
        }

        /*mBinding.buttonStart.setOnClickListener {
            val dialog = MechProgressDialog(requireContext()).setTitle("Loading...").create()
            dialog.show()
            val request = ClockRequest(
                null,
                selectedTask?.jobID.orEmpty(),
                selectedService?.serviceCode.orEmpty(),
                selectedService?.serviceName.orEmpty()
            )
            apiViewModel.clockIn(
                request,
                "Bearer ${MechCardPref.signedInMechanic?.mechanicaccessToken}"
            ) { taskData ->
                dialog.dismiss()
                taskData?.let {
                    selectedTask = taskData
                    try {
                        clockInDateAndTime =
                            SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(taskData.clockinDateTime)?.time ?: System.currentTimeMillis()
                        getServices()
//                        updateUIForSelectedService()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }*/

        mBinding.buttonEnd.setOnClickListener {
            val dialog = MechProgressDialog(requireContext()).setTitle("Loading...").create()
            dialog.show()
            val request = ClockRequest(
                selectedService?.taskID,
                selectedTask?.jobID.orEmpty(),
                selectedService?.serviceCode.orEmpty(),
                selectedService?.serviceName.orEmpty()
            )
            apiViewModel.clockOut(
                request,
                "Bearer ${MechCardPref.signedInMechanic?.mechanicaccessToken}"
            ) { taskData ->
                dialog.dismiss()
                taskData?.let {
                    if(it.actionresult.status == "0"){
                        selectedTask = taskData.clockoutData
                        try {
//                            clockInDateAndTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(taskData.clockoutData.clockinDateTime)?.time ?: System.currentTimeMillis()
//                            val clockOutDateTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(taskData.clockoutData.clockOutDateTime)?.time ?: System.currentTimeMillis()

                            getServices()
//                        updateUIForSelectedService()
//                            setCountdownTime(clockOutDateTime - clockInDateAndTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }else {
                        toastNow(it.actionresult.message)
                    }

                }
            }
        }

        mBinding.buttonPause.setOnClickListener {
            val dialog = MechProgressDialog(requireContext()).setTitle("Loading...").create()
            dialog.show()
            val request = ClockRequest(
                selectedService?.taskID,
                selectedTask?.jobID.orEmpty(),
                selectedService?.serviceCode.orEmpty(),
                selectedService?.serviceName.orEmpty()
            )
            apiViewModel.pause(
                request,
                "Bearer ${MechCardPref.signedInMechanic?.mechanicaccessToken}"
            ) { taskData ->
                dialog.dismiss()
                taskData?.let {
                    try {
                        if (it.actionresult.status == "0") {
                            selectedTask = it.pausedData
//                            clockInDateAndTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(it.pausedData.clockinDateTime)?.time ?: System.currentTimeMillis()
//                            val pausedDateAndTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(it.pausedData.lastPausedDateTime)?.time ?: System.currentTimeMillis()

                            getServices()
//                        updateUIForSelectedService()
//                            setCountdownTime(pausedDateAndTime - clockInDateAndTime)
                        } else {
                            toastNow(it.actionresult.message)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        mBinding.buttonResume.setOnClickListener {
            val dialog = MechProgressDialog(requireContext()).setTitle("Loading...").create()
            dialog.show()
            val request = ClockRequest(
                selectedService?.taskID,
                selectedTask?.jobID.orEmpty(),
                selectedService?.serviceCode.orEmpty(),
                selectedService?.serviceName.orEmpty()
            )
            apiViewModel.resume(
                request,
                "Bearer ${MechCardPref.signedInMechanic?.mechanicaccessToken}"
            ) { taskData ->
                dialog.dismiss()
                taskData?.let {
                    if (it.actionresult.status == "0") {
                        selectedTask = taskData.resumedData
                        try {
//                            val clockInDateTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(taskData.resumedData.clockinDateTime)?.time ?: System.currentTimeMillis()
//                            val pausedDateTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(taskData.resumedData.lastPausedDateTime)?.time ?: System.currentTimeMillis()
//                            val resumedDateTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(taskData.resumedData.lastStartOrResumedTime)?.time ?: System.currentTimeMillis()

//                            clockInDateAndTime =
//                                resumedDateTime + (pausedDateTime - clockInDateTime)
                            getServices()
//                        updateUIForSelectedService()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        toastNow(it.actionresult.message)
                    }

                }
            }
        }

        mBinding.imageLogout.setOnClickListener {
            MechCardPref.isUserLogIn = false
            MechCardPref.signedInMechanic = null
            MechCardPref.accessToken = null
            MechCardPref.refreshToken = null
            val startDestination = findNavController().graph.startDestinationId
            val navOptions = NavOptions.Builder()
                .setPopUpTo(startDestination, true)
                .build()
            findNavController().navigate(startDestination, null, navOptions)
        }

        mBinding.imageErpLogo

        getServices()
        startTimer()

        (requireActivity() as MainActivity).setInactivityListener(object : InactivityListener {
            override fun onInactivityCallback() {
                (requireActivity() as MainActivity).resetDisconnectTimer()
//                MechCardPref.clear()
                val startDestination = findNavController().graph.startDestinationId
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(startDestination, true)
                    .build()
                findNavController().navigate(startDestination, null, navOptions)
            }
        })
    }

    private fun startTimer() {
        lifecycleScope.launch {
            delay(1000)
            withContext(Dispatchers.Main) {
                if (isTimerStarted) {
                    val time = if (lastPausedDateAndTime > 0) {
                        (System.currentTimeMillis() - (currentDateTime + serverTimeDelta)) + runningTimeinMinsUntilLastStartorResume
                    } else {
                        System.currentTimeMillis() - (lastStartOrResumedTime + serverTimeDelta)
                    }
//                    val time =
//                        System.currentTimeMillis() - (lastStartOrResumedTime + serverTimeDelta) // + runningTimeinMinsUntilLastStartorResume)
                    setCountdownTime(time)
                }

                startTimer()
            }
        }
    }

    private fun setCountdownTime(time: Long) {
        val sec = String.format("%02d", (time / 1000) % 60)
        val min = String.format("%02d", (time / (60 * 1000)) % 60)
        val hour = String.format("%02d", time / (60 * 60 * 1000))
        mBinding.textCountdown.text = "$hour:$min:$sec"

        // Create a SimpleDateFormat instance
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        // Convert milliseconds to a Date object
        val date = Date(time)

        // Format the date and time
        val formattedDate = dateFormat.format(date)
        val formattedTime = timeFormat.format(date)

//        mBinding.textDate.text = formattedDate
//        mBinding.textTime.text = formattedTime
    }

    private fun updateUIForSelectedService() {
        mBinding.tvServiceCode.text = selectedService?.serviceCode

//        when (selectedTask?.status ?: selectedService?.status) {
        when (selectedService?.status) {
            "WORK IN PROGRESS" -> {
                mBinding.textDate.text = selectedService?.clockinDateTime?.substringBefore(" ")
                mBinding.textTime.text = selectedService?.clockinDateTime?.substringAfter(" ")
                isTimerStarted = true
                mBinding.buttonResume.hide()
                mBinding.buttonStart.hide()
                mBinding.viewPauseEnd.show()
//                val time = System.currentTimeMillis() - (clockInDateAndTime + serverTimeDelta)
                val time = if (lastPausedDateAndTime > 0) {
                    (System.currentTimeMillis() - (currentDateTime + serverTimeDelta)) + runningTimeinMinsUntilLastStartorResume
                } else {
                    System.currentTimeMillis() - (lastStartOrResumedTime + serverTimeDelta)
                }
                setCountdownTime(time)
            }

            "PAUSED" -> {
                mBinding.textDate.text = selectedService?.clockinDateTime?.substringBefore(" ")
                mBinding.textTime.text =
                    selectedService?.clockinDateTime?.substringAfter(" ")
                isTimerStarted = false
                mBinding.buttonResume.show()
                mBinding.buttonStart.hide()
                mBinding.viewPauseEnd.hide()
                val time = if (lastPausedDateAndTime > 0) {
                    (System.currentTimeMillis() - (currentDateTime + serverTimeDelta)) + runningTimeinMinsUntilLastStartorResume
                } else {
                    System.currentTimeMillis() - (lastStartOrResumedTime + serverTimeDelta)
                }
                setCountdownTime(time)
            }

            "COMPLETED" -> {
                mBinding.textDate.text = selectedService?.clockinDateTime?.substringBefore(" ")
                mBinding.textTime.text = selectedService?.clockinDateTime?.substringAfter(" ")
                isTimerStarted = false
                mBinding.buttonResume.hide()
                mBinding.buttonStart.hide()
                mBinding.viewPauseEnd.hide()
//                val time =
//                    System.currentTimeMillis() - (clockInDateAndTime + serverTimeDelta)
                val time = (clockOutDateTime - clockInDateAndTime)
                setCountdownTime(runningTimeinMinsUntilLastStartorResume)
            }

            else -> {
                isTimerStarted = false
                mBinding.textDate.text = ""
                mBinding.textTime.text = ""
                mBinding.textCountdown.text = "00:00:00"
                mBinding.buttonResume.hide()
                mBinding.buttonStart.show()
                mBinding.viewPauseEnd.hide()
            }
        }
    }

    private fun getServices() {

        val taskData = MechCardPref.signedInMechanic?.taskData
        val serviceId = taskData?.serviceID

        val dialog = MechProgressDialog(requireContext()).setTitle("Loading...").create()
        dialog.show()
        apiViewModel.getServices(
            selectedTask?.jobID.orEmpty(),
            "",
            200,
            0,
            "",
            "ASC",
            "Bearer ${MechCardPref.signedInMechanic?.mechanicaccessToken}"
        ) { services ->
            dialog.dismiss()
            serviceList.clear()
            serviceList.addAll(services)
            setServiceAdapter()

            var itemPosition: Int = 0
            serviceList.forEach { service ->
                if (service.serviceCode == serviceId) {
                    Log.e(
                        "TAG",
                        "getServices: ${service.clockinDateTime},  ${service.lastPausedDateTime}"
                    )
                    itemPosition = serviceList.indexOf(service)
                    selectedService = service
                    if (service.clockinDateTime.isNotEmpty()) {
                        clockInDateAndTime =
                            SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(service.clockinDateTime)?.time ?: System.currentTimeMillis()
                    }
                    if (service.currentDateTime.isNotEmpty()) {
                        currentDateTime =
                            SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(service.currentDateTime)?.time ?: System.currentTimeMillis()
                    }

                    if (service.lastPausedDateTime.isNotEmpty()) {
                        lastPausedDateAndTime =
                            SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(service.lastPausedDateTime)?.time ?: System.currentTimeMillis()
                    }

                    if (service.lastStartOrResumedTime.isNotEmpty()) {
                        lastStartOrResumedTime =
                            SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(service.lastStartOrResumedTime)?.time ?: System.currentTimeMillis()
                    }

                    if (service.clockOutDateTime.isNotEmpty()) {
                        clockOutDateTime =
                            SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).parse(service.clockOutDateTime)?.time ?: System.currentTimeMillis()
                    }
                    if (service.runningTimeinMinsUntilLastStartorResume.isNotEmpty() && service.runningTimeinMinsUntilLastStartorResume.toLong()
                            .orZero() > 0
                    ) {
                        runningTimeinMinsUntilLastStartorResume =
                            service.runningTimeinMinsUntilLastStartorResume.toLong()
                                .orZero() * 60 * 1000
                    }
                }
            }
            mBinding.actService.setText(selectedService?.serviceName)
            mBinding.tvServiceCode.setText(selectedService?.serviceCode)
            updateUIForSelectedService()
        }

    }

    private fun setServiceAdapter() {

        adapter = object : ArrayAdapter<Service>(
            requireContext(),
            R.layout.item_service_drop_down,
            R.id.tvServiceName,
            serviceList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

                val view = convertView ?: ItemServiceDropDownBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                ).root

                view.findViewById<TextView>(R.id.tvServiceCode)?.text =
                    serviceList.get(position).serviceCode
                view.findViewById<TextView>(R.id.tvServiceName)?.text =
                    serviceList.get(position).serviceName

                if (serviceList.get(position).status == "PAUSED") {
                    view.findViewById<ImageView>(R.id.ivItemPause).show()
                } else {
                    view.findViewById<ImageView>(R.id.ivItemPause).hide()
                }

                return super.getDropDownView(position, view, parent)
            }
        }
        mBinding.actService.setAdapter(adapter)

    }

    private fun getJobList(jobId: String) {

        apiViewModel.getJobData(
            jobId,
            "Bearer ${MechCardPref.signedInMechanic?.mechanicaccessToken}"
        ) { jobs ->
            mBinding.tvJobName.text = jobs?.id
            mBinding.tvVehicleNo.text = jobs?.vehicleRegistrationNo
        }

    }
}