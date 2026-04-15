package com.alaa.mohamedabdulazim.data.local

import android.content.Context
import android.content.SharedPreferences
import com.alaa.mohamedabdulazim.data.models.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("islamic_app_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: Flow<AppSettings> = _settingsFlow.asStateFlow()

    private fun loadSettings() = AppSettings(
        calculationMethod   = prefs.getInt("calc_method", 5),
        latitude            = prefs.getFloat("latitude", 0f).toDouble(),
        longitude           = prefs.getFloat("longitude", 0f).toDouble(),
        cityName            = prefs.getString("city_name", "") ?: "",
        useGps              = prefs.getBoolean("use_gps", true),
        zekrEnabled         = prefs.getBoolean("zekr_enabled", true),
        zekrIntervalMinutes = prefs.getInt("zekr_interval", 30),
        zekrMode            = prefs.getString("zekr_mode", "sequential") ?: "sequential",
        fajrAthan           = prefs.getString("athan_fajr", "default") ?: "default",
        dhuhrAthan          = prefs.getString("athan_dhuhr", "default") ?: "default",
        asrAthan            = prefs.getString("athan_asr", "default") ?: "default",
        maghribAthan        = prefs.getString("athan_maghrib", "default") ?: "default",
        ishaAthan           = prefs.getString("athan_isha", "default") ?: "default",
        athanVolume         = prefs.getFloat("athan_volume", 1.0f)
    )

    fun save(settings: AppSettings) {
        prefs.edit().apply {
            putInt("calc_method",     settings.calculationMethod)
            putFloat("latitude",      settings.latitude.toFloat())
            putFloat("longitude",     settings.longitude.toFloat())
            putString("city_name",    settings.cityName)
            putBoolean("use_gps",     settings.useGps)
            putBoolean("zekr_enabled",settings.zekrEnabled)
            putInt("zekr_interval",   settings.zekrIntervalMinutes)
            putString("zekr_mode",    settings.zekrMode)
            putString("athan_fajr",   settings.fajrAthan)
            putString("athan_dhuhr",  settings.dhuhrAthan)
            putString("athan_asr",    settings.asrAthan)
            putString("athan_maghrib",settings.maghribAthan)
            putString("athan_isha",   settings.ishaAthan)
            putFloat("athan_volume",  settings.athanVolume)
            apply()
        }
        _settingsFlow.value = settings
    }

    fun getZekrIndex(): Int = prefs.getInt("zekr_index", 0)
    fun saveZekrIndex(i: Int) = prefs.edit().putInt("zekr_index", i).apply()

    fun getSettings(): AppSettings = loadSettings()
}
