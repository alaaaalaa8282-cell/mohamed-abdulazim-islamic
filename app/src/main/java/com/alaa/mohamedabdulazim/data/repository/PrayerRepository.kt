package com.alaa.mohamedabdulazim.data.repository

import com.alaa.mohamedabdulazim.data.api.RetrofitClient
import com.alaa.mohamedabdulazim.data.models.PrayerApiResponse
import com.alaa.mohamedabdulazim.data.models.PrayerTime
import java.text.SimpleDateFormat
import java.util.*

class PrayerRepository {
    private val api = RetrofitClient.prayerApi

    suspend fun getPrayerTimes(
        lat: Double, lng: Double, method: Int = 5
    ): Result<PrayerApiResponse> {
        return try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = sdf.format(Date())
            Result.success(api.getPrayerTimes(date, lat, lng, method))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseAndTagPrayerTimes(timings: Map<String, String>): List<PrayerTime> {
        val prayers = listOf(
            Triple("Fajr",    "الفجر",   "fajr"),
            Triple("Dhuhr",   "الظهر",   "dhuhr"),
            Triple("Asr",     "العصر",   "asr"),
            Triple("Maghrib", "المغرب",  "maghrib"),
            Triple("Isha",    "العشاء",  "isha")
        )

        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMin  = now.get(Calendar.MINUTE)
        val currentTotal = currentHour * 60 + currentMin

        var nextFound = false
        return prayers.mapNotNull { (key, ar, audioKey) ->
            val timeStr = timings[key]?.take(5) ?: return@mapNotNull null
            val parts   = timeStr.split(":")
            if (parts.size < 2) return@mapNotNull null
            val h = parts[0].toIntOrNull() ?: return@mapNotNull null
            val m = parts[1].toIntOrNull() ?: return@mapNotNull null
            val total = h * 60 + m
            val passed = total < currentTotal
            val isNext = !nextFound && !passed
            if (isNext) nextFound = true
            PrayerTime(key, ar, formatTime12(h, m), passed, isNext, audioKey)
        }
    }

    private fun formatTime12(h: Int, m: Int): String {
        val ampm = if (h < 12) "ص" else "م"
        val hour = when {
            h == 0  -> 12
            h > 12  -> h - 12
            else    -> h
        }
        return String.format("%d:%02d %s", hour, m, ampm)
    }
}
