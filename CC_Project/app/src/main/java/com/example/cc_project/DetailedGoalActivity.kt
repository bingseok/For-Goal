package com.example.cc_project

import CompositeDateValidator
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

class DetailedGoalActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var button: ImageButton
    private lateinit var button2: Button
    private val editTextIds = mutableListOf<Int>()
    private val detailedGoalTexts = mutableListOf<String>()
    private val detailedGoalStartDates = mutableListOf<String>()
    private val detailedGoalEndDates = mutableListOf<String>()
    private val repeatValues = mutableListOf<Int>() // 각 항목의 repeat 값을 저장할 리스트
    private lateinit var detailGoalBack: ImageButton
    private val db = Firebase.firestore
    private lateinit var goalName: String
    private lateinit var StartDate: String
    private lateinit var EndDate: String
    private lateinit var uuid: String
    private val uid = Firebase.auth.uid
    private var archieved: Boolean = false

    companion object {
        const val DETAIL_GOAL_TEXT_KEY = "detailGoalText"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detailed_goal)

        container = findViewById(R.id.container)
        scrollView = findViewById(R.id.scrollView)
        button = findViewById(R.id.detailedGoalButton)
        button2 = findViewById(R.id.detailedGoalSaveButton)
        detailGoalBack = findViewById(R.id.detailGoalBack)

        goalName = intent.getStringExtra("goalName") ?: ""
        StartDate = intent.getStringExtra("StartDate") ?: ""
        EndDate = intent.getStringExtra("EndDate") ?: ""
        uuid = intent.getStringExtra("uuid") ?: ""

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        button.setOnClickListener {
            addNewDetailedGoalItem()
        }

        detailGoalBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        button2.setOnClickListener {
            lifecycleScope.launch {
                saveGoalAndFinish()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        })

        addNewDetailedGoalItem() // 기본 입력창 추가
    }

    private fun addNewDetailedGoalItem() {
        val inflater = LayoutInflater.from(this)
        val itemView = inflater.inflate(R.layout.detailed_goal_item, container, false)

        val newEditTextId = View.generateViewId()
        val editText = itemView.findViewById<EditText>(R.id.detailedGoalEditText)
        editText.id = newEditTextId
        editTextIds.add(newEditTextId)
        detailedGoalStartDates.add(StartDate) // 기본값으로 StartDate 설정
        detailedGoalEndDates.add(EndDate) // 기본값으로 EndDate 설정
        repeatValues.add(1) // 기본값으로 1 설정

        // detailedGoalTexts 초기화
        detailedGoalTexts.add("")

        // TextWatcher를 추가하여 텍스트 변경을 감지하고 저장 버튼 활성화 상태를 업데이트
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                button2.isEnabled = s.toString().isNotEmpty()
            }
            override fun afterTextChanged(s: Editable) {
                val currentText = s.toString()
                val editTextIndex = editTextIds.indexOf(editText.id)

                if (editTextIndex >= 0) {
                    detailedGoalTexts[editTextIndex] = currentText
                }
            }
        })

        val spinner = itemView.findViewById<Spinner>(R.id.date_spinner)

        // Firestore에서 holidayofweek 필드 가져오기
        val uid = Firebase.auth.uid
        if (uid != null) {
            db.collection("uID").document(uid).collection("userType").document("userType")
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val holidayOfWeek = document.get("holidayofweek") as? List<Boolean>
                        if (holidayOfWeek != null) {
                            val falseCount = holidayOfWeek.count { !it }
                            val items = Array(falseCount) { i -> "주 ${i + 1}회" }

                            // 스피너 설정 및 리스너 추가
                            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinner.adapter = adapter

                            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                    val editTextIndex = editTextIds.indexOf(newEditTextId)
                                    if (editTextIndex != -1 && editTextIndex < repeatValues.size) {
                                        repeatValues[editTextIndex] = position + 1  // 각 항목의 repeat 값을 저장
                                    }
                                }

                                override fun onNothingSelected(parent: AdapterView<*>) {
                                    // 아무것도 선택되지 않았을 때 처리할 코드
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DetailedGoalActivity", "Error getting userType document", e)
                }
        }

        val pickDetailedDateRange = itemView.findViewById<ImageButton>(R.id.edit_detailed_date_btn)
        val selectedDetailedDateRange = itemView.findViewById<TextView>(R.id.edit_detailed_date)

        pickDetailedDateRange.setOnClickListener {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            val startDateMillis = sdf.parse(StartDate)?.time
            val endDateMillis = sdf.parse((EndDate.toInt() + 1).toString())?.time

            if (startDateMillis != null && endDateMillis != null) {
                val constraintsBuilder = CalendarConstraints.Builder()
                    .setStart(startDateMillis)
                    .setEnd(endDateMillis)
                    .setValidator(CompositeDateValidator(startDateMillis, endDateMillis))

                val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select dates")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build()

                dateRangePicker.addOnPositiveButtonClickListener { selection ->
                    val startDate = selection.first
                    val endDate = selection.second

                    Log.d("STARTDATE", startDate.toString())
                    Log.d("ENDDATE", endDate.toString())

                    val editsdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                    val formattedStartDate = editsdf.format(startDate)
                    val formattedEndDate = editsdf.format(endDate)

                    val editTextIndex = editTextIds.indexOf(newEditTextId)
                    if (editTextIndex != -1 && editTextIndex < detailedGoalStartDates.size && editTextIndex < detailedGoalEndDates.size) {
                        detailedGoalStartDates[editTextIndex] = formattedStartDate.replace(".", "")
                        detailedGoalEndDates[editTextIndex] = formattedEndDate.replace(".", "")
                    }

                    selectedDetailedDateRange.text = "$formattedStartDate - $formattedEndDate"
                }

                dateRangePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
            } else {
                Toast.makeText(this, "Invalid date format for goal start or end date", Toast.LENGTH_SHORT).show()
            }
        }

        // 삭제 버튼
        val deleteButton = itemView.findViewById<ImageButton>(R.id.delete_item_button)
        deleteButton.setOnClickListener {
            val indexToRemove = editTextIds.indexOf(newEditTextId)
            if (indexToRemove in 0 until editTextIds.size) {
                editTextIds.removeAt(indexToRemove)
                if (indexToRemove < detailedGoalTexts.size) {
                    detailedGoalTexts.removeAt(indexToRemove)
                }
                if (indexToRemove < detailedGoalStartDates.size) {
                    detailedGoalStartDates.removeAt(indexToRemove)
                }
                if (indexToRemove < detailedGoalEndDates.size) {
                    detailedGoalEndDates.removeAt(indexToRemove)
                }
                if (indexToRemove < repeatValues.size) {
                    repeatValues.removeAt(indexToRemove)
                }
                container.removeView(itemView)
            }
        }

        container.addView(itemView)

        // Scroll to bottom
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }


    private suspend fun saveGoalAndFinish() {
        val sgName = arrayListOf<String>()
        val specGoalList = arrayListOf<SpecGoal>()

        for (i in detailedGoalTexts.indices) {
            val text = detailedGoalTexts[i]
            val workofday = whenCreate(repeatValues[i])
            val specGoal = SpecGoal(
                UUID.randomUUID().toString(),
                text,
                false,
                detailedGoalStartDates[i],
                detailedGoalEndDates[i],
                repeatValues[i],  // 각 항목의 repeat 값을 사용
                workofday.toList(),
                totalChecklist(repeatValues[i], detailedGoalStartDates[i], detailedGoalEndDates[i], workofday)
            )
            specGoalList.add(specGoal)
            sgName.add(text)
        }

        val current = Goal(
            uuid,
            goalName,
            archieved,
            StartDate,
            EndDate
        )

        db.collection("uID").document(uid!!).collection("Goal")
            .document(current.gid)
            .set(current)
            .addOnSuccessListener {
                var successCount = 0
                for (i in 0 until specGoalList.size) {
                    db.collection("uID").document(uid!!).collection("Goal").document(current.gid).collection("specGoal")
                        .document(specGoalList[i].sgid)
                        .set(specGoalList[i])
                        .addOnSuccessListener {
                            successCount++
                            if (successCount == specGoalList.size) {
                                Toast.makeText(
                                    this,
                                    "목표가 성공적으로 저장되었습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Set result to indicate that the data was saved
                                val intent = Intent()
                                intent.putExtra("UPDATE_HOME_FRAGMENT", true)
                                setResult(RESULT_OK, intent)
                                finish()
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "목표 저장 중 오류가 발생했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "목표 저장 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private suspend fun whenCreate(weekrepeat: Int): BooleanArray {
        var holiday = BooleanArray(7)

        // Firestore에서 데이터 가져오기
        val document = db.collection("uID").document(Firebase.auth.uid!!).collection("userType")
            .document("userType")
            .get()
            .await()

        val temp = document.get("holidayofweek") as? List<Boolean>
        if (temp != null) {
            holiday = temp.toBooleanArray()
        }

        var workDayofWeek = BooleanArray(7) { false }

        if (weekrepeat in 1..7) {
            val falseIndices = holiday.indices.filter { !holiday[it] }

            if (weekrepeat <= falseIndices.size) {
                // weekrepeat이 false 개수보다 작거나 같은 경우
                when (weekrepeat) {
                    1 -> {
                        if (falseIndices.isNotEmpty()) {
                            workDayofWeek[falseIndices.random()] = true
                        }
                    }
                    else -> {
                        val chunkSize = (falseIndices.size / weekrepeat.toFloat()).toInt()
                        for (i in 0 until weekrepeat) {
                            val chunk = falseIndices.drop(i * chunkSize).take(chunkSize)
                            if (chunk.isNotEmpty()) {
                                workDayofWeek[chunk.random()] = true
                            }
                        }
                    }
                }
            } else {
                // weekrepeat이 false 개수보다 많은 경우
                workDayofWeek.fill(true) // 모든 값을 true로 설정
                val trueCount = 7 - weekrepeat
                val trueIndices = holiday.indices.filter { holiday[it] }

                val chunkSize = (trueIndices.size / trueCount.toFloat()).toInt()
                for (i in 0 until trueCount) {
                    val chunk = trueIndices.drop(i * chunkSize).take(chunkSize)
                    if (chunk.isNotEmpty()) {
                        workDayofWeek[chunk.random()] = false
                    }
                }
            }
        }

        return workDayofWeek
    }

    private fun totalChecklist(weekRepeat: Int, startDate: String, endDate: String, workDay: BooleanArray): Int {
        var checkList = 0

        // DateTimeFormatter 정의
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        // 문자열을 LocalDate로 변환
        val date1 = LocalDate.parse(startDate, formatter)
        val date2 = LocalDate.parse(endDate, formatter)

        // 두 날짜 사이의 일 수 계산
        val daysBetween = ChronoUnit.DAYS.between(date1, date2)

        checkList = daysBetween.toInt() / 7 * weekRepeat
        val restDay = daysBetween.toInt() % 7
        if (restDay == 0) {
            return checkList
        }

        val minusDate = date2.minusDays(daysBetween % 7)
        var currentDate = minusDate
        while (!currentDate.isAfter(date2)) {
            val dayOfWeek = currentDate.dayOfWeek.value - 1
            if (workDay[dayOfWeek]) {
                checkList++
            }
            currentDate = currentDate.plusDays(1)
        }

        return checkList
    }
}
