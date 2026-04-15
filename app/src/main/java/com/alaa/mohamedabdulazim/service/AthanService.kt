package com.alaa.mohamedabdulazim.service

import android.app.*
import android.content.*
import android.media.*
import android.os.*
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import com.alaa.mohamedabdulazim.IslamicApplication.Companion.ATHAN_CHANNEL_ID
import com.alaa.mohamedabdulazim.MainActivity
import com.alaa.mohamedabdulazim.R
import com.alaa.mohamedabdulazim.data.local.PreferencesManager

class AthanService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var audioManager: AudioManager
    private lateinit var prefsManager: PreferencesManager
    private var focusRequest: AudioFocusRequest? = null

    companion object {
        const val EXTRA_PRAYER_NAME    = "prayer_name"
        const val EXTRA_PRAYER_NAME_AR = "prayer_name_ar"
        const val EXTRA_ATHAN_KEY      = "athan_key"
        const val NOTIF_ID = 1001
        var isPlaying = false

        fun start(ctx: Context, prayerName: String, prayerNameAr: String, athanKey: String) {
            val intent = Intent(ctx, AthanService::class.java).apply {
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_PRAYER_NAME_AR, prayerNameAr)
                putExtra(EXTRA_ATHAN_KEY, athanKey)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ctx.startForegroundService(intent)
            else ctx.startService(intent)
        }

        fun stop(ctx: Context) = ctx.stopService(Intent(ctx, AthanService::class.java))
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        prefsManager = PreferencesManager(applicationContext)

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "IslamicApp:AthanWakeLock")
        wakeLock?.acquire(10 * 60 * 1000L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayerName   = intent?.getStringExtra(EXTRA_PRAYER_NAME)   ?: "Prayer"
        val prayerNameAr = intent?.getStringExtra(EXTRA_PRAYER_NAME_AR) ?: "الصلاة"
        val athanKey     = intent?.getStringExtra(EXTRA_ATHAN_KEY)      ?: "default"

        // Stop ZekrService to avoid audio conflict
        stopService(Intent(this, ZekrService::class.java))

        // Check active phone call
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        if (tm.callState != TelephonyManager.CALL_STATE_IDLE) {
            stopSelf(); return START_NOT_STICKY
        }

        startForeground(NOTIF_ID, buildNotification(prayerNameAr))
        requestAudioFocus()
        playAthan(athanKey)
        isPlaying = true
        return START_NOT_STICKY
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setOnAudioFocusChangeListener {}
                .build()
            audioManager.requestAudioFocus(focusRequest!!)
        }
    }

    private fun playAthan(athanKey: String) {
        try {
            val settings = prefsManager.getSettings()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setVolume(settings.athanVolume, settings.athanVolume)
                val resId = R.raw.athan_default
                setDataSource(applicationContext, android.net.Uri.parse(
                    "android.resource://${packageName}/$resId"
                ))
                prepare()
                setOnCompletionListener { stopSelf() }
                start()
            }
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun buildNotification(prayerNameAr: String): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopPi = PendingIntent.getService(
            this, 1,
            Intent(this, AthanService::class.java).apply { action = "STOP" },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, ATHAN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mosque)
            .setContentTitle("حان وقت صلاة $prayerNameAr")
            .setContentText("محمد عبد العظيم الطويل الإسلامي")
            .setContentIntent(pi)
            .addAction(R.drawable.ic_mosque, "إيقاف الأذان", stopPi)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        isPlaying = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        }
        wakeLock?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
