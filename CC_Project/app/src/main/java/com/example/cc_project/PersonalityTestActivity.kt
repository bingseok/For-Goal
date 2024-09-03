package com.example.cc_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

var selectedDay = mutableListOf<String>()
var selectedId = -1

class PersonalityTestActivity : AppCompatActivity() {

    private lateinit var questionTextView: TextView
    private lateinit var answerRadioGroup: RadioGroup
    private lateinit var storeButton: Button
    private lateinit var skipButton: Button
    private lateinit var personalTestBack: ImageButton
    private lateinit var radioGroup1: RadioGroup
    private lateinit var radioGroup2: RadioGroup
    private lateinit var radioGroup3: RadioGroup
    private lateinit var radioGroup4: RadioGroup
    private lateinit var radioGroup5: RadioGroup
    val selectedDays = mutableListOf<String>()
    var selectedId1 = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personality_test)
        val db = Firebase.firestore

        storeButton = findViewById(R.id.storeButton)
        skipButton = findViewById(R.id.skipButton)
        personalTestBack = findViewById(R.id.personalTestBack)
        radioGroup1 = findViewById(R.id.answerRadioGroup1)
        radioGroup2 = findViewById(R.id.answerRadioGroup2)
        radioGroup3 = findViewById(R.id.answerRadioGroup3)
        radioGroup4 = findViewById(R.id.answerRadioGroup4)
        radioGroup5 = findViewById(R.id.answerRadioGroup5)

        val checkBox1 = findViewById<CheckBox>(R.id.answerOption6_1)
        val checkBox2 = findViewById<CheckBox>(R.id.answerOption6_2)
        val checkBox3 = findViewById<CheckBox>(R.id.answerOption6_3)
        val checkBox4 = findViewById<CheckBox>(R.id.answerOption6_4)
        val checkBox5 = findViewById<CheckBox>(R.id.answerOption6_5)
        val checkBox6 = findViewById<CheckBox>(R.id.answerOption6_6)
        val checkBox7 = findViewById<CheckBox>(R.id.answerOption6_7)

        storeButton.setOnClickListener {
            selectedId1 = radioGroup1.checkedRadioButtonId
            val selectedId2 = radioGroup2.checkedRadioButtonId
            val selectedId3 = radioGroup3.checkedRadioButtonId
            val selectedId4 = radioGroup4.checkedRadioButtonId
            val selectedId5 = radioGroup5.checkedRadioButtonId

            if (selectedId1 == -1) {
                Toast.makeText(this, "답변을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (selectedId2 == -1) {
                Toast.makeText(this, "답변을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (selectedId3 == -1) {
                Toast.makeText(this, "답변을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (selectedId4 == -1) {
                Toast.makeText(this, "답변을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (selectedId5 == -1) {
                Toast.makeText(this, "답변을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                if (checkBox1.isChecked) {
                    selectedDays.add(checkBox1.text.toString())
                }
                if (checkBox2.isChecked) selectedDays.add(checkBox2.text.toString())
                if (checkBox3.isChecked) selectedDays.add(checkBox3.text.toString())
                if (checkBox4.isChecked) selectedDays.add(checkBox4.text.toString())
                if (checkBox5.isChecked) selectedDays.add(checkBox5.text.toString())
                if (checkBox6.isChecked) selectedDays.add(checkBox6.text.toString())
                if (checkBox7.isChecked) selectedDays.add(checkBox7.text.toString())
                selectedDay = selectedDays
                selectedId = selectedId1

                val daysOfWeek = BooleanArray(7) { index ->
                    val day = when (index) {
                        0 -> "월"
                        1 -> "화"
                        2 -> "수"
                        3 -> "목"
                        4 -> "금"
                        5 -> "토"
                        else -> "일"
                    }
                    selectedDays.contains(day)
                }

                val uid = Firebase.auth.uid
                uid?.let { userId ->
                    db.collection("uID").document(uid).collection("userType").document("userType")
                        .set(hashMapOf("holidayofweek" to daysOfWeek.toList()))
                        .addOnSuccessListener {
                            Log.d("userType set", "success")
                        }
                        .addOnFailureListener { e ->
                            Log.e("userType set", "failure", e)
                        }
                }

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("FRAGMENT_TO_LOAD", "SettingFragment")
                startActivity(intent)
            }
        }

        skipButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("FRAGMENT_TO_LOAD", "SettingFragment")
            startActivity(intent)
        }

        personalTestBack.setOnClickListener {
            onBackPressed()
        }
    }

    companion object {
        fun setPersonalitytype(): String {
            return if (selectedDay.isEmpty()) {
                if (selectedId == -1) {
                    "유형검사를 실시하지 않았어요"
                } else {
                    "나는 헤르미온느"
                }
            } else {
                "${selectedDay} 요일 좋아"
            }
        }
    }
}
