package com.example.cc_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

import com.example.cc_project.databinding.ActivitySigninBinding

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.cc_project.databinding.ActivityAuthBinding

class SigninActivity : AppCompatActivity(){

    lateinit var binding: ActivitySigninBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signBtn.setOnClickListener {
            //이메일,비밀번호 회원가입........................
            val email = binding.authEmailEditView.text.toString()
            val password = binding.authPasswordEditView.text.toString()
            AuthUse.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){task ->
                    binding.authEmailEditView.text.clear()
                    binding.authPasswordEditView.text.clear()
                    if(task.isSuccessful){
                        AuthUse.auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener{ sendTask ->
                                if(sendTask.isSuccessful){
                                    Toast.makeText(baseContext, "회원가입에서 성공, 전송된 메일을 확인해 주세요",
                                        Toast.LENGTH_SHORT).show()
                                    //changeVisibility("logout")
                                    startActivity(Intent(this,AuthActivity::class.java))
                                }else {
                                    Toast.makeText(baseContext, "메일 발송 실패", Toast.LENGTH_SHORT).show()
                                    // changeVisibility("logout")
                                    startActivity(Intent(this,AuthActivity::class.java))
                                }
                            }
                    }else {
                        Toast.makeText(baseContext, "회원가입 실패", Toast.LENGTH_SHORT).show()
                        // changeVisibility("logout")
                        startActivity(Intent(this,AuthActivity::class.java))
                    }
                }

        }
    }
}