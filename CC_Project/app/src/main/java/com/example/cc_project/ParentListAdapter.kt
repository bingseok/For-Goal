package com.example.cc_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ParentListAdapter(
    private val goals: List<Goal>,
    private val specGoalData: Map<String, List<SpecGoal>>,
    private val onDeleteClick: (Goal, List<SpecGoal>) -> Unit
) : RecyclerView.Adapter<ParentListAdapter.GoalViewHolder>() {

    private val expandedPositionSet: MutableSet<Int> = mutableSetOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.parent_item_layout, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val goal = goals[position]
        holder.bind(goal, position)
    }

    override fun getItemCount(): Int = goals.size

    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.parent_title_text_view)
        private val childRecyclerView: RecyclerView = itemView.findViewById(R.id.child_recycler_view)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_goal_button)

        fun bind(goal: Goal, position: Int) {
            titleTextView.text = goal.goalname

            val specGoals = specGoalData[goal.gid] ?: emptyList()
            val childAdapter = ChildListAdapter(specGoals)
            childRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            childRecyclerView.adapter = childAdapter

            val isExpanded = expandedPositionSet.contains(position)
            childRecyclerView.visibility = if (isExpanded) View.VISIBLE else View.GONE

            // Set background based on position
            val backgroundResource = when (position % 6) { // 6가지 배경 색상이므로
                0 -> R.drawable.rounded_background_blue
                1 -> R.drawable.rounded_background_yellow
                2 -> R.drawable.rounded_background_green
                3 -> R.drawable.rounded_background_orange
                4 -> R.drawable.rounded_background_purple
                5 -> R.drawable.rounded_background_red
                else -> R.drawable.rounded_background_blue // 기본적으로 첫 번째 배경 색상을 사용
            }
            itemView.setBackgroundResource(backgroundResource)

            itemView.setBackgroundResource(backgroundResource)

            itemView.setOnClickListener {
                if (isExpanded) {
                    expandedPositionSet.remove(position)
                } else {
                    expandedPositionSet.add(position)
                }
                notifyItemChanged(position)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(goal, specGoals)
            }
        }
    }
}
