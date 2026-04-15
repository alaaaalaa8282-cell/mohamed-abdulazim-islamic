package com.alaa.mohamedabdulazim.data.models

data class PrayerApiResponse(
    val code: Int,
    val status: String,
    val data: PrayerData
)

data class PrayerData(
    val timings: Map<String, String>,
    val date: DateInfo
)

data class DateInfo(
    val readable: String,
    val hijri: HijriDate
)

data class HijriDate(
    val date: String,
    val year: String,
    val month: HijriMonth,
    val weekday: HijriWeekday
)

data class HijriMonth(val number: Int, val en: String, val ar: String)
data class HijriWeekday(val en: String, val ar: String)

data class PrayerTime(
    val name: String,
    val nameAr: String,
    val time: String,
    val isPassed: Boolean = false,
    val isNext: Boolean = false,
    val athanAudioKey: String
)

data class AppSettings(
    val calculationMethod: Int = 5,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val cityName: String = "",
    val useGps: Boolean = true,
    val zekrEnabled: Boolean = true,
    val zekrIntervalMinutes: Int = 30,
    val zekrMode: String = "sequential",
    val fajrAthan: String = "default",
    val dhuhrAthan: String = "default",
    val asrAthan: String = "default",
    val maghribAthan: String = "default",
    val ishaAthan: String = "default",
    val athanVolume: Float = 1.0f
)
