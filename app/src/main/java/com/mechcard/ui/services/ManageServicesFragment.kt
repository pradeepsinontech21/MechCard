package com.mechcard.ui.services

import AutoCompleteAdapter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.mechcard.databinding.ItemServiceDropDownBinding
import com.mechcard.models.ClockRequest
import com.mechcard.models.JobData
import com.mechcard.models.Service
import com.mechcard.models.TaskData
import com.mechcard.pref.MechCardPref
import com.mechcard.ui.custom.MechProgressDialog
import com.mechcard.ui.`interface`.InactivityListener
import com.mechcard.ui.runningServices.RunningServicesFragment
import com.mechcard.ui.services.adapter.ManageServiceAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ManageServicesFragment : Fragment() {

    companion object {
        fun newInstance() = RunningServicesFragment()
    }

    private lateinit var viewModel: RunningServicesViewModel
    private lateinit var apiViewModel: ApiViewModel
    private lateinit var mBinding: FragmentManageServicesBinding

    private val args by navArgs<ManageServicesFragmentArgs>()

    private lateinit var adapter: ManageServiceAdapter

    private var serviceList = ArrayList<Service>()
    private var filterserviceList = ArrayList<Service>()
    private var selectedService: Service? = null
    private var selectedTask: TaskData? = null

    private var isTimerStarted = false
    private var serverTimeDelta = 0L
    private var clockInDateAndTime = 0L
    private var lastPausedDateAndTime = 0L
    private var lastStartOrResumedTime = 0L
    private var clockOutDateTime = 0L
    private var currentDateTime = 0L
    private var runningTimeinMinsUntilLastStartorResume = 0L
    private var selectedvalue=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_manage_services, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(RunningServicesViewModel::class.java)
        apiViewModel = ViewModelProvider(this).get(ApiViewModel::class.java)

        mBinding.tvMechanicName.text =
            "${MechCardPref.signedInMechanic?.mechanicName} (${MechCardPref.signedInMechanic?.mechanicid})"
        mBinding.tvJobName.text = args.selectedJob.id
        mBinding.tvVehicleNo.text = args.selectedJob.vehicleRegistrationNo

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
            findNavController().navigateUp()
        }

        mBinding.actService.setOnFocusChangeListener { view, b ->
            mBinding.actService.showDropDown()
        }

        mBinding.actService.setOnClickListener { view ->
            mBinding.actService.showDropDown()

            val selectedserviceList = serviceList.filter { it ->
                it.serviceName.equals(
                    mBinding.actService.text.toString(),
                    false
                )
            }
            if(selectedserviceList.isNotEmpty()){
                selectedserviceList[0].let { selectedJob ->
                    this.selectedService = selectedJob
//                    mBinding.tvVehicleNo.text = selectedJob.vehicleRegistrationNo
//                    mBinding.tvCustName.text = selectedJob.customerName
//                    mBinding.tvJobDesc.text = selectedJob.description
//                    mBinding.buttonSelectJob.isEnabled = true
                }
            }
        }

        mBinding.actService.setDropDownBackgroundDrawable(ColorDrawable(Color.WHITE));

        mBinding.actService.setOnItemClickListener { parent, view, position, l ->
            val entry: Service = parent.getAdapter().getItem(position) as Service
            selectedService = entry
            selectedService?.let {
                if (it.clockinDateTime.isNotEmpty()) {
                    clockInDateAndTime =
                        SimpleDateFormat(
                            "dd-MMM-yyyy HH:mm:ss",
                            Locale.getDefault()
                        ).parse(it.clockinDateTime)?.time ?: 0
                }
                if (it.currentDateTime.isNotEmpty()) {
                    currentDateTime =
                        SimpleDateFormat(
                            "dd-MMM-yyyy HH:mm:ss",
                            Locale.getDefault()
                        ).parse(it.currentDateTime)?.time ?: System.currentTimeMillis()
                }

                if (it.lastPausedDateTime.isNotEmpty()) {
                    lastPausedDateAndTime =
                        SimpleDateFormat(
                            "dd-MMM-yyyy HH:mm:ss",
                            Locale.getDefault()
                        ).parse(it.lastPausedDateTime)?.time ?: System.currentTimeMillis()
                }
                if (it.lastStartOrResumedTime.isNotEmpty()) {
                    lastStartOrResumedTime =
                        SimpleDateFormat(
                            "dd-MMM-yyyy HH:mm:ss",
                            Locale.getDefault()
                        ).parse(it.lastStartOrResumedTime)?.time ?: System.currentTimeMillis()
                }
                if (it.clockOutDateTime.isNotEmpty()) {
                    clockOutDateTime =
                        SimpleDateFormat(
                            "dd-MMM-yyyy HH:mm:ss",
                            Locale.getDefault()
                        ).parse(it.clockOutDateTime)?.time ?: System.currentTimeMillis()
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

        mBinding.buttonStart.setOnClickListener {
            val dialog = MechProgressDialog(requireContext()).setTitle("Loading...").create()
            dialog.show()
            val request = ClockRequest(
                null,
                args.selectedJob.id,
                selectedService?.serviceCode.orEmpty(),
                selectedService?.serviceName.orEmpty()
            )
            apiViewModel.clockIn(
                request,
                "Bearer ${MechCardPref.signedInMechanic?.mechanicaccessToken}"
            ) { taskData ->
                dialog.dismiss()
                taskData?.let {
                    if (it.actionresult.status == "0") {
                        selectedTask = it.taskData
                        try {
//                            clockInDateAndTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(it.taskData.clockinDateTime).time
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

        mBinding.buttonEnd.setOnClickListener {
            val dialog = MechProgressDialog(requireContext()).setTitle("Loading...").create()
            dialog.show()
            val request = ClockRequest(
                selectedService?.taskID,
                args.selectedJob.id,
                selectedService?.serviceCode.orEmpty(),
                selectedService?.serviceName.orEmpty()
            )
            apiViewModel.clockOut(
                request,
                "Bearer ${MechCardPref.signedInMechanic?.mechanicaccessToken}"
            ) { taskData ->
                dialog.dismiss()
                taskData?.let {
                    if (it.actionresult.status == "0") {
                        selectedTask = taskData.clockoutData
                        try {
//                            clockInDateAndTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(taskData.clockoutData.clockinDateTime).time
//                            val clockOutDateTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(taskData.clockoutData.clockOutDateTime).time

                            getServices()
//                        updateUIForSelectedService()
//                            setCountdownTime(clockOutDateTime - clockInDateAndTime)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
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
                args.selectedJob.id,
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
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(it.pausedData.clockinDateTime).time
//                            val pausedDateAndTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(it.pausedData.lastPausedDateTime).time

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
                args.selectedJob.id,
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
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(taskData.resumedData.clockinDateTime).time
//                            val pausedDateTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(taskData.resumedData.lastPausedDateTime).time
//                            val resumedDateTime =
//                                SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(taskData.resumedData.lastStartOrResumedTime).time

//                            clockInDateAndTime = resumedDateTime + (pausedDateTime - clockInDateTime)
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

        mBinding.root.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_MOVE) {

                }

                if(mBinding.actService.hasFocus()){
                    mBinding.actService.clearFocus()
                }
                return false
            }
        })
        mBinding.tvmainservice.setOnClickListener(View.OnClickListener {
            if(mBinding.actService.hasFocus()){
                mBinding.actService.clearFocus()
            }
        })
        mBinding.tvframeservice.setOnClickListener(View.OnClickListener {
            if(mBinding.actService.hasFocus()){
                mBinding.actService.clearFocus()
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
//                setCountdownTime(time)
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
        val dialog = MechProgressDialog(requireContext()).setTitle("Loading...").create()
        dialog.show()
        apiViewModel.getServices(
            args.selectedJob.id,
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

            selectedService?.let {
                serviceList.forEach { service ->
                    if (it.serviceCode == service.serviceCode) {
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
            }
            updateUIForSelectedService()
        }

    }

    private fun setServiceAdapter() {
        adapter = object : ManageServiceAdapter(
            requireContext(),
            R.layout.item_service_drop_down,
            serviceList,
            object:ManageServiceAdapter.OnSelectedItem{
                override fun onSelected(valuedata: Service) {
//                    adapter.notifyDataSetChanged()
                    selectedvalue=valuedata.serviceName
                    selectedService = valuedata
                    selectedService?.let {
                        if (it.clockinDateTime.isNotEmpty()) {
                            clockInDateAndTime =
                                SimpleDateFormat(
                                    "dd-MMM-yyyy HH:mm:ss",
                                    Locale.getDefault()
                                ).parse(it.clockinDateTime)?.time ?: 0
                        }
                        if (it.currentDateTime.isNotEmpty()) {
                            currentDateTime =
                                SimpleDateFormat(
                                    "dd-MMM-yyyy HH:mm:ss",
                                    Locale.getDefault()
                                ).parse(it.currentDateTime)?.time ?: System.currentTimeMillis()
                        }

                        if (it.lastPausedDateTime.isNotEmpty()) {
                            lastPausedDateAndTime =
                                SimpleDateFormat(
                                    "dd-MMM-yyyy HH:mm:ss",
                                    Locale.getDefault()
                                ).parse(it.lastPausedDateTime)?.time ?: System.currentTimeMillis()
                        }
                        if (it.lastStartOrResumedTime.isNotEmpty()) {
                            lastStartOrResumedTime =
                                SimpleDateFormat(
                                    "dd-MMM-yyyy HH:mm:ss",
                                    Locale.getDefault()
                                ).parse(it.lastStartOrResumedTime)?.time ?: System.currentTimeMillis()
                        }
                        if (it.clockOutDateTime.isNotEmpty()) {
                            clockOutDateTime =
                                SimpleDateFormat(
                                    "dd-MMM-yyyy HH:mm:ss",
                                    Locale.getDefault()
                                ).parse(it.clockOutDateTime)?.time ?: System.currentTimeMillis()
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
                } },
            object :ManageServiceAdapter.OnNothingSelectedItem{
                override fun onNothingSelected() {
                    Log.e("Pass Condition :","When nothing compare to any item")
                }

            }

        ) {}
        mBinding.actService.setAdapter(adapter)
        mBinding.actService.setOnFocusChangeListener(View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                //this if condition is true when edittext lost focus...
                //check here for number is larger than 10 or not

                mBinding.actService.setText(this.selectedvalue)
            }
        })

    }

//    private fun setServiceAdapter() {
//
//        adapter = object : ArrayAdapter<Service>(
//            requireContext(),
//            R.layout.item_service_drop_down,
//            R.id.tvServiceName,
//            serviceList
//        ) {
//            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//
//                val view = convertView ?: ItemServiceDropDownBinding.inflate(
//                    layoutInflater,
//                    parent,
//                    false
//                ).root
//
//                view.findViewById<TextView>(R.id.tvServiceCode)?.text =
//                    serviceList.get(position).serviceCode
//                view.findViewById<TextView>(R.id.tvServiceName)?.text =
//                    serviceList.get(position).serviceName
//
//                if (serviceList.get(position).status == "PAUSED") {
//                    view.findViewById<ImageView>(R.id.ivItemPause).show()
//                } else {
//                    view.findViewById<ImageView>(R.id.ivItemPause).hide()
//                }
//
//                return super.getDropDownView(position, view, parent)
//            }
//        }
//        mBinding.actService.setAdapter(adapter)
//
//    }
}