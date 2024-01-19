package com.mechcard.ui.jobs

import AutoCompleteAdapter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Filter.FilterListener
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.mechcard.MainActivity
import com.mechcard.R
import com.mechcard.apis.ApiViewModel
import com.mechcard.databinding.FragmentJobsBinding
import com.mechcard.models.JobData
import com.mechcard.pref.MechCardPref
import com.mechcard.ui.custom.MechProgressDialog
import com.mechcard.ui.`interface`.InactivityListener


class JobsFragment : Fragment(), FilterListener {

    companion object {
        fun newInstance() = JobsFragment()
    }

    private lateinit var viewModel: JobsViewModel
    private lateinit var apiViewModel: ApiViewModel
    private lateinit var mBinding: FragmentJobsBinding

    lateinit var adapter: AutoCompleteAdapter
    private var jobList = ArrayList<JobData>()
    private var filterJobList = ArrayList<JobData>()

    private var selectedJob: JobData? = null
    private var selectedvalue=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_jobs, container, false)


        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(JobsViewModel::class.java)
        apiViewModel = ViewModelProvider(this).get(ApiViewModel::class.java)

        mBinding.tvMechanicName.text =
            "${MechCardPref.signedInMechanic?.mechanicName} (${MechCardPref.signedInMechanic?.mechanicid})"

        mBinding.textBack.setOnClickListener {
            findNavController().navigateUp()
        }

        mBinding.buttonSelectJob.setOnClickListener {
            selectedJob?.let {
                val direction =
                    JobsFragmentDirections.actionJobsFragmentToManageServicesFragment(it)
                findNavController().navigate(direction)
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

        mBinding.typesFilterServicesSpinner.setDropDownBackgroundDrawable(ColorDrawable(Color.WHITE));

        mBinding.typesFilterServicesSpinner.setOnItemClickListener { adapterView, view, i, l ->
            val selectedJobList = jobList.filter { it ->
                it.id.equals(
                    mBinding.typesFilterServicesSpinner.text.toString(),
                    false
                )

            }
            if(selectedJobList.isNotEmpty()){
                selectedJobList[0].let { selectedJob ->
                    this.selectedJob = selectedJob
                    mBinding.tvVehicleNo.text = selectedJob.vehicleRegistrationNo
                    mBinding.tvCustName.text = selectedJob.customerName
                    mBinding.tvJobDesc.text = selectedJob.description
                    mBinding.buttonSelectJob.isEnabled = true

                }
                Log.e("Selectedjob",selectedJob!!.id.toString())
            }
        }

        mBinding.root.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_MOVE) {

                }

                if(mBinding.typesFilterServicesSpinner.hasFocus()){
                    mBinding.typesFilterServicesSpinner.clearFocus()
                }
                return false
            }
        })

        mBinding.viewMainJob.setOnClickListener(View.OnClickListener {
            if(mBinding.typesFilterServicesSpinner.hasFocus()){
                mBinding.typesFilterServicesSpinner.clearFocus()
            }
        })

//        mBinding.typesFilterServicesSpinner.addTextChangedListener(object : TextWatcher {
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                if(s.toString().length>8){
//                    mBinding.typesFilterServicesSpinner.setListSelection(0);
//                    for ( vehicle in jobList) {
//
//                      Log.e("filtervalue",s.toString())
//                        Log.e("recivevalue",vehicle.id.toString())
//                        if (vehicle.id.toString().equals(s.toString(),true)) {
//                            // ...
//                            Log.e("filtervalue",s.toString())
//                            Log.e("recivevalue",vehicle.id.toString())
//                        }
//                    }
//
//
//                    val selectedJobList = jobList.filter { it ->
//                        it.id.equals(
//                            mBinding.typesFilterServicesSpinner.text.toString(),
//                            false
//                        )
//                    }
//
//                    if(selectedJobList.isNotEmpty()){
//                        selectedJobList[0].let { selectedJob ->
////                                    selectedJob = selectedJob
//                            mBinding.tvVehicleNo.text = selectedJob.vehicleRegistrationNo
//                            mBinding.tvCustName.text = selectedJob.customerName
//                            mBinding.tvJobDesc.text = selectedJob.description
//                            mBinding.buttonSelectJob.isEnabled = true
//                        }
//                    }
//
//
//                    Log.e("selectedjoblist",selectedJobList.size.toString())
//                }
//            }
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
//            override fun afterTextChanged(s: Editable) {
//
//            }
//        })

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

        getJobList()
    }

    private fun getJobList() {
        val dialog = MechProgressDialog(requireContext()).setTitle("Loading...").create()
        dialog.show()
        apiViewModel.getJobList(
            "",
            "",
            9999,
            0,
            "",
            "ASC",
            "Bearer ${MechCardPref.signedInMechanic?.mechanicaccessToken}"
        ) { jobs ->
            dialog.dismiss()
            jobList.clear()
            jobList.addAll(jobs)
            setJobAdapter()
        }

    }

    private fun setJobAdapter() {
        adapter = AutoCompleteAdapter(
            requireContext(),
            R.layout.layout_country_spinner,
            jobList,

            object:AutoCompleteAdapter.OnSelectedItem{
                override fun onSelected(valuedata: JobData) {
//                    adapter.notifyDataSetChanged()
                    selectedJob = valuedata
                    selectedvalue=valuedata.id.toString()
                    mBinding.tvVehicleNo.text = valuedata.vehicleRegistrationNo
                    mBinding.tvCustName.text = valuedata.customerName
                    mBinding.tvJobDesc.text = valuedata.description
                    mBinding.buttonSelectJob.isEnabled = true
//                    val itemPosition: Int = adapter.getPosition(valuedata)

                }
            } ,object:AutoCompleteAdapter.OnNothingSelectedItem{
                override fun onNothingSelected() {
                    mBinding.tvVehicleNo.text = ""
                    mBinding.tvCustName.text = ""
                    mBinding.tvJobDesc.text = ""
                }
            }
        )


        mBinding.typesFilterServicesSpinner.setAdapter(adapter)
        mBinding.typesFilterServicesSpinner.setThreshold(1);
        mBinding.typesFilterServicesSpinner.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                //this if condition is true when edittext lost focus...
                //check here for number is larger than 10 or not

                mBinding.typesFilterServicesSpinner.setText(this.selectedvalue)
            }
        })


    }

    override fun onFilterComplete(count: Int) {

        Toast.makeText(activity,"hii",Toast.LENGTH_LONG).show()
    }

    fun dispatchTouchEvent(ev: MotionEvent?): Boolean {


        return false
    }



}