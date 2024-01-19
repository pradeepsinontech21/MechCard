package com.mechcard.ui.supervisor.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.mechcard.models.StatusModel


class StatusAdapter(
    private val context: Context,
    petsList: List<StatusModel>,
    private val listner: onClickListner
) :
    RecyclerView.Adapter<StatusAdapter.MyViewHolder?>() {
    private val petsList: List<StatusModel>
    private var selectedItem: Int

    companion object {
        private const val lastClickedPosition = -1
    }

    init {
        this.petsList = petsList
        selectedItem = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(com.mechcard.R.layout.statusitem, parent, false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = petsList[position];
        holder.itemView.tag = item
        holder.name.text = item.stausName
        holder.cardView.setCardBackgroundColor(context.resources.getColor(com.mechcard.R.color.greycolor))
        if (selectedItem == position) {
            holder.cardView.setCardBackgroundColor(context.resources.getColor(com.mechcard.R.color.primary))
        }
        holder.itemView.setOnClickListener {
            val previousItem = selectedItem
            selectedItem = position
            notifyItemChanged(previousItem)
            notifyItemChanged(position)
            listner.onClick(item,position)
        }
    }

    override fun getItemCount(): Int {
        return petsList.size
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView
        var cardView: CardView

        init {
            name = view.findViewById<View>(com.mechcard.R.id.tv_pets) as TextView
            cardView = view.findViewById<View>(com.mechcard.R.id.cardview) as CardView
        }
    }

    interface onClickListner {
        fun onClick(status: StatusModel, position: Int)
    }
}