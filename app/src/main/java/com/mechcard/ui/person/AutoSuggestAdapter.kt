package com.mechcard.ui.person

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable

class AutoSuggestAdapter<T : Any>(context: Context, resource: Int, val mlistData: ArrayList<T>) :
    ArrayAdapter<String>(context, resource), Filterable {


    /*    init {
            mlistData = ArrayList()
        }*/

    fun setData(list: List<T>?) {
        mlistData.clear()
        mlistData.addAll(list.orEmpty())
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return mlistData.size
    }

    override fun getItem(position: Int): String {
        return mlistData[position].toString()
    }

    /**
     * Used to Return the full object directly from adapter.
     *
     * @param position
     * @return
     */
    fun getObject(position: Int): T {
        return mlistData[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
//                if (constraint != null) {
                filterResults.values = mlistData
                filterResults.count = mlistData.size
//                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                /* if (results.count > 0) {
                     notifyDataSetChanged()
                 } else {
                     notifyDataSetInvalidated()
                 }*/
            }
        }
    }
}