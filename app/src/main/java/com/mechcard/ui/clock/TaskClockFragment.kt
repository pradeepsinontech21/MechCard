package com.mechcard.ui.clock

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mechcard.R

class TaskClockFragment : Fragment() {

    companion object {
        fun newInstance() = TaskClockFragment()
    }

    private lateinit var viewModel: TaskClockViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_clock, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TaskClockViewModel::class.java)
        // TODO: Use the ViewModel
    }

}