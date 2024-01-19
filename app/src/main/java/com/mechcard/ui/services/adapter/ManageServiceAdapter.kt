package com.mechcard.ui.services.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.livinglifetechway.k4kotlin.core.hide
import com.livinglifetechway.k4kotlin.core.show
import com.mechcard.R
import com.mechcard.models.Service


open class ManageServiceAdapter(
    private val mContext: Context,
    private val mLayoutResourceId: Int,
    cities: List<Service>,
    val btnlistener: OnSelectedItem,
    val btnNothingselectedlistener: OnNothingSelectedItem
) :
    ArrayAdapter<Service>(mContext, mLayoutResourceId, cities) {
    private val city: MutableList<Service> = ArrayList(cities)
    private var allCities: List<Service> = ArrayList(cities)
    var mSelectedItem: OnSelectedItem? =btnlistener
    override fun getCount(): Int {
        return city.size
    }
    override fun getItem(position: Int): Service {
        return city[position]
    }



    override fun getFilter(): Filter {
        return object : Filter() {
            override fun convertResultToString(resultValue: Any) :String {
                return (resultValue as Service).serviceName
            }
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    val citySuggestion: MutableList<Service> = ArrayList()
                    for (city in allCities) {
                        if (city.serviceName.startsWith(constraint.toString(),true)
                        ) {
                            citySuggestion.add(city)
                        }else{
                            btnNothingselectedlistener.onNothingSelected()
                        }
                    }
                    filterResults.values = citySuggestion
                    filterResults.count = citySuggestion.size
                }
                return filterResults
            }
            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults
            ) {
                city.clear()


                if (results.count > 0) {
                    if(results.count==1){
                        for (result in results.values as List<*>) {
                            if (result is Service) {
                                mSelectedItem!!.onSelected(result)
                                city.add(result)
                            }
                        }

                        notifyDataSetChanged()

                    }else{
                        for (result in results.values as List<*>) {
                            if (result is Service) {
                                city.add(result)
                            }
                        }
                        notifyDataSetChanged()
                    }
                } else if (constraint == null) {
                    city.addAll(allCities)
                    notifyDataSetInvalidated()
                }
            }
        }
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.item_service_drop_down, parent, false)
        }
        val people = city?.get(position)
        if (people != null) {
            val tvServiceCode = view!!.findViewById<View>(R.id.tvServiceCode) as TextView

            val tvServiceName = view!!.findViewById<View>(R.id.tvServiceName) as TextView


            val ivItemPause = view!!.findViewById<View>(R.id.ivItemPause) as ImageView

            tvServiceCode.text=people.serviceCode.toString()
            tvServiceName.text=people.serviceName.toString()

            if (people.status == "PAUSED") {
                ivItemPause.show()
            } else {
                ivItemPause.hide()
            }
        }
        return view!!
    }

    open interface OnSelectedItem {
        fun onSelected(valuedata: Service)
    }
    open interface OnNothingSelectedItem {
        fun onNothingSelected()
    }

}