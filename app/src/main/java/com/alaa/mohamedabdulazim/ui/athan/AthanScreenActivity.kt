package com.alaa.mohamedabdulazim.ui.athan

import android.app.KeyguardManager
import android.os.*
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.alaa.mohamedabdulazim.R
import com.alaa.mohamedabdulazim.service.AthanService

class AthanScreenActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // اظهر على شاشة القفل
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON  or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        setContentView(R.layout.activity_athan_screen)

        val prayerNameAr = intent.getStringExtra(AthanService.EXTRA_PRAYER_NAME_AR) ?: "الصلاة"

        findViewById<TextView>(R.id.tv_prayer_name).text  = prayerNameAr
        findViewById<TextView>(R.id.tv_athan_label).text  = "حان وقت صلاة $prayerNameAr"

        // صورة الوالد — تأكد إن عندك ملف father_photo.jpg في res/drawable
        try {
            findViewById<ImageView>(R.id.iv_father_photo)
                .setImageResource(R.drawable.father_photo)
        } catch (e: Exception) {
            // لو الصورة مش موجودة هيعرض أيقونة المسجد تلقائياً من الـ XML
        }

        findViewById<Button>(R.id.btn_stop_athan).setOnClickListener {
            stopAndClose()
        }

        // إغلاق تلقائي بعد 10 دقايق
        handler.postDelayed({ stopAndClose() }, 10 * 60 * 1000L)
    }

    private fun stopAndClose() {
        AthanService.stop(this)
        handler.removeCallbacksAndMessages(null)
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
