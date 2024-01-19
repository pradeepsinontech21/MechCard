package com.mechcard.ui.services.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mechcard.R
import com.mechcard.models.PendingJobs
import com.mechcard.models.Service


class PendingJobAdapter(
    private var jobArrayList: List<PendingJobs>,
    val onclicklistener: OnSelectedItem
) :
    RecyclerView.Adapter<PendingJobAdapter.ViewHolder>(), View.OnClickListener {
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.table_row_layout, viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val  pendingJobs=jobArrayList[position]

        holder.tvJobId.text=pendingJobs.jobid
        holder.tvVehicleId.text=pendingJobs.vehicleid
        holder.tvCustName.text=pendingJobs.customername
        holder.tvDesc.text=pendingJobs.description
        holder.tvStatus.text=pendingJobs.status
        setTagview(holder,position)
        setClickListner(holder)

    }

    private fun setClickListner(holder: PendingJobAdapter.ViewHolder) {
        holder.tvJobId.setOnClickListener(this)
        holder.tvVehicleId.setOnClickListener(this)
        holder.tvCustName.setOnClickListener(this)
        holder.tvDesc.setOnClickListener(this)
        holder.tvStatus.setOnClickListener(this)
    }

    private fun setTagview(holder: ViewHolder, position: Int) {
        holder.tvJobId.setTag(position);
        holder.tvVehicleId.setTag(position);
        holder.tvCustName.setTag(position);
        holder.tvDesc.setTag(position);
        holder.tvStatus.setTag(position);
    }

    override fun getItemCount(): Int {
        return jobArrayList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJobId: TextView = itemView.findViewById(R.id.tv_jobid)
        val tvVehicleId: TextView = itemView.findViewById(R.id.tv_vehicleid)
        val tvCustName: TextView = itemView.findViewById(R.id.tv_custname)
        val tvDesc: TextView = itemView.findViewById(R.id.tv_description)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_status)


    }

    fun updateData(datalist: List<PendingJobs>) {
//        jobArrayList.clear()
//        jobArrayList.addAll(datalist)
        notifyDataSetChanged()
    }

    open interface OnSelectedItem {
        fun onSelected(valuedata: PendingJobs,type:String,value:String)
    }

    override fun onClick(v: View?) {

        val position = v!!.getTag() as Int

//display toast with position of cardview in recyclerview list upon click


        when(v!!.id){
            R.id.tv_jobid->{
                onclicklistener.onSelected(jobArrayList[position],v.context.getString(R.string.tv_jobid),jobArrayList[position].jobid)
            }
            R.id.tv_vehicleid->{
                onclicklistener.onSelected(jobArrayList[position],v.context.getString(R.string.tv_vehicleid),jobArrayList[position].vehicleid)
            }
            R.id.tv_custname->{
                onclicklistener.onSelected(jobArrayList[position],v.context.getString(R.string.tv_customername),jobArrayList[position].customername)
            }
            R.id.tv_description->{
                onclicklistener.onSelected(jobArrayList[position],v.context.getString(R.string.tv_description),jobArrayList[position].description)
            }
            R.id.tv_status->{
                onclicklistener.onSelected(jobArrayList[position],v.context.getString(R.string.tv_status),jobArrayList[position].status)
            }
        }
    }

}