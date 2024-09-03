package com.example.cc_project

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.example.cc_project.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    val db = Firebase.firestore

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        requestNotificationPermission()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        if (!AuthUse.checkAuth()) {
            startActivity(Intent(applicationContext, AuthActivity::class.java))
        }

        setBottomNavigationView()

        // 앱 초기 실행 시 홈화면 또는 특정 프래그먼트로 설정
        if (savedInstanceState == null) {
            val fragmentToLoad = intent.getStringExtra("FRAGMENT_TO_LOAD")
            when (fragmentToLoad) {
                "SettingFragment" -> binding.bottomNavigationView.selectedItemId = R.id.fragment_setting
                else -> binding.bottomNavigationView.selectedItemId = R.id.fragment_home
            }
        }
    }

    private fun setBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, HomeFragment()).commit()
                    true
                }
                R.id.fragment_search -> {
                    supportFragmentManager.beginTransaction().replace(R.id.main_container, CalenderFragment()).commit()
                    true
                }
                R.id.fragment_setting -> {
                    if (AuthUse.checkAuth()) {
                        supportFragmentManager.beginTransaction().replace(R.id.main_container, SettingFragment()).commit()
                    } else {
                        startActivity(Intent(applicationContext, AuthActivity::class.java))
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    // 권한이 이미 부여됨
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                    // 권한이 필요한 이유 설명
                    Toast.makeText(this, "이 앱은 알림을 보내기 위해 알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // 권한 요청
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}
