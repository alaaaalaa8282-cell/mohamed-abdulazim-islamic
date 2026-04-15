package com.alaa.mohamedabdulazim.viewmodel

import android.app.Application
import android.app.AlarmManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alaa.mohamedabdulazim.data.local.PreferencesManager
import com.alaa.mohamedabdulazim.data.models.PrayerTime
import com.alaa.mohamedabdulazim.data.repository.PrayerRepository
import com.alaa.mohamedabdulazim.receiver.AlarmReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PrayerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo  = PrayerRepository()
    private val prefs = PreferencesManager(app)

    private val _prayers     = MutableStateFlow<List<PrayerTime>>(emptyList())
    val prayers: StateFlow<List<PrayerTime>> = _prayers

    private val _hijriDate   = MutableStateFlow("")
    val hijriDate: StateFlow<String> = _hijriDate

    private val _error       = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading     = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _nextPrayer  = MutableStateFlow<PrayerTime?>(null)
    val nextPrayer: StateFlow<PrayerTime?> = _nextPrayer

    private val _countdown   = MutableStateFlow("")
    val countdown: StateFlow<String> = _countdown

    fun fetchPrayerTimes(lat: Double, lng: Double) {
        viewModelScope.launch {
            _loading.value = true
            _error.value   = null
            val settings   = prefs.getSettings()
            val result = repo.getPrayerTimes(lat, lng, settings.calculationMethod)
            result.onSuccess { response ->
                val list = repo.parseAndTagPrayerTimes(response.data.timings)
                _prayers.value   = list
                _nextPrayer.value = list.firstOrNull { it.isNext }
                try {
                    val hDate = response.data.date.hijri
                    _hijriDate.value = "${hDate.weekday.ar} ${hDate.date.split("-")[0]} ${hDate.month.ar} ${hDate.year} هـ"
                } catch (e: Exception) { _hijriDate.value = "" }
                scheduleAthanAlarms(list, response.data.timings, settings.fajrAthan, settings.dhuhrAthan,
                    settings.asrAthan, settings.maghribAthan, settings.ishaAthan)
            }.onFailure {
                _error.value = "تعذر جلب المواقيت. تحقق من الإنترنت."
            }
            _loading.value = false
        }
    }

    private fun scheduleAthanAlarms(
        prayers: List<PrayerTime>,
        rawTimings: Map<String, String>,
        fajrA: String, dhuhrA: String, asrA: String, magA: String, ishaA: String
    ) {
        val ctx = getApplication<Application>()
        val am  = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val keys = listOf("Fajr","Dhuhr","Asr","Maghrib","Isha")
        val arNames = mapOf("Fajr" to "الفجر","Dhuhr" to "الظهر","Asr" to "العصر","Maghrib" to "المغرب","Isha" to "العشاء")
        val athanMap = mapOf("Fajr" to fajrA,"Dhuhr" to dhuhrA,"Asr" to asrA,"Maghrib" to magA,"Isha" to ishaA)

        keys.forEachIndexed { i, key ->
            val timeStr = rawTimings[key]?.take(5) ?: return@forEachIndexed
            val parts = timeStr.split(":")
            if (parts.size < 2) return@forEachIndexed
            val h = parts[0].toIntOrNull() ?: return@forEachIndexed
            val m = parts[1].toIntOrNull() ?: return@forEachIndexed
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
            }
            AlarmReceiver.scheduleAthan(ctx, key, arNames[key] ?: key,
                athanMap[key] ?: "default", cal.timeInMillis, 1000 + i)
        }
    }

    fun updateCountdown() {
        val next = _nextPrayer.value ?: return
        val prayers = _prayers.value
        val idx = prayers.indexOfFirst { it.isNext }
        if (idx < 0) return
        // We need the raw time - rebuild from prayers
        // For countdown, just show a simple timer
        _countdown.value = "⏳ ${next.nameAr}"
    }
}
