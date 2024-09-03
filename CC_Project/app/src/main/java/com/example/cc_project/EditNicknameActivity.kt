package com.example.cc_project

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditNicknameActivity : AppCompatActivity() {

    private lateinit var nicknameEditText: EditText
    private lateinit var saveButton: Button
    private val db = Firebase.firestore
    private val userID = Firebase.auth.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_nickname)

        nicknameEditText = findViewById(R.id.nickname_edit_text)
        saveButton = findViewById(R.id.save_button)

        // 현재 닉네임 불러오기
        db.collection("uID").document(userID!!).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nickname = document.getString("nickname")
                    nicknameEditText.setText(nickname)
                }
            }

        saveButton.setOnClickListener {
            val newNickname = nicknameEditText.text.toString()

            if (newNickname.isNotEmpty()) {
                db.collection("uID").document(userID!!)
                    .set(mapOf("nickname" to newNickname))
                    .addOnSuccessListener {
                        Toast.makeText(this, "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)  // 결과 설정
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "닉네임 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
