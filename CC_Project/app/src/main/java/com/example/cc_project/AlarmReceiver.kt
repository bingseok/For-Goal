package com.example.cc_project

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 알림을 받았을 때 실행되는 부분
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림을 보내는 작업
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 버전 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("default", "Default Channel", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Default Channel Description"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "default")
            .setContentTitle("ForGoal")
            .setContentText("오늘 할일 확인하기")
            .setContentIntent(pendingIntent) // 알림 클릭 시 실행될 Intent 설정
            .setSmallIcon(R.drawable.logo) // 작은 아이콘 설정 (적절한 아이콘으로 변경)
            .setAutoCancel(true) // 클릭 시 알림 삭제
            .build()

        notificationManager.notify(1, notification)
    }
}
