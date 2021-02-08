package com.example.mobilecomputing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.mobilecomputing.db.ReminderInfo
import com.example.mobilecomputing.databinding.ReminderItemBinding


class ReminderAdaptor(context: Context, private val list: List<ReminderInfo>) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, container: ViewGroup?): View? {
        var rowBinding = ReminderItemBinding.inflate(inflater, container, false)

        rowBinding.reminderMessage.text = list[position].message
        rowBinding.reminderTime.text = list[position].time
        rowBinding.reminderLocation.text = list[position].location

        return rowBinding.root
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

}