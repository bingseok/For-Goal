package com.example.cc_project

import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView

class DeleteItemsAdapter(private val goal: Goal, private val specGoals: List<SpecGoal>) :
    RecyclerView.Adapter<DeleteItemsAdapter.ViewHolder>() {

    private val selectedItems = mutableListOf<Any>()
    private val handler = Handler(Looper.getMainLooper())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val prefixTextView: TextView = itemView.findViewById(R.id.prefix_text_view)
        val checkBox: CheckBox = itemView.findViewById(R.id.delete_item_checkbox)
        val titleTextView: TextView = itemView.findViewById(R.id.delete_item_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.delete_item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        val customFont: Typeface? = ResourcesCompat.getFont(context, R.font.mango)

        holder.checkBox.setOnCheckedChangeListener(null)

        if (position == 0) {
            holder.prefixTextView.visibility = View.GONE
            holder.titleTextView.text = goal.goalname
            holder.titleTextView.setTypeface(customFont, android.graphics.Typeface.BOLD)
            holder.checkBox.isChecked = selectedItems.contains(goal)
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(goal)
                    selectAllSpecGoals()
                } else {
                    selectedItems.remove(goal)
                    deselectAllSpecGoals()
                }
                handler.post { notifyDataSetChanged() }
            }
        } else {
            holder.prefixTextView.visibility = View.VISIBLE
            holder.prefixTextView.text = "└"
            val specGoal = specGoals[position - 1]
            holder.titleTextView.text = specGoal.specgoalname
            holder.titleTextView.setTypeface(customFont, android.graphics.Typeface.NORMAL)
            holder.checkBox.isChecked = selectedItems.contains(specGoal)
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(specGoal)
                    if (selectedItems.containsAll(specGoals) && !selectedItems.contains(goal)) {
                        selectedItems.add(goal)
                        handler.post { notifyItemChanged(0) }
                    }
                } else {
                    selectedItems.remove(specGoal)
                    if (selectedItems.contains(goal)) {
                        selectedItems.remove(goal)
                        handler.post { notifyItemChanged(0) }
                    }
                }
                handler.post { notifyItemChanged(position) }
            }
        }
    }

    override fun getItemCount(): Int = specGoals.size + 1

    private fun selectAllSpecGoals() {
        selectedItems.addAll(specGoals)
    }

    private fun deselectAllSpecGoals() {
        selectedItems.removeAll(specGoals)
    }

    fun getSelectedItems(): List<Any> = selectedItems
}
