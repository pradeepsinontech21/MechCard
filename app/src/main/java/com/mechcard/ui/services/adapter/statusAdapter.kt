package com.mechcard.ui.services.adapter



import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mechcard.models.JobStatus
import javax.annotation.Nullable


class statusAdapter(context: Context, resource: Int, var items: List<MyModel>)
    : ArrayAdapter<statusAdapter.MyModel>(context, resource, items) {

    val inflater: LayoutInflater = LayoutInflater.from(context)

    // Custom for this adapter. It's only here as an example
    data class MyModel(val id: Int, val name: String)


    // If required, get the ID from your Model. If your desired return value can't be converted to long use getItem(int) instead
    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, container: ViewGroup): View {
        var view: View? = convertView
        if (view == null) {
            view = inflater.inflate(com.mechcard.R.layout.spinner_text, container, false)
        }
        (view?.findViewById(android.R.id.text1) as TextView).text = getItem(position)!!.name
        return view
    }

    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        var view: View? = convertView
        if (view == null) {
            view = inflater.inflate(com.mechcard.R.layout.simple_spinner_dropdown, parent, false)
        }
        (view?.findViewById(android.R.id.text1) as TextView).text = getItem(position)!!.name
        return view
    }
}