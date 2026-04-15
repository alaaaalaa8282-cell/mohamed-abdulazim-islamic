package com.alaa.mohamedabdulazim.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.alaa.mohamedabdulazim.data.local.PreferencesManager
import com.alaa.mohamedabdulazim.service.AthanService
import com.alaa.mohamedabdulazim.service.ZekrService

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_ATHAN       = "com.alaa.mohamedabdulazim.ATHAN"
        const val ACTION_ZEKR        = "com.alaa.mohamedabdulazim.ZEKR"
        const val EXTRA_PRAYER_NAME  = "prayer_name"
        const val EXTRA_PRAYER_AR    = "prayer_name_ar"
        const val EXTRA_ATHAN_KEY    = "athan_key"
        const val REQUEST_ZEKR       = 9000

        fun scheduleAthan(ctx: Context, prayerName: String, prayerNameAr: String,
                          athanKey: String, triggerAtMillis: Long, requestCode: Int) {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(ctx, AlarmReceiver::class.java).apply {
                action = ACTION_ATHAN
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_PRAYER_AR, prayerNameAr)
                putExtra(EXTRA_ATHAN_KEY, athanKey)
            }
            val pi = PendingIntent.getBroadcast(
                ctx, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            else
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }

        fun cancelAthan(ctx: Context, requestCode: Int) {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = PendingIntent.getBroadcast(
                ctx, requestCode, Intent(ctx, AlarmReceiver::class.java),
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ATHAN -> {
                val prayerName   = intent.getStringExtra(EXTRA_PRAYER_NAME)   ?: return
                val prayerNameAr = intent.getStringExtra(EXTRA_PRAYER_AR)     ?: return
                val athanKey     = intent.getStringExtra(EXTRA_ATHAN_KEY)     ?: "default"
                AthanService.start(context, prayerName, prayerNameAr, athanKey)
            }
            ACTION_ZEKR -> {
                val prefs = PreferencesManager(context)
                if (prefs.getSettings().zekrEnabled)
                    ZekrService.start(context)
            }
        }
    }
}
