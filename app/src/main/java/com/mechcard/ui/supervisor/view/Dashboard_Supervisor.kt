package com.mechcard.ui.supervisor.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.livinglifetechway.k4kotlin.core.androidx.toastNow
import com.mechcard.MainActivity
import com.mechcard.R
import com.mechcard.apis.ApiViewModel
import com.mechcard.databinding.FragmentDashboardSupervisorBinding
import com.mechcard.models.PendingJobs
import com.mechcard.models.StatusModel
import com.mechcard.pref.MechCardPref
import com.mechcard.ui.custom.MechProgressDialog
import com.mechcard.ui.`interface`.InactivityListener
import com.mechcard.ui.services.adapter.PendingJobAdapter
import com.mechcard.ui.services.objects.User
import com.mechcard.ui.supervisor.adapter.StatusAdapter
import com.mechcard.utils.statustypeList


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class Dashboard_Supervisor : Fragment(), View.OnClickListener,
    SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentDashboardSupervisorBinding? = null
    private val binding get() = _binding!!

    private var pendingjoblist = ArrayList<PendingJobs>()
    private lateinit var pendingjobAdapter: PendingJobAdapter
    private lateinit var user: User
    var selectedStatus: String = ""
    var selectdropdownstatus: String = ""
    private var PCURRENTPAGE: Int = 1
    private var isLastPage = false
    private var isFirstPage = false
    private var PTOTALPAGE = 0
    private var isLoading = false
    var itemCount = 20
    var searchvalue = mutableListOf<String>()


    var PJOBID: String = ""
    var PVID: String = ""
    var PCUSTNAME: String = ""
    var PSTATUS: String = "PENDING"
    var selectedpos: Int = 0
    var ORDER_FORMET="ASC"
    var PAGE_SIZE=10

    private lateinit var apiViewModel: ApiViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDashboardSupervisorBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiViewModel = ViewModelProvider(this).get(ApiViewModel::class.java)

        if (MechCardPref.signedInMechanic?.mechanicName != null && !MechCardPref.signedInMechanic?.mechanicName.toString()
                .isEmpty()
        ) {
            binding.tvMechanicName.text = MechCardPref.signedInMechanic?.mechanicName
        }

        createView()
        binding.btnAddServiceCategory.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            toastNow("" + MechCardPref.signedInMechanic?.mechanicName.toString())
            findNavController().navigate(R.id.action_dashboardSupervisorFragment_to_AddServiceFragment)
        }

        binding.btnUpdateTime.setOnClickListener(View.OnClickListener {

        })

        (requireActivity() as MainActivity).setInactivityListener(object : InactivityListener {
            override fun onInactivityCallback() {
                (requireActivity() as MainActivity).resetDisconnectTimer()
//                MechCardPref.clear()

                if (binding.etcustomername.text.toString().isNotEmpty()) {
                    onRefresh()
                } else {
                    val startDestination = findNavController().graph.startDestinationId
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(startDestination, true)
                        .build()
                    findNavController().navigate(startDestination, null, navOptions)
                }
            }
        })
    }


    private fun createView() {
        initlizeview()
        clickListner()
    }

    private fun initlizeview() {

        binding.tableRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.tableRecyclerView.isNestedScrollingEnabled = false
        binding.tvCurrentpage.setText(PCURRENTPAGE.toString())
        binding.tvTotalpage.setText(PTOTALPAGE.toString())
        binding.tvTotalitem.setText(itemCount.toString())
        getPageList()
    }

    private fun getPageList() {
        attachStatusAdapter()
        getPendingjobList()
    }

    private fun clickListner() {
        binding.btnSearch.setOnClickListener(this)
        binding.btnPendingJobsBackword.setOnClickListener(this)
        binding.btnPendingJobsForword.setOnClickListener(this)
        binding.swiprefresh.setOnRefreshListener(this);
        binding.btnImgSpinner.setOnClickListener(this)
        binding.btnAddServiceCategory.setOnClickListener(this)
        binding.imageLogout.setOnClickListener(this)
        binding.btnUpdateTime.setOnClickListener(this)
    }

    private fun attachStatusAdapter() {
        val years = arrayOf("Select Status", "Pending", "Completed")
        val pendingstatusArrayAdapter: ArrayAdapter<CharSequence?> =
            object : ArrayAdapter<CharSequence?>(
                requireActivity(), com.mechcard.R.layout.spinner_text, years
            ) {
                override fun isEnabled(position: Int): Boolean {
                    return if (position == 0) {
                        false
                    } else {
                        true
                    }
                }
            }
        pendingstatusArrayAdapter.setDropDownViewResource(com.mechcard.R.layout.simple_spinner_dropdown)
        binding.pendingjobspinner.setAdapter(pendingstatusArrayAdapter)
        binding.pendingjobspinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position > 0) {
                        // Notify the selected item text

                        selectdropdownstatus = years[position]
//                    Toast.makeText(
//                        requireActivity(),
//                        "Selected : $selectedStatus",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

            }
    }


    private fun filterWithQuery(query: String) {
        if (query.isNotEmpty()) {
            val filteredList: ArrayList<PendingJobs> = onFilterChanged(query)
//            attachAdapter(filteredList)
        } else if (query.isEmpty()) {
//            attachAdapter(pendingjoblist)
        }
    }

    private fun onFilterChanged(filterQuery: String): ArrayList<PendingJobs> {
        val filteredList = ArrayList<PendingJobs>()
        if (filterQuery.contains(",")) {
            val searchChar = filterQuery.split(",")
            Log.e("Search Char", searchChar.toString())
            for (i in 0 until searchChar.size) {
                for (currentSport in pendingjoblist) {
                    if (currentSport.jobid.contains(searchChar[i]) || currentSport.vehicleid.contains(
                            searchChar[i]
                        ) || currentSport.customername.contains(searchChar[i]) || currentSport.status.contains(
                            searchChar[i]
                        )
                    ) {
                        filteredList.add(currentSport)
                    }
                }
            }
        } else {
            for (currentSport in pendingjoblist) {
                if (currentSport.jobid.contains(filterQuery) || currentSport.vehicleid.contains(
                        filterQuery
                    ) || currentSport.customername.contains(filterQuery) || currentSport.status.contains(
                        filterQuery
                    )
                ) {
                    filteredList.add(currentSport)
                }
            }
        }
        return filteredList
    }

    private fun attachAdapter() {
        pendingjobAdapter = PendingJobAdapter(pendingjoblist, object : PendingJobAdapter.OnSelectedItem {
            override fun onSelected(valuedata: PendingJobs, type: String, value: String) {
                toastNow(type + " :" + value)
            }

        })
        binding.tableRecyclerView.adapter = pendingjobAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View?) {
        selectedStatus = binding.etcustomername.text.toString()
        if (selectdropdownstatus.isNotEmpty()) {
            if (selectedStatus.toString().isNotEmpty()) {
                selectedStatus = selectedStatus + "," + selectdropdownstatus
            } else {
                selectedStatus = selectedStatus + selectdropdownstatus
            }
        }

        when (v!!.id) {
            R.id.btn_Search -> {

                binding.etcustomername.clearFocus()
                binding.btnSearch.requestFocus()
                hideKeyboardFrom(requireActivity(), binding.etcustomername)
//                if (selectedStatus != "") {
//
//                    filterWithQuery(selectedStatus)
//                } else {
//                    toastNow("Please Select Status")
//                }
                SearchDialog()
            }

            R.id.btn_pending_jobs_forword -> {
                if (PCURRENTPAGE < PTOTALPAGE) {
                    PCURRENTPAGE++
                    getPendingjobList()
                    isLastPage = false
                } else {
                    toastNow("It's Last page")
                    isLastPage = true
                }
            }

            R.id.btn_pending_jobs_backword -> {
                if (PCURRENTPAGE != 1) {
                    PCURRENTPAGE--
                    getPendingjobList()
                    isFirstPage = false
                } else {
                    toastNow("It's First page")
                    isFirstPage = true
                }
            }

            R.id.btn_img_spinner -> {
                binding.pendingjobspinner.performClick()
            }

            R.id.btn_add_service_category -> {

            }

            R.id.btn_update_time -> {
                binding.btnUpdateTime.tooltipText = "Update time"
                Toast.makeText(activity, "Update Time", Toast.LENGTH_LONG).show()
            }

            R.id.image_logout -> {
                logoutuser()
            }
        }
    }

    private fun logoutuser() {
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

    fun hideKeyboardFrom(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

//    private fun updatePage(currentPage: Int) {
//        getPendingjobList(currentPage)
//        attachStatusAdapter()
////        if (currentPage == 1) {
////            pendingjoblist.clear()
////            pendingjoblist.addAll(pendingJobList(requireActivity()))
////            attachAdapter(pendingjoblist)
////            attachStatusAdapter()
////        }
////        if (currentPage == 2) {
////            pendingjoblist.clear()
////            pendingjoblist.addAll(pendingJobPageList(requireActivity()))
////            attachAdapter(pendingjoblist)
////            attachStatusAdapter()
////        }
//    }

    override fun onRefresh() {
        createView()
        PCURRENTPAGE = 1;
        binding.swiprefresh.isRefreshing = false
        binding.etcustomername.setText("")
        selectdropdownstatus = ""
        selectdropdownstatus = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getPendingjobList() {
        val dialog = MechProgressDialog(requireContext()).setTitle("Loading...").create()
        dialog.show()
        apiViewModel.getPendingJobList(
            PAGE_SIZE,
            PCURRENTPAGE,
            "",
            ORDER_FORMET,
            PJOBID,
            PVID,
            PCUSTNAME,
            PSTATUS,
            "Bearer ${MechCardPref.signedInMechanic?.mechanicaccessToken}"
        ) { jobs ->

            dialog.dismiss()
            Log.e("response", jobs.toString())
            try {
                if (jobs!!.actionresult!!.status.equals("0")) {
                    val details = jobs!!.pagingDetails
                    PTOTALPAGE = details!!.noofpages!!.toInt()
                    binding.tvCurrentpage.text = details.currentpage.toString()
                    binding.tvTotalpage.text = details!!.noofpages.toString()
                    binding.tvTotalitem.text = details.totalrecords.toString()
                    attachAdapter()
                }
            } catch (e: Exception) {
                dialog.dismiss()
                Log.e("response", e.toString())
            }
            pendingjoblist.clear()
            pendingjoblist.addAll(jobs!!.pendingJobList)
            attachAdapter()
        }

    }


    private fun SearchDialog() {

        // initialize dialog
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customView = inflater.inflate(R.layout.pending_dialog_layout, null, false)


        //dialog button
        val btnClose = customView.findViewById<AppCompatImageView>(R.id.btn_close)
        val btnCancel = customView.findViewById<AppCompatButton>(R.id.btn_cancel)
        val btnSubmit = customView.findViewById<AppCompatButton>(R.id.btn_Search)


        //dialog edittext
        val edtjobid = customView.findViewById<AppCompatEditText>(R.id.edtjobid)
        val edtvehcileid = customView.findViewById<AppCompatEditText>(R.id.edtvhcleid)
        val edtcustomername = customView.findViewById<AppCompatEditText>(R.id.edtcustomername)

       //dialog recyclerview for status view
        val statustypeview = customView.findViewById<RecyclerView>(R.id.recyc_status)
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        statustypeview.setLayoutManager(linearLayoutManager)
        val recyclerAdapter = StatusAdapter(
            requireContext(),
            statustypeList(requireActivity()),
            object : StatusAdapter.onClickListner {
                override fun onClick(statusitem: StatusModel, position: Int) {
                    selectedpos = position
                    PSTATUS = statusitem.stausName
                }

            })
        statustypeview.adapter = recyclerAdapter


        // add view on dialog
        builder.setView(customView)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()

        clickListner()
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnSubmit.setOnClickListener {
            PJOBID = edtjobid.text.toString()
            PVID = edtvehcileid.text.toString()
            PCUSTNAME = edtcustomername.text.toString()
            if (PJOBID.isNotEmpty() || PVID.isNotEmpty() || PCUSTNAME.isNotEmpty() || PSTATUS.isNotEmpty()) {
                if (PJOBID.isNotEmpty()) {
                    searchvalue.add(PJOBID)
                } else if (PVID.isNotEmpty()) {
                    searchvalue.add(PVID)
                } else if (PCUSTNAME.isNotEmpty()) {
                    searchvalue.add(PCUSTNAME)
                } else if (PSTATUS.isNotEmpty()) {
                    searchvalue.add(PSTATUS)
                }
                alertDialog.dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter search field for further process",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnClose.setOnClickListener {
            alertDialog.dismiss()
        }
    }
}