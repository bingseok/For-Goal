package com.example.cc_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.cc_project.AuthUse.Companion.db
import com.example.cc_project.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // onBackPressedDispatcher를 사용하여 뒤로 가기 버튼 눌렀을 때 애플리케이션 종료
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })

        changeVisibility("logout")

        val requestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // 구글 로그인 결과 처리
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            AuthUse.email = account.email
                            changeVisibility("login")
                            Toast.makeText(baseContext, "로그인 성공", Toast.LENGTH_SHORT).show()
                        } else {
                            changeVisibility("logout")
                            Toast.makeText(baseContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: ApiException) {
                changeVisibility("logout")
                Toast.makeText(baseContext, "API오류", Toast.LENGTH_SHORT).show()
            }
        }

        binding.logoutBtn.setOnClickListener {
            // 로그아웃
            auth.signOut()
            AuthUse.email = null
            changeVisibility("logout")
        }

        binding.goSignInBtn.setOnClickListener {
            changeVisibility("signin")
        }

        binding.googleLoginBtn.setOnClickListener {
            // 구글 로그인
            val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
            requestLauncher.launch(signInIntent)
        }

        binding.signBtn.setOnClickListener {
            // 이메일, 비밀번호 회원가입
            val email = binding.authEmailEditView.text.toString()
            val password = binding.authPasswordEditView.text.toString()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if (task.isSuccessful) {
                        auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { sendTask ->
                                if (sendTask.isSuccessful) {
                                    Toast.makeText(
                                        baseContext, "회원가입에 성공했습니다. 전송된 메일을 확인해 주세요",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    changeVisibility("logout")
                                } else {
                                    Toast.makeText(baseContext, "메일 발송 실패", Toast.LENGTH_SHORT).show()
                                    changeVisibility("logout")
                                }
                            }
                    } else {
                        Toast.makeText(baseContext, "회원가입 실패", Toast.LENGTH_SHORT).show()
                        changeVisibility("logout")
                    }
                }
        }

        binding.loginBtn.setOnClickListener {
            // 이메일, 비밀번호 로그인
            val email = binding.authEmailEditView.text.toString()
            val password = binding.authPasswordEditView.text.toString()
            Log.d("AuthActivity", "email:$email, password:$password")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if (task.isSuccessful) {
                        if (AuthUse.checkAuth()) {
                            AuthUse.email = email
                            changeVisibility("login")
                        } else {
                            Toast.makeText(baseContext, "전송된 메일로 이메일 인증이 되지 않았습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(baseContext, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun changeVisibility(mode: String) {
        val uid = auth.currentUser?.uid
        if (mode == "login") {
            if (uid != null) {
                db.collection("uID").document(uid).collection("userType").document("userType")
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                        } else {
                            startActivity(Intent(applicationContext, InitializtionPersonalityTestActivity::class.java))
                        }
                    }
                    .addOnFailureListener {
                        startActivity(Intent(applicationContext, InitializtionPersonalityTestActivity::class.java))
                    }
            } else {
                Log.e("AuthActivity", "UID is null. Cannot change visibility to login.")
            }
        } else if (mode == "logout") {
            binding.run {
                authMainTextView.text = "로그인 하거나 회원가입 해주세요."
                logoutBtn.visibility = View.GONE
                goSignInBtn.visibility = View.VISIBLE
                googleLoginBtn.visibility = View.VISIBLE
                authEmailEditView.visibility = View.VISIBLE
                authPasswordEditView.visibility = View.VISIBLE
                signBtn.visibility = View.GONE
                loginBtn.visibility = View.VISIBLE
            }
        } else if (mode == "signin") {
            startActivity(Intent(applicationContext, SigninActivity::class.java))
        }
    }
}
