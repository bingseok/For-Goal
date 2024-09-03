package com.example.cc_project

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

var savedHour = 8
var savedMinute = 30
val db = Firebase.firestore
val userID = Firebase.auth.uid

class SettingFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var alarm: Alarm
    private lateinit var lineChart: LineChart
    private lateinit var holiday: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var recyclerView: RecyclerView
    private lateinit var chartAdapter: ChartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)
        sharedPreferences = requireContext().getSharedPreferences(requireContext().packageName + "_preferences", Context.MODE_PRIVATE)

        holiday = view.findViewById(R.id.test_type_label)

        alarm = Alarm(requireContext())

        val notificationTimeLabel: TextView = view.findViewById(R.id.notification_time_label)
        val testTypeLabel: TextView = view.findViewById(R.id.test_type_label)

        val emailText: TextView = view.findViewById(R.id.email_text)

        val editNicknameLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // 닉네임 변경 후 새로고침
                loadNickname(emailText)
            }
        }

        loadNickname(emailText)

        // 저장된 알림 시간 불러오기
        savedHour = sharedPreferences.getInt("saved_hour", 8)
        savedMinute = sharedPreferences.getInt("saved_minute", 30)

        val initialSelectedTime = if (savedHour < 12) {
            "오전 ${savedHour}시 ${savedMinute}분 에 알림"
        } else if (savedHour == 12) {
            "오후 ${savedHour}시 ${savedMinute}분 에 알림"
        } else {
            "오후 ${savedHour - 12}시 ${savedMinute}분 에 알림"
        }
        notificationTimeLabel.text = initialSelectedTime
        val button = view.findViewById<ImageButton>(R.id.setting_button)
        button.setOnClickListener {
            // 팝업 창을 띄우는 메소드를 호출합니다.
            showPopupWindow(it)
        }

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        chartAdapter = ChartAdapter(emptyList())
        recyclerView.adapter = chartAdapter

        // Goal별 달성도를 표시하는 원형 그래프 추가
        fetchGoalData()

        // LineChart 초기화
        lineChart = view.findViewById(R.id.line_chart)
        fetchLineChartData()

        loadHolidayData()

        return view
    }

    private fun showPopupWindow(anchorView: View) {
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_layout, null)

        val popupWindow = PopupWindow(
            popupView,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        val modifyNotificationTimeButton = popupView.findViewById<Button>(R.id.button_modify_notification_time)
        val recheckTypeButton = popupView.findViewById<Button>(R.id.button_recheck_type)
        val modifyNicknameButton = popupView.findViewById<Button>(R.id.button_modify_nickname)
        val logoutButton = popupView.findViewById<Button>(R.id.button_logout)

        modifyNotificationTimeButton.setOnClickListener {
            showTimePickerDialog()
        }

        recheckTypeButton.setOnClickListener {
            startActivity(Intent(activity, PersonalityTestActivity::class.java))
        }

        modifyNicknameButton.setOnClickListener {
            showEditNicknameDialog()
        }

        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        popupWindow.showAsDropDown(anchorView)
    }

    private fun loadHolidayData() {
        val userID = auth.currentUser?.uid ?: return
        db.collection("uID").document(userID).collection("userType").document("userType")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val holidayOfWeek = document.get("holidayofweek") as? List<Boolean>
                    holidayOfWeek?.let {
                        val daysOfWeek = listOf("월", "화", "수", "목", "금", "토", "일")
                        val holidays = it.mapIndexedNotNull { index, isHoliday ->
                            if (isHoliday) daysOfWeek[index] else null
                        }
                        holiday.text = "휴일: ${holidays.joinToString(", ")}"
                    }
                }
            }
            .addOnFailureListener {
                holiday.text = "휴일 정보를 가져오는 데 실패했습니다."
            }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("예") { dialog, id ->
                // 로그아웃 처리
                Firebase.auth.signOut()
                AuthUse.email = null
                Toast.makeText(context, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
                // 로그인 화면으로 이동하는 코드 추가 (필요 시)
                startActivity(Intent(activity, AuthActivity::class.java))
            }
            .setNegativeButton("아니오") { dialog, id ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun showEditNicknameDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_edit_nickname, null)
        val editNickname = dialogView.findViewById<EditText>(R.id.nickname_edit_text)
        val saveButton = dialogView.findViewById<Button>(R.id.save_button)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
            .setCancelable(true)

        val dialog = builder.create()

        saveButton.setOnClickListener {
            val newNickname = editNickname.text.toString().trim()
            if (newNickname.isNotEmpty()) {
                // Save the new nickname to Firestore
                userID?.let { uid ->
                    db.collection("uID").document(uid)
                        .update("nickname", newNickname)
                        .addOnSuccessListener {
                            Toast.makeText(context, "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                            loadNickname(view?.findViewById(R.id.email_text)!!)
                            dialog.dismiss()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context,
                                "닉네임 변경에 실패했습니다: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            } else {
                Toast.makeText(context, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun loadNickname(emailText: TextView) {
        val userID = auth.currentUser?.uid ?: return
        db.collection("uID").document(userID).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    if (document.contains("nickname")) {
                        val nickname = document.getString("nickname")
                        emailText.text = "안녕하세요 ${nickname} 님"
                    } else {
                        emailText.text = "안녕하세요 ${AuthUse.email} 님"
                    }
                }
            }
            .addOnFailureListener {
                emailText.text = "안녕하세요 ${AuthUse.email} 님"
            }
    }

    private fun showTimePickerDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_time_picker, null)
        val timePicker: TimePicker = dialogView.findViewById(R.id.custom_time_picker)
        val setTimeButton: Button = dialogView.findViewById(R.id.button_set_time)

        // 시간 형식을 24시간 형식으로 설정
        timePicker.setIs24HourView(true)

        // 현재 저장된 시간으로 설정
        timePicker.hour = savedHour
        timePicker.minute = savedMinute

        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        // 다이얼로그를 중앙에 위치시키는 코드 추가
        alertDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        alertDialog.window?.setGravity(Gravity.CENTER)
        setTimeButton.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            // 알림 시간을 저장하는 로직 추가
            savedHour = hour
            savedMinute = minute
            updateNotificationTimeLabel(hour, minute)
            saveNotificationTimeToPreferences(hour, minute)
            alarm.setAlarm(hour, minute) // 알람 설정
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun saveNotificationTimeToPreferences(hourOfDay: Int, minute: Int) {
        sharedPreferences.edit().apply {
            putInt("saved_hour", hourOfDay)
            putInt("saved_minute", minute)
            apply()
        }
    }

    private fun updateNotificationTimeLabel(hourOfDay: Int, minute: Int) {
        val formattedTime = if (hourOfDay < 12) {
            "오전 ${hourOfDay}시 ${minute}분 에 알림"
        } else if (hourOfDay == 12) {
            "오후 ${hourOfDay}시 ${minute}분 에 알림"
        } else {
            "오후 ${hourOfDay - 12}시 ${minute}분 에 알림"
        }
        view?.findViewById<TextView>(R.id.notification_time_label)?.text = formattedTime
    }

    private fun fetchGoalData() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("uID").document(uid)
                .collection("Goal")
                .get()
                .addOnSuccessListener { result ->
                    val data = mutableListOf<GoalData>()
                    val goals = result.documents

                    for (goalDocument in goals) {
                        val goalName = goalDocument.getString("goalname") ?: ""
                        val gid = goalDocument.id
                        val startDate = goalDocument.getString("startdate")
                        val endDate = goalDocument.getString("finishdate")


                        db.collection("uID").document(uid)
                            .collection("Goal").document(gid)
                            .collection("specGoal")
                            .get()
                            .addOnSuccessListener { specGoalResult ->
                                var totalCheckSum = 0L
                                var currentCheckSum = 0L

                                for (specGoalDocument in specGoalResult) {
                                    val totalCheck = specGoalDocument.getLong("totalCheck") ?: 0L
                                    Log.d("totalCheck", "$totalCheck")
                                    val currentCheck = specGoalDocument.getLong("currentCheck") ?: 0L
                                    Log.d("currentCheck", "$currentCheck")
                                    totalCheckSum += totalCheck
                                    currentCheckSum += currentCheck
                                }

                                val completionRate = if (totalCheckSum != 0L) {
                                    currentCheckSum.toFloat() / totalCheckSum.toFloat() * 100
                                } else {
                                    0f
                                }
                                Log.d("completionRate", "$completionRate")

                                val goalData = GoalData(goalName, completionRate, startDate!!, endDate!!)
                                data.add(goalData)

                                chartAdapter.setData(data)
                            }
                            .addOnFailureListener { exception ->
                                Log.d(TAG, "Error getting specGoal documents: ", exception)

                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting Goal documents: ", exception)
                }
        }
    }

    private fun fetchLineChartData() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("uID").document(uid).collection("Feedback")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val entries = mutableListOf<Entry>()
                    val labels = mutableListOf<String>()
                    val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd")
                    val outputDateFormat = DateTimeFormatter.ofPattern("MM.dd")

                    var index = 0f
                    for (document in result.documents.reversed()) {
                        val dateStr = document.getString("date") ?: continue
                        val completeNumber = document.get("completeNumber").toString().toFloatOrNull() ?: 0f
                        val totalNumber = document.get("totalNumber").toString().toFloatOrNull() ?: 1f
                        val ratio = if (totalNumber > 0) (completeNumber / totalNumber) * 100 else 0f

                        entries.add(Entry(index, ratio))

                        val localDate = LocalDate.parse(dateStr, dateFormat)
                        val formattedDate = localDate.format(outputDateFormat)
                        labels.add(formattedDate)
                        index += 1f
                    }

                    // 빈 슬롯을 채워 x축을 7칸으로 고정
                    while (labels.size < 7) {
                        labels.add("")
                    }

                    val typeface: Typeface? = ResourcesCompat.getFont(requireContext(), R.font.mango)

                    val dataSet = LineDataSet(entries, "Completion Ratio").apply {
                        color = (ContextCompat.getColor(requireContext(), R.color.main_color))
                        valueTextColor = Color.BLACK
                        valueTypeface = typeface
                        setDrawCircles(true)
                        setCircleColor(ContextCompat.getColor(requireContext(), R.color.dark_gray))
                        setDrawValues(true)
                        setDrawCircleHole(true)
                        lineWidth = 4f
                        circleRadius = 8f
                        setValueTextSize(12f)
                    }

                    val lineData = LineData(dataSet)
                    lineChart.data = lineData

                    lineChart.apply {
                        axisLeft.apply {
                            axisMinimum = 0f
                            axisMaximum = 100f
                            setDrawGridLines(false)
                            setDrawLabels(true)
                            setTypeface(typeface)
                        }
                        axisRight.isEnabled = false
                        xAxis.apply {
                            valueFormatter = IndexAxisValueFormatter(labels)
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            labelCount = 6 // x축을 7칸으로 고정
                            setAvoidFirstLastClipping(true)
                            spaceMax = 0.5f // 오른쪽 마진 추가
                            setTypeface(typeface)
                        }
                        description.isEnabled = false
                        legend.isEnabled = false
                        setScaleEnabled(false) // 확대/축소 비활성화
                        isDragEnabled = true // 드래그 활성화
                        setVisibleXRangeMaximum(6f) // 한 번에 보이는 최대 x축 레이블 수
                        moveViewToX(if (labels.size > 6) (labels.size - 6).toFloat() else 0f) // 최근 7개 데이터가 보이도록 초기 위치 설정
                        invalidate()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting Feedback documents: ", exception)
                }
        }
    }
}
