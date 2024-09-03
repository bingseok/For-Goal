package com.example.cc_project

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class setGoalActivity : AppCompatActivity() {

    private lateinit var detailGoalContainer: LinearLayout
    private lateinit var setGoalBack: ImageButton
    private lateinit var setGoalButton: ImageButton
    private var count = 0
    lateinit var StartDate: String
    lateinit var EndDate: String
    var longValue: Long = 0
    private var specGoalList = arrayListOf<SpecGoal>()
    private var uuid = UUID.randomUUID().toString()
    private val db = Firebase.firestore
    private lateinit var pickDateRange: ImageButton
    private lateinit var selectedDateRange: TextView
    private lateinit var goalName: EditText
    private var archieved: Boolean = false

    private lateinit var detailedGoalLauncher: ActivityResultLauncher<Intent>

    private companion object {
        const val DETAIL_GOAL_TEXT_KEY = "detailGoalText"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_set_goal)
        setGoalButton = findViewById(R.id.set_next_button)
        pickDateRange = findViewById(R.id.edit_date_btn)
        selectedDateRange = findViewById(R.id.edit_date)
        goalName = findViewById(R.id.goalName)
        setGoalBack = findViewById(R.id.setGoalBack)

        // 상태바 침투 방지
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        detailedGoalLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val shouldUpdateHome = result.data?.getBooleanExtra("UPDATE_HOME_FRAGMENT", false) ?: false
                if (shouldUpdateHome) {
                    setResult(RESULT_OK, result.data)
                    finish()
                }
            }
        }

        pickDateRange.setOnClickListener {
            val today = MaterialDatePicker.todayInUtcMilliseconds()
            val constraintsBuilder = CalendarConstraints.Builder()
                .setStart(today) // 시작 날짜를 오늘 날짜로 설정
                .setValidator(DateValidatorPointForward.now()) // 오늘 이후 날짜만 선택 가능하게 설정

            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select dates")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build()

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                val startDate = selection.first
                val endDate = selection.second

                val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                val formattedStartDate = sdf.format(startDate)
                val formattedEndDate = sdf.format(endDate)

                StartDate = formattedStartDate.replace(".", "")
                EndDate = formattedEndDate.replace(".", "")
                selectedDateRange.text = "$formattedStartDate - $formattedEndDate"
            }

            dateRangePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
        }

        setGoalBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setGoalButton.setOnClickListener {
            val goalNameText = goalName.text.toString().trim()
            if (goalNameText.isNotEmpty() && this::StartDate.isInitialized && this::EndDate.isInitialized) {
                val intent = Intent(this, DetailedGoalActivity::class.java)
                intent.putExtra("goalName", goalNameText)
                intent.putExtra("StartDate", StartDate)
                intent.putExtra("EndDate", EndDate)
                intent.putExtra("uuid", uuid)
                detailedGoalLauncher.launch(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left) // Add this line
            } else {
                Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // 뒤로 가기 버튼을 눌렀을 때 수행할 작업
            }
        })
    }
}
