package com.example.cc_project

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CheckListAdapter(
    private val checkList: List<Pair<String, SpecGoal>>,
    private val onStatusChange: () -> Unit
) : RecyclerView.Adapter<CheckListAdapter.ViewHolder>() {

    private val db = Firebase.firestore
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_check_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (goalId, specGoal) = checkList[position]
        holder.bind(goalId, specGoal)
    }

    override fun getItemCount(): Int {
        return checkList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val specGoalNameTextView: TextView = itemView.findViewById(R.id.tv_spec_goal_name)
        private val isAchievedCheckBox: CheckBox = itemView.findViewById(R.id.cb_is_achieved)
        private var specGoalId: String? = null

        fun bind(goalId: String, specGoal: SpecGoal) {
            specGoalId = specGoal.sgid // Store the specGoalId

            specGoalNameTextView.text = specGoal.specgoalname
            isAchievedCheckBox.setOnCheckedChangeListener(null) // Remove previous listener
            isAchievedCheckBox.isChecked = specGoal.achieved
            applyStrikethrough(specGoalNameTextView, specGoal.achieved)

            isAchievedCheckBox.setOnCheckedChangeListener { _, isChecked ->
                specGoalId?.let { sgid ->
                    val specGoalCollectionRef = db.collection("uID").document(uid!!).collection("Goal").document(goalId).collection("specGoal")

                    // Firestore에서 currentCheck 값을 가져옵니다.
                    specGoalCollectionRef.document(sgid).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val currentCheck = document.getLong("currentCheck") ?: 0L
                                val updatedCheck = if (isChecked) currentCheck + 1 else currentCheck - 1

                                // Firestore에 업데이트합니다.
                                specGoalCollectionRef.document(sgid)
                                    .update(
                                        mapOf(
                                            "currentCheck" to updatedCheck,
                                            "achieved" to isChecked // Update achieved based on checkbox state
                                        )
                                    )
                                    .addOnSuccessListener {
                                        // Firebase 업데이트가 성공한 경우 UI를 업데이트합니다.
                                        specGoal.achieved = isChecked // Update specGoal object
                                        itemView.post {
                                            notifyDataSetChanged() // RecyclerView 전체를 새로 고침하거나
                                            notifyItemChanged(adapterPosition) // 특정 항목만 새로 고침합니다.
                                        }
                                        onStatusChange()
                                    }
                            }
                        }
                }
            }
        }

        private fun applyStrikethrough(textView: TextView, isStrikethrough: Boolean) {
            val text = textView.text.toString()
            val spannableString = SpannableString(text)

            if (isStrikethrough) {
                spannableString.setSpan(StrikethroughSpan(), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannableString.setSpan(ForegroundColorSpan(Color.RED), 0, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                val spans = spannableString.getSpans(0, spannableString.length, Any::class.java)
                for (span in spans) {
                    spannableString.removeSpan(span)
                }
            }

            textView.text = spannableString
        }
    }

}
