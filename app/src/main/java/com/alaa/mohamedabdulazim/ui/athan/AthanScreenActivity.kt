package com.alaa.mohamedabdulazim.ui.athan

import android.app.KeyguardManager
import android.os.*
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alaa.mohamedabdulazim.R
import com.alaa.mohamedabdulazim.service.AthanService
import com.alaa.mohamedabdulazim.ui.theme.IslamicTheme

class AthanScreenActivity : ComponentActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // اظهر على شاشة القفل
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KeyguardManager::class.java)
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON  or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON  or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        val prayerNameAr = intent.getStringExtra(AthanService.EXTRA_PRAYER_NAME_AR) ?: "الصلاة"

        // إغلاق تلقائي بعد 10 دقايق
        handler.postDelayed({ stopAndClose() }, 10 * 60 * 1000L)

        setContent {
            IslamicTheme {
                AthanScreen(
                    prayerNameAr = prayerNameAr,
                    onStop = { stopAndClose() }
                )
            }
        }
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

@Composable
private fun AthanScreen(prayerNameAr: String, onStop: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B3A2D))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(48.dp))

            // أيقونة المسجد
            Icon(
                painter = painterResource(id = R.drawable.ic_mosque),
                contentDescription = null,
                tint = Color(0xFFC9A84C),
                modifier = Modifier.size(90.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // صورة دائرية (ic_mosque كـ fallback)
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFC9A84C)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_mosque),
                    contentDescription = null,
                    tint = Color(0xFF1B3A2D),
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // اسم الصلاة
            Text(
                text = prayerNameAr,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC9A84C),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // نص الأذان
            Text(
                text = "حان وقت صلاة $prayerNameAr",
                fontSize = 22.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // إهداء للوالد
            Text(
                text = "إهداء إلى روح والدي\nمحمد عبد العظيم الطويل",
                fontSize = 14.sp,
                color = Color(0xFF8BAF8B),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        // زر الإيقاف
        Button(
            onClick = onStop,
            modifier = Modifier
                .width(220.dp)
                .height(56.dp)
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFC9A84C),
                contentColor   = Color(0xFF1B3A2D)
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Text(
                text = "إيقاف الأذان",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
