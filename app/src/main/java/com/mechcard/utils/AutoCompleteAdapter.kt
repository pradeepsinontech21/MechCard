
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.mechcard.R
import com.mechcard.models.JobData


class AutoCompleteAdapter(
    private val mContext: Context,
    private val mLayoutResourceId: Int,
    cities: List<JobData>,
    val btnlistener: OnSelectedItem,
    val btnNothingselectedlistener: OnNothingSelectedItem
) :
    ArrayAdapter<JobData>(mContext, mLayoutResourceId, cities) {
    private val city: MutableList<JobData> = ArrayList(cities)
    private var allCities: List<JobData> = ArrayList(cities)
    var mSelectedItem: OnSelectedItem? =btnlistener
    override fun getCount(): Int {
        return city.size
    }
    override fun getItem(position: Int): JobData {
        return city[position]
    }



    override fun getFilter(): Filter {
        return object : Filter() {
            override fun convertResultToString(resultValue: Any) :String {
                return (resultValue as JobData).id
            }
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    val citySuggestion: MutableList<JobData> = ArrayList()
                    for (city in allCities) {
                        if (city.id.startsWith(constraint.toString(),true)
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
                           if (result is JobData) {
                               mSelectedItem!!.onSelected(result)
                               city.add(result)

//                              if(result.id.toString().equals(constraint)){
////                                  mSelectedItem!!.onSelected(result)
//                              }
                           }
                       }

                       notifyDataSetChanged()

                   }else{
                       for (result in results.values as List<*>) {
                           if (result is JobData) {
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
            view = inflater.inflate(R.layout.row_item, parent, false)
        }
        val people = city?.get(position)
        if (people != null) {
            val lblName = view!!.findViewById<View>(R.id.lbl_name) as TextView
            lblName?.setText(people.id.toString())
        }
        return view!!
    }

    open interface OnSelectedItem {
        fun onSelected(valuedata:JobData)
    }

    open interface OnNothingSelectedItem {
        fun onNothingSelected()
    }
}