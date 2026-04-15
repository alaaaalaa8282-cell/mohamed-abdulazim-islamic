package com.alaa.mohamedabdulazim.service

import android.app.*
import android.content.*
import android.media.*
import android.os.*
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import com.alaa.mohamedabdulazim.IslamicApplication.Companion.ZEKR_CHANNEL_ID
import com.alaa.mohamedabdulazim.MainActivity
import com.alaa.mohamedabdulazim.R
import com.alaa.mohamedabdulazim.data.local.AdhkarData
import com.alaa.mohamedabdulazim.data.local.PreferencesManager
import com.alaa.mohamedabdulazim.receiver.AlarmReceiver

class ZekrService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var audioManager: AudioManager
    private lateinit var prefs: PreferencesManager
    private var focusRequest: AudioFocusRequest? = null

    companion object {
        const val NOTIF_ID = 2001
        const val ACTION_STOP   = "com.alaa.mohamedabdulazim.STOP_ZEKR"
        const val ACTION_SKIP   = "com.alaa.mohamedabdulazim.SKIP_ZEKR"
        var isRunning = false

        fun start(ctx: Context) {
            val intent = Intent(ctx, ZekrService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ctx.startForegroundService(intent)
            else ctx.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        prefs = PreferencesManager(applicationContext)
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "IslamicApp:ZekrWakeLock")
        wakeLock?.acquire(5 * 60 * 1000L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle stop/skip actions
        when (intent?.action) {
            ACTION_STOP -> { stopSelf(); return START_NOT_STICKY }
            ACTION_SKIP -> {
                val idx = prefs.getZekrIndex()
                val list = AdhkarData.zekrForBackground
                prefs.saveZekrIndex((idx + 1) % list.size)
                scheduleNext()
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val settings = prefs.getSettings()
        if (!settings.zekrEnabled) { stopSelf(); return START_NOT_STICKY }

        // Don't play during athan or phone calls
        if (AthanService.isPlaying) { scheduleNext(); stopSelf(); return START_NOT_STICKY }
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        if (tm.callState != TelephonyManager.CALL_STATE_IDLE) {
            scheduleNext(); stopSelf(); return START_NOT_STICKY
        }

        // Don't interrupt if other audio playing (music/video)
        if (audioManager.isMusicActive) { scheduleNext(); stopSelf(); return START_NOT_STICKY }

        val list  = AdhkarData.zekrForBackground
        val index = if (settings.zekrMode == "sequential") prefs.getZekrIndex() else prefs.getZekrIndex()
        val zekr  = list[index % list.size]

        if (settings.zekrMode == "sequential")
            prefs.saveZekrIndex((index + 1) % list.size)

        startForeground(NOTIF_ID, buildNotification(zekr.text))
        scheduleNext()
        isRunning = true
        stopSelf()
        return START_NOT_STICKY
    }

    private fun scheduleNext() {
        val settings = prefs.getSettings()
        if (!settings.zekrEnabled) return
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            this, AlarmReceiver.REQUEST_ZEKR,
            Intent(this, AlarmReceiver::class.java).apply { action = AlarmReceiver.ACTION_ZEKR },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + settings.zekrIntervalMinutes * 60_000L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        else
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }

    private fun buildNotification(zekrText: String): Notification {
        val contentPi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val stopPi = PendingIntent.getService(
            this, 10,
            Intent(this, ZekrService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )
        val skipPi = PendingIntent.getService(
            this, 11,
            Intent(this, ZekrService::class.java).apply { action = ACTION_SKIP },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, ZEKR_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentTitle("ذكر • محمد عبد العظيم الطويل")
            .setContentText(zekrText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(zekrText))
            .setContentIntent(contentPi)
            .addAction(0, "إيقاف", stopPi)
            .addAction(0, "التالي", skipPi)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    override fun onDestroy() {
        isRunning = false
        mediaPlayer?.release()
        mediaPlayer = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        wakeLock?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
