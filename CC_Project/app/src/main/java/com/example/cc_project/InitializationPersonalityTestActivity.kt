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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

var selectedDay1 = mutableListOf<String>()
var selectedId0 = -1

/*
data class PersonalityTest (
    val uid : String = "",
    var maxgap : Int = 1,
    var numholidays : Int = 0, // 휴일 수
    var sameproject : Int = 0, // 같은 프로젝트 흐트려 놓기
    var holidaynext : Int = 0, // 휴일 다음날
    var holidayofweek : ArrayList<boolean> = arrayListOf() // 휴일 언제인지
)
*/
class InitializtionPersonalityTestActivity : AppCompatActivity() {


    private lateinit var questionTextView: TextView
    private lateinit var answerRadioGroup: RadioGroup
    private lateinit var storeButton: Button
    private lateinit var skipButton: Button
    private lateinit var personalTestBack : ImageButton
    private lateinit var radioGroup1: RadioGroup
    private lateinit var radioGroup2: RadioGroup
    private lateinit var radioGroup3: RadioGroup
    private lateinit var radioGroup4: RadioGroup
    private lateinit var radioGroup5: RadioGroup
    val selectedDays = mutableListOf<String>()
    var selectedId1 =-1




    // 그렇게 하기로함



    override fun onCreate(savedInstanceState: Bundle?) {
        // 아직 피드백을 정할때 값을 정할 예정
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personality_test_initialization)
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


            // 아무것도 체크되지 않았을 경우
            if (selectedId1 == -1) {
                Toast.makeText(this, "답변을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (selectedId2 == -1) {
                Toast.makeText(this, "답변을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (selectedId3 == -1) {
                Toast.makeText(this, "답변을 입력해주세요", Toast.LENGTH_SHORT).show()
            }else if (selectedId4 == -1) {
                Toast.makeText(this, "답변을 입력해주세요", Toast.LENGTH_SHORT).show()
            }else if (selectedId5 == -1) {
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
                //Toast.makeText(this, "${selectedDays}", Toast.LENGTH_SHORT).show()
                selectedDay1=selectedDays
              //  Toast.makeText(this, "${selectedDay}", Toast.LENGTH_SHORT).show()
                selectedId0 = selectedId1


                val daysOfWeek = BooleanArray(7) { index ->
                    // 월요일은 index가 0, 일요일은 index가 6일 때 true로 설정
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



                startActivity(Intent(this, MainActivity::class.java))
            }


        }
        skipButton.setOnClickListener {
            val daysOfWeek = BooleanArray(7) { false }
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
            startActivity(Intent(this, MainActivity::class.java))
        }



        personalTestBack.setOnClickListener {
            onBackPressed()
            ///커밋해봄
        }
    }



    /// 여기서 부터 firebase 연동

}
