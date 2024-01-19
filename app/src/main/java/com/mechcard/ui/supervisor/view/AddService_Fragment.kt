package com.mechcard.ui.supervisor.view
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.livinglifetechway.k4kotlin.core.androidx.toastNow
import com.mechcard.R
import com.mechcard.databinding.FragmentSecondBinding
import com.mechcard.models.PendingJobs
import com.mechcard.models.Service
import com.mechcard.ui.services.adapter.PendingJobAdapter
import com.mechcard.ui.services.objects.User
import com.mechcard.utils.pendingJobList
import com.mechcard.utils.pendingJobPageList


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddService_Fragment : Fragment(){

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private var pendingjoblist = ArrayList<PendingJobs>()
    private lateinit var pendingjobAdapter: PendingJobAdapter
    private lateinit var user : User
    var selectedStatus:String=""
    private var currentPage: Int = 1
    private var isLastPage = false
    private var isFirstPage = false
    private var totalPage = 2
    private var isLoading = false
    var itemCount = 20

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createView()
    }

    private fun createView() {

    }

}