package com.example.cc_project

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mainButton: ImageButton
    private lateinit var checkListView: RecyclerView
    private lateinit var noCheckListMessage: TextView
    private lateinit var noGoalListMessage: TextView
    private lateinit var todoStatusTextView: TextView

    private val db = Firebase.firestore
    private val uid = Firebase.auth.uid

    private lateinit var setGoalLauncher: ActivityResultLauncher<Intent>
    private lateinit var addSpecGoalLauncher: ActivityResultLauncher<Intent>
    private lateinit var checkListAdapter: CheckListAdapter
    private var checkList = mutableListOf<Pair<String, SpecGoal>>()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        mainButton = view.findViewById(R.id.main_button)
        checkListView = view.findViewById(R.id.check_list_container)
        noCheckListMessage = view.findViewById(R.id.no_check_list_message)
        noGoalListMessage = view.findViewById(R.id.no_goal_list_message)
        todoStatusTextView = view.findViewById(R.id.tv_todo_status)

        checkListView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(context)

        loadData()

        setGoalLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                loadData()
            }
        }

        addSpecGoalLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                loadData()
            }
        }

        mainButton.setOnClickListener {
            val setGoalIntent = Intent(activity, setGoalActivity::class.java)
            setGoalLauncher.launch(setGoalIntent)
        }

        return view
    }

    private fun loadData() {
        lifecycleScope.launch {
            val goals = getGoalData()
            val firestoreSpecGoalData = getSpecGoalData(goals)

            withContext(Dispatchers.Main) {
                setupCheckListView(goals, firestoreSpecGoalData)
                setupGoalListView(goals, firestoreSpecGoalData)
            }
        }
    }

    private fun setupCheckListView(goals: List<Goal>, firestoreSpecGoalData: Map<String, List<SpecGoal>>) {
        checkList.clear()
        val currentDayOfWeek = LocalDate.now().dayOfWeek.value - 1 // 현재 요일을 숫자로 가져옴 (1: 월요일, 7: 일요일)
        Log.d("currentDayOfWeek", "$currentDayOfWeek")

        for (goal in goals) {
            firestoreSpecGoalData[goal.gid]?.let { specGoals ->
                for (specGoal in specGoals) {
                    if (specGoal.dayofweek[currentDayOfWeek] == true)
                        checkList.add(goal.gid to specGoal)
                }
            }
        }

        if (checkList.isEmpty()) {
            noCheckListMessage.visibility = View.VISIBLE
            checkListView.visibility = View.GONE
            todoStatusTextView.text = "(0/0)"
        } else {
            noCheckListMessage.visibility = View.GONE
            checkListView.visibility = View.VISIBLE
            checkListAdapter = CheckListAdapter(checkList) {
                updateTodoStatus()
                handler.post {
                    checkListAdapter.notifyDataSetChanged()
                }
            }
            checkListView.adapter = checkListAdapter
            updateTodoStatus()
        }
    }

    private fun setupGoalListView(goals: List<Goal>, firestoreSpecGoalData: Map<String, List<SpecGoal>>) {
        if (goals.isEmpty()) {
            noGoalListMessage.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            noGoalListMessage.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            val adapter = ParentListAdapter(goals, firestoreSpecGoalData, ::onDeleteOrAddSpecGoalClick)
            recyclerView.adapter = adapter
        }
    }

    private suspend fun getGoalData(): List<Goal> = withContext(Dispatchers.IO) {
        val firestoreGoalData = mutableListOf<Goal>()
        val current = LocalDate.now()
        val formatter = DateTimeFormatter.BASIC_ISO_DATE
        val formatted = current.format(formatter)
        val date: String = formatted

        val uid = Firebase.auth.uid
        if (uid == null) {
            return@withContext emptyList<Goal>()
        }

        val snapshot = db.collection("uID").document(uid).collection("Goal")
            .whereGreaterThanOrEqualTo("finishdate", date)
            .whereLessThanOrEqualTo("startdate", date)
            .get()
            .await()

        for (document in snapshot.documents) {
            val goal = document.toObject(Goal::class.java)
            if (goal != null) {
                firestoreGoalData.add(goal)
            }
        }

        return@withContext firestoreGoalData
    }

    private suspend fun getSpecGoalData(goals: List<Goal>): Map<String, List<SpecGoal>> = withContext(Dispatchers.IO) {
        val firestoreSpecGoalData = mutableMapOf<String, List<SpecGoal>>()
        val uid = Firebase.auth.uid
        if (uid == null) {
            return@withContext emptyMap<String, List<SpecGoal>>()
        }
        val userDocRef = db.collection("uID").document(uid)

        for (goal in goals) {
            val snapshot = userDocRef.collection("Goal").document(goal.gid)
                .collection("specGoal")
                .get()
                .await()
            val specGoals = snapshot.map { it.toObject(SpecGoal::class.java) }
            firestoreSpecGoalData[goal.gid] = specGoals
        }
        return@withContext firestoreSpecGoalData
    }

    private fun onDeleteOrAddSpecGoalClick(goal: Goal, specGoals: List<SpecGoal>) {
        val dialogView = layoutInflater.inflate(R.layout.delete_confirmation_dialog, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.delete_items_recycler_view)
        val deleteButton = dialogView.findViewById<Button>(R.id.confirm_delete_button)
        val addButton = dialogView.findViewById<Button>(R.id.add_spec_goal_button)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = DeleteItemsAdapter(goal, specGoals)
        recyclerView.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        deleteButton.setOnClickListener {
            val selectedItems = adapter.getSelectedItems()
            if (selectedItems.isNotEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setMessage("선택한 항목을 삭제하시겠습니까?")
                    .setPositiveButton("예") { _, _ ->
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                selectedItems.forEach { item ->
                                    when (item) {
                                        is Goal -> {
                                            val goalRef = db.collection("uID").document(uid!!).collection("Goal").document(goal.gid)
                                            goalRef.collection("specGoal").get().await().forEach { specGoalDoc ->
                                                specGoalDoc.reference.delete().await()
                                            }
                                            goalRef.delete().await()
                                        }
                                        is SpecGoal -> {
                                            db.collection("uID").document(uid!!).collection("Goal").document(goal.gid)
                                                .collection("specGoal").document(item.sgid).delete().await()
                                        }
                                    }
                                }
                            }
                            loadData()
                            dialog.dismiss()
                        }
                    }
                    .setNegativeButton("아니오") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .create()
                    .show()
            } else {
                Toast.makeText(requireContext(), "삭제할 항목을 선택하세요", Toast.LENGTH_SHORT).show()
            }
        }

        addButton.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(requireContext(), DetailedGoalActivity::class.java)
            intent.putExtra("goalName", goal.goalname)
            intent.putExtra("StartDate", goal.startdate)
            intent.putExtra("EndDate", goal.finishdate)
            intent.putExtra("uuid", goal.gid)
            addSpecGoalLauncher.launch(intent)
        }

        dialog.show()
    }

    private fun updateTodoStatus() {
        val total = checkList.size
        val completed = checkList.count { it.second.achieved }
        todoStatusTextView.text = "($completed/$total)"

        // 날짜 형식 설정
        val current = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val formattedDate = current.format(formatter)

        // Firestore에 저장할 데이터 생성
        val feedback = Feedback(
            date = formattedDate,
            totalNumber = total,
            completeNumber = completed
        )

        // Firestore에 데이터 저장
        uid?.let {
            db.collection("uID").document(it).collection("Feedback").document(formattedDate)
                .set(feedback)
                .addOnSuccessListener {
                    Log.d("Firestore", "Feedback successfully written!")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error writing feedback", e)
                }
        }
    }
}
