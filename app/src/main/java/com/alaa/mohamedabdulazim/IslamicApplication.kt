package com.alaa.mohamedabdulazim

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class IslamicApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            nm.createNotificationChannel(NotificationChannel(
                ATHAN_CHANNEL_ID, "أذان الصلاة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات أوقات الصلاة والأذان"
                setSound(null, null)
            })

            nm.createNotificationChannel(NotificationChannel(
                ZEKR_CHANNEL_ID, "الأذكار التلقائية",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات الذكر التلقائي"
            })
        }
    }

    companion object {
        const val ATHAN_CHANNEL_ID = "athan_channel"
        const val ZEKR_CHANNEL_ID  = "zekr_channel"
    }
}
