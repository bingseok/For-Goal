package com.example.cc_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class ScheduleListAdapter(private val scheduleList: List<Map<String, Any>>) :
    RecyclerView.Adapter<ScheduleListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ViewHolder(view)
    }

    private fun formatDate(date: String): String {
        val inputFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val parsedDate = inputFormat.parse(date)
        return outputFormat.format(parsedDate)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = scheduleList[position]
        holder.specGoalNameTextView.text = schedule["specgoalname"] as? String ?: ""

        holder.startDateTextView.text = formatDate(schedule["startdate"] as? String ?: "")
        holder.finishDateTextView.text = formatDate(schedule["finishdate"] as? String ?: "")
    }


    override fun getItemCount(): Int {
        return scheduleList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val specGoalNameTextView: TextView = itemView.findViewById(R.id.specGoalNameTextView)
        val startDateTextView: TextView = itemView.findViewById(R.id.startDateTextView)
        val finishDateTextView: TextView = itemView.findViewById(R.id.finishDateTextView)
    }
}